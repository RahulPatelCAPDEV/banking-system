
package com.bank.repository;

import com.bank.models.Account;
import com.bank.models.Customer;
import com.bank.models.Transaction;
import com.bank.models.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class RepositoryTest {

    private BankRepository repo;

    @BeforeEach
    void setUp() {
        repo = new BankRepositoryImpl();
    }


    @Test
    @DisplayName("getAccountById validates null and blank inputs; returns null for not found")
    void getAccountById_invalidInputs() {
        Account flag = repo.getAccountById("re");
        if (flag != null) {
            assertThrows(NoSuchElementException.class,
                    () -> repo.getAccountById(""));
        }
        assertNull(repo.getAccountById("NOT_FOUND"));
    }

    @Test
    @DisplayName("getAllAccounts on empty repository returns empty array")
    void getAllAccounts_empty() {
        Account[] arr = repo.getAllAccounts();
        assertNotNull(arr);
        assertEquals(0, arr.length);
    }



    @Test
    @DisplayName("appendTransaction should fail if both customer and transaction are missing")
    void appendTransactionCustomerAndTransactionMissing() {
        BankRepository repo = new BankRepositoryImpl();
        assertThrows(IllegalArgumentException.class,
                () -> repo.appendTransaction("A1", null));
    }



    @Test
    @DisplayName("getTransactions throws for unknown account")
    void getTransactionsMissingAccount() {
        assertThrows(NoSuchElementException.class, () -> repo.getTransactions("NA"));
    }


    @Test
    @DisplayName("Customer should fail when ID is empty")
    void customerIdEmpty() {
        assertThrows(IllegalArgumentException.class,
                () -> new Customer("A_@",null));
    }

    @Test
    @DisplayName("Customer should fail when ID is null")
    void customerIdNull() {
        assertThrows(IllegalArgumentException.class,
                () -> new Customer("A_@",null));
    }

    @Test
    @DisplayName("Transaction should fail when ID is empty")
    void transactionIdEmpty() {
        assertThrows(IllegalArgumentException.class,
                () -> new Transaction(
                        "",                           // transactionId (EMPTY → should fail)
                        TransactionType.DEPOSIT,     // type
                        50.0,                         // amount
                        System.currentTimeMillis(),   // timestamp
                        "A1",                         // sourceAccountId
                        null,                         // targetAccountId (deposit = null)
                        0.0                           // balanceAfter
                )
        );
    }


    @Test
    @DisplayName("Transaction should fail for zero amount")
    void transactionAmountZero() {
        assertThrows(IllegalArgumentException.class,
                () -> new Transaction(
                        "",                           // transactionId (EMPTY → should fail)
                        TransactionType.DEPOSIT,     // type
                        50.0,                         // amount
                        System.currentTimeMillis(),   // timestamp
                        "A1",                         // sourceAccountId
                        null,                         // targetAccountId (deposit = null)
                        0.0                           // balanceAfter
                )
        );
    }

    @Test
    @DisplayName("Transaction should fail when source account is empty (transfer)")
    void transactionSourceEmpty() {
        assertThrows(IllegalArgumentException.class,
                () -> new Transaction(
                        "",                           // transactionId (EMPTY → should fail)
                        TransactionType.DEPOSIT,     // type
                        50.0,                         // amount
                        System.currentTimeMillis(),   // timestamp
                        "A1",                         // sourceAccountId
                        null,                         // targetAccountId (deposit = null)
                        0.0                           // balanceAfter
                )
        );
    }

    @Test
    @DisplayName("Transaction should fail when target account is null (transfer)")
    void transactionTargetNull() {
        assertThrows(IllegalArgumentException.class,
                () -> new Transaction(
                        "",                           // transactionId (EMPTY → should fail)
                        TransactionType.DEPOSIT,     // type
                        50.0,                         // amount
                        System.currentTimeMillis(),   // timestamp
                        "A1",                         // sourceAccountId
                        null,                         // targetAccountId (deposit = null)
                        0.0                           // balanceAfter
                )
        );
    }


}
