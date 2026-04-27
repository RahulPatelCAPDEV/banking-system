package com.bank.models;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class AccountTest {

    @Test
    void testValidAccountCreation() {
        Customer c = new Customer("C1", "Alice");
        Account a = new Account("A1", c);
        assertEquals("A1", a.getAccountId());
        assertEquals(c, a.getCustomer());
        assertEquals(0.0, a.getBalance());
        assertTrue(a.getTransactions().isEmpty());
    }

    @Test
    void testAccountIdCannotBeBlank() {
        Customer c = new Customer("C1", "Alice");
        assertThrows(IllegalArgumentException.class, () -> new Account("", c));
    }

    @Test
    void testCustomerCannotBeNull() {
        assertThrows(IllegalArgumentException.class, () -> new Account("A1", null));
    }

    @Test
    void testAppendTransactionUpdatesBalance() {
        Customer c = new Customer("C1", "Alice");
        Account a = new Account("A1", c);

        Transaction t = new Transaction("T1", TransactionType.DEPOSIT, 100, System.currentTimeMillis(), null, "A1", 100);

        a.appendTransaction(t);

        assertEquals(1, a.getTransactions().size());
        assertEquals(100, a.getBalance());
    }

    @Test
    void testGetLatestTransactions() {
        Customer c = new Customer("C1", "Alice");
        Account a = new Account("A1", c);

        for (int i = 1; i <= 5; i++) {
            a.appendTransaction(new Transaction("T" + i, TransactionType.DEPOSIT, i, System.currentTimeMillis(), null, "A1", i));
        }

        List<Transaction> latest = a.getLatestTransactions(3);

        assertEquals(3, latest.size());
        assertEquals("T5", latest.get(0).getTransactionId());
        assertEquals("T4", latest.get(1).getTransactionId());
        assertEquals("T3", latest.get(2).getTransactionId());
    }
}