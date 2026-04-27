
package com.bank.repository;

import com.bank.models.Account;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import com.bank.models.Account;
import com.bank.models.Transaction;
import com.bank.models.TransactionType;

public class BankRepositoryImpl implements BankRepository {



//	@Override
//	public void seedData(List<Account> accounts) {
//
//	}


//	@Override
//	public Account getAccountById(String accountId) {
//
//		return null;
//	}


    private final List<Account> accounts = new ArrayList<>();
    private final List<List<Transaction>> transactions = new ArrayList<>();


    private Transaction depositTx(String id, double amount, String accountId, double balanceAfter) {
        return new Transaction(id, TransactionType.DEPOSIT, amount, System.currentTimeMillis(),
                accountId, null, balanceAfter);
    }

    private Transaction withdrawalTx(String id, double amount, String accountId, double balanceAfter) {
        return new Transaction(id, TransactionType.WITHDRAWAL, amount, System.currentTimeMillis(),
                accountId, null, balanceAfter);
    }

    // Simple in-memory tx id generator for transfer()
    private long txSeq = 1L;
    private String nextTxnId() {
        return "TX-" + (txSeq++);
    }

    @Override
    public void seedData(List<Account> seedAccounts) {
        if (seedAccounts == null) return;
        for (Account a : seedAccounts) {
            saveAccount(a);
        }
    }

    @Override
    public Account saveAccount(Account account) {
        if (account == null || account.getAccountId() == null || account.getAccountId().trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid account");
        }

        if (getAccountById(account.getAccountId()) != null) {
            throw new IllegalArgumentException("Duplicate account id: " + account.getAccountId());
        }

        accounts.add(account);
        transactions.add(new ArrayList<>());
        return account;
    }

    @Override
    public Account getAccountById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return null; // per your test: return null for invalid/not-found
        }
        for (Account acc : accounts) {
            if (acc != null && Objects.equals(acc.getAccountId(), id)) {
                return acc;
            }
        }
        return null;
    }

    @Override
    public Account[] getAllAccounts() {
        // With ArrayList backing, we return a snapshot array (NOT the internal store).
        return accounts.toArray(new Account[0]);
    }

    @Override
    public void appendTransaction(String accountId, Transaction txn) {
        if (txn == null) {
            throw new IllegalArgumentException("Transaction is null");
        }

        int index = indexOf(accountId);
        if (index == -1) {
            throw new NoSuchElementException("Account not found: " + accountId);
        }

        transactions.get(index).add(txn);

       
    }

    @Override
    public Transaction[] getTransactions(String accountId) {
        int index = indexOf(accountId);
        if (index == -1) {
            throw new NoSuchElementException("Account not found: " + accountId);
        }

        List<Transaction> txns = transactions.get(index);
        return txns.toArray(new Transaction[0]);
    }

    @Override
    public void transfer(String sourceAccountId, String targetAccountId, double amount) {
        // Basic validations
        if (sourceAccountId == null || sourceAccountId.trim().isEmpty()) {
            throw new IllegalArgumentException("sourceAccountId cannot be null or blank");
        }
        if (targetAccountId == null || targetAccountId.trim().isEmpty()) {
            throw new IllegalArgumentException("targetAccountId cannot be null or blank");
        }
        if (Objects.equals(sourceAccountId, targetAccountId)) {
            throw new IllegalArgumentException("Self transfer is not allowed");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }

        // Resolve indices
        int sIdx = indexOf(sourceAccountId);
        int dIdx = indexOf(targetAccountId);

        if (sIdx == -1) {
            throw new NoSuchElementException("Account not found: " + sourceAccountId);
        }
        if (dIdx == -1) {
            throw new NoSuchElementException("Account not found: " + targetAccountId);
        }

        Account src = accounts.get(sIdx);
        Account dst = accounts.get(dIdx);

        // Funds validation
        double srcBalance = src.getBalance();
        if (srcBalance < amount) {
            throw new IllegalArgumentException("Insufficient funds");
        }

        // --- Validate then compute everything first (atomic) ---
        double newSrcBal = srcBalance - amount;
        double newDstBal = dst.getBalance() + amount;

        // Create both transactions with correct balanceAfter
        String outId = nextTxnId();
        String inId  = nextTxnId();
        long now = System.currentTimeMillis();
        Transaction outTxn = new Transaction(
                outId,
                TransactionType.TRANSFER_OUT,
                amount,
                       now,
                sourceAccountId,
                targetAccountId,
                newSrcBal
        );

        Transaction inTxn = new Transaction(
                inId,
                TransactionType.TRANSFER_IN,
                amount,
                now,
                sourceAccountId,
                targetAccountId,
                newDstBal
        );

        src.setBalance(newSrcBal);
        dst.setBalance(newDstBal);

        transactions.get(sIdx).add(outTxn);
        transactions.get(dIdx).add(inTxn);
    }

    // Helpers
    private int indexOf(String id) {
        if (id == null || id.trim().isEmpty()) {
            return -1;
        }
        for (int i = 0; i < accounts.size(); i++) {
            Account acc = accounts.get(i);
            if (acc != null && Objects.equals(acc.getAccountId(), id)) {
                return i;
            }
        }
        return -1;
    }



    private int getIndex(String id) {
        if (id == null) return -1;
        for (int i = 0; i < accounts.size(); i++) {
            Account acc = accounts.get(i);
            if (Objects.equals(acc.getAccountId(), id)) {
                return i;
            }
        }
        return -1;
    }
}
