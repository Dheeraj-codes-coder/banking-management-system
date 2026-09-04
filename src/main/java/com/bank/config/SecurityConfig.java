package com.bank.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.bank.entity.Customer;
import com.bank.repository.CustomerRepository;

@Configuration
public class SecurityConfig {

	@Autowired
	private CustomerRepository customerRepository;

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	UserDetailsService userDetailsService() {
		return email -> {
			Customer customer = customerRepository.findByEmail(email)
					.orElseThrow(() -> new UsernameNotFoundException("Customer not found"));
			return User.withUsername(customer.getEmail())
					.password(customer.getPassword())
					.roles(customer.getRole())
					.build();
		};
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable());
		http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
		http.authorizeHttpRequests(request -> request
				.requestMatchers("/", "/*.html",
						"/css/**", "/js/**", "/favicon.ico", "/customer/save", "/error", "/openapi.yaml")
				.permitAll()
				.requestMatchers("/customer/fetch", "/customer/find/**", "/customer/update/**",
						"/customer/delete/**", "/account/save/**", "/account/fetch", "/account/find/**",
						"/account/customer/**", "/account/balance/**", "/account/deposit/**",
						"/account/withdraw/**", "/account/transfer/**", "/transaction/fetch",
						"/transaction/account/**")
				.hasRole("ADMIN")
				.anyRequest().authenticated());
		http.httpBasic(Customizer.withDefaults());
		return http.build();
	}
}
