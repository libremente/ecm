package it.tredi.ecm.dao.repository;

import java.util.Set;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import it.tredi.ecm.dao.entity.Evento;
import it.tredi.ecm.dao.entity.Pagamento;
import it.tredi.ecm.dao.entity.Provider;
import it.tredi.ecm.dao.entity.QuotaAnnuale;

public interface QuotaAnnualeRepository extends CrudRepository<QuotaAnnuale, Long> {

//	@Query("SELECT distinct p FROM Provider p WHERE p.status IN ('ACCREDITATO_PROVVISORIAMENTE','ACCREDITATO_STANDARD') and p.id NOT IN (SELECT distinct qa.provider.id FROM QuotaAnnuale qa WHERE qa.annoPagamento = :annoPagamento) and qa.pagamento.codiceEsito not in ('PAA_ESEGUITO')")
//	public Set<Provider> findAllProviderNotPagamentoEffettuato(@Param("annoPagamento")Integer annoPagamento);

	public Set<QuotaAnnuale> findAll();
	
	public Set<QuotaAnnuale> findAllByProviderId(Long providerId);
	
	@Query("SELECT p FROM Pagamento p WHERE p.id IN (SELECT qa.pagamento.id FROM QuotaAnnuale qa WHERE qa.pagato = false AND qa.pagInCorso = true)")
	public Set<Pagamento> getPagamentiProviderDaVerificare();
	
	@Query("SELECT p FROM Provider p WHERE p.id IN (SELECT distinct qa.provider.id FROM QuotaAnnuale qa WHERE qa.pagato = false AND qa.pagInCorso = false)")
	public Set<Provider> findAllProviderNotPagamentoEffettuato(@Param("annoPagamento")Integer annoPagamento);
}


// and p.codiceEsito not in ('PAA_ESEGUITO', 'PAA_PAGAMENTO_ANNULLATO', 'PAA_PAGAMENTO_SCADUTO', 'PAA_ENTE_NON_VALIDO', 'PAA_ID_SESSION_NON_VALIDO')")

