package com.bank.config;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.bank.entity.Customer;
import com.bank.repository.CustomerRepository;

@Component
public class AdminDataLoader implements CommandLineRunner {

	@Autowired
	private CustomerRepository customerRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Value("${bank.admin.email}")
	private String adminEmail;

	@Value("${bank.admin.password}")
	private String adminPassword;

	@Override
	public void run(String... args) throws Exception {
		Optional<Customer> optional = customerRepository.findByEmail(adminEmail);
		if (!optional.isPresent()) {
			Customer admin = new Customer();
			admin.setName("Bank Admin");
			admin.setEmail(adminEmail);
			admin.setPhone(9999999999L);
			admin.setAddress("Bank Office");
			admin.setPassword(passwordEncoder.encode(adminPassword));
			admin.setRole("ADMIN");
			customerRepository.save(admin);
		}
	}
}
