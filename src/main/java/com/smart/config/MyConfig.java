package com.smart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class MyConfig {
	
	@Bean
	public UserDetailsService getUserDetailsService() {

		
		return new UserDetailsServiceimpl();
   }
	
  
   
   //configure methos
   
   @Bean
   public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
	   
	   
	   httpSecurity.authenticationProvider(daoAuthenticationProvider());
       httpSecurity
               .authorizeRequests(requests -> requests
                       .requestMatchers("/admin/**").hasRole("ADMIN")
                       .requestMatchers("/user/**").hasRole("USER")
                       .requestMatchers("/**").permitAll())
               .formLogin(login -> login.loginPage("/signin")
                       .defaultSuccessUrl("/user/index"))
               .csrf(csrf -> csrf.disable());
	   
	   DefaultSecurityFilterChain defaultSecurityFilterChain= httpSecurity.build();
	   
	   return defaultSecurityFilterChain;
   }
   
   @Bean
   public BCryptPasswordEncoder passwordEncoder() {
	   return new BCryptPasswordEncoder();
   }
   
   @Bean
   public AuthenticationManager authenticationManagerBean(AuthenticationConfiguration configuration) throws Exception{
	   return configuration.getAuthenticationManager();
   }
   
   @Bean
   public DaoAuthenticationProvider daoAuthenticationProvider() {
	   
	   DaoAuthenticationProvider provider= new DaoAuthenticationProvider();
	   provider.setUserDetailsService(this.getUserDetailsService());
	   provider.setPasswordEncoder(passwordEncoder());
	   return provider;
   }
   
	
}