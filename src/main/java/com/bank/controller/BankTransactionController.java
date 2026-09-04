package com.bank.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.bank.entity.BankTransaction;
import com.bank.repository.BankTransactionRepository;

@RestController
public class BankTransactionController {

	@Autowired
	private BankTransactionRepository bankTransactionRepository;

	@GetMapping("/transaction/fetch")
	public List<BankTransaction> fetchTransactions() {
		return bankTransactionRepository.findAll();
	}

	@GetMapping("/transaction/account/{accountId}")
	public List<BankTransaction> findTransactionsByAccount(@PathVariable int accountId) {
		return bankTransactionRepository.findByAccountAccountIdOrderByTransactionDateDesc(accountId);
	}
}
