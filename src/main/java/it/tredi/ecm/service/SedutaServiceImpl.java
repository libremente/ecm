package it.tredi.ecm.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.hibernate.validator.internal.util.privilegedactions.SetAccessibility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.tredi.ecm.dao.entity.Account;
import it.tredi.ecm.dao.entity.Accreditamento;
import it.tredi.ecm.dao.entity.Seduta;
import it.tredi.ecm.dao.entity.Valutazione;
import it.tredi.ecm.dao.entity.ValutazioneCommissione;
import it.tredi.ecm.dao.enumlist.AccreditamentoStatoEnum;
import it.tredi.ecm.dao.enumlist.AccreditamentoTipoEnum;
import it.tredi.ecm.dao.enumlist.ProfileEnum;
import it.tredi.ecm.dao.repository.SedutaRepository;
import it.tredi.ecm.dao.repository.ValutazioneCommissioneRepository;
import it.tredi.ecm.service.bean.EcmProperties;
import it.tredi.ecm.utils.Utils;
import lombok.val;

@Service
public class SedutaServiceImpl implements SedutaService {
	private static Logger LOGGER = LoggerFactory.getLogger(SedutaServiceImpl.class);

	@Autowired private SedutaRepository sedutaRepository;
	@Autowired private ValutazioneCommissioneRepository valutazioneCommissioneRepository;
	@Autowired private AccreditamentoService accreditamentoService;
	@Autowired private EcmProperties ecmProperties;
	@Autowired private WorkflowService workflowService;
	@Autowired private EmailService emailService;
	@Autowired private AccountService accountService;


	@Override
	public void save(Seduta seduta) {
		sedutaRepository.save(seduta);
	}

	@Override
	public Set<Seduta> getAllSedute() {
		return sedutaRepository.findAll();
	}

	@Override
	public Seduta getSedutaById(Long sedutaId) {
		return sedutaRepository.findOne(sedutaId);
	}

	@Override
	public void removeSedutaById(Long sedutaId) {
		sedutaRepository.delete(sedutaId);
	}

	//se la data non antecedente ad adesso + 30 minuti, posso editare / cancellare la seduta
	@Override
	public boolean canEditSeduta(Seduta seduta) {
		if(seduta.isNew() || seduta.getData().isAfter(LocalDate.now()) || (seduta.getData().isEqual(LocalDate.now()) && seduta.getOra().isAfter(LocalTime.now().plusMinutes(ecmProperties.getSedutaValidationMinutes()))))
			return true;
		else return false;
	}

	//se la data è passata e NON è già stata valutata la seduta può essere valutata
	@Override
	public boolean canBeEvaluated(Seduta seduta) {
		if(seduta.isEseguitoTaskInsOdgAccreditamenti() && !seduta.isNew() &&
				(seduta.getData().isBefore(LocalDate.now()) || (seduta.getData().isEqual(LocalDate.now()) && seduta.getOra().isBefore(LocalTime.now()))) &&
				!seduta.isLocked())
			return true;
		else return false;
	}

	//controllo se la seduta può essere bloccata
	@Override
	public boolean canBeLocked(Seduta seduta) {
		if (!(seduta.getValutazioniCommissione() == null) && !seduta.getValutazioniCommissione().isEmpty() && !seduta.isLocked()) {
			boolean value = true;
			//Controllo se sono state date le valutazioni a tutte le domande
			for (ValutazioneCommissione vc : seduta.getValutazioniCommissione()) {
				if (vc.getStato() == null || vc.getValutazioneCommissione() == null || vc.getValutazioneCommissione().isEmpty())
					value = false;
			}
			return value;
		}
		else return false;
	}

	@Override
	public boolean canBeRemoved(Long sedutaId) {
		Seduta seduta = sedutaRepository.findOne(sedutaId);
		return (seduta.getValutazioniCommissione() == null || seduta.getValutazioniCommissione().isEmpty());
	}

	@Override
	public Set<Accreditamento> getAccreditamentiInSeduta(Long sedutaId) {
		Seduta seduta = sedutaRepository.findOne(sedutaId);
		Set<Accreditamento> result = new HashSet<Accreditamento>();
		for (ValutazioneCommissione vc : seduta.getValutazioniCommissione()) {
			result.add(vc.getAccreditamento());
		}
		return result;
	}

	@Override
	public Set<Seduta> getAllSeduteAfter(LocalDate date, LocalTime time) {
		Set<Seduta> sedute = sedutaRepository.findAllByDataAndOraAceptable(date, time);
		return sedute;
	}

	@Override
	public void moveValutazioneCommissione(ValutazioneCommissione val, Seduta from, Seduta to) throws Exception {
		//rimuove stato e commento sulla valutazione
		val.setStato(null);
		val.setValutazioneCommissione(null);
		//sposta la valutazione
		Set<ValutazioneCommissione> valutazioniFrom = from.getValutazioniCommissione();
		Set<ValutazioneCommissione> valutazioniTo = to.getValutazioniCommissione();
		valutazioniFrom.remove(val);
		valutazioniTo.add(val);
		from.setValutazioniCommissione(valutazioniFrom);
		to.setValutazioniCommissione(valutazioniTo);
		val.setSeduta(to);
		sedutaRepository.save(from);
		sedutaRepository.save(to);
		valutazioneCommissioneRepository.save(val);

		//se la domanda era in valutazione commissione e setto lo stato in ins_odg
		Accreditamento accreditamento = val.getAccreditamento();
		if(accreditamento.isValutazioneCommissione() || accreditamento.isValutazioneCommissioneVariazioneDati())
			workflowService.eseguiTaskInserimentoEsitoOdgForCurrentUser(accreditamento, AccreditamentoStatoEnum.INS_ODG);
	}

	@Override
	//@Transactional //TODO ANDREABBE MESSO?
	//Corrisponde al task Inserimento Esito ODG
	public void chiudiSeduta(Long sedutaId) throws Exception {
		Seduta seduta =  sedutaRepository.findOne(sedutaId);
		for(ValutazioneCommissione val : seduta.getValutazioniCommissione()){
			if(val.getAccreditamento().getStato() == AccreditamentoStatoEnum.VALUTAZIONE_COMMISSIONE || val.getAccreditamento().getStatoVariazioneDati() == AccreditamentoStatoEnum.VALUTAZIONE_COMMISSIONE)
				accreditamentoService.inviaValutazioneCommissione(seduta, val.getAccreditamento().getId(), val.getStato());
		}

		if(canBeLocked(seduta)) {
			seduta.setLocked(true);
			sedutaRepository.save(seduta);
		}else throw new Exception("Seduta non bloccabile");

	}

	@Override
	public Map<Long, Set<AccreditamentoStatoEnum>> prepareMappaStatiValutazione(Seduta seduta) throws Exception {
		Map<Long, Set<AccreditamentoStatoEnum>> mappa = new HashMap<Long, Set<AccreditamentoStatoEnum>>();
		for (ValutazioneCommissione vc : seduta.getValutazioniCommissione()) {
			Set<AccreditamentoStatoEnum> value = new HashSet<AccreditamentoStatoEnum>();

			List<AccreditamentoStatoEnum> possibiliStati = workflowService.getInserimentoEsitoOdgStatiPossibiliAccreditamento(vc.getAccreditamento().getWorkflowInCorso().getProcessInstanceId());
			value.addAll(possibiliStati);

			value.remove(AccreditamentoStatoEnum.INS_ODG);

			mappa.put(vc.getAccreditamento().getId(), value);
		}
		return mappa;
	}

	@Override
	public void addValutazioneCommissioneToSeduta(String motivazioneDaInserire, Long idAccreditamentoDaInserire, Seduta seduta) {
		ValutazioneCommissione valutazioneDaInserire = new ValutazioneCommissione();
		valutazioneDaInserire.setOggettoDiscussione(motivazioneDaInserire);
		valutazioneDaInserire.setAccreditamento(accreditamentoService.getAccreditamento(idAccreditamentoDaInserire));
		valutazioneDaInserire.setSeduta(seduta);
		Set<ValutazioneCommissione> setValutazioni = seduta.getValutazioniCommissione();
		setValutazioni.add(valutazioneDaInserire);
		seduta.setValutazioniCommissione(setValutazioni);
		valutazioneCommissioneRepository.save(valutazioneDaInserire);
		sedutaRepository.save(seduta);
	}

	@Override
	public void removeValutazioneCommissioneFromSeduta(Long valutazioneCommissioneId) {
		ValutazioneCommissione valutazioneDaEliminare = valutazioneCommissioneRepository.findOne(valutazioneCommissioneId);

		valutazioneCommissioneRepository.delete(valutazioneDaEliminare);
	}

	//cerca nel DB la prossima Seduta (a partire da oggi)
	@Override
	public Seduta getNextSeduta() {
		LocalDate oggi = LocalDate.now();
		Seduta prossimaSedutaOggi = sedutaRepository.findFirstByDataAndOraAfterOrderByOraAsc(oggi, LocalTime.now());
		if (prossimaSedutaOggi != null)
			return prossimaSedutaOggi;
		else
			return sedutaRepository.findFirstByDataAfterOrderByDataAsc(oggi);
	}

	@Override
	public void inviaMailACommissioneEcm() throws Exception {
		LOGGER.debug(Utils.getLogMessage("Inio email ai componenti della Commissione ECM"));
		Set<String> commissioneECM = accountService.getEmailByProfileEnum(ProfileEnum.COMMISSIONE);
		emailService.inviaConvocazioneACommissioneECM(commissioneECM);
	}

	@Override
	public void bloccaSeduta(Long sedutaId) throws Exception {
		Seduta seduta = sedutaRepository.findOne(sedutaId);
		bloccaSeduta(seduta);
	}

	@Override
	@Transactional
	public void eseguiBloccoSeduteDaBloccare() {
		//Ricavo la lista delle sedute da bloccare
		//Occorre settare la seduta come
		LocalDateTime nowPlusSedutaValidationMinute = LocalDateTime.now().plusMinutes(ecmProperties.getSedutaValidationMinutes());
		Set<Seduta> sedute = sedutaRepository.findSeduteDaBloccare(nowPlusSedutaValidationMinute);
		for(Seduta seduta : sedute) {
			this.bloccaSeduta(seduta);
		}
	}

	private void bloccaSeduta(Seduta seduta) {
		//Set<Accreditamento> listaInOdg = getAccreditamentiInSeduta(seduta.getId());
		Set<Accreditamento> listaInOdg = new HashSet<Accreditamento>();
		for(ValutazioneCommissione vc : seduta.getValutazioniCommissione())
			listaInOdg.add(vc.getAccreditamento());
		boolean error = false;
		for(Accreditamento a : listaInOdg){
			//Processo solo gli accreditamenti nello stato corretto perche' si potrebbe verificare un errore su un accreditamento e non verificarsi su un altro
			//quindi il secondo cambierebbe restituendo errore per tutte le chiamate successive rendendo impossibile eseguire il bloccaSeduta
			if(
					(a.getTipoDomanda() == AccreditamentoTipoEnum.PROVVISORIO && (a.getStato() == AccreditamentoStatoEnum.INS_ODG || a.getStatoVariazioneDati() == AccreditamentoStatoEnum.INS_ODG))
					||
					//TODO controllare se anche per la standard lo stato e' INS_ODG e quindi il metodo da chiamare resta inserisciInValutazioneCommissioneForCurrentUser
					(a.getTipoDomanda() == AccreditamentoTipoEnum.STANDARD && (a.getStato() == AccreditamentoStatoEnum.INS_ODG || a.getStatoVariazioneDati() == AccreditamentoStatoEnum.INS_ODG)))
			{
				try {
					accreditamentoService.inserisciInValutazioneCommissioneForSystemUser(a.getId());
				} catch (Exception ex) {
					error = true;
				}
			}
		}
		if(!error) {
			//Indichiamo che e' stato eseguito il task InsOdg su tutti gli accreditamenti in seduta
			seduta.setEseguitoTaskInsOdgAccreditamenti(true);
			sedutaRepository.save(seduta);
		}
	}
}
