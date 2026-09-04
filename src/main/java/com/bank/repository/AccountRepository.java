package com.bank.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bank.entity.Account;

public interface AccountRepository extends JpaRepository<Account, Integer> {

	Optional<Account> findByAccountNumber(String accountNumber);

	List<Account> findByCustomerCustomerId(int customerId);
}
