package com.bank.banking_service.service;

import com.bank.exceptions.InsufficientFundsException;
import com.bank.exceptions.InvalidAccountException;
import com.bank.exceptions.NegativeOrZeroAmountException;
import com.bank.models.Transaction;
import com.bank.models.TransactionType;
import com.bank.repository.BankRepository;
import com.bank.repository.BankRepositoryImpl;
import com.bank.service.BankService;
import com.bank.service.BankServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BankingIntegrationTest {
    private BankService svc;

    @BeforeEach
    void setup() {

        BankRepository repo = new BankRepositoryImpl();
        svc = new BankServiceImpl(repo);
        svc.init();

    }

    @Test
    void deposit_updatesBalance_andAppearsInLast10() {
        double before = svc.getBalance("A1");
        double after = svc.deposit("A1", 75.0);
        assertEquals(before + 75.0, after);

        Transaction[] last = svc.getLast10Transactions("A1").toArray(new Transaction[0]);
        assertTrue(last.length >= 1);
        Transaction t = last[last.length - 1];
        assertEquals(TransactionType.DEPOSIT, t.getType());
        assertEquals(75.0, t.getAmount());
        assertEquals(after, t.getBalanceAfter());
    }

    @Test
    void withdraw_updatesBalance_andAppearsInLast10_noNegative() {
        double start = svc.getBalance("A1");
        double after = svc.withdraw("A1", 250.0);
        assertEquals(start - 250.0, after);
        assertTrue(after >= 0.0, "Post-withdraw balance should never be negative");

        Transaction[] last = svc.getLast10Transactions("A1").toArray(new Transaction[0]);
        Transaction t = last[last.length - 1];
        assertEquals(TransactionType.WITHDRAWAL, t.getType());
        assertEquals(250.0, t.getAmount());
        assertEquals(after, t.getBalanceAfter());
    }

    @Test
    void transfer_updatesBothBalances_andAppendsBothSides() {
        double srcBefore = svc.getBalance("A1");
        double dstBefore = svc.getBalance("A2");

        double srcAfter = svc.transfer("A1", "A2", 120.0);

        assertEquals(srcBefore - 120.0, srcAfter);
        assertEquals(dstBefore + 120.0, svc.getBalance("A2"));

        Transaction[] srcTx = svc.getLast10Transactions("A1").toArray(new Transaction[0]);
        Transaction[] dstTx = svc.getLast10Transactions("A2").toArray(new Transaction[0]);

        Transaction tOut = srcTx[srcTx.length - 1];
        Transaction tIn  = dstTx[dstTx.length - 1];

        assertEquals(TransactionType.TRANSFER_OUT, tOut.getType());
        assertEquals(120.0, tOut.getAmount());
        assertEquals(srcAfter, tOut.getBalanceAfter());

        assertEquals(TransactionType.TRANSFER_IN, tIn.getType());
        assertEquals(120.0, tIn.getAmount());
        assertEquals(dstBefore + 120.0, tIn.getBalanceAfter());
    }

    @Test
    void transfer_insufficientFunds_isAtomic_noPartialUpdates() {
        double srcBefore = svc.getBalance("A1");
        double dstBefore = svc.getBalance("A2");

        assertThrows(InsufficientFundsException.class,
                () -> svc.transfer("A1", "A2", srcBefore + 1.0));

        assertEquals(srcBefore, svc.getBalance("A1"));
        assertEquals(dstBefore, svc.getBalance("A2"));
    }

    @Test
    void invalidAccount_throwsInvalidAccountException_everywhere() {
        assertThrows(InvalidAccountException.class, () -> svc.getBalance("AXXX"));
        assertThrows(InvalidAccountException.class, () -> svc.deposit("AXXX", 10.0));
        assertThrows(InvalidAccountException.class, () -> svc.withdraw("AXXX", 10.0));
        assertThrows(InvalidAccountException.class, () -> svc.transfer("AXXX", "A001", 10.0));
        assertThrows(InvalidAccountException.class, () -> svc.transfer("A001", "AXXX", 10.0));
        assertThrows(InvalidAccountException.class, () -> svc.getLast10Transactions("AXXX"));
    }

    @Test
    void neverNegativeBalance_acrossSequence() {
        double before = svc.getBalance("A1");
        assertTrue(before >= 0.0);

        double afterWithdraw = svc.withdraw("A1", 90.0);
        assertEquals(before - 90.0, afterWithdraw);
        assertTrue(afterWithdraw >= 0.0);

        assertThrows(InsufficientFundsException.class, () -> svc.withdraw("A1", afterWithdraw + 0.01));
        assertEquals(afterWithdraw, svc.getBalance("A1"));

        double afterDeposit = svc.deposit("A1", 100.0);
        assertEquals(afterWithdraw + 100.0, afterDeposit);

        double toRestore = afterDeposit - before;
        double restored = svc.withdraw("A1", toRestore);
        assertEquals(before, restored);
        assertTrue(svc.getBalance("A1") >= 0.0);
    }

    @Test
    void arraysConsistent_repoTransactionsAccumulate() {
        for (int i = 0; i < 7; i++) svc.deposit("A1", 1.0);
        Transaction[] last = svc.getLast10Transactions("A1").toArray(new Transaction[0]);
        assertEquals(10, last.length);

        for (int i = 0; i < 5; i++) svc.deposit("A1", 1.0);
        Transaction[] last2 = svc.getLast10Transactions("A1").toArray(new Transaction[0]);
        assertEquals(10, last2.length);
    }

    @Test
    void negativeAmounts_rejected_uniformly() {
        assertThrows(NegativeOrZeroAmountException.class, () -> svc.deposit("A1", -1.0));
        assertThrows(NegativeOrZeroAmountException.class, () -> svc.withdraw("A1", -1.0));
        assertThrows(NegativeOrZeroAmountException.class, () -> svc.transfer("A1", "A2", -1.0));
    }


}
