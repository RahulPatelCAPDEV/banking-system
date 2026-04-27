package com.bank.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TransactionTest {

    @Test
    void testValidTransactionCreation() {
        Transaction t = new Transaction("T1", TransactionType.DEPOSIT, 100,
                System.currentTimeMillis(), null, "A1", 100);

        assertEquals("T1", t.getTransactionId());
        assertEquals(TransactionType.DEPOSIT, t.getType());
        assertEquals("A1", t.getTargetAccountId());
        assertEquals(100, t.getBalanceAfter());
    }

    @Test
    void testTransactionIdCannotBeBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> new Transaction("", TransactionType.DEPOSIT, 100, System.currentTimeMillis(), null, "A1", 100));
    }

    @Test
    void testAmountMustBePositive() {
        assertThrows(IllegalArgumentException.class,
                () -> new Transaction("T1", TransactionType.DEPOSIT, -1, System.currentTimeMillis(), null, "A1", 100));
    }
}