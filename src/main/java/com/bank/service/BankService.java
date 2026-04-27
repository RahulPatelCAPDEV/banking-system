
// src/service/BankService.java


package com.bank.service;

import com.bank.models.Transaction;
import java.util.List;

public interface BankService {

    /**
     * Initializes the system by loading sample accounts
     * and seeding them into the repository.
     */
    void init();

    /** 
     * Returns the balance of the given account.
     */
    double getBalance(String accountId);

    /**
     * Deposit amount into the account.
     * Day‑1: Stub implementation returns existing balance.
     */
    double deposit(String accountId, double amount);

    /**
     * Withdraw amount from account.
     * Day‑1: Stub implementation, validations only.
     */
    double withdraw(String accountId, double amount);

    /**
     * Transfer amount between two accounts.
     * Day‑1: Stub implementation, validations only.
     *
     * @return
     */
    double transfer(String sourceAccountId, String targetAccountId, double amount);

    /**
     * Returns last 10 transactions for an account.
     * Day‑1: You may return empty list or repository results.
     */
    List<Transaction> getLast10Transactions(String accountId);
}

