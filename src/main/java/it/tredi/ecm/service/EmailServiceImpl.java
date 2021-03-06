package it.tredi.ecm.service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring4.SpringTemplateEngine;

import it.tredi.ecm.dao.entity.AlertEmail;
import it.tredi.ecm.dao.entity.Provider;
import it.tredi.ecm.dao.enumlist.AlertTipoEnum;
import it.tredi.ecm.service.bean.EcmProperties;

@Service
public class EmailServiceImpl implements EmailService {
	private static Logger LOGGER = LoggerFactory.getLogger(EmailServiceImpl.class);

	@Autowired private JavaMailSender javaMailSender;
	@Autowired private EcmProperties ecmProperties;
	@Autowired private SpringTemplateEngine templateEngine;

	@Override
	public void send(SimpleMailMessage mailMessage) {
		LOGGER.info("Sending email");
		javaMailSender.send(mailMessage);
	}

	@Override
	public void send(String from, String to, String subject, String body, boolean isHtml) throws Exception{
		LOGGER.info("Invio email da template");
		MimeMessage message = javaMailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true);
		helper.setFrom(from);
		helper.setTo(to);
		helper.setSubject(subject);
		helper.setText(body, isHtml);
		javaMailSender.send(message);
	}

	@Override
	public void inviaNotificaAReferee(String referee, String provider) throws Exception {
		LOGGER.info("Invio notifica a Referee");
		Context context = new Context();
		context.setVariable("applicationBaseUrl", ecmProperties.getApplicationBaseUrl());
		context.setVariable("provider", provider);
		String message = templateEngine.process("assegnaDomandaReferee", context);

		send(ecmProperties.getEmailSegreteriaEcm(), referee, "Assegnazione di incarico di valutazione della domanda di accreditamento", message, true);
	}

	@Override
	public void inviaConvocazioneACommissioneECM(Set<String> commissione) throws Exception {
		LOGGER.info("Invio convocazione a Commissione Ecm");
		Context context = new Context();
		context.setVariable("applicationBaseUrl", ecmProperties.getApplicationBaseUrl());
		String message = templateEngine.process("convocazioneCommissione", context);

		for(String email : commissione){
			send(ecmProperties.getEmailSegreteriaEcm(),email, "Convocazione seduta Commissione Regionale ECM per la valutazione delle domande di accreditamento provvisorio dei Provider", message, true);
		}
	}

	@Override
	public void inviaNotificaASegreteriaMancataValutazioneReferee(String segreteria, String provider) throws Exception{
		LOGGER.info("Invio Notifica a " + segreteria + " per mancata valutazione dei referee");
		Context context = new Context();
		context.setVariable("applicationBaseUrl", ecmProperties.getApplicationBaseUrl());
		context.setVariable("provider",provider );
		String message = templateEngine.process("notificaSegreteriaMancataValutazioneReferee", context);
		send(ecmProperties.getEmailSegreteriaEcm(), segreteria, "Scadenza termine di 15 gg per la valutazione della domanda di accreditamento provvisorio da parte dei Referee assegnatari", message, true);
	}

	@Override
	public void inviaAlertErroreDiSistema(String alert) throws Exception {
		LOGGER.info("Invio Alert errore di Sistema");
		Context context = new Context();
		context.setVariable("alert", alert);
		String message = templateEngine.process("alertErroreDiSistema", context);
		send(ecmProperties.getEmailSegreteriaEcm(), ecmProperties.getEmailSegreteriaEcm(), "Errore di Sistema Applicativo ECM", message, true);
	}

	@Override
	public void inviaAlertScadenzaReInvioIntegrazioneAccreditamento(AlertEmail alert) throws Exception {
		LOGGER.info("inviaAlertScadenzaReInvioIntegrazioneAccreditamento " + alert.getTipo());
		Context context = new Context();
		String subject = "";
		
		if(alert.getTipo() == AlertTipoEnum.SCADENZA_REINVIO_INTEGRAZIONI_ACCREDITAMENTO_PROVVISORIO){
			context.setVariable("isStandard", false);
			context.setVariable("isPreavvisoDiRigetto", false);
			subject += "Avviso di scadenza termini per reinvio integrazioni richieste sulla domanda di accreditamento provvisorio";
		}

		if(alert.getTipo() == AlertTipoEnum.SCADENZA_REINVIO_INTEGRAZIONI_PREAVVISO_DI_RIGETTO_ACCREDITAMENTO_PROVVISORIO){
			context.setVariable("isStandard", false);
			context.setVariable("isPreavvisoDiRigetto", true);
			subject += "Avviso di scadenza termini per reinvio integrazioni relative al preavviso di rigetto richieste sulla domanda di accreditamento provvisorio";
		}

		if(alert.getTipo() == AlertTipoEnum.SCADENZA_REINVIO_INTEGRAZIONI_PREAVVISO_DI_RIGETTO_ACCREDITAMENTO_STANDARD){
			context.setVariable("isStandard", true);
			context.setVariable("isPreavvisoDiRigetto", false);
			subject += "Avviso di scadenza termini per reinvio integrazioni relative al preavviso di rigetto richieste sulla domanda di accreditamento standard";
		}

		if(alert.getTipo() == AlertTipoEnum.SCADENZA_REINVIO_INTEGRAZIONI_ACCREDITAMENTO_STANDARD){
			context.setVariable("isStandard", true);
			context.setVariable("isPreavvisoDiRigetto", true);
			subject += "Avviso di scadenza termini per reinvio integrazioni richieste sulla domanda di accreditamento standard";
		}
		
		subject += " presentata";

		String message = templateEngine.process("alertScadenzaReinvioIntegrazioniAccreditamento", context);

		for(String dst : alert.getDestinatari()){
			send(ecmProperties.getEmailSegreteriaEcm(), dst, subject, message, true);
		}
	}

	@Override
	public void inviaAlertScadenzaAccreditamento(AlertEmail alert) throws Exception {
		LOGGER.info("inviaAlertScadenzaAccreditamento " + alert.getTipo());
		Context context = new Context();
		String subject = "Termine scadenza di accreditamento";

		if(alert.getTipo() == AlertTipoEnum.SCADENZA_ACCREDITAMENTO_PROVVISORIO){
			context.setVariable("isStandard", false);
			subject += " provvisorio";
		}

		if(alert.getTipo() == AlertTipoEnum.SCADENZA_ACCREDITAMENTO_STANDARD){
			context.setVariable("isStandard", true);
			subject += " standard";
		}
		
		subject += " come Provider ECM";

		String message = templateEngine.process("alertScadenzaAccreditamento", context);

		for(String dst : alert.getDestinatari()){
			send(ecmProperties.getEmailSegreteriaEcm(), dst, subject, message, true);
		}
	}

	@Override
	public void inviaConfermaReInvioIntegrazioniAccreditamento(boolean isStandard, boolean isPreavvisoRigetto, Provider provider) throws Exception {
		LOGGER.info("inviaConfermaReInvioIntegrazioneAccreditamento (standard/preavvisoRigetto): " + isStandard + "/" + isPreavvisoRigetto);
		Context context = new Context();
		String subject = "Corretto reinvio domanda di accreditamento ";
		
		context.setVariable("isStandard", isStandard);
		context.setVariable("isPreavvisoDiRigetto", isPreavvisoRigetto);

		if(isStandard)
			subject += " standard";
		else
			subject += " provvisorio";

		if(isPreavvisoRigetto) {
			subject += " in stato \"Preavviso di rigetto\"";
		}else {
			subject += " in stato \"Richiesta di integrazioni\"";
		}


		String message = templateEngine.process("confermaInvioIntegrazioni", context);

		Set<String> destinatari = new HashSet<String>();

		if(provider.getLegaleRappresentante() != null)
			destinatari.add(provider.getLegaleRappresentante().getAnagrafica().getEmail());
		if(provider.getDelegatoLegaleRappresentante() != null)
			destinatari.add(provider.getDelegatoLegaleRappresentante().getAnagrafica().getEmail());

		for(String dst : destinatari){
			send(ecmProperties.getEmailSegreteriaEcm(), dst, subject, message, true);
		}
	}

	@Override
	public void inviaAlertScadenzaPagamento(AlertEmail alert) throws Exception {
		LOGGER.info("inviaAlertScadenzaPagamento");
		Context context = new Context();
		String subject = "Scadenza termini per il pagamento del contributo annuo";
		String message = templateEngine.process("alertScadenzaContributoAnnuo", context);

		for(String dst : alert.getDestinatari()){
			send(ecmProperties.getEmailSegreteriaEcm(), dst, subject, message, true);
		}
	}

	@Override
	public void inviaAlertScadenzaPFA(AlertEmail alert) throws Exception {
		LOGGER.info("inviaAlertScadenzaPFA");
		Context context = new Context();
		String subject = "Avviso Scadenza Compilazione Piano Formativo Annuale";

		String message = templateEngine.process("alertScadenzaPFA", context);

		for(String dst : alert.getDestinatari()){
			send(ecmProperties.getEmailSegreteriaEcm(), dst, subject, message, true);
		}
	}

	@Override
	public void inviaAlertScadenzaRelazioneAnnuale(AlertEmail alert) throws Exception {
		LOGGER.info("inviaAlertScadenzaRelazioneAnnuale");
		Context context = new Context();
		String subject = "Avviso Scadenza Inserimento Relazione Annuale";

		String message = templateEngine.process("alertScadenzaRelazioneAnnuale", context);

		for(String dst : alert.getDestinatari()){
			send(ecmProperties.getEmailSegreteriaEcm(), dst, subject, message, true);
		}
	}

	@Override
	public void inviaAlertScadenzaPagamentoRendicontazioneEvento(AlertEmail alert) throws Exception {
		LOGGER.info("inviaAlertScadenzaPagamentoRendicontazioneEvento");
		Context context = new Context();
		String subject = "Avviso Scadenza Pagamento e Rendicontazione Evento";

		context.setVariable("eventoIdentificativo", alert.getEvento().getCodiceIdentificativo());
		context.setVariable("eventoTitolo", alert.getEvento().getTitolo());

		String message = templateEngine.process("alertScadenzaPagamentoRendicontazioneEvento", context);

		for(String dst : alert.getDestinatari()){
			send(ecmProperties.getEmailSegreteriaEcm(), dst, subject, message, true);
		}
	}

	@Override
	public void inviaAlertScadenzaValutazioneReferee(AlertEmail alert) throws Exception {
		LOGGER.info("inviaAlertScadenzaValutazioneReferee");
		Context context = new Context();
		String subject = "Scadenza Valutazione Domanda Accreditamento";

		context.setVariable("applicationBaseUrl", ecmProperties.getApplicationBaseUrl());
		context.setVariable("provider", alert.getProvider().getDenominazioneLegale());

		String message = templateEngine.process("alertScadenzaValutazioneReferee", context);

		for(String dst : alert.getDestinatari()){
			send(ecmProperties.getEmailSegreteriaEcm(), dst, subject, message, true);
		}
	}

	@Override
	public void inviaAlertScadenzaInvioAccreditamentoStandard(AlertEmail alert) throws Exception {
		LOGGER.info("inviaAlertScadenzaInvioAccreditamentoStandard");
		Context context = new Context();
		String subject = "Scadenza compilazione  domanda di accreditamento standard";

		String message = templateEngine.process("alertScadenzaInvioAccreditamentoStandard", context);

		for(String dst : alert.getDestinatari()){
			send(ecmProperties.getEmailSegreteriaEcm(), dst, subject, message, true);
		}
	}

	@Override
	public void inviaNotificaATeamLeader(String referee, String provider) throws Exception {
		LOGGER.info("Invio notifica a Team Leader");
		Context context = new Context();
		context.setVariable("applicationBaseUrl", ecmProperties.getApplicationBaseUrl());
		context.setVariable("provider", provider);
		String message = templateEngine.process("assegnaDomandaTeamLeader", context);

		send(ecmProperties.getEmailSegreteriaEcm(), referee, "Assegnamento Domanda da Valutare", message, true);
	}

	@Override
	public void inviaConvocazioneValutazioneSulCampo(Set<String> valutatori, LocalDateTime data, String provider) throws Exception {
		LOGGER.info("Invio convocazione Valutazione Sul Campo");
		Context context = new Context();
		context.setVariable("applicationBaseUrl", ecmProperties.getApplicationBaseUrl());
		context.setVariable("data", data);
		context.setVariable("provider", provider);
		String message = templateEngine.process("convocazioneValutazioneSulCampo", context);

		for(String email : valutatori){
			send(ecmProperties.getEmailSegreteriaEcm(),email, "Convocazione Valutazione Sul Campo", message, true);
		}
	}

	@Override
	public void inviaNotificaNuovaComunicazioneForProvider(String fullName, String email) throws Exception {
		LOGGER.info("Invio notifica a Legale/Delegato per nuova Comunicazione");
		Context context = new Context();
		context.setVariable("applicationBaseUrl", ecmProperties.getApplicationBaseUrl());
		context.setVariable("fullName", fullName);
		String message = templateEngine.process("nuovaComunicazioneForProvider", context);

		send(ecmProperties.getEmailSegreteriaEcm(), email, "Nuova Comunicazione per il suo Provider", message, true);

	}

	@Override
	public void inviaNotificaFirmaResponsabileSegreteriaEcm(Set<String> responsabili, Long accreditamentoId) throws Exception {
		LOGGER.info("Invio notifica al Rappresentante Segreteria ECM per nuovo documento da firmare");
		Context context = new Context();
		context.setVariable("applicationBaseUrl", ecmProperties.getApplicationBaseUrl());
		context.setVariable("accreditamentoId", accreditamentoId);
		String message = templateEngine.process("nuovoDocumentoDaFirmare", context);

		for(String email : responsabili) {
			send(ecmProperties.getEmailSegreteriaEcm(),email, "Nuovo documento da firmare digitalmente", message, true);
		}
	}
}
