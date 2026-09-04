package com.bank.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bank.entity.BankTransaction;

public interface BankTransactionRepository extends JpaRepository<BankTransaction, Integer> {

	List<BankTransaction> findByAccountAccountIdOrderByTransactionDateDesc(int accountId);
}
