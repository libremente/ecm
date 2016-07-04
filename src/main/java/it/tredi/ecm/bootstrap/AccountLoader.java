package it.tredi.ecm.bootstrap;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import it.tredi.ecm.dao.entity.Role;
import it.tredi.ecm.dao.enumlist.RoleEnum;
import it.tredi.ecm.dao.entity.Account;
import it.tredi.ecm.dao.entity.Profile;
import it.tredi.ecm.dao.repository.AccountRepository;
import it.tredi.ecm.dao.repository.ProfileRepository;
import it.tredi.ecm.dao.repository.RoleRepository;

@Component
@org.springframework.context.annotation.Profile("dev")
public class AccountLoader implements ApplicationListener<ContextRefreshedEvent> {

	private final static Logger LOGGER = LoggerFactory.getLogger(AccountLoader.class);
	
	private final RoleRepository roleRepository;
	private final AccountRepository accountRepository;
	private final ProfileRepository profileRepository;
	
	@Autowired
	public AccountLoader(AccountRepository accountRepository, RoleRepository roleRepository, ProfileRepository profileRepository) {
		this.accountRepository = accountRepository;
		this.roleRepository = roleRepository;
		this.profileRepository = profileRepository;
	}
	
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		LOGGER.info("BOOTSTRAP ECM - Initializing ACL...");
		
		Set<Account> accounts = accountRepository.findAll();
			
		if(accounts.isEmpty()){
			Role role_readAccreditamento = new Role();
			role_readAccreditamento.setName(RoleEnum.ACCREDITAMENTO_SHOW.name());
			role_readAccreditamento.setDescription("ACCREDITAMENTO (LETTURA)");
			roleRepository.save(role_readAccreditamento);
			
			Role role_writeAccreditamento = new Role();
			role_writeAccreditamento.setName(RoleEnum.ACCREDITAMENTO_EDIT.name());
			role_writeAccreditamento.setDescription("ACCREDITAMENTO (SCRITTURA)");
			roleRepository.save(role_writeAccreditamento);
			
			Role role_readAllAccreditamento = new Role();
			role_readAllAccreditamento.setName(RoleEnum.ACCREDITAMENTO_SHOW_ALL.name());
			role_readAllAccreditamento.setDescription("ACCREDITAMENTO (LETTURA TUTTI)");
			roleRepository.save(role_readAllAccreditamento);
			
			Role role_readProvider = new Role();
			role_readProvider.setName(RoleEnum.PROVIDER_SHOW.name());
			role_readProvider.setDescription("PROVIDER (LETTURA)");
			roleRepository.save(role_readProvider);
			
			Role role_writeProvider = new Role();
			role_writeProvider.setName(RoleEnum.PROVIDER_EDIT.name());
			role_writeProvider.setDescription("PROVIDER (SCRITTURA)");
			roleRepository.save(role_writeProvider);
			
			Role role_readAllProvider = new Role();
			role_readAllProvider.setName(RoleEnum.PROVIDER_SHOW_ALL.name());
			role_readAllProvider.setDescription("PROVIDER (LETTURA TUTTI)");
			roleRepository.save(role_readAllProvider);
			
			Role role_readUser = new Role();
			role_readUser.setName(RoleEnum.USER_SHOW.name());
			role_readUser.setDescription("UTENTI (LETTURA)");
			roleRepository.save(role_readUser);
			
			Role role_writeUser = new Role();
			role_writeUser.setName(RoleEnum.USER_EDIT.name());
			role_writeUser.setDescription("UTENTI (SCRITTURA)");
			roleRepository.save(role_writeUser);
			
			Role role_readAllUser = new Role();
			role_readAllUser.setName(RoleEnum.USER_SHOW_ALL.name());
			role_readAllUser.setDescription("UTENTI (LETTURA TUTTI)");
			roleRepository.save(role_readAllUser);
			
			Profile profile_provider = new Profile();
			profile_provider.setName("PROVIDER");
			profile_provider.getRoles().add(role_readProvider);
			profile_provider.getRoles().add(role_writeProvider);
			profile_provider.getRoles().add(role_readAccreditamento);
			profile_provider.getRoles().add(role_writeAccreditamento);
			profileRepository.save(profile_provider);
			
			Profile profile_admin = new Profile();
			profile_admin.setName("ADMIN");
			profile_admin.getRoles().add(role_readUser);
			profile_admin.getRoles().add(role_writeUser);
			profile_admin.getRoles().add(role_readAllUser);
			profileRepository.save(profile_admin);
			
			Account provider = new Account();
			provider.setUsername("provider");
			provider.setPassword("$2a$10$JCx8DPs0l0VNFotVGkfW/uRyJzFfc8HkTi5FQy0kpHSpq7W4iP69.");
			//provider.setPassword("admin");
			provider.setEmail("tiommi@3di.it");
			provider.setChangePassword(false);
			provider.setEnabled(true);
			provider.setExpiresDate(null);
			provider.setLocked(false);
			provider.getProfiles().add(profile_provider);
			
			accountRepository.save(provider);
//			try{
//				accountService.save(provider);
//			}catch(Exception ex){
//				ex.printStackTrace();
//			}
			
			Account admin = new Account();
			admin.setUsername("admin");
			admin.setPassword("$2a$10$JCx8DPs0l0VNFotVGkfW/uRyJzFfc8HkTi5FQy0kpHSpq7W4iP69.");
			//admin.setPassword("admin");
			admin.setEmail("dpranteda@3di.it");
			admin.setChangePassword(false);
			admin.setEnabled(true);
			admin.setExpiresDate(null);
			admin.setLocked(false);
			admin.getProfiles().add(profile_admin);
			admin.getProfiles().add(profile_provider);
			
			accountRepository.save(admin);
			
//			try{
//				accountService.save(admin);
//			}catch (Exception ex){
//				ex.printStackTrace();
//			}
		}else{
			LOGGER.info("BOOTSTRAP ECM - Acl not empty...");
		}
	}
}
