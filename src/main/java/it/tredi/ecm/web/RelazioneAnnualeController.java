package it.tredi.ecm.web;

import java.time.LocalDate;

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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import it.tredi.ecm.dao.entity.Account;
import it.tredi.ecm.dao.entity.File;
import it.tredi.ecm.dao.entity.Provider;
import it.tredi.ecm.dao.entity.RelazioneAnnuale;
import it.tredi.ecm.service.FileService;
import it.tredi.ecm.service.ProviderService;
import it.tredi.ecm.service.RelazioneAnnualeService;
import it.tredi.ecm.service.bean.EcmProperties;
import it.tredi.ecm.utils.Utils;
import it.tredi.ecm.web.bean.Message;
import it.tredi.ecm.web.bean.RelazioneAnnualeWrapper;
import it.tredi.ecm.web.validator.RelazioneAnnualeValidator;

@Controller
public class RelazioneAnnualeController {
	public static final Logger LOGGER = LoggerFactory.getLogger(RelazioneAnnualeController.class);

	private final String LIST = "relazioneAnnuale/relazioneAnnualeList";
	private final String EDIT = "relazioneAnnuale/relazioneAnnualeEdit";
	private final String SHOW = "relazioneAnnuale/relazioneAnnualeShow";

	@Autowired
	private RelazioneAnnualeService relazioneAnnualeService;
	@Autowired
	private ProviderService providerService;
	@Autowired
	private RelazioneAnnualeValidator relazioneAnnualeValidator;
	@Autowired
	private FileService fileService;
	@Autowired
	private EcmProperties ecmProperties;

	@InitBinder
	public void setAllowedFields(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	@ModelAttribute("relazioneAnnualeWrapper")
	public RelazioneAnnualeWrapper getRelazioneAnnuale(@RequestParam(name = "editId", required = false) Long id)
			throws Exception {
		if (id != null) {
			return prepareWrapperEditByEditId(relazioneAnnualeService.getRelazioneAnnuale(id));
		}
		return new RelazioneAnnualeWrapper();
	}

	@RequestMapping("/provider/relazioneAnnuale/list")
	public String getListRelazioniAnnualiForCurrentProvider(Model model, RedirectAttributes redirectAttrs) {
		LOGGER.info(Utils.getLogMessage("GET /provider/relazioneAnnuale/list"));
		try {
			Provider currentProvider = providerService.getProvider();
			long providerId = currentProvider.getId();
			return "redirect:/provider/" + providerId + "/relazioneAnnuale/list";
		} catch (Exception ex) {
			LOGGER.error(Utils.getLogMessage("GET /provider/relazioneAnnuale/list"), ex);
			redirectAttrs.addFlashAttribute("message",
					new Message("message.errore", "message.errore_eccezione", "error"));
			return "redirect:/home";
		}
	}

	@PreAuthorize("@securityAccessServiceImpl.canShowProvider(principal,#providerId)")
	@RequestMapping("/provider/{providerId}/relazioneAnnuale/list")
	public String getListRelazioniAnnualiForProvider(@PathVariable Long providerId, Model model,
			RedirectAttributes redirectAttrs) {
		LOGGER.info(Utils.getLogMessage("GET /provider/" + providerId + "/relazioneAnnuale/list"));
		try {
			model.addAttribute(providerId);
			Account user = Utils.getAuthenticatedUser().getAccount();
			if (user.isSegreteria() || user.isProvider())
				model.addAttribute("canInsertRelazioneAnnuale", providerService.canInsertRelazioneAnnuale(providerId));
			else {
				model.addAttribute("canInsertRelazioneAnnuale", false);
			}
			model.addAttribute("relazioneList", relazioneAnnualeService.getAllRelazioneAnnualeByProviderId(providerId));
			LOGGER.info(Utils.getLogMessage("VIEW: " + LIST));
			return LIST;
		} catch (Exception ex) {
			LOGGER.error(Utils.getLogMessage("GET /provider/" + providerId + "/relazioneAnnuale/list"), ex);
			redirectAttrs.addFlashAttribute("message",
					new Message("message.errore", "message.errore_eccezione", "error"));
			return "redirect:/home";
		}
	}

	@PreAuthorize("@securityAccessServiceImpl.canEditProvider(principal,#providerId)")
	@RequestMapping("/provider/{providerId}/relazioneAnnuale/insert")
	public String inserisciRelazioniAnnualiForProvider(@PathVariable Long providerId, Model model,
			RedirectAttributes redirectAttrs) {
		LOGGER.info(Utils.getLogMessage("GET /provider/" + providerId + "/relazioneAnnuale/insert"));
		try {
			RelazioneAnnuale relazioneAnnuale = relazioneAnnualeService.createRelazioneAnnuale(providerId);
			if (relazioneAnnuale == null) {
				redirectAttrs.addFlashAttribute("message",
						new Message("message.errore", "message.errore_eccezione", "error"));
				return "redirect:/provider/" + providerId + "/relazioneAnnuale/list";
			} else {
				return goToNew(model, prepareWrapperNew(relazioneAnnuale));
			}
		} catch (Exception ex) {
			LOGGER.error(Utils.getLogMessage("GET /provider/" + providerId + "/relazioneAnnuale/list"), ex);
			redirectAttrs.addFlashAttribute("message",
					new Message("message.errore", "message.errore_eccezione", "error"));
			return "redirect:/home";
		}
	}

	@PreAuthorize("@securityAccessServiceImpl.canEditProvider(principal,#providerId)")
	@RequestMapping("/provider/{providerId}/relazioneAnnuale/{relazioneAnnualeId}/edit")
	public String editRelazioniAnnualiForProvider(@PathVariable Long providerId, @PathVariable Long relazioneAnnualeId,
			Model model, RedirectAttributes redirectAttrs) {
		LOGGER.info(Utils.getLogMessage("GET /provider/" + providerId + "/relazioneAnnuale/insert"));
		try {
			RelazioneAnnualeWrapper wrapper = prepareWrapperEdit(
					relazioneAnnualeService.getRelazioneAnnuale(relazioneAnnualeId));
			return goToEdit(model, wrapper);
		} catch (Exception ex) {
			LOGGER.error(Utils.getLogMessage("GET /provider/" + providerId + "/relazioneAnnuale/list"), ex);
			redirectAttrs.addFlashAttribute("message",
					new Message("message.errore", "message.errore_eccezione", "error"));
			return "redirect:/home";
		}
	}

	@PreAuthorize("@securityAccessServiceImpl.canShowProvider(principal,#providerId)")
	@RequestMapping("/provider/{providerId}/relazioneAnnuale/{relazioneAnnualeId}/show")
	public String showRelazioniAnnualiForProvider(@PathVariable Long providerId, @PathVariable Long relazioneAnnualeId,
			Model model, RedirectAttributes redirectAttrs) {
		LOGGER.info(Utils.getLogMessage("GET /provider/" + providerId + "/relazioneAnnuale/insert"));
		try {
			RelazioneAnnualeWrapper wrapper = prepareWrapperShow(
					relazioneAnnualeService.getRelazioneAnnuale(relazioneAnnualeId));
			return goToShow(model, wrapper);
		} catch (Exception ex) {
			LOGGER.error(Utils.getLogMessage("GET /provider/" + providerId + "/relazioneAnnuale/list"), ex);
			redirectAttrs.addFlashAttribute("message",
					new Message("message.errore", "message.errore_eccezione", "error"));
			return "redirect:/home";
		}
	}

	@PreAuthorize("@securityAccessServiceImpl.canEditProvider(principal,#providerId)")
	@RequestMapping(value = "/provider/{providerId}/relazioneAnnuale/save", method = RequestMethod.POST)
	public String saveRelazioniAnnualiForProvider(@PathVariable Long providerId,
			@ModelAttribute("relazioneAnnualeWrapper") RelazioneAnnualeWrapper wrapper, BindingResult result,
			Model model, RedirectAttributes redirectAttrs) {
		LOGGER.info(Utils.getLogMessage("POST /provider/" + providerId + "/relazioneAnnuale/save"));
		try {
			File file = wrapper.getRelazioneFinale();
			if (file != null && !file.isNew())
				wrapper.setRelazioneFinale(fileService.getFile(file.getId()));

			relazioneAnnualeValidator.validate(wrapper.getRelazioneAnnuale(), wrapper.getRelazioneFinale(), result,
					"relazioneAnnuale.", providerId);
			if (result.hasErrors()) {
				model.addAttribute("message",
						new Message("message.errore", "message.inserire_campi_required", "error"));
				LOGGER.info(Utils.getLogMessage("VIEW: " + EDIT));
				return EDIT;
			} else {
				relazioneAnnualeService.elaboraRelazioneAnnualeAndSave(wrapper.getRelazioneAnnuale(),
						wrapper.getRelazioneFinale(), false);
				redirectAttrs.addFlashAttribute("message",
						new Message("message.completato", "message.relazione_annuale_salvata", "success"));
				LOGGER.info(Utils.getLogMessage("REDIRECT: /provider/" + providerId + "/relazioneAnnuale/list"));
				return "redirect:/provider/{providerId}/relazioneAnnuale/list";
			}
		} catch (Exception ex) {
			LOGGER.error(Utils.getLogMessage("GET /provider/" + providerId + "/relazioneAnnuale/list"), ex);
			redirectAttrs.addFlashAttribute("message",
					new Message("message.errore", "message.errore_eccezione", "error"));
			return "redirect:/home";
		}
	}

	@PreAuthorize("@securityAccessServiceImpl.canEditProvider(principal,#providerId)")
	@RequestMapping(value = "/provider/{providerId}/relazioneAnnuale/draftSave", method = RequestMethod.POST)
	public String saveAsBozzaRelazioniAnnualiForProvider(@PathVariable Long providerId,
			@ModelAttribute("relazioneAnnualeWrapper") RelazioneAnnualeWrapper wrapper, BindingResult result,
			Model model, RedirectAttributes redirectAttrs) {
		LOGGER.info(Utils.getLogMessage("POST /provider/" + providerId + "/relazioneAnnuale/draftSave"));
		try {
			relazioneAnnualeService.elaboraRelazioneAnnualeAndSave(wrapper.getRelazioneAnnuale(),
					wrapper.getRelazioneFinale(), true);
			redirectAttrs.addFlashAttribute("message",
					new Message("message.completato", "message.bozza_relazione_annuale_salvata", "success"));
			LOGGER.info(Utils.getLogMessage("REDIRECT: /provider/" + providerId + "/relazioneAnnuale/"
					+ wrapper.getRelazioneAnnuale().getId() + "/show"));
			return "redirect:/provider/{providerId}/relazioneAnnuale/" + wrapper.getRelazioneAnnuale().getId()
					+ "/show";
		} catch (Exception ex) {
			LOGGER.error(Utils.getLogMessage("POST /provider/" + providerId + "/relazioneAnnuale/draftSave"), ex);
			redirectAttrs.addFlashAttribute("message",
					new Message("message.errore", "message.errore_eccezione", "error"));
			return "redirect:/provider/{providerId}/relazioneAnnuale/list";
		}
	}

	@PreAuthorize("@securityAccessServiceImpl.canEditProvider(principal,#providerId)")
	@RequestMapping(value = "/provider/{providerId}/relazioneAnnuale/{relazioneAnnualeId}/reload", method = RequestMethod.GET)
	public String reloadRelazioniAnnualiForProvider(@PathVariable Long providerId,
			@PathVariable Long relazioneAnnualeId,
			@ModelAttribute("relazioneAnnualeWrapper") RelazioneAnnualeWrapper wrapper, BindingResult result,
			Model model, RedirectAttributes redirectAttrs) {
		LOGGER.info(Utils
				.getLogMessage("POST /provider/" + providerId + "/relazioneAnnuale/" + relazioneAnnualeId + "/realod"));
		try {
			RelazioneAnnuale relazioneAnnuale = relazioneAnnualeService.getRelazioneAnnuale(relazioneAnnualeId);
			relazioneAnnualeService.elaboraRelazioneAnnualeAndSave(relazioneAnnuale,
					relazioneAnnuale.getRelazioneFinale(), true);
			redirectAttrs.addFlashAttribute("message",
					new Message("message.completato", "message.relazione_annuale_rielaborata", "success"));
			LOGGER.info(Utils.getLogMessage(
					"REDIRECT: /provider/" + providerId + "/relazioneAnnuale/" + relazioneAnnualeId + "/show"));
			return "redirect:/provider/{providerId}/relazioneAnnuale/" + relazioneAnnualeId + "/show";
		} catch (Exception ex) {
			LOGGER.error(Utils.getLogMessage("POST /provider/" + providerId + "/relazioneAnnuale/draftSave"), ex);
			redirectAttrs.addFlashAttribute("message",
					new Message("message.errore", "message.errore_eccezione", "error"));
			return "redirect:/provider/{providerId}/relazioneAnnuale/list";
		}
	}

	private RelazioneAnnualeWrapper prepareWrapperNew(RelazioneAnnuale relazioneAnnuale) {
		RelazioneAnnualeWrapper wrapper = new RelazioneAnnualeWrapper();
		// relazioneAnnuale.setAnnoRiferimento(LocalDate.now().getYear());
		wrapper.setProviderId(relazioneAnnuale.getProvider().getId());
		wrapper.setRelazioneAnnuale(relazioneAnnuale);
		return wrapper;
	}

	private RelazioneAnnualeWrapper prepareWrapperEdit(RelazioneAnnuale relazioneAnnuale) {
		return prepareWrapperEdit(relazioneAnnuale, false);
	}

	private RelazioneAnnualeWrapper prepareWrapperEditByEditId(RelazioneAnnuale relazioneAnnuale) {
		return prepareWrapperEdit(relazioneAnnuale, true);
	}

	private RelazioneAnnualeWrapper prepareWrapperEdit(RelazioneAnnuale relazioneAnnuale, boolean reloadById) {
		RelazioneAnnualeWrapper wrapper = new RelazioneAnnualeWrapper();
		wrapper.setProviderId(relazioneAnnuale.getProvider().getId());
		wrapper.setRelazioneAnnuale(relazioneAnnuale);
		if (!reloadById)
			wrapper.setRelazioneFinale(relazioneAnnuale.getRelazioneFinale());
		return wrapper;
	}

	private RelazioneAnnualeWrapper prepareWrapperShow(RelazioneAnnuale relazioneAnnuale) {
		RelazioneAnnualeWrapper wrapper = new RelazioneAnnualeWrapper();
		wrapper.setProviderId(relazioneAnnuale.getProvider().getId());
		wrapper.setRelazioneAnnuale(relazioneAnnuale);

		return wrapper;
	}

	private boolean canConfirmRA() {
		return !LocalDate.now()
				.isAfter(LocalDate.of(LocalDate.now().getYear(), ecmProperties.getRelazioneAnnualeMesePeriodoNuovo(),
						ecmProperties.getRelazioneAnnualeGiornoPeriodoNuovo()));
	}

	private String goToNew(Model model, RelazioneAnnualeWrapper wrapper) {
		model.addAttribute("canConfirmRA", canConfirmRA());
		model.addAttribute("relazioneAnnualeWrapper", wrapper);
		LOGGER.info(Utils.getLogMessage("VIEW: " + EDIT));
		return EDIT;
	}

	private String goToEdit(Model model, RelazioneAnnualeWrapper wrapper) {
		model.addAttribute("canConfirmRA", canConfirmRA());
		model.addAttribute("relazioneAnnualeWrapper", wrapper);
		LOGGER.info(Utils.getLogMessage("VIEW: " + EDIT));
		return EDIT;
	}

	private String goToShow(Model model, RelazioneAnnualeWrapper wrapper) {
		model.addAttribute("relazioneAnnualeWrapper", wrapper);
		LOGGER.info(Utils.getLogMessage("VIEW: " + SHOW));
		return SHOW;
	}

}
