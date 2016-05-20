package it.tredi.ecm.service;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.tredi.ecm.dao.entity.Provider;
import it.tredi.ecm.dao.entity.Sede;
import it.tredi.ecm.dao.enumlist.Costanti;
import it.tredi.ecm.dao.repository.SedeRepository;

@Service
public class SedeServiceImpl implements SedeService{
	private static Logger LOGGER = LoggerFactory.getLogger(SedeServiceImpl.class);
	
	@Autowired
	private SedeRepository sedeRepository;
	@Autowired
	private ProviderService providerService;

	@Override
	public Sede getSede(Long id) {
		LOGGER.debug("Recupero Sede: " + id);
		return sedeRepository.findOne(id);
	}
	
	@Override
	@Transactional
	public void save(Sede sede) {
		LOGGER.debug("Salvataggio Sede");
		sedeRepository.save(sede);
	}
	
	@Override
	@Transactional
	public void save(Sede sede, Provider currentProvider, String tipologiaSede) {
		save(sede);
		
		if(tipologiaSede.equals(Costanti.SEDE_LEGALE))
			currentProvider.setSedeLegale(sede);
		else if(tipologiaSede.equals(Costanti.SEDE_OPERATIVA))
			currentProvider.setSedeOperativa(sede);
		
		providerService.save(currentProvider);
	}
}
