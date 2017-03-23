package it.tredi.ecm.dao.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

import org.javers.core.metamodel.annotation.DiffIgnore;
import org.javers.core.metamodel.annotation.TypeName;

import it.tredi.ecm.dao.enumlist.ProgettiDiMiglioramentoFasiDaInserireFSCEnum;
import it.tredi.ecm.dao.enumlist.TipologiaEventoFSCEnum;
import it.tredi.ecm.dao.enumlist.TipologiaGruppoFSCEnum;
import it.tredi.ecm.dao.enumlist.VerificaApprendimentoFSCEnum;
import it.tredi.ecm.dao.enumlist.VerificaPresenzaPartecipantiEnum;
import lombok.Getter;
import lombok.Setter;

@TypeName("EventoFSC")
@Entity
@Getter
@Setter
@DiscriminatorValue(value = "FSC")
public class EventoFSC extends Evento{
	@Embedded
	private SedeEvento sedeEvento;

	@Enumerated(EnumType.STRING)
	@Column(name="tipologia_evento_fsc")
	private TipologiaEventoFSCEnum tipologiaEventoFSC;
	@Enumerated(EnumType.STRING)
	private TipologiaGruppoFSCEnum tipologiaGruppo;

	@Enumerated(EnumType.STRING)
	private ProgettiDiMiglioramentoFasiDaInserireFSCEnum fasiDaInserire;

	private Boolean sperimentazioneClinica;
	private Boolean ottenutoComitatoEtico;

	@Column(columnDefinition = "text")
	private String descrizioneProgetto;

	@ElementCollection
	@Enumerated(EnumType.STRING)
	private Set<VerificaApprendimentoFSCEnum> verificaApprendimento;

	@ElementCollection
	@Enumerated(EnumType.STRING)
	private Set<VerificaPresenzaPartecipantiEnum> verificaPresenzaPartecipanti;

	@Column(columnDefinition = "text")
	private String indicatoreEfficaciaFormativa;

	private String fasiAzioniRuoliJson;

	@OneToMany(mappedBy="evento", cascade=CascadeType.ALL, orphanRemoval=true)
	private List<FaseAzioniRuoliEventoFSCTypeA> fasiAzioniRuoli = new ArrayList<FaseAzioniRuoliEventoFSCTypeA>();

	@ElementCollection
	private List<RiepilogoRuoliFSC> riepilogoRuoli = new ArrayList<RiepilogoRuoliFSC>();

	@DiffIgnore
	private Integer numeroTutor;

}
