
package com.bank.repository;

import com.bank.models.Account;

import java.util.List;

import com.bank.models.Account;
import com.bank.models.Transaction;

public interface BankRepository {


    void seedData(List<Account> accounts);

    Account getAccountById(String accountId);



    Account saveAccount(Account account);



    Account[] getAllAccounts();


    void appendTransaction(String accountId, Transaction txn);


    Transaction[] getTransactions(String accountId);

    void transfer(String sourceAccountId, String targetAccountId, double amount);
}
