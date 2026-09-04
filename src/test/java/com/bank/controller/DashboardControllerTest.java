package com.bank.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import com.bank.entity.Account;
import com.bank.entity.Customer;
import com.bank.repository.AccountRepository;
import com.bank.repository.BankTransactionRepository;
import com.bank.repository.CustomerRepository;

@ExtendWith(MockitoExtension.class)
public class DashboardControllerTest {

	@Mock
	private CustomerRepository customerRepository;

	@Mock
	private AccountRepository accountRepository;

	@Mock
	private BankTransactionRepository bankTransactionRepository;

	@Mock
	private Authentication authentication;

	@InjectMocks
	private DashboardController dashboardController;

	@Test
	public void cannotDepositIntoAnotherCustomerAccountTest() {
		Customer customer = new Customer();
		customer.setEmail("another@gmail.com");

		Account account = new Account();
		account.setAccountId(1);
		account.setBalance(5000);
		account.setCustomer(customer);

		when(authentication.getName()).thenReturn("dheeraj@gmail.com");
		when(accountRepository.findById(1)).thenReturn(Optional.of(account));

		String result = dashboardController.deposit(authentication, 1, 1000);

		assertEquals("Access denied", result);
		assertEquals(5000, account.getBalance());
		verify(accountRepository, never()).save(account);
	}
}
