package com.bank.util;

import com.bank.exceptions.DuplicateCustomerAccountException;
import com.bank.exceptions.InsufficientFundsException;
import com.bank.exceptions.InvalidAccountException;
import com.bank.exceptions.NegativeOrZeroAmountException;
import com.bank.models.Account;
import com.bank.models.Transaction;
import com.bank.repository.BankRepository;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

public final class ValidationUtil {

    private ValidationUtil() {} 

    public static Account getValidAccountOrThrow(BankRepository repo, String accountId) {
        if (accountId == null || accountId.trim().isEmpty()) {
            throw new InvalidAccountException("Account id required");
        }
        
        Account acc = repo.getAccountById(accountId);
        if (acc == null) {
            throw new InvalidAccountException("Account not found: " + accountId);
        }
        return acc;
    }

    public static void checkPositiveAmount(Double amount) {
        if (amount == null || amount <= 0.0d) {
            throw new NegativeOrZeroAmountException("Amount must be > 0");
        }
    }

    public static void checkSufficientFundsOrThrow(
            Double balance, Double amount, String accountId) {

        if (balance == null) {
            throw new IllegalArgumentException("Balance cannot be null");
        }

        checkPositiveAmount(amount);

        if (balance < amount) {
            throw new InsufficientFundsException("Insufficient funds for account " + accountId);
        }
    }

    public static void checkNoDuplicateCustomerAccount(Account[] accounts) {
        if (accounts == null) return;

        Set<String> seenCustomerIds = new HashSet<>();
        for (Account a : accounts) {
            if (a == null) continue;
            String cid = a.getCustomer().getCustomerId();
            if (!seenCustomerIds.add(cid)) {
                throw new DuplicateCustomerAccountException(
                        "Customer " + cid + " has multiple accounts");
            }
        }
    }

    public static Transaction[] latest10(Transaction[] all) {
        if (all == null) return new Transaction[0];

        int count = 0;
        for (int i = 0; i < all.length; i++) {
            if (all[i] != null) count++;
        }
        int start = (count > 10) ? (count - 10) : 0;
        int size = count - start;

        Transaction[] last = new Transaction[size];
        int idx = 0;
        for (int i = start; i < count; i++) {
            last[idx++] = all[i];
        }
        return last;
    }
    public static void checkSufficientFunds(Account account, double amount) {
        if (account == null) {
            throw new InvalidAccountException("Account cannot be null");
        }



        if (account.getBalance() < amount) {
            throw new InsufficientFundsException(
                    String.format("Insufficient funds. Balance: %.2f, Required: %.2f",
                            account.getBalance(), amount)
            );
        }

    }
}

