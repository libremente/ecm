package it.tredi.ecm;

import java.util.List;

import javax.transaction.Transactional;

import org.apache.commons.lang3.SerializationUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import it.tredi.ecm.dao.entity.AnagraficaEvento;
import it.tredi.ecm.dao.entity.AnagraficaEventoBase;
import it.tredi.ecm.dao.entity.Evento;
import it.tredi.ecm.dao.entity.EventoRES;
//import it.tredi.ecm.dao.entity.AnagraficaFullEvento;
import it.tredi.ecm.dao.entity.PersonaEvento;
//import it.tredi.ecm.dao.entity.PersonaFullEvento;
import it.tredi.ecm.dao.entity.PersonaFullEvento;
import it.tredi.ecm.dao.repository.EventoRepository;
import it.tredi.ecm.dao.repository.PersonaEventoRepository;
import it.tredi.ecm.service.AnagraficaEventoService;
import it.tredi.ecm.service.EventoService;
import it.tredi.ecm.service.ProviderService;
import it.tredi.ecm.web.bean.RicercaEventoWrapper;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
//@ActiveProfiles("dev")
@ActiveProfiles("abarducci")
//@WithUserDetails("provider")
@Rollback(false)
@Ignore
public class EventoTest {

	@Autowired private AnagraficaEventoService anagraficaEventoService;
	@Autowired private PersonaEventoRepository personaEventoRepo;
	@Autowired private EventoService eventoService;
	@Autowired private ProviderService providerService;

	@Autowired private EventoRepository eventoRepository;

	@Test
	@Ignore
	public void loadEvento() throws Exception {
		/*
		PersonaEvento personaEvento = new PersonaEvento();
		PersonaFullEvento personaFullEvento = new PersonaFullEvento();
		AnagraficaFullEvento anagFullEvento = new AnagraficaFullEvento();
		//AnagraficaFullEvento anagFullEvento = new AnagraficaFullEvento();


		System.out.println("loadEvento");
		*/
		//PersonaFullEvento pfev = new PersonaFullEvento();

		Evento evento = eventoRepository.findOneForRiedizione(2356L);

		System.out.println("evento: " + evento.getId());
	}

	@Test
	@Ignore
	public void savePersonaEvento() throws Exception {
		AnagraficaEvento anag = anagraficaEventoService.getAnagraficaEventoByCodiceFiscaleForProvider("aaaa", 413L);
		PersonaEvento pfev = new PersonaEvento(anag);
		pfev.setQualifica("ppp");

		PersonaEvento p = SerializationUtils.clone(pfev);
		personaEventoRepo.save(p);

		System.out.println(p.getId());
	}

	@Test
	@Transactional
	@Ignore
	public void saveEvento() throws Exception {
	//	PersonaEvento p = personaEventoRepo.findOne(4112L);

		PersonaEvento p = new PersonaEvento();
		p.setAnagrafica(new AnagraficaEventoBase());
		p.setId(4112L);
		p.setQualifica("sto cazzo");
		p.getAnagrafica().setStraniero(false);

		EventoRES ev = new EventoRES();
		ev.setProvider(providerService.getProvider(413L));
		//ev.getResponsabili().add(p);
		try{
			personaEventoRepo.save(p);
			eventoService.save(ev);
		}catch (Exception ex){
			ex.printStackTrace();
		}
		System.out.println("fatto");
	}

	@Test
	//@Ignore
	public void cercaEvento() throws Exception {
		RicercaEventoWrapper wrapper = new RicercaEventoWrapper();
		//wrapper.setCampoIdEvento("3699");
		//wrapper.setCampoIdProvider(1224L);
		wrapper.setCampoIdEvento("");
		List<Evento> eventi = eventoService.cerca(wrapper);

		System.out.println("eventi.size(): " + eventi.size());
	}

}
