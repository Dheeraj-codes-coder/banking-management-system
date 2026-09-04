package com.bank.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

@Entity
public class Account {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int accountId;

	@Column(unique = true, nullable = false)
	@NotBlank(message = "Account number is required")
	private String accountNumber;

	@NotBlank(message = "Account type is required")
	private String accountType;

	@PositiveOrZero(message = "Opening balance cannot be negative")
	private double balance;

	@ManyToOne
	@JoinColumn(name = "customer_id")
	@JsonIgnore
	private Customer customer;

	@OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
	@JsonIgnore
	private List<BankTransaction> transactions;

	public int getAccountId() {
		return accountId;
	}

	public void setAccountId(int accountId) {
		this.accountId = accountId;
	}

	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public String getAccountType() {
		return accountType;
	}

	public void setAccountType(String accountType) {
		this.accountType = accountType;
	}

	public double getBalance() {
		return balance;
	}

	public void setBalance(double balance) {
		this.balance = balance;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	@JsonProperty("customerId")
	public int getCustomerId() {
		if (customer != null) {
			return customer.getCustomerId();
		}
		return 0;
	}

	public List<BankTransaction> getTransactions() {
		return transactions;
	}

	public void setTransactions(List<BankTransaction> transactions) {
		this.transactions = transactions;
	}
}
