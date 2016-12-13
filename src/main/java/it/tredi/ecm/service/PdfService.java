package it.tredi.ecm.service;

import it.tredi.ecm.dao.entity.File;
import it.tredi.ecm.pdf.PdfAccreditamentoProvvisorioAccreditatoInfo;
import it.tredi.ecm.pdf.PdfAccreditamentoProvvisorioIntegrazionePreavvisoRigettoInfo;
import it.tredi.ecm.pdf.PdfAccreditamentoProvvisorioRigettoInfo;

public interface PdfService {
	public File creaPdfAccreditamentoProvvisiorioIntegrazione(PdfAccreditamentoProvvisorioIntegrazionePreavvisoRigettoInfo integrazioneInfo) throws Exception;
	public File creaPdfAccreditamentoProvvisiorioPreavvisoRigetto(PdfAccreditamentoProvvisorioIntegrazionePreavvisoRigettoInfo preavvisoRigettoInfo) throws Exception;
	public File creaPdfAccreditamentoProvvisiorioDiniego(PdfAccreditamentoProvvisorioRigettoInfo diniegoInfo) throws Exception;
	public File creaPdfAccreditamentoProvvisiorioAccreditato(PdfAccreditamentoProvvisorioAccreditatoInfo accreditatoInfo) throws Exception;

	public File creaPdfAccreditamentoStandardIntegrazione(PdfAccreditamentoProvvisorioIntegrazionePreavvisoRigettoInfo integrazioneInfo) throws Exception;
	public File creaPdfAccreditamentoStandardPreavvisoRigetto(PdfAccreditamentoProvvisorioIntegrazionePreavvisoRigettoInfo preavvisoRigettoInfo) throws Exception;
	public File creaPdfAccreditamentoStandardDiniego(PdfAccreditamentoProvvisorioRigettoInfo diniegoInfo) throws Exception;
	public File creaPdfAccreditamentoStandardAccreditato(PdfAccreditamentoProvvisorioAccreditatoInfo accreditatoInfo) throws Exception;
}
