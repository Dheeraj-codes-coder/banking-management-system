package com.bank.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.bank.entity.Customer;
import com.bank.repository.CustomerRepository;

@ExtendWith(MockitoExtension.class)
public class CustomerControllerTest {

	@Mock
	private CustomerRepository customerRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@InjectMocks
	private CustomerController customerController;

	@Test
	public void saveCustomerTest() {
		Customer customer = new Customer();
		customer.setName("Dheeraj");
		customer.setEmail("dheeraj@gmail.com");
		customer.setPhone(9876543210L);
		customer.setAddress("Kalaburagi");
		customer.setPassword("password");

		when(customerRepository.findByEmail(customer.getEmail())).thenReturn(Optional.empty());
		when(passwordEncoder.encode("password")).thenReturn("encoded-password");

		String result = customerController.saveCustomer(customer);

		assertEquals("Customer saved successfully", result);
		assertEquals("encoded-password", customer.getPassword());
		assertEquals("USER", customer.getRole());
		verify(customerRepository).save(customer);
	}
}
