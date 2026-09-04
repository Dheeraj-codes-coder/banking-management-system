package com.bank.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.bank.entity.Customer;
import com.bank.repository.CustomerRepository;

import jakarta.validation.Valid;

@RestController
public class CustomerController {

	@Autowired
	private CustomerRepository customerRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@PostMapping("/customer/save")
	public String saveCustomer(@Valid @RequestBody Customer customer) {
		Optional<Customer> optional = customerRepository.findByEmail(customer.getEmail());
		if (optional.isPresent()) {
			return "Customer email already exists";
		}
		customer.setPassword(passwordEncoder.encode(customer.getPassword()));
		customer.setRole("USER");
		customerRepository.save(customer);
		return "Customer saved successfully";
	}

	@GetMapping("/customer/fetch")
	public List<Customer> fetchCustomers() {
		return customerRepository.findAll();
	}

	@GetMapping("/customer/find/{customerId}")
	public Object findCustomer(@PathVariable int customerId) {
		Optional<Customer> optional = customerRepository.findById(customerId);
		if (optional.isPresent()) {
			return optional.get();
		}
		return "Customer not found";
	}

	@PatchMapping("/customer/update/{customerId}")
	public String updateCustomer(@PathVariable int customerId, @Valid @RequestBody Customer customer) {
		Optional<Customer> optional = customerRepository.findById(customerId);
		if (optional.isPresent()) {
			Optional<Customer> emailCustomer = customerRepository.findByEmail(customer.getEmail());
			if (emailCustomer.isPresent() && emailCustomer.get().getCustomerId() != customerId) {
				return "Customer email already exists";
			}
			Customer oldCustomer = optional.get();
			oldCustomer.setName(customer.getName());
			oldCustomer.setEmail(customer.getEmail());
			oldCustomer.setPhone(customer.getPhone());
			oldCustomer.setAddress(customer.getAddress());
			oldCustomer.setPassword(passwordEncoder.encode(customer.getPassword()));
			if (oldCustomer.getRole() == null) {
				oldCustomer.setRole("USER");
			}
			customerRepository.save(oldCustomer);
			return "Customer updated successfully";
		}
		return "Customer not found";
	}

	@DeleteMapping("/customer/delete/{customerId}")
	public String deleteCustomer(@PathVariable int customerId) {
		Optional<Customer> optional = customerRepository.findById(customerId);
		if (optional.isPresent()) {
			customerRepository.deleteById(customerId);
			return "Customer deleted successfully";
		}
		return "Customer not found";
	}
}
