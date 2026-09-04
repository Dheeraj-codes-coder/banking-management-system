package com.bank.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.bank.entity.Account;
import com.bank.entity.BankTransaction;
import com.bank.entity.Customer;
import com.bank.repository.AccountRepository;
import com.bank.repository.BankTransactionRepository;
import com.bank.repository.CustomerRepository;

import jakarta.validation.Valid;

@RestController
public class DashboardController {

	@Autowired
	private CustomerRepository customerRepository;

	@Autowired
	private AccountRepository accountRepository;

	@Autowired
	private BankTransactionRepository bankTransactionRepository;

	@GetMapping("/customer/me")
	public Object findMyProfile(Authentication authentication) {
		Optional<Customer> optional = customerRepository.findByEmail(authentication.getName());
		if (optional.isPresent()) {
			return optional.get();
		}
		return "Customer not found";
	}

	@GetMapping("/account/my")
	public List<Account> findMyAccounts(Authentication authentication) {
		Optional<Customer> optional = customerRepository.findByEmail(authentication.getName());
		if (optional.isPresent()) {
			return accountRepository.findByCustomerCustomerId(optional.get().getCustomerId());
		}
		return new ArrayList<Account>();
	}

	@Transactional
	@PostMapping("/account/my/save")
	public String saveMyAccount(Authentication authentication, @Valid @RequestBody Account account) {
		Optional<Customer> optionalCustomer = customerRepository.findByEmail(authentication.getName());
		if (optionalCustomer.isPresent()) {
			Optional<Account> optionalAccount = accountRepository.findByAccountNumber(account.getAccountNumber());
			if (optionalAccount.isPresent()) {
				return "Account number already exists";
			}
			if (!account.getAccountType().equalsIgnoreCase("SAVINGS")
					&& !account.getAccountType().equalsIgnoreCase("CURRENT")) {
				return "Account type must be SAVINGS or CURRENT";
			}
			account.setAccountType(account.getAccountType().toUpperCase());
			account.setCustomer(optionalCustomer.get());
			accountRepository.save(account);
			if (account.getBalance() > 0) {
				saveTransaction(account, "CREDIT", account.getBalance(), "Opening balance");
			}
			return "Account created successfully";
		}
		return "Customer not found";
	}

	@Transactional
	@PatchMapping("/account/my/deposit/{accountId}/{amount}")
	public String deposit(Authentication authentication, @PathVariable int accountId, @PathVariable double amount) {
		Optional<Account> optional = accountRepository.findById(accountId);
		if (optional.isPresent()) {
			Account account = optional.get();
			if (!isOwner(account, authentication)) {
				return "Access denied";
			}
			if (amount <= 0) {
				return "Deposit amount must be greater than zero";
			}
			account.setBalance(account.getBalance() + amount);
			accountRepository.save(account);
			saveTransaction(account, "CREDIT", amount, "Cash deposit");
			return "Amount deposited successfully";
		}
		return "Account not found";
	}

	@Transactional
	@PatchMapping("/account/my/withdraw/{accountId}/{amount}")
	public String withdraw(Authentication authentication, @PathVariable int accountId, @PathVariable double amount) {
		Optional<Account> optional = accountRepository.findById(accountId);
		if (optional.isPresent()) {
			Account account = optional.get();
			if (!isOwner(account, authentication)) {
				return "Access denied";
			}
			if (amount <= 0) {
				return "Withdrawal amount must be greater than zero";
			}
			if (account.getBalance() < amount) {
				return "Insufficient balance";
			}
			account.setBalance(account.getBalance() - amount);
			accountRepository.save(account);
			saveTransaction(account, "DEBIT", amount, "Cash withdrawal");
			return "Amount withdrawn successfully";
		}
		return "Account not found";
	}

	@Transactional
	@PatchMapping("/account/my/transfer/{fromAccountId}/{toAccountNumber}/{amount}")
	public String transfer(Authentication authentication, @PathVariable int fromAccountId,
			@PathVariable String toAccountNumber, @PathVariable double amount) {
		Optional<Account> optionalFrom = accountRepository.findById(fromAccountId);
		Optional<Account> optionalTo = accountRepository.findByAccountNumber(toAccountNumber);
		if (optionalFrom.isPresent() && optionalTo.isPresent()) {
			Account fromAccount = optionalFrom.get();
			Account toAccount = optionalTo.get();
			if (!isOwner(fromAccount, authentication)) {
				return "Access denied";
			}
			if (fromAccount.getAccountId() == toAccount.getAccountId()) {
				return "Sender and receiver accounts cannot be the same";
			}
			if (amount <= 0) {
				return "Transfer amount must be greater than zero";
			}
			if (fromAccount.getBalance() < amount) {
				return "Insufficient balance";
			}

			fromAccount.setBalance(fromAccount.getBalance() - amount);
			toAccount.setBalance(toAccount.getBalance() + amount);
			accountRepository.save(fromAccount);
			accountRepository.save(toAccount);
			saveTransaction(fromAccount, "DEBIT", amount,
					"Transferred to account " + toAccount.getAccountNumber());
			saveTransaction(toAccount, "CREDIT", amount,
					"Received from account " + fromAccount.getAccountNumber());
			return "Amount transferred successfully";
		}
		return "Sender or receiver account not found";
	}

	@GetMapping("/transaction/my/{accountId}")
	public Object findMyTransactions(Authentication authentication, @PathVariable int accountId) {
		Optional<Account> optional = accountRepository.findById(accountId);
		if (optional.isPresent()) {
			if (!isOwner(optional.get(), authentication)) {
				return "Access denied";
			}
			return bankTransactionRepository.findByAccountAccountIdOrderByTransactionDateDesc(accountId);
		}
		return "Account not found";
	}

	private boolean isOwner(Account account, Authentication authentication) {
		return account.getCustomer() != null
				&& account.getCustomer().getEmail().equals(authentication.getName());
	}

	private void saveTransaction(Account account, String type, double amount, String description) {
		BankTransaction bankTransaction = new BankTransaction();
		bankTransaction.setAccount(account);
		bankTransaction.setTransactionType(type);
		bankTransaction.setAmount(amount);
		bankTransaction.setTransactionDate(LocalDateTime.now());
		bankTransaction.setDescription(description);
		bankTransactionRepository.save(bankTransaction);
	}
}
