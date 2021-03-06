package it.tredi.ecm.service.bean;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EngineeringProperties {
	private String ipa;
	private String password;
	private String servizio;
	private String endpointPagamenti;
	private String datiSpecificiRiscossione;
	private String tipoDovutoEvento;
	private String tipoDovutoQuotaAnnua;
	private boolean useProxy;
	private String proxyHost;
	private String proxyPort;
	private String proxyUsername;
	private String proxyPassword;
	private int causaleLength;
	private String versione;

	private String firmaUrl;
	private String firmaIdclassificazione;
	private String firmaReferer;

	private String protocolloCodApplicativo;
	private String protocolloOperatoreEntrata;
	private String protocolloCodStruttura;
	private String protocolloIdc;
	private String protocolloEndpoint;
	private String protocolloServiceVersione;
	private String protocolloWebrainbowEndpoint;
	private String protocolloWebrainbowUfficioCreatoreEntrata;
	private String protocolloWebrainbowUfficioCreatoreUscita;
}
