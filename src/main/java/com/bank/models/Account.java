package com.bank.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Account {

    private final String accountId;
    private final Customer customer;
    private double balance;
    private final List<Transaction> transactions = new ArrayList<>();

    public Account(String accountId, Customer customer) {
        if (accountId == null || accountId.trim().isEmpty()) {
            throw new IllegalArgumentException("accountId cannot be blank");
        }
        if (customer == null) {
            throw new IllegalArgumentException("customer cannot be null");
        }
        this.accountId = accountId;
        this.customer = customer;
    }

    public String getAccountId() {
        return accountId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public double getBalance() {
        return balance;
    }

    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    public void appendTransaction(Transaction t) {
        transactions.add(t);
        this.balance = t.getBalanceAfter(); // make sure t.getBalanceAfter() is correct
    }

    public List<Transaction> getLatestTransactions(int n) {
        int size = transactions.size();
        if (n > size) n = size;
        List<Transaction> sub = transactions.subList(size - n, size);
        List<Transaction> reversed = new ArrayList<>(sub);
        Collections.reverse(reversed);
        return reversed;
    }

    public void setBalance(double newBalance) {
        if (newBalance < 0) {
            throw new IllegalArgumentException("Balance cannot be negative");
        }
        this.balance = newBalance;
    }
}