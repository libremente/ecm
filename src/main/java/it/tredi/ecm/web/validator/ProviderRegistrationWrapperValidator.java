package it.tredi.ecm.web.validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import it.tredi.ecm.dao.entity.Persona;
import it.tredi.ecm.service.bean.ProviderRegistrationWrapper;
import it.tredi.ecm.utils.Utils;

@Component
public class ProviderRegistrationWrapperValidator{
	private static final Logger LOGGER = LoggerFactory.getLogger(ProviderRegistrationWrapperValidator.class);

	@Autowired private AccountValidator accountValidator;
	@Autowired private FileValidator fileValidator;
	@Autowired private ProviderValidator providerValidator;

	public void validate(Object target, Errors errors) {
		LOGGER.info(Utils.getLogMessage("Validazione ProviderRegistrationWrapper"));
		ProviderRegistrationWrapper providerForm = (ProviderRegistrationWrapper)target;
		//Provider nulti account
		//accountValidator.validate(providerForm.getProvider().getAccount(), errors, "provider.account.");
		//In registrazione viene aggiunto un solo account
		accountValidator.validateForProviderRegistration(providerForm.getAccount(), errors, "account.");

		providerValidator.validateForRegistrazione(providerForm.getProvider(), errors, "provider.");
		validateLegale(providerForm.getLegale(), errors);

//		//Delegato consentito solo per alcuni tipi di Provider
//		//allegato obbligatorio solo se e' stato selezionato il flag delegato
//		if(providerForm.getProvider().getTipoOrganizzatore() != null && providerForm.getProvider().getTipoOrganizzatore().isTipoP() && providerForm.isDelegato() == null)
//			errors.rejectValue("delegato", "error.empty");

		if(providerForm.isDelegato()) {
			//TODO se viene riabilitato questo file -> recuperare il codice fiscale da passare al filevalidator per la verifica sulla firma digitale
			fileValidator.validateData(providerForm.getDelega(), errors, "delega");
		}

		Utils.logDebugErrorFields(LOGGER, errors);

	}

	private void validateLegale(Persona legale, Errors errors){
		if(legale.getAnagrafica().getCognome().isEmpty())
			errors.rejectValue("legale.anagrafica.cognome", "error.empty");
		if(legale.getAnagrafica().getNome().isEmpty())
			errors.rejectValue("legale.anagrafica.nome", "error.empty");


		/*@since ERM014009
		if(legale.getAnagrafica().getCodiceFiscale().isEmpty())
			errors.rejectValue("legale.anagrafica.codiceFiscale", "error.empty");
			*/
		Utils.rejectIfCodFiscIncorrect(legale.getAnagrafica().getCodiceFiscale(), errors, "legale.anagrafica.codiceFiscale");


		if(legale.getAnagrafica().getPec().isEmpty())
			errors.rejectValue("legale.anagrafica.pec", "error.empty");
		if(legale.getAnagrafica().getEmail().isEmpty())
			errors.rejectValue("legale.anagrafica.email", "error.empty");
	}

}
