package com.bank.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bank.entity.Account;
import com.bank.entity.BankTransaction;
import com.bank.repository.AccountRepository;
import com.bank.repository.BankTransactionRepository;
import com.bank.repository.CustomerRepository;

@ExtendWith(MockitoExtension.class)
public class AccountControllerTest {

	@Mock
	private AccountRepository accountRepository;

	@Mock
	private CustomerRepository customerRepository;

	@Mock
	private BankTransactionRepository bankTransactionRepository;

	@InjectMocks
	private AccountController accountController;

	@Test
	public void depositTest() {
		Account account = new Account();
		account.setAccountId(1);
		account.setBalance(5000);

		when(accountRepository.findById(1)).thenReturn(Optional.of(account));

		String result = accountController.deposit(1, 1000);

		assertEquals("Amount deposited successfully. Current balance: 6000.0", result);
		assertEquals(6000, account.getBalance());
		verify(accountRepository).save(account);
		verify(bankTransactionRepository).save(any(BankTransaction.class));
	}

	@Test
	public void insufficientBalanceTest() {
		Account account = new Account();
		account.setAccountId(1);
		account.setBalance(500);

		when(accountRepository.findById(1)).thenReturn(Optional.of(account));

		String result = accountController.withdraw(1, 1000);

		assertEquals("Insufficient balance", result);
		verify(accountRepository, never()).save(account);
	}
}
