package it.tredi.ecm.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/*
 * Sostituito da MultiHttpSecurityConfig
 * che gestisce sia  Security CasAuthentication che UsernamePasswordAuthentication
 *
 * */
//@Configuration
//@EnableGlobalMethodSecurity(prePostEnabled = true)
//@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private UserDetailsService userDetailsService;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
		 .antMatchers("/", "/bootstrap/**", "/images/**", "/crlcu-multiselect/**", "/gentella/**", "/engineering/**", "/clockpicker/**", "/shared/**", "/main", "/providerRegistration", "/confirmRegistration", "/user/resetPassword", "/file/upload", "/spinJS/**", "/backToTop/**", "/bootstrapSelect/**", "/workflow/**", "/cas/**","/bugfix/**").permitAll()

		 .antMatchers("/admin/**").hasAuthority("ADMIN")
         .anyRequest().authenticated()
         .and()
			 .formLogin()
			 	.loginPage("/login").failureUrl("/login?error")
			 	.usernameParameter("username").passwordParameter("password")
			 	.permitAll()
         .and()
	         .logout()
	         	.logoutUrl("/logout").logoutSuccessUrl("/")
	         	.deleteCookies("remember-me")
	         	.permitAll()
	     .and()
	     	.exceptionHandling().accessDeniedPage("/403")
	     .and()
	     	.rememberMe();

		http.csrf().ignoringAntMatchers("/engineering/firma/back")
	     .and()
	     	.headers().frameOptions().sameOrigin();

		//http.addFilterAfter(new UserChangePasswordCheckFilter(), FilterSecurityInterceptor.class);
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth
			.userDetailsService(userDetailsService)
			.passwordEncoder(new BCryptPasswordEncoder());
	}

	@Override
	public void configure(WebSecurity web) throws Exception {
		 web.ignoring().antMatchers("/", "/images/**", "/gentella/**", "/bootstrap/**", "/shared/**", "/main", "/spinJS/**", "/backToTop/**", "/bootstrapSelect/**");
	}
}
