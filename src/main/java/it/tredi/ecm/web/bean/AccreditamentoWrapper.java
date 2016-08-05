package it.tredi.ecm.web.bean;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import it.tredi.ecm.dao.entity.Account;
import it.tredi.ecm.dao.entity.Accreditamento;
import it.tredi.ecm.dao.entity.DatiAccreditamento;
import it.tredi.ecm.dao.entity.FieldEditabileAccreditamento;
import it.tredi.ecm.dao.entity.Persona;
import it.tredi.ecm.dao.entity.PianoFormativo;
import it.tredi.ecm.dao.entity.Professione;
import it.tredi.ecm.dao.entity.Provider;
import it.tredi.ecm.dao.entity.Sede;
import it.tredi.ecm.dao.enumlist.FileEnum;
import it.tredi.ecm.dao.enumlist.IdFieldEnum;
import it.tredi.ecm.dao.enumlist.SubSetFieldEnum;
import it.tredi.ecm.utils.Utils;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccreditamentoWrapper {

	//dati domanda di accreditamento
	private Accreditamento accreditamento;

	//dati provider
	private Provider provider;

	private Sede sedeLegale;
	private Sede sedeOperativa;

	private Persona legaleRappresentante;
	private Persona delegatoLegaleRappresentante;

	//dati domanda accreditamento
	private DatiAccreditamento datiAccreditamento;

	//dati anagrafiche interne provider
	private Persona responsabileSegreteria;
	private Persona responsabileAmministrativo;
	private Persona coordinatoreComitatoScientifico;
	private List<Persona> componentiComitatoScientifico = new ArrayList<Persona>();
	private Persona responsabileSistemaInformatico;
	private Persona responsabileQualita;

	/*	flag per stato avanzamento domanda	*/
	private boolean providerStato;
	private boolean sedeLegaleStato;
	private boolean sedeOperativaStato;
	private boolean legaleRappresentanteStato;
	private boolean delegatoLegaleRappresentanteStato;

	private boolean datiAccreditamentoStato;

	private boolean responsabileSegreteriaStato;
	private boolean responsabileAmministrativoStato;
	private boolean responsabileSistemaInformaticoStato;
	private boolean responsabileQualitaStato;

	private boolean comitatoScientificoStato;
	private String comitatoScientificoErrorMessage;

	private boolean attoCostitutivoStato;
	private boolean esperienzaFormazioneStato;
	private boolean utilizzoStato;
	private boolean sistemaInformaticoStato;
	private boolean pianoQualitaStato;
	private boolean dichiarazioneLegaleStato;
	private boolean dichiarazioneEsclusioneStato;

	private boolean sezione1Stato;
	private boolean sezione2Stato;
	private boolean sezione3Stato;
	private boolean sezione4Stato;

	//stati per i pulsanti segreteria
	private boolean canPrendiInCarica;
	private boolean canValutaDomanda;
	private boolean canAssegnaNuovoGruppo;
	private boolean canConfermaValutazione;

	//Piano Formativo
	private PianoFormativo pianoFormativo;


	public AccreditamentoWrapper(){};
	public AccreditamentoWrapper(Accreditamento accreditamento){
		this.setAccreditamento(accreditamento);

		// PROVIDER
		this.setProvider(accreditamento.getProvider());

		//SEDE LEGALE
		Sede sede = accreditamento.getProvider().getSedeLegale();
		this.setSedeLegale( sede != null ? sede : new Sede());

		//SEDE OPERATIVA
		sede = accreditamento.getProvider().getSedeOperativa();
		this.setSedeOperativa( sede != null ? sede : new Sede());

		//DATI ACCREDITAMENTO
		DatiAccreditamento datiAccreditamento = accreditamento.getDatiAccreditamento();
		this.setDatiAccreditamento(datiAccreditamento != null ? datiAccreditamento : new DatiAccreditamento());

		// LEGALE RAPPRESENTANTE E RESPONSABILI
		for(Persona p : accreditamento.getProvider().getPersone()){
			if(p.isLegaleRappresentante())
				this.setLegaleRappresentante(p);
			else if(p.isDelegatoLegaleRappresentante())
				this.setDelegatoLegaleRappresentante(p);
			else if(p.isResponsabileSegreteria())
				this.setResponsabileSegreteria(p);
			else if(p.isResponsabileAmministrativo())
				this.setResponsabileAmministrativo(p);
			else if(p.isResponsabileSistemaInformatico())
				this.setResponsabileSistemaInformatico(p);
			else if(p.isResponsabileQualita())
				this.setResponsabileQualita(p);
			else if(p.isCoordinatoreComitatoScientifico())
				this.setCoordinatoreComitatoScientifico(p);
			else if(p.isComponenteComitatoScientifico())
				this.getComponentiComitatoScientifico().add(p);
		}
	}




	public void checkStati(int numeroComponentiComitatoScientifico, int numeroProfessionistiSanitarie, Set<Professione> elencoProfessioniDeiComponenti, int professioniDeiComponentiAnaloghe,Set<String> filesDelProvider,String mode){
		//TODO migliorare la logica per evitare di fare troppi if
		// ad esempio inizializzare gli stati a true e poi ad ogni controllo se fallisce si mette il false sia allo stato che al valid
		// cosi facendo valid è settato in automatico senza rifare tutti i controlli
		//
		//NON lo faccio adesso perchè voglio capire in fase di validazione della domanda come gestiremo i vari stati

		//check sulla modalità di visualizzazione
		if(mode.equals("show") || mode.equals("edit")) {
			providerStato = (provider.getRagioneSociale()!= null) ? true : false;

			sedeLegaleStato = (sedeLegale != null && !sedeLegale.isNew()) ? true : false;
			sedeOperativaStato = (sedeOperativa != null && !sedeOperativa.isNew()) ? true : false;

			//check sul cv unico campo non settabile in registrazione o in modifica delle anagrafiche, ma solo durante la domanda di accreditamento
			legaleRappresentanteStato = (legaleRappresentante != null && !legaleRappresentante.isNew() && legaleRappresentante.getAnagrafica().getCellulare() != null && !legaleRappresentante.getAnagrafica().getCellulare().isEmpty()) ? true : false;
			delegatoLegaleRappresentanteStato = (delegatoLegaleRappresentante != null && !delegatoLegaleRappresentante.isNew() && delegatoLegaleRappresentante.getAnagrafica().getCellulare() != null && !delegatoLegaleRappresentante.getAnagrafica().getCellulare().isEmpty()) ? true : false;

			datiAccreditamentoStato = (datiAccreditamento != null && !datiAccreditamento.isNew()) ? true : false;

			responsabileSegreteriaStato = (responsabileSegreteria != null && !responsabileSegreteria.isNew()) ? true : false;
			responsabileAmministrativoStato = (responsabileAmministrativo != null && !responsabileAmministrativo.isNew()) ? true : false;
			responsabileSistemaInformaticoStato = (responsabileSistemaInformatico != null && !responsabileSistemaInformatico.isNew()) ? true : false;
			responsabileQualitaStato = (responsabileQualita != null && !responsabileQualita.isNew()) ? true : false;

			checkComitatoScientifico_fromDB(numeroComponentiComitatoScientifico, numeroProfessionistiSanitarie, elencoProfessioniDeiComponenti, professioniDeiComponentiAnaloghe);
			setFilesStato(filesDelProvider);

			sezione1Stato = (providerStato && sedeLegaleStato && sedeOperativaStato && legaleRappresentanteStato && datiAccreditamentoStato) ? true : false;
			sezione2Stato = (responsabileSegreteriaStato && responsabileAmministrativoStato && responsabileSistemaInformaticoStato && responsabileQualitaStato && comitatoScientificoStato) ? true : false;
			sezione3Stato = (attoCostitutivoStato && esperienzaFormazioneStato && utilizzoStato && sistemaInformaticoStato && pianoQualitaStato && dichiarazioneLegaleStato) ? true : false;
		}
		if(mode.equals("validate")) {
			//TODO logica validazione stato
			providerStato = false;
			sedeLegaleStato = false;
			sedeOperativaStato = false;
			legaleRappresentanteStato = false;
			delegatoLegaleRappresentanteStato = false;

			datiAccreditamentoStato = false;

			responsabileSegreteriaStato = false;
			responsabileAmministrativoStato = false;
			responsabileSistemaInformaticoStato = false;
			responsabileQualitaStato = false;

			attoCostitutivoStato = false;
			esperienzaFormazioneStato = false;
			utilizzoStato = false;
			sistemaInformaticoStato = false;
			pianoQualitaStato = false;
			dichiarazioneLegaleStato = false;
			dichiarazioneEsclusioneStato = false;

			//sezioni validate o meno
			sezione1Stato = (providerStato && sedeLegaleStato && sedeOperativaStato && legaleRappresentanteStato && datiAccreditamentoStato) ? true : false;
			sezione2Stato = (responsabileSegreteriaStato && responsabileAmministrativoStato && responsabileSistemaInformaticoStato && responsabileQualitaStato && comitatoScientificoStato) ? true : false;
			sezione3Stato = (attoCostitutivoStato && esperienzaFormazioneStato && utilizzoStato && sistemaInformaticoStato && pianoQualitaStato && dichiarazioneLegaleStato) ? true : false;
			sezione4Stato = false;
		}
	}

	public void checkSegreteriaButtons(){
		//pulsante prendi in carica TODO sostituire con logica query Bonita
		if(accreditamento.isValutazioneSegreteriaAssegnamento()) {
			canPrendiInCarica = true;
		}

		//pulsante assegna nuovo gruppo crecm TODO CONTROLLARE!
		if(accreditamento.getGruppoCrecm() == null) {
			canAssegnaNuovoGruppo = true;
		}
	}

	public void checkCanValidate(Account user){
		//pulsante valuta domanda TODO CONTROLLARE!
		if(accreditamento.getTeamValutazione() != null &&
			accreditamento.getGruppoCrecm() != null &&
			(accreditamento.getTeamValutazione().getComponentiSegreteria().contains(user) ||
				accreditamento.getGruppoCrecm().getValutatori().contains(user))) {
			canValutaDomanda = true;
		}
	}

	//mi trovo accreditamentoValidate quindi gli stati rappresentano l'avanzamento della valutazione
	public void checkConfirmValidate(){
		//pulsante invia valutazione domanda TODO CONTROLLARE!
		if(sezione1Stato && sezione2Stato && sezione3Stato && sezione4Stato) {
			canConfermaValutazione = true;
		}
	}

	/*
	 * [A] Almeno 5 componenti (coordinatore incluso)
	 * [B] Almeno 5 professionisti Sanitari
	 * [C] Se settato "Generale" -> Almeno 2 professioni diverse tra i componenti
	 * [D] Se settato "Settoriale" -> Almeno 2 professioni analoghe a quelle selezionate (almeno che non sia stata selezionate solo 1 professione)
	 * [D-bis] Se Selezionata solo 1 professione -> deve conicidere con quella e UNICA dei componenti del comitato scientifico
	 * */
	public void checkComitatoScientifico_fromDB(int numeroComponentiComitatoScientifico, int numeroProfessionistiSanitari, Set<Professione> elencoProfessioniDeiComponenti, int professioniDeiComponentiAnaloghe){
		comitatoScientificoStato = true;

		int professioniDeiComponenti = elencoProfessioniDeiComponenti.size();

		//[A]
		if(numeroComponentiComitatoScientifico < 4 || (coordinatoreComitatoScientifico == null || coordinatoreComitatoScientifico.isNew())){
			comitatoScientificoStato = false;
			comitatoScientificoErrorMessage = "error.numero_minimo_comitato";
		}//[B]
		else if(numeroProfessionistiSanitari < 5){
			comitatoScientificoStato = false;
			comitatoScientificoErrorMessage = "error.numero_minimo_professionisti_sanitari";
		}else{
			// mi assicuro che sono stati gia' inseriti i dati relativi all'accreditamento
			if(datiAccreditamentoStato){
				//[C]
				if(datiAccreditamento.getProfessioniAccreditamento().equalsIgnoreCase("generale")){
					if(professioniDeiComponenti < 2){
						comitatoScientificoStato = false;
						comitatoScientificoErrorMessage = "error.numero_minimo_professioni";
					}
				}//[D]
				else{
					if(datiAccreditamento.getProfessioniSelezionate().size() > 1){
						if(professioniDeiComponentiAnaloghe < 2){
							comitatoScientificoStato = false;
							comitatoScientificoErrorMessage = "error.numero_minimo_professioni_settoriale";
						}
					}//[D - bis]
					else{
						if(elencoProfessioniDeiComponenti.size() > 1 || elencoProfessioniDeiComponenti.size() == 0){
							comitatoScientificoStato = false;
							comitatoScientificoErrorMessage = "error.professioni_non_conformi_con_accreditamento";
						}else{
							if(elencoProfessioniDeiComponenti.iterator().next() != datiAccreditamento.getProfessioniSelezionate().iterator().next()){
								comitatoScientificoStato = false;
								comitatoScientificoErrorMessage = "error.professioni_non_conformi_con_accreditamento";
							}
						}
					}
				}
			}else{
				comitatoScientificoStato = false;
				comitatoScientificoErrorMessage = "error.datiaccreditamento_mancanti";
			}
		}
	}

	private void setFilesStato(Set<String> filesDelProvider){
		if(filesDelProvider.contains(FileEnum.FILE_ATTO_COSTITUTIVO))
			attoCostitutivoStato = true;
		if(filesDelProvider.contains(FileEnum.FILE_ESPERIENZA_FORMAZIONE))
			esperienzaFormazioneStato = true;
		if(filesDelProvider.contains(FileEnum.FILE_UTILIZZO))
			utilizzoStato = true;
		if(filesDelProvider.contains(FileEnum.FILE_SISTEMA_INFORMATICO))
			sistemaInformaticoStato = true;
		if(filesDelProvider.contains(FileEnum.FILE_PIANO_QUALITA))
			pianoQualitaStato = true;
		if(filesDelProvider.contains(FileEnum.FILE_DICHIARAZIONE_LEGALE))
			dichiarazioneLegaleStato = true;
		if(filesDelProvider.contains(FileEnum.FILE_DICHIARAZIONE_ESCLUSIONE))
			dichiarazioneEsclusioneStato = true;
	}

	public boolean isComitatoScientificoEditabile(){
		Set<IdFieldEnum> ids = Utils.getSubsetOfIdFieldEnum(new HashSet<FieldEditabileAccreditamento>(getAccreditamento().getIdEditabili()), SubSetFieldEnum.COMITATO_SCIENTIFICO);
		if(ids.isEmpty())
			return false;
		else
			return true;
	}

	//la domanda è stata compilata in tutte le sue parti (tutti i flag sono TRUE)
	public boolean isCompleta(){
		if(sezione1Stato && sezione2Stato && sezione3Stato)
			return true;
		else
			return false;
	}

	//Inserisci piano formativo e blocca idEditabili
	public boolean isCanInsertPianoFormativo(){
		if(accreditamento.isBozza() && isCompleta() && !accreditamento.hasPianoFormativo())
			return true;
		else
			return false;
	}

	//Invia domanda alla segreteria cambiando stato all'accreditamento e rendendo la domanda non più modificabile
	public boolean isCanSend(){
		if(accreditamento.isBozza() && isCompleta() && isPianoFormativoCompleto())
			return true;
		else
			return false;
	}

	//ci sono eventi inseriti nel piano formativo
	private boolean isPianoFormativoCompleto(){
		return ((accreditamento.getPianoFormativo() != null) && !accreditamento.getPianoFormativo().getEventi().isEmpty());
	}

	public boolean isCanInsertEventoInPianoFormativo(){
		return (accreditamento.hasPianoFormativo());
		//return (accreditamento.hasPianoFormativo() && accreditamento.isEditabile());
		//TODO controllo FieldEditabileAccreditamento
	}
}
