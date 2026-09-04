package com.bank.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
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
public class AccountController {

	@Autowired
	private AccountRepository accountRepository;

	@Autowired
	private CustomerRepository customerRepository;

	@Autowired
	private BankTransactionRepository bankTransactionRepository;

	@PostMapping("/account/save/{customerId}")
	@Transactional
	public String saveAccount(@PathVariable int customerId, @Valid @RequestBody Account account) {
		Optional<Customer> optionalCustomer = customerRepository.findById(customerId);
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
			return "Account saved successfully";
		}
		return "Customer not found";
	}

	@GetMapping("/account/fetch")
	public List<Account> fetchAccounts() {
		return accountRepository.findAll();
	}

	@GetMapping("/account/find/{accountId}")
	public Object findAccount(@PathVariable int accountId) {
		Optional<Account> optional = accountRepository.findById(accountId);
		if (optional.isPresent()) {
			return optional.get();
		}
		return "Account not found";
	}

	@GetMapping("/account/customer/{customerId}")
	public List<Account> findAccountsByCustomer(@PathVariable int customerId) {
		return accountRepository.findByCustomerCustomerId(customerId);
	}

	@GetMapping("/account/balance/{accountId}")
	public Object checkBalance(@PathVariable int accountId) {
		Optional<Account> optional = accountRepository.findById(accountId);
		if (optional.isPresent()) {
			return optional.get().getBalance();
		}
		return "Account not found";
	}

	@Transactional
	@PatchMapping("/account/deposit/{accountId}/{amount}")
	public String deposit(@PathVariable int accountId, @PathVariable double amount) {
		Optional<Account> optional = accountRepository.findById(accountId);
		if (optional.isPresent()) {
			if (amount <= 0) {
				return "Deposit amount must be greater than zero";
			}
			Account account = optional.get();
			account.setBalance(account.getBalance() + amount);
			accountRepository.save(account);
			saveTransaction(account, "CREDIT", amount, "Cash deposit");
			return "Amount deposited successfully. Current balance: " + account.getBalance();
		}
		return "Account not found";
	}

	@Transactional
	@PatchMapping("/account/withdraw/{accountId}/{amount}")
	public String withdraw(@PathVariable int accountId, @PathVariable double amount) {
		Optional<Account> optional = accountRepository.findById(accountId);
		if (optional.isPresent()) {
			if (amount <= 0) {
				return "Withdrawal amount must be greater than zero";
			}
			Account account = optional.get();
			if (account.getBalance() < amount) {
				return "Insufficient balance";
			}
			account.setBalance(account.getBalance() - amount);
			accountRepository.save(account);
			saveTransaction(account, "DEBIT", amount, "Cash withdrawal");
			return "Amount withdrawn successfully. Current balance: " + account.getBalance();
		}
		return "Account not found";
	}

	@Transactional
	@PatchMapping("/account/transfer/{fromAccountId}/{toAccountId}/{amount}")
	public String transfer(@PathVariable int fromAccountId, @PathVariable int toAccountId,
			@PathVariable double amount) {
		if (fromAccountId == toAccountId) {
			return "Sender and receiver accounts cannot be the same";
		}
		if (amount <= 0) {
			return "Transfer amount must be greater than zero";
		}

		Optional<Account> optionalFrom = accountRepository.findById(fromAccountId);
		Optional<Account> optionalTo = accountRepository.findById(toAccountId);
		if (optionalFrom.isPresent() && optionalTo.isPresent()) {
			Account fromAccount = optionalFrom.get();
			Account toAccount = optionalTo.get();
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
