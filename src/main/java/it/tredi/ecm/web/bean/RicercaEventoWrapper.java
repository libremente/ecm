package it.tredi.ecm.web.bean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import org.springframework.format.annotation.DateTimeFormat;

import it.tredi.ecm.dao.entity.Disciplina;
import it.tredi.ecm.dao.entity.Obiettivo;
import it.tredi.ecm.dao.entity.Professione;
import it.tredi.ecm.dao.enumlist.EventoStatoEnum;
import it.tredi.ecm.dao.enumlist.MetodoDiLavoroEnum;
import it.tredi.ecm.dao.enumlist.MetodologiaDidatticaFADEnum;
import it.tredi.ecm.dao.enumlist.MetodologiaDidatticaRESEnum;
import it.tredi.ecm.dao.enumlist.ProceduraFormativa;
import it.tredi.ecm.dao.enumlist.TipologiaEventoFADEnum;
import it.tredi.ecm.dao.enumlist.TipologiaEventoFSCEnum;
import it.tredi.ecm.dao.enumlist.TipologiaEventoRESEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RicercaEventoWrapper {
	private Long campoIdProvider;
	private String denominazioneLegale;
	private Long campoIdEvento;
	private String titoloEvento;
	
	private Set<Obiettivo> obiettiviNazionaliSelezionati;
	private Set<Obiettivo> obiettiviRegionaliSelezionati;
	private Set<Professione> professioniSelezionate;
	private Set<Disciplina> disciplineSelezionate;
	
	@DateTimeFormat (pattern = "dd/MM/yyyy")
	private LocalDate dataInizioStart;
	@DateTimeFormat (pattern = "dd/MM/yyyy")
	private LocalDate dataInizioEnd;
	
	@DateTimeFormat (pattern = "dd/MM/yyyy")
	private LocalDate dataFineStart;
	@DateTimeFormat (pattern = "dd/MM/yyyy")
	private LocalDate dataFineEnd;
	
	private BigDecimal crediti;
	private String provincia;
	private String comune;
	private String luogo;
	
	private Set<EventoStatoEnum> statiSelezionati;
	@DateTimeFormat (pattern = "dd/MM/yyyy")
	private LocalDate dataScadenzaPagamentoStart;
	@DateTimeFormat (pattern = "dd/MM/yyyy")
	private LocalDate dataScadenzaPagamentoEnd;
	
	//specifici per le varie tipologie di evento
	private Set<ProceduraFormativa> tipologieSelezionate;
	private Set<TipologiaEventoRESEnum> tipologieRES;
	private Set<TipologiaEventoFSCEnum> tipologieFSC;
	private Set<TipologiaEventoFADEnum> tipologieFAD;
	
	private Set<Obiettivo> obiettiviNazionaliList;
	private Set<Obiettivo> obiettiviRegionaliList;
	private Set<Professione> professioniList;
	private Set<Disciplina> disciplineList;
	
	//campo non di ricerca ma il solito id che mettiamo in hidden per gestire il form
	private Long providerId;
	
	
}