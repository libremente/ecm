package it.tredi.ecm.service;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import it.tredi.ecm.dao.entity.Account;
import it.tredi.ecm.dao.entity.Accreditamento;
import it.tredi.ecm.dao.entity.FieldIntegrazioneAccreditamento;
import it.tredi.ecm.dao.entity.FieldValutazioneAccreditamento;
import it.tredi.ecm.dao.entity.Persona;
import it.tredi.ecm.dao.entity.Provider;
import it.tredi.ecm.dao.entity.Sede;
import it.tredi.ecm.dao.entity.Valutazione;
import it.tredi.ecm.dao.entity.VerbaleValutazioneSulCampo;
import it.tredi.ecm.dao.enumlist.AccreditamentoStatoEnum;
import it.tredi.ecm.dao.enumlist.IdFieldEnum;
import it.tredi.ecm.dao.enumlist.ProfileEnum;
import it.tredi.ecm.dao.enumlist.SubSetFieldEnum;
import it.tredi.ecm.dao.enumlist.ValutazioneTipoEnum;
import it.tredi.ecm.dao.repository.ProfileRepository;
import it.tredi.ecm.dao.repository.ValutazioneRepository;
import it.tredi.ecm.service.bean.EcmProperties;
import it.tredi.ecm.utils.Utils;
import it.tredi.ecm.web.bean.FieldValutazioniRipetibiliWrapper;

@Service
public class ValutazioneServiceImpl implements ValutazioneService {
	private static Logger LOGGER = LoggerFactory.getLogger(ValutazioneServiceImpl.class);

	@Autowired private ValutazioneRepository valutazioneRepository;
	@Autowired private FieldValutazioneAccreditamentoService fieldValutazioneAccreditamentoService;
	@Autowired private ProfileRepository profileRepository;
	@Autowired private AccountService accountService;
	@Autowired private AccreditamentoService accreditamentoService;
	@Autowired private EcmProperties ecmProperties;
	@Autowired private EmailService emailService;
	@Autowired private AlertEmailService alertEmailService;
	@PersistenceContext EntityManager entityManager;
	@Autowired private MessageSource messageSource;
	@Autowired private FieldIntegrazioneAccreditamentoService fieldIntegrazioneAccreditamentoService;
	@Autowired private PersonaService personaService;
	@Autowired private SedeService sedeService;
	@Autowired private WorkflowService workflowService;

	@Override
	public Valutazione getValutazione(Long valutazioneId) {
		LOGGER.debug(Utils.getLogMessage("Recupero Valutazione " + valutazioneId));
		Valutazione valutazione = valutazioneRepository.findOne(valutazioneId);
		return valutazione;
	}

	@Override
	@Transactional
	public void save(Valutazione valutazione) {
		LOGGER.debug(Utils.getLogMessage("Salvataggio Valutazione"));
		valutazioneRepository.save(valutazione);
	}

	@Override
	public void saveAndFlush(Valutazione valutazione) {
		LOGGER.debug(Utils.getLogMessage("Salvataggio Valutazione + flush"));
		valutazioneRepository.saveAndFlush(valutazione);
	}

	@Override
	public void delete(Valutazione valutazione) {
		LOGGER.debug(Utils.getLogMessage("Eliminazione Valutazione id: " + valutazione.getId()));
		valutazioneRepository.delete(valutazione);
	}

	@Override
	public Valutazione getValutazioneByAccreditamentoIdAndAccountIdAndNotStoricizzato(Long accreditamentoId, Long accountId) {
		LOGGER.debug(Utils.getLogMessage("Recupero Valutazione per l'accreditamento " + accreditamentoId + " eseguita dall'utente " + accountId));
		Valutazione valutazione = valutazioneRepository.findOneByAccreditamentoIdAndAccountIdAndStoricizzatoFalse(accreditamentoId, accountId);
		return valutazione;
	}


	@Override
	public Set<Valutazione> getAllValutazioniForAccreditamentoIdAndNotStoricizzato(Long accreditamentoId) {
		LOGGER.debug(Utils.getLogMessage("Recupero tutte le Valutazione per l'accreditamento " + accreditamentoId));
		Set<Valutazione> allValutazioni = valutazioneRepository.findAllByAccreditamentoIdAndStoricizzatoFalseOrderByAccount(accreditamentoId);
		return allValutazioni;
	}

	@Override
	public Set<Valutazione> getAllValutazioniCompleteForAccreditamentoIdAndNotStoricizzato(Long accreditamentoId) {
		LOGGER.debug(Utils.getLogMessage("Recupero tutte le Valutazioni complete per l'accreditamento " + accreditamentoId));
		Set<Valutazione> allCompleteValutazioni = valutazioneRepository.findAllByAccreditamentoIdAndStoricizzatoFalseAndDataValutazioneNotNullOrderByAccount(accreditamentoId);
		return allCompleteValutazioni;
	}

	@Override
	public Set<Account> getAllValutatoriForAccreditamentoId(Long accreditamentoId) {
		LOGGER.debug(Utils.getLogMessage("Ottengo la lista dei valutatori dell'accreditamento " + accreditamentoId));
		Set<Valutazione> valutazioni = valutazioneRepository.findAllByAccreditamentoIdAndStoricizzatoFalseOrderByAccount(accreditamentoId);
		Set<Account> valutatori = new HashSet<Account>();
		for (Valutazione v : valutazioni) {
			valutatori.add(v.getAccount());
		}
		return valutatori;
	}

	@Override
	public Map<Account, Map<IdFieldEnum, FieldValutazioneAccreditamento>> getMapValutatoreValutazioniByAccreditamentoIdAndSubSet(Long accreditamentoId, SubSetFieldEnum subset) {
		LOGGER.debug(Utils.getLogMessage("Genero la mappa valutatori - valutazione dell'accreditamento " + accreditamentoId + " per il subset " + subset));
		Map<Account, Map<IdFieldEnum, FieldValutazioneAccreditamento>> mappaValutatoreValutazioni = new HashMap<Account, Map<IdFieldEnum, FieldValutazioneAccreditamento>>();
		Set<Valutazione> allValutazioni = valutazioneRepository.findAllByAccreditamentoIdAndStoricizzatoFalseOrderByAccount(accreditamentoId);
		//per ogni valutazione dell'accreditamento
		for (Valutazione v : allValutazioni) {
			//mi faccio restituire la valutazione relativa al subset e al determinato valutatore
			Map<IdFieldEnum, FieldValutazioneAccreditamento> mapValutazioni = fieldValutazioneAccreditamentoService.filterFieldValutazioneBySubSetAsMap(v.getValutazioni(), subset);
			//inserisco il tutto nella mappa valutatoreValutazioni
			mappaValutatoreValutazioni.put(v.getAccount(), mapValutazioni);
		}
		return mappaValutatoreValutazioni;
	}

	@Override
	public Map<Account, Map<IdFieldEnum, FieldValutazioneAccreditamento>> getMapValutatoreValutazioniByAccreditamentoIdAndObjectId(Long accreditamentoId, Long id) {
		LOGGER.debug(Utils.getLogMessage("Genero la mappa valutatori - valutazione dell'accreditamento " + accreditamentoId + " per l'oggetto " + id));
		Map<Account, Map<IdFieldEnum, FieldValutazioneAccreditamento>> mappaValutatoreValutazioni = new HashMap<Account, Map<IdFieldEnum, FieldValutazioneAccreditamento>>();
		Set<Valutazione> allValutazioni = valutazioneRepository.findAllByAccreditamentoIdAndStoricizzatoFalseOrderByAccount(accreditamentoId);
		//per ogni valutazione dell'accreditamento
		for (Valutazione v : allValutazioni) {
			//mi faccio restituire la valutazione relativa al id dell'oggetto e al determinato valutatore
			Map<IdFieldEnum, FieldValutazioneAccreditamento> mapValutazioni = fieldValutazioneAccreditamentoService.filterFieldValutazioneByObjectAsMap(v.getValutazioni(), id);
			//inserisco il tutto nella mappa valutatoreValutazioni
			mappaValutatoreValutazioni.put(v.getAccount(), mapValutazioni);
		}
		return mappaValutatoreValutazioni;
	}

	@Override
	public int countRefereeNotValutatoriForAccreditamentoId(Long accreditamentoId) {
		LOGGER.debug(Utils.getLogMessage("Conto i referee che non hanno valutato l'accreditamento " + accreditamentoId));
		return ecmProperties.getNumeroReferee() - valutazioneRepository.countRefereeValutatoriWithDataValutazioneForAccreditamentoId(accreditamentoId, profileRepository.findOneByProfileEnum(ProfileEnum.REFEREE).get());
	}

	@Override
	public Set<Account> getAccountValutatoriWithDataForAccreditamentoId(Long accreditamentoId) {
		LOGGER.debug(Utils.getLogMessage("Restituisco i referee che hanno valutato l'accreditamento " + accreditamentoId));
		Set<Account> valutatori = new HashSet<Account>();
		valutatori = valutazioneRepository.getAccountValutatoriWithDataValutazioneForAccreditamentoId(accreditamentoId);
		return valutatori;
	}

	@Override
	public Map<Long, Account> getValutatoreSegreteriaForAccreditamentiList(Set<Accreditamento> accreditamentoSet) {
		LOGGER.debug(Utils.getLogMessage("Carico la mappa di chi ha preso in carica gli accreditamenti"));
		Map<Long, Account> mappaAccreditamentoAccountValutatore = new HashMap<Long, Account>();
		for (Accreditamento a : accreditamentoSet) {
			Account account = valutazioneRepository.getAccountSegreteriaValutatoreForAccreditamentoId(a.getId(), profileRepository.findOneByProfileEnum(ProfileEnum.SEGRETERIA).get());
			if (account != null)
				mappaAccreditamentoAccountValutatore.put(a.getId(), account);
		}
		return mappaAccreditamentoAccountValutatore;
	}

	@Override
	public Map<Long, Set<Account>> getValutatoriForAccreditamentiList(Set<Accreditamento> accreditamentoSet) {
		LOGGER.debug(Utils.getLogMessage("Carico la mappa dei valutatori degli accreditamenti"));
		Map<Long, Set<Account>> mappaAccreditamentoIdAccountValutatori = new HashMap<Long, Set<Account>>();
		for (Accreditamento a : accreditamentoSet) {
			Set<Account> accounts = valutazioneRepository.getAllAccountValutatoriForAccreditamentoIdOrderByAccount(a.getId());
			mappaAccreditamentoIdAccountValutatori.put(a.getId(), accounts);
		}
		return mappaAccreditamentoIdAccountValutatori;
	}

	@Override
	public void updateValutazioniNonDate(Long accreditamentoId) throws Exception {
		LOGGER.debug(Utils.getLogMessage("Aggiornamento valutazioni non date per accreditamento: " + accreditamentoId));
		Set<Valutazione> valutazioni = getAllValutazioniForAccreditamentoIdAndNotStoricizzato(accreditamentoId);
		for(Valutazione v : valutazioni){
			if(v.getTipoValutazione() == ValutazioneTipoEnum.REFEREE && v.getDataValutazione() == null){
				Account referee = v.getAccount();
				//aggiorna il contatore
				referee.setValutazioniNonDate(referee.getValutazioniNonDate() + 1);
				//aggiunge la domanda alla liste di quelle non valutate dal referee
				Set<Accreditamento> domandeNonValutate = referee.getDomandeNonValutate();
				domandeNonValutate.add(accreditamentoService.getAccreditamento(accreditamentoId));
				referee.setDomandeNonValutate(domandeNonValutate);

				accountService.save(referee);

				//infine elimino la valutazione
				valutazioneRepository.delete(v);
			}
		}

		emailService.inviaNotificaASegreteriaMancataValutazioneReferee(ecmProperties.getEmailSegreteriaEcm(), accreditamentoService.getAccreditamento(accreditamentoId).getProvider().getDenominazioneLegale());
	}

	@Override
	public void dataOraScadenzaPossibilitaValutazioneCRECM(Long accreditamentoId, LocalDateTime date) throws Exception {
		LOGGER.debug(Utils.getLogMessage("Aggiornamento dataora massima (" + date + ") entro la quale effettuare la valutazione CRECM per accreditamento: " + accreditamentoId));
		Set<Valutazione> valutazioni = getAllValutazioniForAccreditamentoIdAndNotStoricizzato(accreditamentoId);

		Set<Account> refereeGroup = new HashSet<Account>();

		for(Valutazione v : valutazioni){
			if(v.getTipoValutazione() == ValutazioneTipoEnum.REFEREE){
				refereeGroup.add(v.getAccount());
				v.setDataOraScadenzaPossibilitaValutazione(date);
				valutazioneRepository.save(v);
			}
		}

		Provider provider = accreditamentoService.getAccreditamento(accreditamentoId).getProvider();

		alertEmailService.creaAlertForReferee(refereeGroup, provider, date);
	}

	@Override
	public void dataOraScadenzaPossibilitaValutazione(Long accreditamentoId, LocalDateTime date) throws Exception {
		LOGGER.debug(Utils.getLogMessage("Aggiornamento dataora massima (" + date + ") entro la quale effettuare la valutazione Team Leader per accreditamento: " + accreditamentoId));
		Set<Valutazione> valutazioni = getAllValutazioniForAccreditamentoIdAndNotStoricizzato(accreditamentoId);

		Set<Account> refereeGroup = new HashSet<Account>();

		for(Valutazione v : valutazioni){
			if(v.getTipoValutazione() == ValutazioneTipoEnum.TEAM_LEADER){
				refereeGroup.add(v.getAccount());
				v.setDataOraScadenzaPossibilitaValutazione(date);
				valutazioneRepository.save(v);
			}
		}

		Provider provider = accreditamentoService.getAccreditamento(accreditamentoId).getProvider();

		alertEmailService.creaAlertForReferee(refereeGroup, provider, date);
	}

	@Override
	public Set<Valutazione> getAllValutazioniForAccountNotStoricizzate(Long accountId) {
		LOGGER.debug(Utils.getLogMessage("Recupero tutte le valutazioni per il referee: " + accountId));
		return valutazioneRepository.findAllByAccountIdAndStoricizzatoFalse(accountId);
	}

	@Override
	public Map<Long,LocalDateTime> getScadenzaValutazioneByValutatoreId(Long accountId) {
		LOGGER.debug(Utils.getLogMessage("Recupero le date di scadenza per il referee: " + accountId));

		Map<Long, LocalDateTime> mappaScadenze = new HashMap<Long, LocalDateTime>();

		Set<Valutazione> valutazioni = getAllValutazioniForAccountNotStoricizzate(accountId);
		for(Valutazione v :  valutazioni){
			mappaScadenze.put(v.getAccreditamento().getId(), v.getDataOraScadenzaPossibilitaValutazione());
		}
		return mappaScadenze;
	}

	@Override
	public Valutazione detachValutazione(Valutazione valutazione) throws Exception {
		LOGGER.debug(Utils.getLogMessage("DETACH valutazione id: " + valutazione.getId()));

		Utils.touchFirstLevelOfEverything(valutazione);

		LOGGER.debug(Utils.getLogMessage("DETACH field valutazione"));
		for(FieldValutazioneAccreditamento fva : valutazione.getValutazioni()) {
			LOGGER.debug(Utils.getLogMessage("DETACH field valutazione id: " + fva.getId()));
			entityManager.detach(fva);
		}

		entityManager.detach(valutazione);

		return valutazione;
	}

	@Override
	public void cloneDetachedValutazione(Valutazione valStoricizzata) {

		LOGGER.debug(Utils.getLogMessage("Procedura di clonazione valutazione - start"));

		LOGGER.debug(Utils.getLogMessage("Clonazione field valutazione"));
		Set<FieldValutazioneAccreditamento> valutazioniInStorico = new HashSet<FieldValutazioneAccreditamento>();
		for(FieldValutazioneAccreditamento fva : valStoricizzata.getValutazioni()) {
			LOGGER.debug(Utils.getLogMessage("Clonazione field valutazione id: " + fva.getId()));
			fva.setId(null);
			fieldValutazioneAccreditamentoService.save(fva);
			valutazioniInStorico.add(fva);
		}
		valStoricizzata.setValutazioni(valutazioniInStorico);
		valStoricizzata.setId(null);

		LOGGER.debug(Utils.getLogMessage("Procedura di detach e clonazione valutazione - success"));
	}

	@Override
	public void copiaInStorico(Valutazione valutazione) throws Exception {
		Valutazione valStoricizzata = detachValutazione(valutazione);
		cloneDetachedValutazione(valStoricizzata);
		valStoricizzata.setStoricizzato(true);
		save(valStoricizzata);
	}

	@Override
	public void setEsitoForEnabledFields(Valutazione valutazione, Boolean esito) throws Exception {
		for(FieldValutazioneAccreditamento f : valutazione.getValutazioni() ) {
			if(f.isEnabled()) {
				f.setEsito(esito);
			}
		}
	}

	@Override
	public Valutazione getValutazioneSegreteriaForAccreditamentoIdNotStoricizzato(Long accreditamentoId) {
		LOGGER.debug(Utils.getLogMessage("Recupero la valutazione attiva (non storicizzata) per l'accreditamento id:" + accreditamentoId + " della segreteria"));
		return valutazioneRepository.findOneByAccreditamentoIdAndTipoValutazioneAndStoricizzatoFalse(accreditamentoId, ValutazioneTipoEnum.SEGRETERIA_ECM);
	}

	@Override
	public Set<Valutazione> getAllValutazioniStoricizzateForAccreditamentoId(Long accreditamentoId) {
		LOGGER.debug(Utils.getLogMessage("Recupero tutte le valutazioni Storicizzate per l'accreditamento id: " + accreditamentoId));
		return valutazioneRepository.findAllByAccreditamentoIdAndStoricizzatoTrueAndAccreditamentoStatoValutazioneNotNullOrderByDataValutazioneAsc(accreditamentoId);
	}

	@Override
	public Map<String, Map<IdFieldEnum, FieldValutazioneAccreditamento>> getMapAllValutazioneSingoli(Valutazione valutazione, Accreditamento accreditamento) {
		Map<String, Map<IdFieldEnum, FieldValutazioneAccreditamento>> mappaValutazione = new HashMap<String, Map<IdFieldEnum, FieldValutazioneAccreditamento>>();
		//PROVIDER
		mappaValutazione.put("provider", fieldValutazioneAccreditamentoService.filterFieldValutazioneBySubSetAsMap(valutazione.getValutazioni(), SubSetFieldEnum.PROVIDER));
		//LEGALE RAPPRESENTANTE
		mappaValutazione.put("legaleRappresentante", fieldValutazioneAccreditamentoService.filterFieldValutazioneBySubSetAsMap(valutazione.getValutazioni(), SubSetFieldEnum.LEGALE_RAPPRESENTANTE));
		//DELEGATO RAPPRESENTANTE
		mappaValutazione.put("delegatoRappresentante", fieldValutazioneAccreditamentoService.filterFieldValutazioneBySubSetAsMap(valutazione.getValutazioni(), SubSetFieldEnum.DELEGATO_LEGALE_RAPPRESENTANTE));
		//DATI ACCREDITAMENTO
		mappaValutazione.put("datiAccreditamento", fieldValutazioneAccreditamentoService.filterFieldValutazioneBySubSetAsMap(valutazione.getValutazioni(), SubSetFieldEnum.DATI_ACCREDITAMENTO));
		//RESPONSABILE SEGRETERIA
		mappaValutazione.put("responsabileSegreteria", fieldValutazioneAccreditamentoService.filterFieldValutazioneBySubSetAsMap(valutazione.getValutazioni(), SubSetFieldEnum.RESPONSABILE_SEGRETERIA));
		//RESPONSABILE AMMINISTRATIVO
		mappaValutazione.put("responsabileAmministrativo", fieldValutazioneAccreditamentoService.filterFieldValutazioneBySubSetAsMap(valutazione.getValutazioni(), SubSetFieldEnum.RESPONSABILE_AMMINISTRATIVO));
		//RESPONSABILE SISTEMA INFORMATICO
		mappaValutazione.put("responsabileSistemaInformatico", fieldValutazioneAccreditamentoService.filterFieldValutazioneBySubSetAsMap(valutazione.getValutazioni(), SubSetFieldEnum.RESPONSABILE_SISTEMA_INFORMATICO));
		//RESPONSABILE QUALITA
		mappaValutazione.put("responsabileQualita", fieldValutazioneAccreditamentoService.filterFieldValutazioneBySubSetAsMap(valutazione.getValutazioni(), SubSetFieldEnum.RESPONSABILE_QUALITA));
		//ALLEGATI
		mappaValutazione.put("allegati", fieldValutazioneAccreditamentoService.filterFieldValutazioneBySubSetAsMap(valutazione.getValutazioni(), SubSetFieldEnum.ALLEGATI_ACCREDITAMENTO));
		//VERBALE VALUTAZIONE SUL CAMPO
		mappaValutazione.put("valutazioniSulCampo", fieldValutazioneAccreditamentoService.filterFieldValutazioneBySubSetAsMap(valutazione.getValutazioni(), SubSetFieldEnum.VALUTAZIONE_SUL_CAMPO));

		return mappaValutazione;
	}

	@Override
	public Map<String, FieldValutazioniRipetibiliWrapper> getMapAllValutazioneRipetibili(Valutazione valutazione, Accreditamento accreditamento) {
		Map<String, FieldValutazioniRipetibiliWrapper> mappaValutazioniRipetibili = new HashMap<String, FieldValutazioniRipetibiliWrapper>();
		//SEDI
		FieldValutazioniRipetibiliWrapper fvrwSedi = new FieldValutazioniRipetibiliWrapper();
		fvrwSedi.setSubset("sedi");
		String sedeOperativaLabel = messageSource.getMessage("label.sede_operativa", null, Locale.ITALIAN);
		String sedeLegaleLabel = messageSource.getMessage("label.sede_legale", null, Locale.ITALIAN);
		for(Sede sede : accreditamento.getProvider().getSedi()) {
			if(sede.isSedeLegale()) {
				fvrwSedi.getMappaValutazioni().put(sedeLegaleLabel + ": " + sede.getIndirizzo() + " (" + sede.getComune() + ")", fieldValutazioneAccreditamentoService.filterFieldValutazioneByObjectAsMap(valutazione.getValutazioni(), sede.getId()));
			}
			else {
				fvrwSedi.getMappaValutazioni().put(sedeOperativaLabel + ": " + sede.getIndirizzo() + " (" + sede.getComune() + ")", fieldValutazioneAccreditamentoService.filterFieldValutazioneByObjectAsMap(valutazione.getValutazioni(), sede.getId()));
			}
		}
		mappaValutazioniRipetibili.put(fvrwSedi.getSubset(), fvrwSedi);
		//COMPONENTI COMITATO SCIENTIFICO
		FieldValutazioniRipetibiliWrapper fvrwComp = new FieldValutazioniRipetibiliWrapper();
		fvrwComp.setSubset("componentiComitato");
		String componenteComitatoLabel = messageSource.getMessage("label.componente_comitato", null, Locale.ITALIAN);
		String coordinatoreComitatoLabel = messageSource.getMessage("label.coordinatore_comitato", null, Locale.ITALIAN);
		for(Persona persona : accreditamento.getProvider().getComponentiComitatoScientifico()) {
			if(persona.isCoordinatoreComitatoScientifico()) {
				fvrwComp.getMappaValutazioni().put(coordinatoreComitatoLabel + ": " + persona.getAnagrafica().getNome() + " " + persona.getAnagrafica().getCognome(), fieldValutazioneAccreditamentoService.filterFieldValutazioneByObjectAsMap(valutazione.getValutazioni(),  persona.getId()));
			}
			else {
				fvrwComp.getMappaValutazioni().put(componenteComitatoLabel + ": " + persona.getAnagrafica().getNome() + " " + persona.getAnagrafica().getCognome(), fieldValutazioneAccreditamentoService.filterFieldValutazioneByObjectAsMap(valutazione.getValutazioni(),  persona.getId()));
			}
		}
		mappaValutazioniRipetibili.put(fvrwComp.getSubset(), fvrwComp);
		return mappaValutazioniRipetibili;
	}

	//debug mode
	@Transactional
	@Override
	public void valutaTuttiSi(Accreditamento accreditamento, Account account) {
		Valutazione valutazione = getValutazioneByAccreditamentoIdAndAccountIdAndNotStoricizzato(accreditamento.getId(), account.getId());
		//Eliminando e riinserendo si perde il valore del campo enabled
		valutazione.getValutazioni().clear();
		valutazione.setValutazioni(fieldValutazioneAccreditamentoService.createAllFieldValutazioneAndSetEsito(true, accreditamento));
		save(valutazione);

		if(accreditamento.isValutazioneSulCampo()) {
			VerbaleValutazioneSulCampo verbale = accreditamento.getVerbaleValutazioneSulCampo();
			Set<FieldValutazioneAccreditamento> valSulCampo = new HashSet<FieldValutazioneAccreditamento>();
			for(FieldValutazioneAccreditamento val : valutazione.getValutazioni()) {
				if(val.getIdField().getSubSetField() == SubSetFieldEnum.VALUTAZIONE_SUL_CAMPO)
					valSulCampo.add(val);
			}
			verbale.getDatiValutazioneSulCampo().getValutazioniSulCampo().clear();
			verbale.getDatiValutazioneSulCampo().setValutazioniSulCampo(valSulCampo);
			accreditamento.setVerbaleValutazioneSulCampo(verbale);
			accreditamentoService.save(accreditamento);
		}
	}

	//sblocca i campi e attiva i fieldValutazione per il subset passato
	@Override
	public void resetEsitoAndEnabledForSubset(Valutazione valutazione, List<FieldValutazioneAccreditamento> campiDaSbloccare) {
		for(FieldValutazioneAccreditamento fiaDaSbloccare : campiDaSbloccare) {
			for(FieldValutazioneAccreditamento fia : valutazione.getValutazioni()) {
				if(fia.getIdField() == fiaDaSbloccare.getIdField() && fia.getObjectReference() == fiaDaSbloccare.getObjectReference()) {
					fia.setEnabled(true);
					fia.setEsito(null);
					fia.setModificatoInIntegrazione(fiaDaSbloccare.getModificatoInIntegrazione());
					fieldValutazioneAccreditamentoService.save(fia);
				}
			}
		}
	}

	/* Inizializza la valutazione di un utente (in genere crecm o team leader) che non ha ancora mai valutato da 0 la domanda
	 * se ci troviamo in uno stato SUCCESSIVO ad un'integrazione, crea una valutazione completamente valutata positivamente e sblocca/flagga
	 * solo i campi relativi all'integrazione */
	@Override
	public void initializeFieldValutazioni(Valutazione valutazione, Accreditamento accreditamento) {
		LOGGER.debug(Utils.getLogMessage("Creazione fieldValutazioni per valutazione: " + valutazione.getId()));

		if(accreditamento.getStatoUltimaIntegrazione() == null) {
			valutazione.setValutazioni(fieldValutazioneAccreditamentoService.getValutazioniDefault(accreditamento));
		}
		else {
			valutazione.setValutazioni(fieldValutazioneAccreditamentoService.createAllFieldValutazioneAndSetEsitoAndEnabled(true, false, accreditamento));
			Long accreditamentoId = accreditamento.getId();
			Long workFlowProcessInstanceId = accreditamento.getWorkflowInCorso().getProcessInstanceId();
			AccreditamentoStatoEnum statoIntegrazione = accreditamento.getStatoUltimaIntegrazione();

			Set<FieldIntegrazioneAccreditamento> fieldIntegrazioneList = fieldIntegrazioneAccreditamentoService.getAllFieldIntegrazionePerSbloccoValutazioneForAccreditamentoByContainer(accreditamentoId, statoIntegrazione, workFlowProcessInstanceId);
			sbloccaValutazioneByFieldIntegrazioneList(valutazione, fieldIntegrazioneList);
		}
	}


	//metodo che gestisce l'abilitazione dei fieldValutazione a seconda dei fieldIntegrazione
	public void sbloccaValutazioneByFieldIntegrazioneList(Valutazione valutazione, Set<FieldIntegrazioneAccreditamento> fieldIntegrazioneList) {
		Set<FieldValutazioneAccreditamento> fieldValutazioni = valutazione.getValutazioni();
		for(FieldIntegrazioneAccreditamento fieldIntegrazione : fieldIntegrazioneList){
			FieldValutazioneAccreditamento field = null;
			LOGGER.debug(Utils.getLogMessage("Sblocco valutazione per " + fieldIntegrazione.getIdField()));
			if(fieldIntegrazione.getObjectReference() != -1){
				//multi-istanza
				field = Utils.getField(fieldValutazioni, fieldIntegrazione.getObjectReference(), fieldIntegrazione.getIdField());
			}else{
				//non multi-istanza
				field = Utils.getField(fieldValutazioni, fieldIntegrazione.getIdField());
			}
			//se lo trova lo sblocca
			if(field != null && !field.isEnabled()){
				field.setEsito(null);
				field.setEnabled(true);
				field.setNote(null);
				if(fieldIntegrazione.isModificato())
					field.setModificatoInIntegrazione(true);
				else
					field.setModificatoInIntegrazione(false);
				fieldValutazioni.add(field);
			}
			//se non lo trova ed è una creazione/rimozione/sostituzione lo crea
			else {
				if(IdFieldEnum.isFull(fieldIntegrazione.getIdField()) && !fieldIntegrazione.isFittizio()) {
					//IN CASO DI MULTISTANZA
					//cerco l'oggetto da valutare su db, se non lo trovo significa che l'aggiunta dell'oggetto non
					//è stata approvata dalla segreteria o la rimozione è stata approvata, percui non devo creare il fieldvalutazione
					Long id = fieldIntegrazione.getObjectReference();
					if(fieldIntegrazione.getObjectReference() == -1 ||
							personaService.getPersona(id) != null ||
							sedeService.getSede(id) != null) {
						field = new FieldValutazioneAccreditamento();
						field.setEsito(null);
						field.setEnabled(true);
						field.setNote(null);
						field.setModificatoInIntegrazione(true);
						field.setAccreditamento(fieldIntegrazione.getAccreditamento());
						field.setObjectReference(fieldIntegrazione.getObjectReference());
						field.setIdField(fieldIntegrazione.getIdField());
						fieldValutazioneAccreditamentoService.save(field);
						fieldValutazioni.add(field);
					}
				}
			}
		}
		//ciclo per gli idField enum cercado quelli raggruppati (prendo il padre)
		for(IdFieldEnum id : IdFieldEnum.values()) {
			FieldIntegrazioneAccreditamento fieldInteg = null;
			FieldValutazioneAccreditamento fieldVal = null;
			boolean modificato = false;
			if(!id.getGruppo().isEmpty()) {
				//controllo se ci sono fieldIntegrazione attivi per i figli
				for(IdFieldEnum idGruppo : id.getGruppo()) {
					fieldInteg = Utils.getField(fieldIntegrazioneList, idGruppo);
					if(fieldInteg != null) {
						//se ci sono mi faccio dare il fieldValutazione del padre
						fieldVal = Utils.getField(fieldValutazioni, id);
						if(fieldVal != null && fieldInteg.isModificato()) {
							modificato = true;
						}
					}
				}
				//modifico di conseguenza il fieldValutazione del padre
				if(fieldVal != null && !fieldVal.isEnabled()) {
					fieldVal.setEsito(null);
					fieldVal.setEnabled(true);
					fieldVal.setNote(null);
					fieldVal.setModificatoInIntegrazione(modificato);
				}
			}
		}
		valutazione.setValutazioni(fieldValutazioni);
		save(valutazione);
	}


	@Override
	public void valutazioneIdNotStoricizzatoAndAccountId(Long valutazioneId, Long accountId) {
		LOGGER.debug(Utils.getLogMessage("Riassegnamento valutazione " + valutazioneId + "di domanda di Accreditamento ad un ALTRO account" + accountId));

		Valutazione valutazione = getValutazione(valutazioneId);
		Set<Account> accountSegreteria = accountService.getUserByProfileEnum(ProfileEnum.SEGRETERIA);
		for (Account account : accountSegreteria) {
			if(account.getId().equals(accountId)) {
				valutazione.setAccount(account);
				valutazioneRepository.save(valutazione);
				return;
			}
		}
	}

	@Override
	public void riassegnaAccountValutazioneNotStoricizzato(Long valutazioneId, Long accountId) throws Exception {
		LOGGER.debug(Utils.getLogMessage("Riassegnamento valutazione " + valutazioneId + " di domanda di Accreditamento ad un ALTRO account: " + accountId));

		Valutazione valutazione = getValutazione(valutazioneId);
		Account account = accountService.getUserById(accountId);
		valutazione.setAccount(account);
		save(valutazione);

		//lato bonita assegno il task all'utente
		Accreditamento accreditamento = valutazione.getAccreditamento();
		workflowService.rilasciaTask(accreditamento);
	}


}



