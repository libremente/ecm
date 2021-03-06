package it.tredi.ecm.web.validator;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import it.tredi.ecm.dao.entity.File;
import it.tredi.ecm.dao.entity.Provider;
import it.tredi.ecm.dao.enumlist.FileEnum;
import it.tredi.ecm.dao.enumlist.TipoOrganizzatore;
import it.tredi.ecm.service.FileService;
import it.tredi.ecm.service.ProviderService;
import it.tredi.ecm.service.bean.EcmProperties;
import it.tredi.ecm.service.bean.VerificaFirmaDigitale;
import it.tredi.ecm.utils.Utils;

@Component
public class FileValidator {
	private static final Logger LOGGER = LoggerFactory.getLogger(FileValidator.class);

	@Autowired private EcmProperties ecmProperties;
	@Autowired private MessageSource messageSource;
	@Autowired private ProviderService providerService;
	@Autowired private FileService fileService;

	//validator per file salvati su db
	public void validate(Object target, Errors errors, String prefix, Long providerId) throws Exception{
		validate(target, errors, prefix, providerId, true);
	}
	
	public void validate(Object target, Errors errors, String prefix, Long providerId, boolean doFirmCheck) throws Exception{
		validateData(target, errors, prefix);
		if(doFirmCheck) validateFirma(target, errors, prefix, providerId);
	}
	
	public void validateData(Object target, Errors errors, String prefix) {
		LOGGER.info(Utils.getLogMessage("Validazione File"));
		File file = (File)target;
		if(file != null && !file.isNew()) {
			file = fileService.getFile(file.getId());
			file.getFileData().size();
		}
		if(file == null || file.isNew() || file.getNomeFile().isEmpty() || file.getData().length == 0){
			errors.rejectValue(prefix, "error.empty");
		}else{

//			if(file.getData().length > ecmProperties.getMultipartMaxFileSize()){
//				errors.rejectValue(prefix, "error.maxFileSize", new Object[]{String.valueOf(ecmProperties.getMultipartMaxFileSize()/(1024*1024) )},"");
//			}

			int maxSize = ecmProperties.getMultipartMaxFileSize();
			if(file.isATTOCOSTITUTIVO()){
				maxSize = ecmProperties.getMultipartMaxFileSize4MB();
			}

			if(file.getData().length > maxSize){
				errors.rejectValue(prefix, "error.maxFileSize", new Object[]{String.valueOf(maxSize/(1024*1024))},"");
			}
		}
	}

	public void validateFirma(Object target, Errors errors, String prefix, Long providerId) throws Exception{
		File file = (File)target;
		if(file != null && !file.isNew()) {
			file = fileService.getFile(file.getId());
			file.getFileData().size();
		}
		if(!(file == null || file.isNew() || file.getNomeFile().isEmpty() || file.getData().length == 0)){

			//ERM015894 - si valida solo firma ma non piu CF
			validateIsSigned(target, errors, prefix);
			
			/*
			//se il cf della firma non appartiene al legale rappresentane o al delegato del legale rappresentante, allora non è valido
			if(!validateFirmaCF(file, providerId))
				errors.rejectValue(prefix, "error.codiceFiscale.firmatario");
				*/
		}
	}

	public boolean validateFirmaCF(Object target, Long providerId) throws Exception{
		File file = (File)target;
		if(file != null && !file.isNew()) {
			file = fileService.getFile(file.getId());
			file.getFileData().size();
		}
		if(!(file == null || file.isNew() || file.getNomeFile().isEmpty() || file.getData().length == 0)){

			//20170103 - dpranteda: se il provider è di tipo AZIENDE_SANITARIE, non bisogna fare il controllo sul CF in quanto ci sono provider unificati
			Provider provider = providerService.getProvider(providerId);
			/*
			if(provider.getTipoOrganizzatore() == TipoOrganizzatore.AZIENDE_SANITARIE)
				return true;
			*/

			VerificaFirmaDigitale verificaFirmaDigitale = new VerificaFirmaDigitale(file.getNomeFile(), file.getData());
			String cfLegaleRappresentante = providerService.getCodiceFiscaleLegaleRappresentantePerVerificaFirmaDigitale(providerId);
			String cfDelegatoLegaleRappresentante = providerService.getCodiceFiscaleDelegatoLegaleRappresentantePerVerificaFirmaDigitale(providerId);

			//se il cf della firma non appartiene al legale rappresentane o al delegato del legale rappresentante, allora non è valido
			if((!verificaFirmaDigitale.getLastSignerCF().isEmpty()) && (cfLegaleRappresentante.equalsIgnoreCase(verificaFirmaDigitale.getLastSignerCF()) || cfDelegatoLegaleRappresentante.equalsIgnoreCase(verificaFirmaDigitale.getLastSignerCF())))
				return true;

			return false;
		}

		return false;
	}

	//validator per file new
	public String validate(Object target, String contentType) throws Exception{
		LOGGER.info(Utils.getLogMessage("Validazione File AJAX Upload"));
		File file = (File)target;
		String error = "";
		if(file == null || file.getNomeFile().isEmpty() || file.getData().length == 0){
			error = messageSource.getMessage("error.file_vuoto", null, Locale.getDefault());
		}else{
			//validazione file xml/csv/xml.p7m/xml.zip.p7m
			if(file.getTipo() == FileEnum.FILE_REPORT_PARTECIPANTI) {
//				if(!(contentType.equalsIgnoreCase("application/xml") ||
//						contentType.equalsIgnoreCase("text/xml") ||
//						contentType.equalsIgnoreCase("application/pkcs7-mime") ||
//						contentType.equalsIgnoreCase("application/x-pkcs7-mime") ||
//						contentType.equalsIgnoreCase("text/csv")))
//					error = messageSource.getMessage("error.formatNonAcceptedXML", new Object[]{}, Locale.getDefault());
			}
			else if(file.getTipo() == FileEnum.FILE_EVENTI_PIANO_FORMATIVO) {
				if (!file.getNomeFile().toUpperCase().endsWith(".CSV"))
					error = messageSource.getMessage("error.formatNonAcceptedCSV", new Object[]{}, Locale.getDefault());
			}
			//validation file jpg/png for logo of provider
			else if(file.getTipo() == FileEnum.FILE_POVIDER_LOGO) {
				if ((!file.getNomeFile().toUpperCase().endsWith(".JPG")) && (!file.getNomeFile().toUpperCase().endsWith(".PNG")))
					error = messageSource.getMessage("error.formatNonAcceptedJPG", new Object[]{}, Locale.getDefault());
			}
			//validazione file pdf/pdf.p7m
			else {
				//rimosso controllo sul MIME per motivi di compatibilità con le configurazioni browser dei vari utenti
//				if(!(contentType.equalsIgnoreCase("application/pdf") || contentType.equalsIgnoreCase("application/pkcs7-mime") || contentType.equalsIgnoreCase("application/x-pkcs7-mime")))
//					error = messageSource.getMessage("error.formatNonAccepted", new Object[]{}, Locale.getDefault());
				//inserito il controllo sull'estensione del file
				if (!(file.getNomeFile().toUpperCase().endsWith(".PDF") || file.getNomeFile().toUpperCase().endsWith(".PDF.P7M") || file.getNomeFile().toUpperCase().endsWith(".PDF.P7C")))
					error = messageSource.getMessage("error.formatNonAccepted", new Object[]{}, Locale.getDefault());
			}

//			if(file.getData().length > ecmProperties.getMultipartMaxFileSize()){
//				error = messageSource.getMessage("error.maxFileSize", new Object[]{String.valueOf(ecmProperties.getMultipartMaxFileSize()/(1024*1024) )}, Locale.getDefault());
//			}

			int maxSize = ecmProperties.getMultipartMaxFileSize();
			if(file.isATTOCOSTITUTIVO()){
				maxSize = ecmProperties.getMultipartMaxFileSize4MB();
			}

			if(file.getData().length > maxSize){
				error = messageSource.getMessage("error.maxFileSize", new Object[]{String.valueOf(maxSize/(1024*1024) )}, Locale.getDefault());
			}
		}
		return error;
	}
	
	public void validateWithCondition(Object target, Errors errors, String prefix, Boolean condition, Long providerId) throws Exception{
		validateWithCondition(target, errors, prefix, condition, providerId, true);
	}

	public void validateWithCondition(Object target, Errors errors, String prefix, Boolean condition, Long providerId, boolean doFirmCheck) throws Exception{
		LOGGER.info(Utils.getLogMessage("Validazione File required su condizione"));
		File file = (File)target;
		if(condition == true)
			validate(target, errors, prefix, providerId, doFirmCheck);
		else {
			if(file != null && !file.isNew() && !file.getNomeFile().isEmpty())
				validate(file, errors, prefix, providerId, doFirmCheck);
		}
	}

	// ERM015894 - dal invio a cogeaps serve possibilita controlare ".xml.p7m" allora si agiunge variante con extension
	// default rimane ".pdf"
	public void validateIsSigned(Object target, Errors errors, String prefix, String extension) throws Exception{
		String ext = extension.toUpperCase();
		LOGGER.info(Utils.getLogMessage("Validazione File is Signed"));
		File file = (File) target;
		if (file == null)
			errors.rejectValue(prefix,"error.empty");
		else if(!(file.getNomeFile().toUpperCase().endsWith(ext) || file.getNomeFile().toUpperCase().endsWith(ext + ".P7M") ||	file.getNomeFile().toUpperCase().endsWith(ext + ".P7C")))
			errors.rejectValue(prefix,"error.formatNonAccepted");
		else{
			file = fileService.getFile(file.getId());
			VerificaFirmaDigitale verificaFirmaDigitale = new VerificaFirmaDigitale(file.getNomeFile(), file.getData());
			String lastSignerCF = verificaFirmaDigitale.getLastSignerCF();
			if(lastSignerCF == null || lastSignerCF.isEmpty())
				errors.rejectValue(prefix,"error.file_non_firmato");
		}
	}
	
	public void validateIsSigned(Object target, Errors errors, String prefix) throws Exception{
		validateIsSigned(target, errors, prefix, ".PDF");
	}
}
