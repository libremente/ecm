package it.tredi.ecm.web;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import it.tredi.ecm.dao.entity.Account;
import it.tredi.ecm.dao.entity.Accreditamento;
import it.tredi.ecm.dao.entity.Anagrafica;
import it.tredi.ecm.dao.entity.EventoPianoFormativo;
import it.tredi.ecm.dao.entity.FieldValutazioneAccreditamento;
import it.tredi.ecm.dao.entity.Persona;
import it.tredi.ecm.dao.entity.Professione;
import it.tredi.ecm.dao.entity.Provider;
import it.tredi.ecm.dao.entity.Sede;
import it.tredi.ecm.dao.entity.Valutazione;
import it.tredi.ecm.dao.entity.VerbaleValutazioneSulCampo;
import it.tredi.ecm.dao.enumlist.AccreditamentoStatoEnum;
import it.tredi.ecm.dao.enumlist.AccreditamentoTipoEnum;
import it.tredi.ecm.dao.enumlist.AccreditamentoWrapperModeEnum;
import it.tredi.ecm.dao.enumlist.IdFieldEnum;
import it.tredi.ecm.dao.enumlist.ProfileEnum;
import it.tredi.ecm.dao.enumlist.SubSetFieldEnum;
import it.tredi.ecm.dao.enumlist.TipoIntegrazioneEnum;
import it.tredi.ecm.service.AccountService;
import it.tredi.ecm.service.AccreditamentoService;
import it.tredi.ecm.service.AccreditamentoStatoHistoryService;
import it.tredi.ecm.service.AccreditamentoStatoHistoryServiceImpl;
import it.tredi.ecm.service.DatiAccreditamentoService;
import it.tredi.ecm.service.EmailService;
import it.tredi.ecm.service.FieldIntegrazioneAccreditamentoService;
import it.tredi.ecm.service.FieldValutazioneAccreditamentoService;
import it.tredi.ecm.service.FileService;
import it.tredi.ecm.service.IntegrazioneService;
import it.tredi.ecm.service.PersonaService;
import it.tredi.ecm.service.ProviderService;
import it.tredi.ecm.service.SedeService;
import it.tredi.ecm.service.TokenService;
import it.tredi.ecm.service.ValutazioneService;
import it.tredi.ecm.service.bean.CurrentUser;
import it.tredi.ecm.service.bean.EcmProperties;
import it.tredi.ecm.utils.Utils;
import it.tredi.ecm.web.bean.AccreditamentoWrapper;
import it.tredi.ecm.web.bean.Message;
import it.tredi.ecm.web.bean.ResponseState;
import it.tredi.ecm.web.bean.RichiestaIntegrazioneWrapper;
import it.tredi.ecm.web.bean.VerbaleValutazioneSulCampoWrapper;
import it.tredi.ecm.web.validator.ValutazioneValidator;

@Controller
public class AccreditamentoController {
	private static Logger LOGGER = LoggerFactory.getLogger(AccreditamentoController.class);

	@Autowired private AccreditamentoService accreditamentoService;
	@Autowired private ProviderService providerService;
	@Autowired private PersonaService personaService;
	@Autowired private SedeService sedeService;
	@Autowired private FileService fileService;
	@Autowired private DatiAccreditamentoService datiAccreditamentoService;

	@Autowired private AccountService accountService;

	@Autowired private ValutazioneService valutazioneService;
	@Autowired private ValutazioneValidator valutazioneValidator;
	@Autowired private TokenService tokenService;
	@Autowired private FieldValutazioneAccreditamentoService fieldValutazioneAccreditamentoService;

	@Autowired private IntegrazioneService integrazioneService;
	@Autowired private FieldIntegrazioneAccreditamentoService fieldIntegrazioneAccreditamentoService;

	@Autowired private EcmProperties ecmProperties;
	@Autowired private EmailService emailService;

	@Autowired private AccreditamentoStatoHistoryService accreditamentoStatoHistoryService;


	@InitBinder
	public void setAllowedFields(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	@RequestMapping("/workflow/token/{token}/accreditamento/{accreditamentoId}/stato/{stato}")
	@ResponseBody
	public ResponseState SetStatoFromBonita(@PathVariable("token") String token, @PathVariable("accreditamentoId") Long accreditamentoId, @PathVariable("stato") AccreditamentoStatoEnum stato,
			@RequestParam(required = false) Integer numeroValutazioniNonDate, @RequestParam(required = false) String dataOraScadenzaPossibiltaValutazione,
			@RequestParam(required = false) Boolean eseguitoDaUtente) throws Exception{
		LOGGER.info(Utils.getLogMessage("GET /workflow/token/{token}/accreditamento/{accreditamentoId}/stato/{stato} token: " + token + "; accreditamentoId: " + accreditamentoId + "; stato: " + stato));

		if(!tokenService.checkTokenAndDelete(token)) {
			String msg = "Impossibile trovare il token passato token: " + token;
			LOGGER.error(msg);
			return new ResponseState(true, msg);
		}
		//modifico lo stato
		if(eseguitoDaUtente != null){
			accreditamentoService.changeState(accreditamentoId, stato, eseguitoDaUtente);
		} else {
			accreditamentoService.changeState(accreditamentoId, stato);
		}

		Accreditamento accreditamento = accreditamentoService.getAccreditamento(accreditamentoId);
		if(accreditamento.getTipoDomanda() == AccreditamentoTipoEnum.PROVVISORIO) {
			if(numeroValutazioniNonDate != null && numeroValutazioniNonDate.intValue() > 0){
				valutazioneService.updateValutazioniNonDate(accreditamentoId);
			}
			if(dataOraScadenzaPossibiltaValutazione != null && !dataOraScadenzaPossibiltaValutazione.isEmpty()) {
				//la data viene passata come stringa in formato yyyy-MM-dd'T'HH:mm:ss
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
				Date date = df.parse(dataOraScadenzaPossibiltaValutazione);
				LocalDateTime ldt = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
				valutazioneService.dataOraScadenzaPossibilitaValutazioneCRECM(accreditamentoId, ldt);
			}
		} else if(accreditamento.getTipoDomanda() == AccreditamentoTipoEnum.STANDARD) {
			if(dataOraScadenzaPossibiltaValutazione != null && !dataOraScadenzaPossibiltaValutazione.isEmpty()) {
				//la data viene passata come stringa in formato yyyy-MM-dd'T'HH:mm:ss
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
				Date date = df.parse(dataOraScadenzaPossibiltaValutazione);
				LocalDateTime ldt = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
				valutazioneService.dataOraScadenzaPossibilitaValutazione(accreditamentoId, ldt);
			}
		}



		return new ResponseState(false, "Stato modificato");

/*
		Account account = accountRepository.findOneByUsername("provider").orElse(null);
		if(account != null) {
			workflowService.saveOrUpdateBonitaUserByAccount(account);
		}
 */
		//TODO modifica stato della domanda da parte del flusso
		//lo facciamo cosi in modo tale da non dover disabilitare la cache di hibernate
		//accreditamentoService.setStato(accreditamentoId, stato);
	}

	/***	Get Lista Accreditamenti per provider CORRENTE	***/
	@RequestMapping("/provider/accreditamento/list")
	public String getAllAccreditamentiForCurrentProvider(RedirectAttributes redirectAttrs) throws Exception{
		LOGGER.info(Utils.getLogMessage("GET /provider/accreditamento/list"));
		try {
			Provider currentProvider = providerService.getProvider();
			if(currentProvider.isNew()){
				throw new Exception("Provider non registrato");
			}else{
				redirectAttrs.addAttribute("providerId",currentProvider.getId());
				LOGGER.info(Utils.getLogMessage("REDIRECT: /provider/" + currentProvider.getId() + "/accreditamento/list"));
				return "redirect:/provider/{providerId}/accreditamento/list";
			}
		}catch (Exception ex){
			LOGGER.error(Utils.getLogMessage("GET /provider/accreditamento/list"),ex);
			redirectAttrs.addFlashAttribute("message", new Message("message.errore", "message.errore_eccezione", "error"));
			LOGGER.info(Utils.getLogMessage("REDIRECT: /home"));
			return "redirect:/home";
		}
	}

	/***	Get Lista Accreditamenti per {providerID}	***/
	@PreAuthorize("@securityAccessServiceImpl.canShowProvider(principal,#providerId)")
	@RequestMapping("/provider/{providerId}/accreditamento/list")
	public String getAllAccreditamentiForProvider(@PathVariable("providerId") Long providerId, Model model, RedirectAttributes redirectAttrs){
		LOGGER.info(Utils.getLogMessage("GET /provider/" + providerId + "/accreditamento/list"));
		try {
			Set<Accreditamento> listaAccreditamenti = accreditamentoService.getAllAccreditamentiForProvider(providerId);
			model.addAttribute("accreditamentoList", listaAccreditamenti);
			model.addAttribute("canProviderCreateAccreditamentoProvvisorio", accreditamentoService.canProviderCreateAccreditamento(providerId,AccreditamentoTipoEnum.PROVVISORIO));
			model.addAttribute("canProviderCreateAccreditamentoStandard", accreditamentoService.canProviderCreateAccreditamento(providerId,AccreditamentoTipoEnum.STANDARD));
			model.addAttribute("providerId", providerId);
			LOGGER.info(Utils.getLogMessage("VIEW: accreditamento/accreditamentoList"));
			return "accreditamento/accreditamentoList";
		}catch (Exception ex){
			LOGGER.error(Utils.getLogMessage("GET /provider/" + providerId + "/accreditamento/list"),ex);
			redirectAttrs.addFlashAttribute("message", new Message("message.errore", "message.errore_eccezione", "error"));
			LOGGER.info(Utils.getLogMessage("REDIRECT: /home"));
			return "redirect:/home";
		}
	}

	/***	Get show Accreditamento {ID}	***/
	@PreAuthorize("@securityAccessServiceImpl.canShowAccreditamento(principal,#id)")
	@RequestMapping("/accreditamento/{id}/show")
	public String showAccreditamento(@PathVariable Long id, Model model, @RequestParam(required = false) String tab, RedirectAttributes redirectAttrs){
		LOGGER.info(Utils.getLogMessage("GET /accreditamento/" + id + "/show"));
		try {
			if (tab != null) {
				model.addAttribute("currentTab", tab);
				LOGGER.info(Utils.getLogMessage("TAB:" + tab));
			}
			Accreditamento accreditamento = accreditamentoService.getAccreditamento(id);
			return goToAccreditamentoShow(model, accreditamento);
		}catch (Exception ex){
			LOGGER.error(Utils.getLogMessage("GET /accreditamento/" + id + "/show"),ex);
			model.addAttribute("message", new Message("message.errore", "message.errore_eccezione", "error"));
			LOGGER.info(Utils.getLogMessage("VIEW: /home"));
			return "redirect:/home";
		}
	}

	/***	Get edit Accreditamento {ID}	***/
	@PreAuthorize("@securityAccessServiceImpl.canEditAccreditamento(principal,#id)")
	@RequestMapping("/accreditamento/{id}/edit")
	public String editAccreditamento(@PathVariable Long id, Model model, @RequestParam(required = false) String tab, RedirectAttributes redirectAttrs){
		LOGGER.info(Utils.getLogMessage("GET /accreditamento/" + id + "/edit"));
		try {
			if (tab != null) {
				LOGGER.info(Utils.getLogMessage("TAB:" + tab));
			}
			Accreditamento accreditamento = accreditamentoService.getAccreditamento(id);
			return goToAccreditamentoEdit(model, accreditamento, tab);
		}catch (Exception ex){
			LOGGER.error(Utils.getLogMessage("GET /accreditamento/" + id + "/edit"),ex);
			model.addAttribute("message", new Message("message.errore", "message.errore_eccezione", "error"));
			LOGGER.info(Utils.getLogMessage("VIEW: /accreditamento/accreditamentoList"));
			return "accreditamento/accreditamentoList";
		}
	}

	/*** Get validate Accreditamento {ID} ***/
	@PreAuthorize("@securityAccessServiceImpl.canValidateAccreditamento(principal,#id)")
	@RequestMapping("/accreditamento/{id}/validate")
	public String validateAccreditamento(@PathVariable Long id, Model model, @RequestParam(required = false) String tab, RedirectAttributes redirectAttrs){
		LOGGER.info(Utils.getLogMessage("GET /accreditamento/" + id + "/validate"));
		try {
			if (tab != null) {
				model.addAttribute("currentTab", tab);
				LOGGER.info(Utils.getLogMessage("TAB:" + tab));
			}
			Accreditamento accreditamento = accreditamentoService.getAccreditamento(id);
			return goToAccreditamentoValidate(model, accreditamento);
		}catch (Exception ex){
			LOGGER.error(Utils.getLogMessage("GET /accreditamento/" + id + "/validate"),ex);
			model.addAttribute("message", new Message("message.errore", "message.errore_eccezione", "error"));
			LOGGER.info(Utils.getLogMessage("VIEW: /accreditamento/accreditamentoList"));
			return "accreditamento/accreditamentoList";
		}
	}

	/*** Get enableField Accreditamento {ID} ***/
	@PreAuthorize("@securityAccessServiceImpl.canEnableField(principal,#id)")
	@RequestMapping("/accreditamento/{id}/enableField")
	public String enableFieldAccreditamento(@PathVariable Long id, Model model, @RequestParam(required = false) String tab, RedirectAttributes redirectAttrs){
		LOGGER.info(Utils.getLogMessage("GET /accreditamento/" + id + "/enableField"));
		try {
			if (tab != null) {
				model.addAttribute("currentTab", tab);
				LOGGER.info(Utils.getLogMessage("TAB:" + tab));
			}
			Accreditamento accreditamento = accreditamentoService.getAccreditamento(id);
			return goToAccreditamentoEnableField(model, accreditamento, tab);
		}catch (Exception ex){
			LOGGER.error(Utils.getLogMessage("GET /accreditamento/" + id + "/enableField"),ex);
			model.addAttribute("message", new Message("message.errore", "message.errore_eccezione", "error"));
			LOGGER.info(Utils.getLogMessage("VIEW: /accreditamento/accreditamentoList"));
			return "accreditamento/accreditamentoList";
		}
	}

	private String goToAccreditamentoShow(Model model, Accreditamento accreditamento) throws Exception{
		return goToAccreditamentoShow(model, accreditamento, null);
	}

	//passo il wrapper che contiene solo la lista dei referee riassegnati
	private String goToAccreditamentoShow(Model model, Accreditamento accreditamento, AccreditamentoWrapper wrapper) throws Exception{
		Boolean hasVerbaleErrors = (Boolean) model.asMap().containsKey("verbaleErrors");
		AccreditamentoWrapper accreditamentoWrapper = prepareAccreditamentoWrapperShow(accreditamento, wrapper, hasVerbaleErrors);
		model.addAttribute("accreditamentoWrapper", accreditamentoWrapper);
		if(accreditamentoWrapper.isCanAssegnaNuovoGruppo()) {
			//inserisce una lista di referee che non contiene quelli già assegnati alla domanda
			Set<Account> refereeList = accountService.getUserByProfileEnum(ProfileEnum.REFEREE);
			Set<Account> oldRefereeList = valutazioneService.getAllValutatoriForAccreditamentoId(accreditamento.getId());
			refereeList.removeAll(oldRefereeList);
			//rimuove dalla lista di tutti i referee selezionabili quelli che erano stati precedentemente incaricati di valutare la domanda
			model.addAttribute("refereeList", refereeList);
		}

		//Carico la storia del flusso
		if(accreditamento.getWorkflowInfoAccreditamento() != null)
			model.addAttribute("accreditamentoHistoryList", accreditamentoStatoHistoryService.getAllByAccreditamentoIdAndProcessInstanceId(accreditamento.getId(), accreditamento.getWorkflowInfoAccreditamento().getProcessInstanceId()));

		LOGGER.info(Utils.getLogMessage("VIEW: /accreditamento/accreditamentoShow"));
		return "accreditamento/accreditamentoShow";
	}

	private String goToAccreditamentoEdit(Model model, Accreditamento accreditamento, String tab) throws Exception{
		AccreditamentoWrapper accreditamentoWrapper = prepareAccreditamentoWrapperEdit(accreditamento);
		model.addAttribute("accreditamentoWrapper", accreditamentoWrapper);
		selectCorrectTab(tab, accreditamentoWrapper, model);
		LOGGER.info(Utils.getLogMessage("VIEW: /accreditamento/accreditamentoEdit"));
		return "accreditamento/accreditamentoEdit";
	}

	private String goToAccreditamentoValidate(Model model, Accreditamento accreditamento) throws Exception {
		return goToAccreditamentoValidate(model, accreditamento, null);
	}

	//passo il wrapper che contiene solo la valutazione complessiva e la lista dei referee selezionati
	private String goToAccreditamentoValidate(Model model, Accreditamento accreditamento, AccreditamentoWrapper wrapper) throws Exception{
		//check se ci sono errori di validazione per settare opportunamente il wrapper
		Boolean hasErrors = false;
		if(model.asMap().get("confirmErrors") != null)
			hasErrors = true;
		//carico la valutazione dell'utente corrente
		Valutazione valutazione = valutazioneService.getValutazioneByAccreditamentoIdAndAccountIdAndNotStoricizzato(accreditamento.getId(), Utils.getAuthenticatedUser().getAccount().getId());
		AccreditamentoWrapper accreditamentoWrapper = prepareAccreditamentoWrapperValidate(accreditamento, valutazione, wrapper, hasErrors);
		model.addAttribute("accreditamentoWrapper", accreditamentoWrapper);
		model.addAttribute("refereeList", accountService.getUserByProfileEnum(ProfileEnum.REFEREE));
		model.addAttribute("numeroReferee", ecmProperties.getNumeroReferee());
		LOGGER.info(Utils.getLogMessage("VIEW: /accreditamento/accreditamentoValidate"));
		return "accreditamento/accreditamentoValidate";
	}

	private String goToAccreditamentoEnableField(Model model, Accreditamento accreditamento, String tab) throws Exception{
		AccreditamentoWrapper accreditamentoWrapper = prepareAccreditamentoWrapperEnableField(accreditamento);
		model.addAttribute("accreditamentoWrapper", accreditamentoWrapper);
		model.addAttribute("richiestaIntegrazioneWrapper", integrazioneService.prepareRichiestaIntegrazioneWrapper(accreditamento.getId(), SubSetFieldEnum.FULL, null));
		model.addAttribute("userCanSendRichiestaIntegrazione",accreditamentoService.canUserInviaRichiestaIntegrazione(accreditamento.getId(), Utils.getAuthenticatedUser()));
		model.addAttribute("giorniIntegrazioneMax", ecmProperties.getGiorniIntegrazioneMax());
		model.addAttribute("giorniIntegrazioneMin", ecmProperties.getGiorniIntegrazioneMin());
		model.addAttribute("giorniIntegrazione", 5);
		LOGGER.info(Utils.getLogMessage("VIEW: /accreditamento/accreditamentoEnableField"));
		return "accreditamento/accreditamentoEnableField";
	}

	@PreAuthorize("@securityAccessServiceImpl.canEnableField(principal,#accreditamentoId)")
	@RequestMapping("/accreditamento/{accreditamentoId}/{idFieldEnum}/{state}")
	public String enableFieldFull(@PathVariable("accreditamentoId") Long accreditamentoId, @PathVariable("idFieldEnum") IdFieldEnum field, @PathVariable("state") boolean state){
		LOGGER.info(Utils.getLogMessage("GET /accreditamento/" + accreditamentoId + "/" + field + "/" + state));
		RichiestaIntegrazioneWrapper wrapper = integrazioneService.prepareRichiestaIntegrazioneWrapper(accreditamentoId, SubSetFieldEnum.FULL, null);
		if(state)
			wrapper.getSelected().add(field);
		else
			wrapper.getSelected().remove(field);
		integrazioneService.saveEnableField(wrapper);
		if(field == IdFieldEnum.COMPONENTE_COMITATO_SCIENTIFICO__FULL){
			LOGGER.info(Utils.getLogMessage("REDIRECT: /accreditamento/" + accreditamentoId + "enableField?tab=tab2"));
			return "redirect:/accreditamento/{accreditamentoId}/enableField?tab=tab2";
		}
		else{
			LOGGER.info(Utils.getLogMessage("REDIRECT: /accreditamento/" + accreditamentoId + "enableField?tab=tab4"));
			return "redirect:/accreditamento/{accreditamentoId}/enableField?tab=tab4";
		}
	}

	//Check per capire in che tab ritornare e con quale messaggio
	private void selectCorrectTab(String tab, AccreditamentoWrapper accreditamentoWrapper, Model model){
		if(tab != null) {
			switch(tab) {

			case "tab1":
				model.addAttribute("currentTab", "tab1");
				break;

			case "tab2":
				if(accreditamentoWrapper.isSezione1Stato())
				{
					model.addAttribute("currentTab", "tab2");
					if (accreditamentoWrapper.getResponsabileSegreteria() == null &&
							accreditamentoWrapper.getResponsabileAmministrativo() == null &&
							accreditamentoWrapper.getResponsabileSistemaInformatico() == null &&
							accreditamentoWrapper.getResponsabileQualita() == null)
					{
						model.addAttribute("message", new Message("message.warning", "message.legale_non_piu_modificabile", "warning"));
					}
				}
				else
				{
					model.addAttribute("currentTab", "tab1");
					model.addAttribute("message", new Message("message.warning", "message.compilare_tab1", "warning"));
				}
				break;

			case "tab3":
				if(accreditamentoWrapper.isSezione2Stato())
				{
					model.addAttribute("currentTab", "tab3");
				}
				else
				{
					if(accreditamentoWrapper.isSezione1Stato()) {
						model.addAttribute("currentTab", "tab2");
						model.addAttribute("message", new Message("message.warning", "message.compilare_tab2", "warning"));
					}
					else {
						model.addAttribute("currentTab", "tab1");
						model.addAttribute("message", new Message("message.warning", "message.compilare_tab1", "warning"));
					}
				}
				break;

			case "tab4":
				if(accreditamentoWrapper.isCompleta())
				{
					model.addAttribute("currentTab", "tab4");
				}
				else {
					if(accreditamentoWrapper.isSezione2Stato()) {
						model.addAttribute("currentTab", "tab3");
						model.addAttribute("message", new Message("message.warning", "message.compilare_altre_tab", "warning"));
					}
					else {
						if(accreditamentoWrapper.isSezione1Stato()) {
							model.addAttribute("currentTab", "tab2");
							model.addAttribute("message", new Message("message.warning", "message.compilare_tab2", "warning"));
						}
						else {
							model.addAttribute("currentTab", "tab1");
							model.addAttribute("message", new Message("message.warning", "message.compilare_tab1", "warning"));
						}
					}
				}
				break;

			default:
				break;
			}
		}
	}

	/*** NEW 	Nuova domanda accreditamento per provider corrente	***/
	@RequestMapping("/provider/accreditamento/{tipoDomanda}/new")
	public String getNewAccreditamentoForCurrentProvider(@PathVariable AccreditamentoTipoEnum tipoDomanda, Model model, RedirectAttributes redirectAttrs) {
		LOGGER.info(Utils.getLogMessage("GET /provider/accreditamento/" + tipoDomanda + "/new"));
		try{
			Long accreditamentoId = accreditamentoService.getNewAccreditamentoForCurrentProvider(tipoDomanda).getId();
			redirectAttrs.addAttribute("id", accreditamentoId);
			LOGGER.info(Utils.getLogMessage("REDIRECT: /accreditamento/" + accreditamentoId + "/edit"));
			return "redirect:/accreditamento/{id}/edit";
		}catch (Exception ex){
			LOGGER.error(Utils.getLogMessage("GET /provider/accreditamento/new"),ex);
			redirectAttrs.addFlashAttribute("message", new Message("message.errore", "message.errore_eccezione", "error"));
			LOGGER.info(Utils.getLogMessage("REDIRECT: /provider/accreditamento/list"));
			return "redirect:/provider/accreditamento/list";
		}
	}

	/*** NEW 	Nuova domanda accreditamento per provider	***/
	@PreAuthorize("@securityAccessServiceImpl.canEditProvider(principal,#providerId)")
	@RequestMapping("/provider/{providerId}/accreditamento/{tipoDomanda}/new")
	public String getNewAccreditamentoForProvider(@PathVariable Long providerId, @PathVariable AccreditamentoTipoEnum tipoDomanda, Model model, RedirectAttributes redirectAttrs) {
		LOGGER.info(Utils.getLogMessage("GET /provider/" + providerId + "/accreditamento/" + tipoDomanda + "/new"));
		try{
			Long accreditamentoId = accreditamentoService.getNewAccreditamentoForProvider(providerId,tipoDomanda).getId();
			redirectAttrs.addAttribute("id", accreditamentoId);
			LOGGER.info(Utils.getLogMessage("REDIRECT: /accreditamento/" + accreditamentoId + "/edit"));
			return "redirect:/accreditamento/{id}/edit";
		}catch (Exception ex){
			LOGGER.error(Utils.getLogMessage("GET /provider/" + providerId +"/accreditamento/new"),ex);
			redirectAttrs.addFlashAttribute("message", new Message("message.errore", "message.errore_eccezione", "error"));
			redirectAttrs.addAttribute("providerId",providerId);
			LOGGER.info(Utils.getLogMessage("REDIRECT: /provider/{providerId}/accreditamento/list"));
			return "redirect:/provider/{providerId}/accreditamento/list";
		}
	}

	/***	INVIA DOMANDA ALLA SEGRETERIA	***/
	@PreAuthorize("@securityAccessServiceImpl.canEditAccreditamento(principal,#accreditamentoId) and @securityAccessServiceImpl.canEditProvider(principal,#providerId)")
	@RequestMapping("/accreditamento/{accreditamentoId}/provider/{providerId}/send")
	public String inviaDomandaAccreditamento(@PathVariable Long accreditamentoId, @PathVariable Long providerId, RedirectAttributes redirectAttrs){
		LOGGER.info(Utils.getLogMessage("GET /accreditamento/" + accreditamentoId +"/provider/" + providerId + "/send"));
		try{
			accreditamentoService.inviaDomandaAccreditamento(accreditamentoId);
			redirectAttrs.addAttribute("providerId",providerId);
			redirectAttrs.addFlashAttribute("message", new Message("message.completato", "message.domanda_inviata", "success"));
			LOGGER.info(Utils.getLogMessage("REDIRECT: /provider/" + providerId + "/accreditamento/list"));
			return "redirect:/provider/{providerId}/accreditamento/list";
		}catch (Exception ex){
			LOGGER.error(Utils.getLogMessage("GET /accreditamento/" + accreditamentoId +"/provider/" + providerId + "/send"),ex);
			redirectAttrs.addAttribute("id",accreditamentoId);
			redirectAttrs.addFlashAttribute("message", new Message("message.errore", "message.errore_eccezione", "error"));
			LOGGER.info(Utils.getLogMessage("REDIRECT: /accreditamento/" + accreditamentoId));;
			return "redirect:/accreditamento/{id}/edit";
		}
	}

	/***	INSERISCI PIANO FORMATIVO	***/
	@PreAuthorize("@securityAccessServiceImpl.canEditAccreditamento(principal,#accreditamentoId) and @securityAccessServiceImpl.canEditProvider(principal,#providerId)")
	@RequestMapping("/accreditamento/{accreditamentoId}/provider/{providerId}/insertPianoFormativo")
	public String inserisciPianoFormativo(@PathVariable Long accreditamentoId, @PathVariable Long providerId, RedirectAttributes redirectAttrs){
		LOGGER.info(Utils.getLogMessage("GET /accreditamento/" + accreditamentoId +"/provider/" + providerId + "/insertPianoFormativo"));
		try{
			accreditamentoService.inserisciPianoFormativo(accreditamentoId);
			redirectAttrs.addAttribute("accreditamentoId", accreditamentoId);
			redirectAttrs.addAttribute("providerId", providerId);
			redirectAttrs.addAttribute("pianoFormativo", LocalDate.now().getYear());
			redirectAttrs.addFlashAttribute("currentTab", "tab4");
			LOGGER.info(Utils.getLogMessage("REDIRECT: /accreditamento/" + accreditamentoId + "/edit"));
			return "redirect:/accreditamento/{accreditamentoId}/edit";
		}catch (Exception ex){
			LOGGER.error(Utils.getLogMessage("GET /accreditamento/" + accreditamentoId +"/provider/" + providerId + "/insertPianoFormativo"),ex);
			redirectAttrs.addAttribute("accreditamentoId",accreditamentoId);
			redirectAttrs.addFlashAttribute("message", new Message("message.errore", "message.errore_eccezione", "error"));
			LOGGER.info(Utils.getLogMessage("REDIRECT: /accreditamento/" + accreditamentoId + "/edit"));
			return "redirect:/accreditamento/{accreditamentoId}/edit";
		}
	}



	/*** METODI PRIVATI PER IL SUPPORTO
	 * @throws Exception ***/
	private AccreditamentoWrapper prepareAccreditamentoWrapperEdit(Accreditamento accreditamento) throws Exception{
		LOGGER.info(Utils.getLogMessage("prepareAccreditamentoWrapper(" + accreditamento.getId() + ") - entering"));

		AccreditamentoWrapper accreditamentoWrapper = new AccreditamentoWrapper(accreditamento);
		if(accreditamento.getStato() == AccreditamentoStatoEnum.INTEGRAZIONE || accreditamento.getStato() == AccreditamentoStatoEnum.PREAVVISO_RIGETTO){
			integrazionePrepareAccreditamentoWrapper(accreditamentoWrapper);
			accreditamentoWrapper.setCanSendIntegrazione(accreditamentoService.canUserInviaIntegrazione(accreditamento.getId(),Utils.getAuthenticatedUser()));
		}

		commonPrepareAccreditamentoWrapper(accreditamentoWrapper, AccreditamentoWrapperModeEnum.EDIT);

		//la segreteria ha sempre tutti gli id edit sbloccati, a meno che non sia in stato integrazione o preavviso di rigetto
		if (Utils.getAuthenticatedUser().getAccount().isSegreteria()
				&& !accreditamento.isIntegrazione() && !accreditamento.isPreavvisoRigetto()) {
			accreditamentoWrapper.setCanSegreteriaEdit(true);
		}
		else accreditamentoWrapper.setCanSegreteriaEdit(false);


		LOGGER.info(Utils.getLogMessage("prepareAccreditamentoWrapper(" + accreditamento.getId() + ") - exiting"));
		return accreditamentoWrapper;
	}

	private AccreditamentoWrapper prepareAccreditamentoWrapperShow(Accreditamento accreditamento, AccreditamentoWrapper accreditamentoWrapper, Boolean hasVerbaleErrors) throws Exception{
		LOGGER.info(Utils.getLogMessage("prepareAccreditamentoWrapperShow(" + accreditamento.getId() + ") - entering"));

		if(accreditamentoWrapper == null)
			accreditamentoWrapper = new AccreditamentoWrapper(accreditamento);
		else
			accreditamentoWrapper.setAllAccreditamento(accreditamento);

		commonPrepareAccreditamentoWrapper(accreditamentoWrapper, AccreditamentoWrapperModeEnum.SHOW);
		CurrentUser user = Utils.getAuthenticatedUser();

		//logica per mostrare i pulsanti relativi alla valutazione della segreteria o i referee
		accreditamentoWrapper.setCanPrendiInCarica(accreditamentoService.canUserPrendiInCarica(accreditamento.getId(), user));
		accreditamentoWrapper.setCanValutaDomanda(accreditamentoService.canUserValutaDomanda(accreditamento.getId(), user));
		accreditamentoWrapper.setCanShowValutazioneRiepilogo(accreditamentoService.canUserValutaDomandaShowRiepilogo(accreditamento.getId(), user));
		accreditamentoWrapper.setCanEnableField(accreditamentoService.canUserEnableField(user, accreditamento.getId()));
		//controllo se devo mostrare i pulsanti presa visione/rimanda in valutazione da parte dello stesso crecm
		accreditamentoWrapper.setCanPresaVisione(accreditamentoService.canUserPresaVisione(accreditamento.getId(), Utils.getAuthenticatedUser()));

		//controllo se l'utente può visualizzare la valutazione
		accreditamentoWrapper.setCanShowValutazione(accreditamentoService.canUserValutaDomandaShow(accreditamento.getId(), user));

		//controllo se devo mostrare il pulsante per riassegnare i referee crecm e in caso quanti
		if(accreditamentoService.canRiassegnaGruppo(accreditamento.getId(), user)) {
			accreditamentoWrapper.setCanAssegnaNuovoGruppo(true);
			accreditamentoWrapper.setRefereeDaRiassegnare(valutazioneService.countRefereeNotValutatoriForAccreditamentoId(accreditamento.getId()));
		}

		//gestione modifica verbale valutazione sul campo
		if(accreditamento.isValutazioneSulCampo() && accreditamento.isStandard() && user.isSegreteria()) {
			//set scelta select
			accreditamentoWrapper.setComponentiCRECM(accountService.getUserByProfileEnum(ProfileEnum.REFEREE));
			accreditamentoWrapper.setOsservatoriRegionali(accountService.getUserByProfileEnum(ProfileEnum.COMPONENTE_OSSERVATORIO));
			accreditamentoWrapper.setComponentiSegreteria(accountService.getUserByProfileEnum(ProfileEnum.SEGRETERIA));
			accreditamentoWrapper.setReferentiInformatici(accountService.getUserByProfileEnum(ProfileEnum.REFERENTE_INFORMATICO));
			accreditamentoWrapper.setSediProvider(accreditamento.getProvider().getSedi());
			if(!hasVerbaleErrors)
				accreditamentoWrapper.setVerbaleValutazioneSulCampo(accreditamento.getVerbaleValutazioneSulCampo());
		}

		LOGGER.info(Utils.getLogMessage("prepareAccreditamentoWrapperShow(" + accreditamento.getId() + ") - exiting"));
		return accreditamentoWrapper;
	}

	private AccreditamentoWrapper prepareAccreditamentoWrapperEnableField(Accreditamento accreditamento){
		LOGGER.info(Utils.getLogMessage("prepareAccreditamentoWrapperEnableField(" + accreditamento.getId() + ") - entering"));
		AccreditamentoWrapper accreditamentoWrapper = new AccreditamentoWrapper(accreditamento);

		commonPrepareAccreditamentoWrapper(accreditamentoWrapper, AccreditamentoWrapperModeEnum.ENABLE_FIELD);

		LOGGER.info(Utils.getLogMessage("prepareAccreditamentoWrapperEnableField(" + accreditamento.getId() + ") - exiting"));
		return accreditamentoWrapper;
	}

	private AccreditamentoWrapper prepareAccreditamentoWrapperValidate(Accreditamento accreditamento, Valutazione valutazione, AccreditamentoWrapper accreditamentoWrapper, Boolean hasErrors) throws Exception{
		LOGGER.info(Utils.getLogMessage("prepareAccreditamentoWrapperValidate(" + accreditamento.getId() + ") - entering"));

		if(accreditamentoWrapper == null)
			accreditamentoWrapper = new AccreditamentoWrapper(accreditamento);
		else
			accreditamentoWrapper.setAllAccreditamento(accreditamento);

		if(accreditamento.getStato() == AccreditamentoStatoEnum.VALUTAZIONE_SEGRETERIA){
			integrazionePrepareAccreditamentoWrapper(accreditamentoWrapper);
		}

		//lista valutazioni per la valutazione complessiva
		accreditamentoWrapper.setValutazioniList(valutazioneService.getAllValutazioniForAccreditamentoIdAndNotStoricizzato(accreditamento.getId()));

		//controllo sul pulsante conferma valutazione
		accreditamentoWrapper.setCanValutaDomanda(accreditamentoService.canUserValutaDomanda(accreditamento.getId(), Utils.getAuthenticatedUser()));

		//controllo se devo mostrare i pulsanti presa visione/rimanda in valutazione da parte dello stesso crecm
		accreditamentoWrapper.setCanPresaVisione(accreditamentoService.canUserPresaVisione(accreditamento.getId(), Utils.getAuthenticatedUser()));

		//inserisco i suoi fieldValutazione nella mappa per il wrapper
		Map<IdFieldEnum, FieldValutazioneAccreditamento> mappa = new HashMap<IdFieldEnum, FieldValutazioneAccreditamento>();
		if(valutazione != null) {
			mappa = fieldValutazioneAccreditamentoService.putSetFieldValutazioneInMap(valutazione.getValutazioni());
		}
		//fieldValutazione relativi al verbale della valutazione sul campo
		if(accreditamento.isValutazioneSulCampo()) {
			Map<IdFieldEnum, FieldValutazioneAccreditamento> mappaValutazioniSulCampo = new HashMap<IdFieldEnum, FieldValutazioneAccreditamento>();
			mappaValutazioniSulCampo = fieldValutazioneAccreditamentoService.putSetFieldValutazioneInMap(accreditamento.getVerbaleValutazioneSulCampo().getDatiValutazioneSulCampo().getValutazioniSulCampo());
			mappa.putAll(mappaValutazioniSulCampo);
		}

		accreditamentoWrapper.setMappa(mappa);

		//init delle strutture dati che servono per la verifica degli stati di valutazione dei multistanza
		Map<Long, Map<IdFieldEnum, FieldValutazioneAccreditamento>> mappaSedi = new HashMap<Long, Map<IdFieldEnum, FieldValutazioneAccreditamento>>();
		Map<Long, Boolean> sediStati = new HashMap<Long, Boolean>();
		Map<IdFieldEnum, FieldValutazioneAccreditamento> mappaSedeLegale = new HashMap<IdFieldEnum, FieldValutazioneAccreditamento>();
		Map<Long, Map<IdFieldEnum, FieldValutazioneAccreditamento>> mappaComponenti = new HashMap<Long, Map<IdFieldEnum, FieldValutazioneAccreditamento>>();
		Map<Long, Boolean> componentiComitatoScientificoStati = new HashMap<Long, Boolean>();
		Map<IdFieldEnum, FieldValutazioneAccreditamento> mappaCoordinatore = new HashMap<IdFieldEnum, FieldValutazioneAccreditamento>();
		Map<Long, Map<IdFieldEnum, FieldValutazioneAccreditamento>> mappaEventi = new HashMap<Long, Map<IdFieldEnum, FieldValutazioneAccreditamento>>();
		Map<Long, Boolean> eventiStati = new HashMap<Long, Boolean>();

		//aggiungo le sedi
		for(Sede s : accreditamentoWrapper.getSedi()) {
			mappaSedi.put(s.getId(), fieldValutazioneAccreditamentoService.filterFieldValutazioneByObjectAsMap(valutazione.getValutazioni(), s.getId()));
			sediStati.put(s.getId(), false);
		}
		//aggiungo anche la sede legale
		Long sedeLegaleId = accreditamentoWrapper.getSedeLegale().getId();
		mappaSedeLegale = fieldValutazioneAccreditamentoService.filterFieldValutazioneByObjectAsMap(valutazione.getValutazioni(), sedeLegaleId);

		//aggiungo i componenti del comitato scientifico
		for(Persona p : accreditamentoWrapper.getComponentiComitatoScientifico()) {
			mappaComponenti.put(p.getId(), fieldValutazioneAccreditamentoService.filterFieldValutazioneByObjectAsMap(valutazione.getValutazioni(), p.getId()));
			componentiComitatoScientificoStati.put(p.getId(), false);
		}
		//aggiungo anche il coordinatore

		if(accreditamentoWrapper.getCoordinatoreComitatoScientifico() != null){
			Long coordinatoreId = accreditamentoWrapper.getCoordinatoreComitatoScientifico().getId();
			mappaCoordinatore = fieldValutazioneAccreditamentoService.filterFieldValutazioneByObjectAsMap(valutazione.getValutazioni(), coordinatoreId);
		}

		//aggiungo gli eventi
//		for(EventoPianoFormativo e : accreditamentoWrapper.getAccreditamento().getPianoFormativo().getEventiPianoFormativo()) {
//			mappaEventi.put(e.getId(), fieldValutazioneAccreditamentoService.filterFieldValutazioneByObjectAsMap(valutazione.getValutazioni(), e.getId()));
//			eventiStati.put(e.getId(), false);
//		}

		accreditamentoWrapper.setMappaSedeLegale(mappaSedeLegale);
		accreditamentoWrapper.setMappaSedi(mappaSedi);
		accreditamentoWrapper.setSediStati(sediStati);
		accreditamentoWrapper.setMappaCoordinatore(mappaCoordinatore);
		accreditamentoWrapper.setMappaComponenti(mappaComponenti);
		accreditamentoWrapper.setComponentiComitatoScientificoStati(componentiComitatoScientificoStati);
		//TODO togliere se confermato che non serve più
//		accreditamentoWrapper.setMappaEventi(mappaEventi);
//		accreditamentoWrapper.setEventiStati(eventiStati);

		//gestione valutazione sul campo / preparazione verbale valutazione sul campo
		if(accreditamento.isValutazioneSulCampo() || (accreditamento.isValutazioneSegreteriaAssegnamento() && accreditamento.isStandard())) {
			//set scelta select
			accreditamentoWrapper.setComponentiCRECM(accountService.getUserByProfileEnum(ProfileEnum.REFEREE));
			accreditamentoWrapper.setOsservatoriRegionali(accountService.getUserByProfileEnum(ProfileEnum.COMPONENTE_OSSERVATORIO));
			accreditamentoWrapper.setComponentiSegreteria(accountService.getUserByProfileEnum(ProfileEnum.SEGRETERIA));
			accreditamentoWrapper.setReferentiInformatici(accountService.getUserByProfileEnum(ProfileEnum.REFERENTE_INFORMATICO));
			accreditamentoWrapper.setSediProvider(accreditamento.getProvider().getSedi());
			//set campi precompilati
			if(!hasErrors) {
				if(accreditamento.getVerbaleValutazioneSulCampo() == null) {
					VerbaleValutazioneSulCampo verbaleValutazioneSulCampo = new VerbaleValutazioneSulCampo();
					verbaleValutazioneSulCampo.setProvider(accreditamento.getProvider());
					verbaleValutazioneSulCampo.setAccreditamento(accreditamento);
					accreditamentoWrapper.setVerbaleValutazioneSulCampo(verbaleValutazioneSulCampo);
				}
				else
					accreditamentoWrapper.setVerbaleValutazioneSulCampo(accreditamento.getVerbaleValutazioneSulCampo());
			}
			else {
				VerbaleValutazioneSulCampo verbaleValutazioneSulCampo = accreditamentoWrapper.getVerbaleValutazioneSulCampo();
				verbaleValutazioneSulCampo.setProvider(accreditamento.getProvider());
				verbaleValutazioneSulCampo.setAccreditamento(accreditamento);
				accreditamentoWrapper.setVerbaleValutazioneSulCampo(verbaleValutazioneSulCampo);
			}
		}

		commonPrepareAccreditamentoWrapper(accreditamentoWrapper, AccreditamentoWrapperModeEnum.VALIDATE);

		LOGGER.info(Utils.getLogMessage("prepareAccreditamentoWrapperValidate(" + accreditamento.getId() + ") - exiting"));
		return accreditamentoWrapper;
	}

	private void commonPrepareAccreditamentoWrapper(AccreditamentoWrapper accreditamentoWrapper, AccreditamentoWrapperModeEnum mode){
		Long providerId = accreditamentoWrapper.getProvider().getId();
		//ALLEGATI
		//Set<String> filesDelProvider = providerService.getFileTypeUploadedByProviderId(providerId);
		Set<String> filesDelProvider = new HashSet<>();
		if(accreditamentoWrapper.getDatiAccreditamento() != null && !accreditamentoWrapper.getDatiAccreditamento().isNew())
			filesDelProvider = datiAccreditamentoService.getFileTypeUploadedByDatiAccreditamentoId(accreditamentoWrapper.getDatiAccreditamento().getId());

		Set<Professione> professioniSelezionate = (accreditamentoWrapper.getAccreditamento().getDatiAccreditamento() != null && !accreditamentoWrapper.getDatiAccreditamento().isNew()) ? accreditamentoWrapper.getDatiAccreditamento().getProfessioniSelezionate() : new HashSet<Professione>();

		int numeroComponentiComitatoScientifico = personaService.numeroComponentiComitatoScientifico(providerId);
		int numeroProfessionistiSanitarie 		= personaService.numeroComponentiComitatoScientificoConProfessioneSanitaria(providerId);
		//int professioniDeiComponenti 			= personaService.numeroProfessioniDistinteDeiComponentiComitatoScientifico(providerId);
		int professioniDeiComponentiAnaloghe 	= (professioniSelezionate.size() > 0) ? personaService.numeroProfessioniDistinteAnalogheAProfessioniSelezionateDeiComponentiComitatoScientifico(providerId, professioniSelezionate) : 0;
		Set<Professione> elencoProfessioniDeiComponenti = personaService.elencoProfessioniDistinteDeiComponentiComitatoScientifico(providerId);

		accreditamentoWrapper.setNoteOsservazioniIntegrazione(accreditamentoWrapper.getAccreditamento().getNoteOsservazioniIntegrazione());
		accreditamentoWrapper.setNoteOsservazioniPreavvisoRigetto(accreditamentoWrapper.getAccreditamento().getNoteOsservazioniPreavvisoRigetto());

//		LOGGER.debug(Utils.getLogMessage("<*>NUMERO COMPONENTI: " + numeroComponentiComitatoScientifico));
//		LOGGER.debug(Utils.getLogMessage("<*>NUMERO PROFESSIONISTI SANITARI: " + numeroProfessionistiSanitarie));
//		LOGGER.debug(Utils.getLogMessage("<*>NUMERO PROFESSIONI DISTINTE: " + elencoProfessioniDeiComponenti.size()));
//		LOGGER.debug(Utils.getLogMessage("<*>NUMERO PROFESSIONI ANALOGHE: " + professioniDeiComponentiAnaloghe));

		accreditamentoWrapper.checkStati(numeroComponentiComitatoScientifico, numeroProfessionistiSanitarie, elencoProfessioniDeiComponenti, professioniDeiComponentiAnaloghe, filesDelProvider, mode);

	}

	private void integrazionePrepareAccreditamentoWrapper(AccreditamentoWrapper accreditamentoWrapper){
		Set<Sede> sediIntegrazione = sedeService.getSediFromIntegrazione(accreditamentoWrapper.getProvider().getId());
		for(Sede s : sediIntegrazione){
			if(s.isDirty())
				accreditamentoWrapper.getSedi().add(s);
		}

		Set<Persona> comitatoIntegrazione = personaService.getComponentiComitatoScientificoFromIntegrazione(accreditamentoWrapper.getProvider().getId());
		for(Persona p : comitatoIntegrazione){
			if(p.isDirty())
				accreditamentoWrapper.getComponentiComitatoScientifico().add(p);
		}

		accreditamentoWrapper.setAggiunti(fieldIntegrazioneAccreditamentoService.getAllObjectIdByTipoIntegrazione(accreditamentoWrapper.getAccreditamento().getId(), TipoIntegrazioneEnum.CREAZIONE));
		accreditamentoWrapper.setEliminati(fieldIntegrazioneAccreditamentoService.getAllObjectIdByTipoIntegrazione(accreditamentoWrapper.getAccreditamento().getId(), TipoIntegrazioneEnum.ELIMINAZIONE));
	}

	//PARTE RELATIVA ALLA VALUTAZIONE

	//prendi in carica segreteria ECM
	@PreAuthorize("@securityAccessServiceImpl.canPrendiInCaricaAccreditamento(principal,#accreditamentoId)")
	@RequestMapping("/accreditamento/{accreditamentoId}/takeCharge")
	public String prendiInCaricoAccreditamento(@PathVariable Long accreditamentoId, RedirectAttributes redirectAttrs){
		LOGGER.info(Utils.getLogMessage("GET /accreditamento/" + accreditamentoId + "/takeCharge"));
		try {
			accreditamentoService.prendiInCarica(accreditamentoId, Utils.getAuthenticatedUser());
			redirectAttrs.addFlashAttribute("message", new Message("message.completato", "message.presa_in_carico", "success"));
			return "redirect:/accreditamento/{accreditamentoId}/validate";
		}catch (Exception ex){
			LOGGER.error(Utils.getLogMessage("GET /accreditamento/" + accreditamentoId + "/takeCharge"),ex);
			redirectAttrs.addAttribute("accreditamentoId",accreditamentoId);
			redirectAttrs.addFlashAttribute("message", new Message("message.errore", "message.errore_eccezione", "error"));
			LOGGER.info(Utils.getLogMessage("REDIRECT: /accreditamento/" + accreditamentoId + "/show"));
			return "redirect:/accreditamento/{accreditamentoId}/show";
		}
	}

	// salva valutazione complessiva (inserisce data e valutazione complessiva)
	// PROVVISORIO
	// se lo stato è VALUTAZIONE_SEGRETERIA_ASSEGNAMENTO assegna un gruppo crecm e crea le valutazioni corrispondenti ai referee
	// se lo stato è VALUTAZIONE_SEGRETERIA riassegna lo stesso gruppo crecm eliminando la data della valutazione corrispondente a ciascun referee
	@PreAuthorize("@securityAccessServiceImpl.canValidateAccreditamento(principal,#accreditamentoId)")
	@RequestMapping(value = "/accreditamento/{accreditamentoId}/confirmEvaluation", method = RequestMethod.POST)
	public String confermaValutazioneAccreditamento(@ModelAttribute("accreditamentoWrapper") AccreditamentoWrapper wrapper, BindingResult result,
			@PathVariable Long accreditamentoId, Model model, RedirectAttributes redirectAttrs){
		LOGGER.info(Utils.getLogMessage("GET /accreditamento/" + accreditamentoId + "/confirmEvaluation"));
		Accreditamento accreditamento = accreditamentoService.getAccreditamento(accreditamentoId);
		try {
			if((accreditamento.isValutazioneSegreteriaAssegnamento() || accreditamento.isValutazioneCrecm()) && accreditamento.isProvvisorio())  {

				//validazione della valutazioneComplessiva
				valutazioneValidator.validateValutazioneComplessiva(wrapper.getRefereeGroup(), wrapper.getValutazioneComplessiva(), AccreditamentoStatoEnum.VALUTAZIONE_SEGRETERIA_ASSEGNAMENTO, result);

				if(result.hasErrors()){
					model.addAttribute("message",new Message("message.errore", "message.inserire_campi_required", "error"));
					model.addAttribute("confirmErrors", true);

					return goToAccreditamentoValidate(model, accreditamento, wrapper);
				}else {
					accreditamentoService.inviaValutazioneDomanda(accreditamentoId, wrapper.getValutazioneComplessiva(), wrapper.getRefereeGroup(), null);
					LOGGER.info(Utils.getLogMessage("REDIRECT: /accreditamento/" + accreditamentoId + "/show"));
					redirectAttrs.addAttribute("accreditamentoId",accreditamentoId);
					redirectAttrs.addFlashAttribute("message", new Message("message.completato", "message.valutazione_complessiva_salvata", "success"));
					return "redirect:/accreditamento/{accreditamentoId}/show";
				}
			}
			// stato VALUTAZIONE_SEGRETERIA dove valuto le integrazioni per domanda accreditamento provvisorio
			else if(accreditamento.isValutazioneSegreteria() && accreditamento.isProvvisorio()){

				//validazione della valutazioneComplessiva
				valutazioneValidator.validateValutazioneComplessiva(wrapper.getRefereeGroup(), wrapper.getValutazioneComplessiva(), AccreditamentoStatoEnum.VALUTAZIONE_SEGRETERIA, result);

				if(result.hasErrors()){
					model.addAttribute("message",new Message("message.errore", "message.inserire_campi_required", "error"));
					model.addAttribute("confirmErrors", true);
					return goToAccreditamentoValidate(model, accreditamento, wrapper);
				}else {
					accreditamentoService.assegnaStessoGruppoCrecm(accreditamentoId, wrapper.getValutazioneComplessiva());

					LOGGER.info(Utils.getLogMessage("REDIRECT: /accreditamento/" + accreditamentoId + "/show"));
					redirectAttrs.addAttribute("accreditamentoId",accreditamentoId);
					redirectAttrs.addFlashAttribute("message", new Message("message.completato", "message.valutazione_salvata_gruppo_riassegnato", "success"));
					return "redirect:/accreditamento/{accreditamentoId}/show";
				}
			}
			// stato VALUTAZIONE_SEGRETERIA dove valuto le integrazioni per domanda accreditamento standard
			else if(accreditamento.isValutazioneSegreteria() && accreditamento.isStandard()){

				//validazione della valutazioneComplessiva
				valutazioneValidator.validateValutazioneComplessivaTeamLeader(wrapper.getValutazioneComplessiva(), AccreditamentoStatoEnum.VALUTAZIONE_SEGRETERIA, result);

				if(result.hasErrors()){
					model.addAttribute("message",new Message("message.errore", "message.inserire_campi_required", "error"));
					model.addAttribute("confirmErrors", true);
					return goToAccreditamentoValidate(model, accreditamento, wrapper);
				}else {
					accreditamentoService.assegnaTeamLeader(accreditamentoId, wrapper.getValutazioneComplessiva());

					LOGGER.info(Utils.getLogMessage("REDIRECT: /accreditamento/" + accreditamentoId + "/show"));
					redirectAttrs.addAttribute("accreditamentoId",accreditamentoId);
					redirectAttrs.addFlashAttribute("message", new Message("message.completato", "message.valutazione_salvata_team_leader_assegnato", "success"));
					return "redirect:/accreditamento/{accreditamentoId}/show";
				}
			}
			else if(accreditamento.isValutazioneSegreteriaAssegnamento() && accreditamento.isStandard()) {

				VerbaleValutazioneSulCampo verbale = wrapper.getVerbaleValutazioneSulCampo();

				//validazione della valutazioneComplessiva
				valutazioneValidator.validateValutazioneSulCampo(verbale, wrapper.getValutazioneComplessiva(), result, "verbaleValutazioneSulCampo.", AccreditamentoStatoEnum.VALUTAZIONE_SEGRETERIA_ASSEGNAMENTO);

				if(result.hasErrors()){
					model.addAttribute("message",new Message("message.errore", "message.inserire_campi_required", "error"));
					model.addAttribute("confirmErrors", true);

					return goToAccreditamentoValidate(model, accreditamento, wrapper);
				}else {
					accreditamentoService.inviaValutazioneDomanda(accreditamentoId, wrapper.getValutazioneComplessiva(), null, verbale);
					LOGGER.info(Utils.getLogMessage("REDIRECT: /accreditamento/" + accreditamentoId + "/show"));
					redirectAttrs.addAttribute("accreditamentoId",accreditamentoId);
					redirectAttrs.addFlashAttribute("message", new Message("message.completato", "message.valutazione_complessiva_salvata", "success"));
					return "redirect:/accreditamento/{accreditamentoId}/show";
				}
			}
			else if(accreditamento.isValutazioneSulCampo()){

				//validate dello stato di destinazione della domanda standard
				if(wrapper.getDestinazioneStatoDomandaStandard() == null)
					result.rejectValue("destinazioneStatoDomandaStandard", "error.empty");
				//validate dell'allegato pdf verbale firmato
				if(wrapper.getVerbalePdfFirmato() == null || wrapper.getVerbalePdfFirmato().isNew())
					result.rejectValue("accreditamento.verbaleValutazioneSulCampoPdf", "error.empty");

				if(result.hasErrors()){
					model.addAttribute("message",new Message("message.errore", "message.inserire_campi_required", "error"));
					model.addAttribute("confirmErrors", true);
					return goToAccreditamentoValidate(model, accreditamento, wrapper);
				}else {
					accreditamentoService.inviaValutazioneSulCampo(accreditamentoId, wrapper.getValutazioneComplessiva(), wrapper.getVerbalePdfFirmato(), wrapper.getDestinazioneStatoDomandaStandard());

					LOGGER.info(Utils.getLogMessage("REDIRECT: /accreditamento/" + accreditamentoId + "/show"));
					redirectAttrs.addAttribute("accreditamentoId",accreditamentoId);
					redirectAttrs.addFlashAttribute("message", new Message("message.completato", "message.valutazione_complessiva_salvata", "success"));
					return "redirect:/accreditamento/{accreditamentoId}/show";
				}
			}

			return goToAccreditamentoValidate(model, accreditamento, wrapper);
		}
		catch (Exception ex){
			LOGGER.error(Utils.getLogMessage("GET /accreditamento/" + accreditamentoId + "/confirmEvaluation"),ex);
			redirectAttrs.addAttribute("accreditamentoId",accreditamentoId);
			redirectAttrs.addFlashAttribute("message", new Message("message.errore", "message.errore_eccezione", "error"));
			LOGGER.info(Utils.getLogMessage("REDIRECT: /accreditamento/" + accreditamentoId + "/validate"));
			return "redirect:/accreditamento/{accreditamentoId}/validate";
		}
	}

    @PreAuthorize("@securityAccessServiceImpl.canEditVerbaleAccreditamento(principal,#accreditamentoId)")
	@RequestMapping(value = "/accreditamento/{accreditamentoId}/verbale/edit", method = RequestMethod.POST)
	public String editVerbaleValutazioneSulCampo(@ModelAttribute("accreditamentoWrapper") AccreditamentoWrapper wrapper, BindingResult result,
			@PathVariable Long accreditamentoId, Model model, RedirectAttributes redirectAttrs){
		LOGGER.info(Utils.getLogMessage("POST /accreditamento/" + accreditamentoId + "/verbale/edit"));
		Accreditamento accreditamento = accreditamentoService.getAccreditamento(accreditamentoId);
		try {

			VerbaleValutazioneSulCampo verbale = wrapper.getVerbaleValutazioneSulCampo();

			//validazione della valutazioneComplessiva
			valutazioneValidator.validateEditVerbale(verbale, result, "verbaleValutazioneSulCampo.");

			if(result.hasErrors()){
				model.addAttribute("message",new Message("message.errore", "message.inserire_campi_required", "error"));
				model.addAttribute("verbaleErrors", true);
				model.addAttribute("currentTab", "tab7");
				return goToAccreditamentoShow(model, accreditamento, wrapper);
			}
			else {
				accreditamentoService.editScheduleVerbaleValutazioneSulCampo(accreditamento, verbale);
				redirectAttrs.addFlashAttribute("message",new Message("message.completato", "message.verbale_salvato", "success"));
				redirectAttrs.addAttribute("tab", "tab7");
				return "redirect:/accreditamento/{accreditamentoId}/show";
			}
		}
		catch (Exception ex){
			LOGGER.error(Utils.getLogMessage("POST /accreditamento/" + accreditamentoId + "/verbale/edit"),ex);
			redirectAttrs.addAttribute("accreditamentoId",accreditamentoId);
			redirectAttrs.addFlashAttribute("message", new Message("message.errore", "message.errore_eccezione", "error"));
			LOGGER.info(Utils.getLogMessage("REDIRECT: /accreditamento/" + accreditamentoId + "/show"));
			return "redirect:/accreditamento/{accreditamentoId}/show";
		}
	}

	@PreAuthorize("@securityAccessServiceImpl.canValidateAccreditamento(principal,#accreditamentoId,#showRiepilogo)")
	@RequestMapping("/accreditamento/{accreditamentoId}/verbaleValutazioneSulCampo/{verbaleValutazioneSulCampoId}/insertValutazione")
	public String insertValutazioniSulCampo(@RequestParam(name = "showRiepilogo", required = false) Boolean showRiepilogo,
			@PathVariable Long accreditamentoId, @PathVariable Long verbaleValutazioneSulCampoId,
			Model model, RedirectAttributes redirectAttrs) {
		LOGGER.info(Utils.getLogMessage("GET /accreditamento/" + accreditamentoId + "/verbaleValutazioneSulCampo/" + verbaleValutazioneSulCampoId + "/insertValutazione"));
		try {
			Accreditamento accreditamento = accreditamentoService.getAccreditamento(accreditamentoId);
			model.addAttribute("verbaleWrapper", prepareVerbaleWrapper(accreditamento));
			model.addAttribute("canValutaDomanda", accreditamentoService.canUserValutaDomanda(accreditamento.getId(), Utils.getAuthenticatedUser()));
			return "accreditamento/accreditamentoValutazioniSulCampo";
		}catch (Exception ex){
			LOGGER.error(Utils.getLogMessage("GET /accreditamento/" + accreditamentoId + "/verbaleValutazioneSulCampo/" + verbaleValutazioneSulCampoId + "/insertValutazione"),ex);
			redirectAttrs.addAttribute("accreditamentoId",accreditamentoId);
			redirectAttrs.addFlashAttribute("message", new Message("message.errore", "message.errore_eccezione", "error"));
			LOGGER.info(Utils.getLogMessage("REDIRECT: /accreditamento/" + accreditamentoId + "/validate"));
			return "redirect:/accreditamento/{accreditamentoId}/validate";
		}
	}

	private VerbaleValutazioneSulCampoWrapper prepareVerbaleWrapper(Accreditamento accreditamento) throws Exception {
		VerbaleValutazioneSulCampoWrapper wrapper = new VerbaleValutazioneSulCampoWrapper();
		wrapper.setAccreditamento(accreditamento);
		wrapper.setVerbale(accreditamento.getVerbaleValutazioneSulCampo());
		wrapper.setMappaErroriValutazione(new HashMap<String, String>());
		//carico la valutazione per l'utente
		Map<IdFieldEnum, FieldValutazioneAccreditamento> mappa = new HashMap<IdFieldEnum, FieldValutazioneAccreditamento>();
		Set<FieldValutazioneAccreditamento> valutazioniSulCampo = accreditamento.getVerbaleValutazioneSulCampo().getDatiValutazioneSulCampo().getValutazioniSulCampo();
		SubSetFieldEnum subset = SubSetFieldEnum.VALUTAZIONE_SUL_CAMPO;
		mappa = fieldValutazioneAccreditamentoService.filterFieldValutazioneBySubSetAsMap(valutazioniSulCampo, subset);
		Map<Account, Map<IdFieldEnum, FieldValutazioneAccreditamento>> mappaValutatoreValutazioni = new HashMap<Account, Map<IdFieldEnum, FieldValutazioneAccreditamento>>();
		Account valutatore = accreditamento.getVerbaleValutazioneSulCampo().getValutatore();
		mappaValutatoreValutazioni.put(valutatore, mappa);
		wrapper.setMappa(mappa);
		wrapper.setMappaValutatoreValutazioni(mappaValutatoreValutazioni);
		//prendo gli idEditabili
		Set<IdFieldEnum> fieldValutazioneSulCampo = IdFieldEnum.getAllForSubset(subset);
		wrapper.setIdEditabili(fieldValutazioneSulCampo);
		return wrapper;
	}

	@PreAuthorize("@securityAccessServiceImpl.canValidateAccreditamento(principal,#accreditamentoId)")
	@RequestMapping("/accreditamento/{accreditamentoId}/verbaleValutazioneSulCampo/{verbaleValutazioneSulCampoId}/insertSottoscrivente")
	public String insertSottoscriventeValutazioniSulCampo(@ModelAttribute("accreditamentoWrapper") AccreditamentoWrapper wrapper,
			@PathVariable Long accreditamentoId, @PathVariable Long verbaleValutazioneSulCampoId,
			Model model, RedirectAttributes redirectAttrs) {
		LOGGER.info(Utils.getLogMessage("GET /accreditamento/" + accreditamentoId + "/verbaleValutazioneSulCampo/" + verbaleValutazioneSulCampoId + "/insertSottoscrivente"));
		try {
			Accreditamento accreditamento = accreditamentoService.getAccreditamento(accreditamentoId);
			prepareAccreditamentoWrapperSottoscrivente(wrapper, accreditamento, false);
			model.addAttribute("accreditamentoWrapper", wrapper);
			return "accreditamento/accreditamentoSottoscriventeVerbale";
		}catch (Exception ex){
			LOGGER.error(Utils.getLogMessage("GET /accreditamento/" + accreditamentoId + "/verbaleValutazioneSulCampo/" + verbaleValutazioneSulCampoId + "/insertSottoscrivente"),ex);
			redirectAttrs.addAttribute("accreditamentoId",accreditamentoId);
			redirectAttrs.addFlashAttribute("message", new Message("message.errore", "message.errore_eccezione", "error"));
			LOGGER.info(Utils.getLogMessage("REDIRECT: /accreditamento/" + accreditamentoId + "/validate"));
			return "redirect:/accreditamento/{accreditamentoId}/validate";
		}
	}

	private void prepareAccreditamentoWrapperSottoscrivente(AccreditamentoWrapper wrapper, Accreditamento accreditamento, Boolean hasErrors) {
		if(!hasErrors) {
			wrapper.setVerbaleValutazioneSulCampo(accreditamento.getVerbaleValutazioneSulCampo());
			wrapper.setCartaIdentita(accreditamento.getVerbaleValutazioneSulCampo().getCartaIdentita());
			if(accreditamento.getVerbaleValutazioneSulCampo().getDelegato() != null)
				wrapper.setDelegaValutazioneSulCampo(accreditamento.getVerbaleValutazioneSulCampo().getDelegato().getDelega());
		}
		wrapper.setAccreditamento(accreditamento);
		wrapper.setLegaleRappresentante(accreditamento.getProvider().getLegaleRappresentante());
	}

	@PreAuthorize("@securityAccessServiceImpl.canValidateAccreditamento(principal,#accreditamentoId)")
	@RequestMapping("/accreditamento/{accreditamentoId}/verbaleValutazioneSulCampo/{verbaleValutazioneSulCampoId}/sottoscrivente/save")
	public String salvaSottoscriventeValutazioniSulCampo(@ModelAttribute("accreditamentoWrapper") AccreditamentoWrapper wrapper, BindingResult result,
			@PathVariable Long accreditamentoId, @PathVariable Long verbaleValutazioneSulCampoId,
			Model model, RedirectAttributes redirectAttrs) {
		LOGGER.info(Utils.getLogMessage("POST /accreditamento/" + accreditamentoId + "/verbaleValutazioneSulCampo/" + verbaleValutazioneSulCampoId + "/sottoscrivente/save"));
		try {

			//attacca gli allegati dal wrapper alla valutazione sul campo
			VerbaleValutazioneSulCampo verbale = wrapper.getVerbaleValutazioneSulCampo();
			if(wrapper.getCartaIdentita() != null && !wrapper.getCartaIdentita().isNew())
				verbale.setCartaIdentita(fileService.getFile(wrapper.getCartaIdentita().getId()));
			if(wrapper.getDelegaValutazioneSulCampo() != null && !wrapper.getDelegaValutazioneSulCampo().isNew())
				verbale.getDelegato().setDelega(fileService.getFile(wrapper.getDelegaValutazioneSulCampo().getId()));

			valutazioneValidator.validateSottoscriventeValutazioneSulCampo(verbale, result, "verbaleValutazioneSulCampo.");
			Accreditamento accreditamento = accreditamentoService.getAccreditamento(accreditamentoId);

			if(result.hasErrors()) {
				prepareAccreditamentoWrapperSottoscrivente(wrapper, accreditamento, true);
				model.addAttribute("message",new Message("message.errore", "message.inserire_campi_required", "error"));
				model.addAttribute("accreditamentoWrapper", wrapper);
				return "accreditamento/accreditamentoSottoscriventeVerbale";
			}
			else {
				accreditamentoService.saveSottoscriventeVerbaleValutazioneSulCampo(accreditamento, verbale);
				redirectAttrs.addFlashAttribute("message",new Message("message.completato", "message.sottoscrivente_inserito", "success"));
				return "redirect:/accreditamento/" + accreditamentoId + "/validate?tab=tab6";
			}
		}catch (Exception ex){
			LOGGER.error(Utils.getLogMessage("POST /accreditamento/" + accreditamentoId + "/verbaleValutazioneSulCampo/" + verbaleValutazioneSulCampoId + "/sottoscrivente/save"),ex);
			redirectAttrs.addFlashAttribute("message", new Message("message.errore", "message.errore_eccezione", "error"));
			LOGGER.info(Utils.getLogMessage("REDIRECT: /accreditamento/" + accreditamentoId + "/verbaleValutazioneSulCampo/" + verbaleValutazioneSulCampoId + "/insertSottoscrivente"));
			return "redirect:/accreditamento/" + accreditamentoId + "/verbaleValutazioneSulCampo/" + verbaleValutazioneSulCampoId + "/insertSottoscrivente";
		}
	}

	@PreAuthorize("@securityAccessServiceImpl.canValidateAccreditamento(principal,#accreditamentoId)")
	@RequestMapping(value = "/accreditamento/{accreditamentoId}/verbaleValutazioneSulCampo/{verbaleValutazioneSulCampoId}/save", method = RequestMethod.POST)
	public String saveValutazioniSulCampo(@ModelAttribute("verbaleWrapper") VerbaleValutazioneSulCampoWrapper wrapper, BindingResult result,
			@PathVariable Long accreditamentoId, @PathVariable Long verbaleValutazioneSulCampoId, Model model, RedirectAttributes redirectAttrs) {
		LOGGER.info(Utils.getLogMessage("POST /accreditamento/" + accreditamentoId + "/verbaleValutazioneSulCampo/" + verbaleValutazioneSulCampoId + "/"));
		try {
			//validator
			Map<String, String> mappaErroriValutazione = valutazioneValidator.validateValutazioneSulCampo(wrapper.getMappa());
			if(!mappaErroriValutazione.isEmpty()){
				LOGGER.debug(Utils.getLogMessage("Validazione fallita"));
				model.addAttribute("canValutaDomanda", accreditamentoService.canUserValutaDomanda(accreditamentoId, Utils.getAuthenticatedUser()));
				model.addAttribute("message",new Message("message.errore", "message.inserire_campi_required", "error"));
				wrapper.setMappaErroriValutazione(mappaErroriValutazione);
				model.addAttribute("verbaleWrapper", wrapper);
				LOGGER.info(Utils.getLogMessage("VIEW: accreditamento/accreditamentoValutazioniSulCampo"));
				return "accreditamento/accreditamentoValutazioniSulCampo";
			}else{
				//salvataggio delle valutazioni
				Accreditamento accreditamento = new Accreditamento();
				accreditamento.setId(accreditamentoId);
				wrapper.getMappa().forEach((k, v) -> {
					v.setIdField(k);
					v.setAccreditamento(accreditamento);
				});
				//salvo sia in verbale che in valutazione
				Valutazione valutazione = valutazioneService.getValutazioneByAccreditamentoIdAndAccountIdAndNotStoricizzato(accreditamento.getId(), Utils.getAuthenticatedUser().getAccount().getId());
				Set<FieldValutazioneAccreditamento> values = new HashSet<FieldValutazioneAccreditamento>(fieldValutazioneAccreditamentoService.saveMapList(wrapper.getMappa()));
				wrapper.getAccreditamento().getVerbaleValutazioneSulCampo().getDatiValutazioneSulCampo().getValutazioniSulCampo().addAll(values);
				valutazione.getValutazioni().addAll(values);
				accreditamentoService.save(wrapper.getAccreditamento());

				redirectAttrs.addAttribute("accreditamentoId", accreditamentoId);
				redirectAttrs.addFlashAttribute("message", new Message("message.completato", "message.valutazione_salvata", "success"));
				redirectAttrs.addFlashAttribute("currentTab","tab6");
				LOGGER.info(Utils.getLogMessage("REDIRECT: /accreditamento/" + accreditamentoId + "/validate"));
				return "redirect:/accreditamento/{accreditamentoId}/validate";
			}
		}catch (Exception ex){
			LOGGER.error(Utils.getLogMessage("POST /accreditamento/" + accreditamentoId + "/verbaleValutazioneSulCampo/" + verbaleValutazioneSulCampoId + "/save"),ex);
			redirectAttrs.addAttribute("accreditamentoId",accreditamentoId);
			redirectAttrs.addFlashAttribute("message", new Message("message.errore", "message.errore_eccezione", "error"));
			LOGGER.info(Utils.getLogMessage("REDIRECT: /accreditamento/" + accreditamentoId + "/validate"));
			return "redirect:/accreditamento/{accreditamentoId}/validate";
		}
	}

	// riassegna la valutazioni ad un nuovo gruppo crecm, avvisa i referee precedentemente assegnati e cancella le loro valutazioni
	@PreAuthorize("@securityAccessServiceImpl.canReassignCRECM(principal,#accreditamentoId)")
	@RequestMapping(value = "/accreditamento/{accreditamentoId}/reassignEvaluation", method = RequestMethod.POST)
	public String riassegnaValutazioneAccreditamento(@ModelAttribute("accreditamentoWrapper") AccreditamentoWrapper wrapper, BindingResult result,
			@PathVariable Long accreditamentoId, Model model, RedirectAttributes redirectAttrs){
		LOGGER.info(Utils.getLogMessage("GET /accreditamento/" + accreditamentoId + "/reassignEvaluation"));
		try {
			//validazione dei nuovi referee
			valutazioneValidator.validateGruppoCrecm(wrapper.getRefereeGroup(), wrapper.getRefereeDaRiassegnare(), result);

			if(result.hasErrors()){
				model.addAttribute("message",new Message("message.errore", "message.inserire_campi_required", "error"));
				model.addAttribute("reassignErrors", true);
				Accreditamento accreditamento = accreditamentoService.getAccreditamento(accreditamentoId);
				return goToAccreditamentoShow(model, accreditamento, wrapper);
			}else {
				accreditamentoService.riassegnaGruppoCrecm(accreditamentoId, wrapper.getRefereeGroup());

				LOGGER.info(Utils.getLogMessage("REDIRECT: /accreditamento/" + accreditamentoId + "/show"));
				redirectAttrs.addAttribute("accreditamentoId",accreditamentoId);
				redirectAttrs.addFlashAttribute("message", new Message("message.completato", "message.gruppoCrecm_riassegnato", "success"));

				return "redirect:/accreditamento/{accreditamentoId}/show";
			}

		}catch (Exception ex){
			LOGGER.error(Utils.getLogMessage("GET /accreditamento/" + accreditamentoId + "/reassignEvaluation"),ex);
			redirectAttrs.addAttribute("accreditamentoId",accreditamentoId);
			redirectAttrs.addFlashAttribute("message", new Message("message.errore", "message.errore_eccezione", "error"));
			LOGGER.info(Utils.getLogMessage("REDIRECT: /accreditamento/" + accreditamentoId + "/show"));
			return "redirect:/accreditamento/{accreditamentoId}/show";
		}
	}

	/***	INVIA DOMANDA RICHIESTA_INTEGRAZIONE	***/
	@PreAuthorize("@securityAccessServiceImpl.canEnableField(principal,#accreditamentoId)")
	@RequestMapping(value = "/accreditamento/{accreditamentoId}/sendRichiestaIntegrazione", method = RequestMethod.POST)
	public String sendRichiestaIntegrazione(@ModelAttribute("giorniIntegrazione") Integer giorniIntegrazione,
			@PathVariable Long accreditamentoId, RedirectAttributes redirectAttrs){
		LOGGER.info(Utils.getLogMessage("GET /accreditamento/" + accreditamentoId + "/sendRichiestaIntegrazione"));
		try{
			//TODO controllare meglio questo punto (modale in accreditamento/enableField)
			if(giorniIntegrazione != null) {
				LOGGER.info(Utils.getLogMessage("Settato timer Bonita Integrazione a: " + giorniIntegrazione + " giorni"));
				AccreditamentoStatoEnum statoAccreditamento = accreditamentoService.getStatoAccreditamento(accreditamentoId);
				if(statoAccreditamento == AccreditamentoStatoEnum.RICHIESTA_INTEGRAZIONE)
					accreditamentoService.inviaRichiestaIntegrazione(accreditamentoId, (long) giorniIntegrazione);
				else if(statoAccreditamento == AccreditamentoStatoEnum.RICHIESTA_PREAVVISO_RIGETTO)
					accreditamentoService.inviaRichiestaPreavvisoRigetto(accreditamentoId, (long) giorniIntegrazione);
				return "redirect:/accreditamento/{accreditamentoId}/show";
			}
			else throw new Exception("Error! giorniIntegrazione is null");
		}catch (Exception ex){
			LOGGER.error(Utils.getLogMessage("GET /accreditamento/" + accreditamentoId + "/sendRichiestaIntegrazione"),ex);
			redirectAttrs.addFlashAttribute("message", new Message("message.errore", "message.errore_eccezione", "error"));
			LOGGER.info(Utils.getLogMessage("REDIRECT: /accreditamento/" + accreditamentoId + "/enableField"));
			return "redirect:/accreditamento/{accreditamentoId}/enableField";
		}
	}

	/***	INVIA DOMANDA INTEGRAZIONE	***/
	@PreAuthorize("@securityAccessServiceImpl.canSendIntegrazione(principal,#accreditamentoId)")
	@RequestMapping("/accreditamento/{accreditamentoId}/provider/{providerId}/sendIntegrazione")
	public String sendIntegrazione(@RequestParam(name="noteOsservazioniIntegrazione.id", required = false) Long idFileIntegrazione,
			@RequestParam(name="noteOsservazioniPreavvisoRigetto.id", required = false) Long idFilePreavvisoRigetto,
			@PathVariable Long accreditamentoId, @PathVariable Long providerId, RedirectAttributes redirectAttrs){
		LOGGER.info(Utils.getLogMessage("GET /accreditamento/" + accreditamentoId + "/provider/" + providerId + "/sendIntegrazione"));
		try{
			if(idFileIntegrazione != null) {
				LOGGER.info(Utils.getLogMessage("Arrivato file note integrazione id: " + idFileIntegrazione));
				accreditamentoService.saveFileNoteOsservazioni(idFileIntegrazione, accreditamentoId);
			}
			if(idFilePreavvisoRigetto != null) {
				LOGGER.info(Utils.getLogMessage("Arrivato file note preavviso rigetto id: " + idFilePreavvisoRigetto));
				accreditamentoService.saveFileNoteOsservazioni(idFilePreavvisoRigetto, accreditamentoId);
			}
			accreditamentoService.inviaIntegrazione(accreditamentoId);
			redirectAttrs.addFlashAttribute("message", new Message("message.completato", "message.invio_integrazione_success", "success"));
			return "redirect:/accreditamento/{accreditamentoId}/show";
		}catch (Exception ex){
			LOGGER.error(Utils.getLogMessage("GET /accreditamento/" + accreditamentoId + "/provider/" + providerId + "/sendIntegrazione"),ex);
			redirectAttrs.addFlashAttribute("message", new Message("message.errore", "message.errore_eccezione", "error"));
			LOGGER.info(Utils.getLogMessage("REDIRECT: /accreditamento/" + accreditamentoId + "/edit"));
			return "redirect:/accreditamento/{accreditamentoId}/edit";
		}
	}

//	@PreAuthorize("@securityAccessServiceImpl.canShowGruppo(principal,#gruppo)")
	@RequestMapping("/accreditamento/{gruppo}/list")
	public String getAllAccreditamentiForGruppo(@PathVariable("gruppo") String gruppo, Model model,
			@RequestParam(name="tipo", required = false) String tipo,
			@RequestParam(name="filterTaken", required = false) Boolean filterTaken,
			@RequestParam(name="filterDone", required = false) Boolean filterDone,
			RedirectAttributes redirectAttrs) throws Exception {
		LOGGER.info(Utils.getLogMessage("GET /accreditamento/" + gruppo + "/list, tipo = " + tipo + ", filterTaken = " + filterTaken + ", filterDone = " + filterDone));
		try {
			Set<Accreditamento> listaAccreditamenti = new HashSet<Accreditamento>();
			Set<AccreditamentoStatoEnum> stati = AccreditamentoStatoEnum.getAllStatoByGruppo(gruppo);
			CurrentUser currentUser = Utils.getAuthenticatedUser();
			if(currentUser.isSegreteria()) {
				for (AccreditamentoStatoEnum stato : stati) {
					listaAccreditamenti.addAll(accreditamentoService.getAllAccreditamentiByStatoAndTipoDomanda(stato, AccreditamentoTipoEnum.getTipoByNome(tipo), filterTaken));
				}
			}
			if(currentUser.isReferee()) {
				for (AccreditamentoStatoEnum stato : stati) {
					listaAccreditamenti.addAll(accreditamentoService.getAllAccreditamentiByStatoAndTipoDomandaForValutatoreId(stato, AccreditamentoTipoEnum.getTipoByNome(tipo), currentUser.getAccount().getId(), filterDone));
				}
			}
			if(currentUser.isProvider()) {
				for (AccreditamentoStatoEnum stato : stati) {
					listaAccreditamenti.addAll(accreditamentoService.getAllAccreditamentiByStatoAndTipoDomandaForProviderId(stato, AccreditamentoTipoEnum.getTipoByNome(tipo), providerService.getProviderIdByAccountId(currentUser.getAccount().getId())));
				}
			}
			//mappo la giusta label da visualizzare
			String stringTipo;
			if (tipo != null)
				stringTipo = tipo;
			else
				stringTipo = "";
			String label = null;
			if (gruppo.equals("valutazioneAssegnamento") && (filterTaken != null && filterTaken == true))
				label = "label.listaDomandeDaPrendereInCarica" + stringTipo;
			if ((gruppo.equals("valutazioneAssegnamento") && (filterTaken == null || filterTaken == false)) || gruppo.equals("valutazioneReferee"))
				label = "label.listaDomandeDaValutare" + stringTipo;
			if (gruppo.equals("valutazione"))
				label = "label.listaDomandeDaValutareIntegrazione" + stringTipo;
			if (gruppo.equals("richiestaIntegrazione"))
				label = "label.listaDomandeRichiestaIntegrazione" + stringTipo;
			if (gruppo.equals("preavvisoRigetto"))
				label= "label.listaDomandePreavvisoRigetto" + stringTipo;
			if (gruppo.equals("assegnamento"))
				label = "label.listaDomandeDaRiassegnare" + stringTipo;

			//prende la mappa<id domanda, set account di chi ha una valutazione per la domanda> per ogni elemento della lista di accreditamenti
			if (currentUser.isSegreteria() || currentUser.isReferee()) {
				Map<Long, Set<Account>> mappaCarica = new HashMap<Long, Set<Account>>();
				mappaCarica = valutazioneService.getValutatoriForAccreditamentiList(listaAccreditamenti);
				model.addAttribute("mappaCarica", mappaCarica);
			}
			if (currentUser.isReferee()) {
				Map<Long,LocalDateTime> mappaScadenze = new HashMap<Long, LocalDateTime>();
				mappaScadenze = valutazioneService.getScadenzaValutazioneByValutatoreId(currentUser.getAccount().getId());
				model.addAttribute("mappaScadenze", mappaScadenze);
				//scadenzaValutazione
			}
			model.addAttribute("label", label);
			model.addAttribute("accreditamentoList", listaAccreditamenti);
			model.addAttribute("canProviderCreateAccreditamentoProvvisorio", false);
			model.addAttribute("canProviderCreateAccreditamentoStandard", false);
			LOGGER.info(Utils.getLogMessage("VIEW: accreditamento/accreditamentoList"));
			return "accreditamento/accreditamentoList";
		}catch (Exception ex){
			LOGGER.error(Utils.getLogMessage("GET /accreditamento/" + gruppo + "/list"),ex);
			redirectAttrs.addFlashAttribute("message", new Message("message.errore", "message.errore_eccezione", "error"));
			LOGGER.info(Utils.getLogMessage("REDIRECT: /home"));
			return "redirect:/home";
		}
	}

	//solo segreteria
	@PreAuthorize("@securityAccessServiceImpl.canShowInScadenza(principal)")
	@RequestMapping("/accreditamento/scadenza/list")
	public String getAllAccreditamentiInScadenza(Model model, RedirectAttributes redirectAttrs) throws Exception{
		LOGGER.info(Utils.getLogMessage("GET /accreditamento/scadenza/list"));
		try {
			Set<Accreditamento> listaAccreditamenti = accreditamentoService.getAllAccreditamentiInScadenza();
			model.addAttribute("label", "label.listaDomandeInScadenza");
			model.addAttribute("accreditamentoList", listaAccreditamenti);
			model.addAttribute("canProviderCreateAccreditamentoProvvisorio", false);
			model.addAttribute("canProviderCreateAccreditamentoStandard", false);
			//prende la mappa<id domanda, set account di chi ha una valutazione per la domanda> per ogni elemento della lista di accreditamenti
			Map<Long, Set<Account>> mappaCarica = new HashMap<Long, Set<Account>>();
			mappaCarica = valutazioneService.getValutatoriForAccreditamentiList(listaAccreditamenti);
			model.addAttribute("mappaCarica", mappaCarica);

			LOGGER.info(Utils.getLogMessage("VIEW: accreditamento/accreditamentoList"));
			return "accreditamento/accreditamentoList";
		}catch (Exception ex){
			LOGGER.error(Utils.getLogMessage("GET /accreditamento/scadenza/list"),ex);
			redirectAttrs.addFlashAttribute("message", new Message("message.errore", "message.errore_eccezione", "error"));
			LOGGER.info(Utils.getLogMessage("REDIRECT: /home"));
			return "redirect:/home";
		}
	}

	//solo segreteria
//TODO	@PreAuthorize("@securityAccessServiceImpl.canShowDaInserireInODG(principal)")
	@RequestMapping("/accreditamento/daInserireODG/list")
	public String getAllAccreditamentiAncoraDaInserireInODG(Model model, RedirectAttributes redirectAttrs) throws Exception{
		LOGGER.info(Utils.getLogMessage("GET /accreditamento/daInserireODG/list"));
		try {
			Set<Accreditamento> listaAccreditamenti = accreditamentoService.getAllAccreditamentiInseribiliInODG();
			model.addAttribute("label", "label.listaDomandeDaInserireInODG");
			model.addAttribute("accreditamentoList", listaAccreditamenti);
			model.addAttribute("canProviderCreateAccreditamentoProvvisorio", false);
			model.addAttribute("canProviderCreateAccreditamentoStandard", false);
			//prende la mappa<id domanda, set account di chi ha una valutazione per la domanda> per ogni elemento della lista di accreditamenti
			Map<Long, Set<Account>> mappaCarica = new HashMap<Long, Set<Account>>();
			mappaCarica = valutazioneService.getValutatoriForAccreditamentiList(listaAccreditamenti);
			model.addAttribute("mappaCarica", mappaCarica);

			LOGGER.info(Utils.getLogMessage("VIEW: accreditamento/accreditamentoList"));
			return "accreditamento/accreditamentoList";
		}catch (Exception ex){
			LOGGER.error(Utils.getLogMessage("GET /accreditamento/daInserireODG/list"),ex);
			redirectAttrs.addFlashAttribute("message", new Message("message.errore", "message.errore_eccezione", "error"));
			LOGGER.info(Utils.getLogMessage("REDIRECT: /home"));
			return "redirect:/home";
		}
	}

//	@PreAuthorize("@securityAccessServiceImpl.canShowNonValutate(principal,#refereeId)
	//TODO solo segreteria e referee interessato
	@RequestMapping("/referee/{refereeId}/accreditamento/nonValutate/list")
	public String getAllDomandeNonValutate(@PathVariable Long refereeId, Model model, RedirectAttributes redirectAttrs) {
		LOGGER.info(Utils.getLogMessage("/referee/" + refereeId + "/accreditamento/nonValutate/list"));
		try {
			Set<Accreditamento> listaDomandeNonValutate = accreditamentoService.getAllDomandeNonValutateByRefereeId(refereeId);
			model.addAttribute("label", "label.listaDomandeNonValutate");
			model.addAttribute("accreditamentoList", listaDomandeNonValutate);
			model.addAttribute("canProviderCreateAccreditamentoProvvisorio", false);
			model.addAttribute("canProviderCreateAccreditamentoStandard", false);
			LOGGER.info(Utils.getLogMessage("VIEW: accreditamento/accreditamentoList"));
			return "accreditamento/accreditamentoList";
		}catch (Exception ex){
			LOGGER.error(Utils.getLogMessage("GET /referee/" + refereeId + "/accreditamento/nonValutate/list"),ex);
			redirectAttrs.addFlashAttribute("message", new Message("message.errore", "message.errore_eccezione", "error"));
			LOGGER.info(Utils.getLogMessage("REDIRECT: /home"));
			return "redirect:/home";
		}
	}

	@PreAuthorize("@securityAccessServiceImpl.canUserPresaVisione(principal,#accreditamentoId)")
	@RequestMapping("/accreditamento/{accreditamentoId}/presaVisione")
	public String presaVisione(@PathVariable Long accreditamentoId, Model model, RedirectAttributes redirectAttrs) throws Exception{
		LOGGER.info(Utils.getLogMessage("GET /accreditamento/" + accreditamentoId + "/presaVisione"));
		try{
			accreditamentoService.presaVisione(accreditamentoId);
			redirectAttrs.addFlashAttribute("message", new Message("message.completato", "message.presa_visione_success", "success"));
			return "redirect:/accreditamento/{accreditamentoId}/show";
		}catch (Exception ex){
			LOGGER.error(Utils.getLogMessage("GET /accreditamento/" + accreditamentoId + "/presaVisione"),ex);
			redirectAttrs.addFlashAttribute("message", new Message("message.errore", "message.errore_eccezione", "error"));
			LOGGER.info(Utils.getLogMessage("REDIRECT: /accreditamento/" + accreditamentoId + "/show"));
			return "redirect:/accreditamento/{accreditamentoId}/show";
		}
	}

	//TODO @PreAuthorize("@securityAccessServiceImpl.canSendIntegrazione(principal,#accreditamentoId)")
		@RequestMapping("/accreditamento/{accreditamentoId}/rivaluta")
		public String rivaluta(@PathVariable Long accreditamentoId, Model model, RedirectAttributes redirectAttrs) throws Exception{
			LOGGER.info(Utils.getLogMessage("GET /accreditamento/" + accreditamentoId + "/presaVisione"));
			try{
				accreditamentoService.rivaluta(accreditamentoId);
				return "redirect:/accreditamento/{accreditamentoId}/validate";
			}catch (Exception ex){
				LOGGER.error(Utils.getLogMessage("GET /accreditamento/" + accreditamentoId + "/presaVisione"),ex);
				redirectAttrs.addFlashAttribute("message", new Message("message.errore", "message.errore_eccezione", "error"));
				LOGGER.info(Utils.getLogMessage("REDIRECT: /accreditamento/" + accreditamentoId + "/show"));
				return "redirect:/accreditamento/{accreditamentoId}/show";
			}
		}

		@RequestMapping("/accreditamento/{accreditamentoId}/runtimeTest")
		public String runtimeTest(@PathVariable Long accreditamentoId, Model model, RedirectAttributes redirectAttrs) throws Exception{
			LOGGER.info(Utils.getLogMessage("GET /accreditamento/" + accreditamentoId + "/runtimeTest"));
			try{
				accreditamentoService.approvaIntegrazione(accreditamentoId);
				return "redirect:/accreditamento/{accreditamentoId}/show";
			}catch (Exception ex){
				LOGGER.error(Utils.getLogMessage("GET /accreditamento/" + accreditamentoId + "/presaVisione"),ex);
				redirectAttrs.addFlashAttribute("message", new Message("message.errore", "message.errore_eccezione", "error"));
				LOGGER.info(Utils.getLogMessage("REDIRECT: /accreditamento/" + accreditamentoId + "/show"));
				return "redirect:/accreditamento/{accreditamentoId}/show";
			}
		}

//TODO		@PreAuthorize("@securityAccessServiceImpl.canShowStorico(principal,#accreditamentoId)")
		@RequestMapping("/accreditamento/{accreditamentoId}/getStorico")
		@ResponseBody
		public Set<Valutazione>getValutazioniStorico(@PathVariable Long accreditamentoId){
			return valutazioneService.getAllValutazioniStoricizzateForAccreditamentoId(accreditamentoId);
		}

		@PreAuthorize("@securityAccessServiceImpl.canEditVerbaleAccreditamento(principal,#accreditamentoId)")
		@RequestMapping("/accreditamento/{accreditamentoId}/inviaConvocazioneSulCampo")
		public String inviaMailConvocazioneValutazioneSulCampo(@PathVariable Long accreditamentoId, Model model, RedirectAttributes redirectAttrs) throws Exception{
			LOGGER.info(Utils.getLogMessage("GET /accreditamento/" + accreditamentoId + "/inviaConvocazioneSulCampo"));
			try{
				accreditamentoService.inviaEmailConvocazioneValutazioneSulCampo(accreditamentoId);
				redirectAttrs.addFlashAttribute("message", new Message("message.completato", "message.email_convocazione_valutazione_sul_campo_inviata", "success"));
				return "redirect:/accreditamento/{accreditamentoId}/show?tab=tab7";
			}catch (Exception ex){
				LOGGER.error(Utils.getLogMessage("GET /accreditamento/" + accreditamentoId + "/show"),ex);
				redirectAttrs.addFlashAttribute("message", new Message("message.errore", "message.errore_eccezione", "error"));
				LOGGER.info(Utils.getLogMessage("REDIRECT: /accreditamento/" + accreditamentoId + "/show"));
				return "redirect:/accreditamento/{accreditamentoId}/show";
			}
		}
}
