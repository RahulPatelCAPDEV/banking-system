package com.bank.service;

import com.bank.models.Account;
import com.bank.models.Transaction;
import com.bank.models.TransactionType;
import com.bank.repository.BankRepository;
import com.bank.util.SampleDataUtil;
import com.bank.util.ValidationUtil;

import java.util.Arrays;
import java.util.List;

public class BankServiceImpl implements BankService {

    private final BankRepository repository;

    public BankServiceImpl(BankRepository repository) {
        this.repository = repository;
    }

    @Override
    public void init() {
        Account[] accounts = SampleDataUtil.createSampleAccounts();
        List<Account> list = Arrays.asList(accounts);

        ValidationUtil.checkNoDuplicateCustomerAccount(accounts);
        repository.seedData(list);
    }

    @Override
    public double getBalance(String accountId) {
        Account acc = ValidationUtil.getValidAccountOrThrow(repository, accountId);
        return acc.getBalance();
    }

    @Override
    public double deposit(String accountId, double amount) {

        Account acc = ValidationUtil.getValidAccountOrThrow(repository, accountId);
        ValidationUtil.checkPositiveAmount(amount);

        double newBalance = acc.getBalance() + amount;

        Transaction depositTxn = new Transaction(
                "T-D-" + System.nanoTime(),
                TransactionType.DEPOSIT,
                amount,
                System.currentTimeMillis(),
                null,
                accountId,
                newBalance
        );

       
        acc.appendTransaction(depositTxn);



        // Update model
        acc.appendTransaction(depositTxn);


        repository.appendTransaction(accountId, depositTxn);

        return newBalance;
    }

    @Override
    public double withdraw(String accountId, double amount) {

        Account acc = ValidationUtil.getValidAccountOrThrow(repository, accountId);
        ValidationUtil.checkPositiveAmount(amount);
        ValidationUtil.checkSufficientFunds(acc, amount);

        double newBal = acc.getBalance() - amount;

        Transaction outTxn = new Transaction(
                "T-W-" + System.nanoTime(),   
                TransactionType.WITHDRAWAL,   
                amount,                       
                System.currentTimeMillis(),   
                accountId,                    
                null,                         
                newBal                        

        );
        
      
        acc.appendTransaction(outTxn);

        repository.appendTransaction(accountId, outTxn);

        return newBal;
    }

    @Override
    public double transfer(String sourceId, String targetId, double amount) {

        if (sourceId != null && sourceId.equals(targetId)) {
            throw new IllegalArgumentException("Cannot transfer to same account");
        }

        Account src = ValidationUtil.getValidAccountOrThrow(repository, sourceId);
        Account dst = ValidationUtil.getValidAccountOrThrow(repository, targetId);
        ValidationUtil.checkPositiveAmount(amount);
        ValidationUtil.checkSufficientFunds(src, amount);

        double srcNew = src.getBalance() - amount;
        double dstNew = dst.getBalance() + amount;

        long now = System.currentTimeMillis();

        Transaction out = new Transaction(
                "T-OUT-" + System.nanoTime(),
                TransactionType.TRANSFER_OUT,
                amount,
                now,
                sourceId,
                targetId,
                srcNew
        );

        Transaction in = new Transaction(
                "T-IN-" + System.nanoTime(),
                TransactionType.TRANSFER_IN,
                amount,
                now,
                sourceId,
                targetId,
                dstNew        // FIXED
        );

        // FIX: apply via model
        src.appendTransaction(out);
        dst.appendTransaction(in);

        repository.appendTransaction(sourceId, out);
        repository.appendTransaction(targetId, in);

        return srcNew;
    }

    @Override
    public List<Transaction> getLast10Transactions(String accountId) {
        Account acc = ValidationUtil.getValidAccountOrThrow(repository, accountId);
        return acc.getLatestTransactions(10);   // FIXED
    }
}