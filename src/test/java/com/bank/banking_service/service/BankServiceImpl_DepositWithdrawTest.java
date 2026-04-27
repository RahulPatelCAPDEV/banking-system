package com.bank.banking_service.service;

import com.bank.exceptions.InsufficientFundsException;
import com.bank.exceptions.InvalidAccountException;
import com.bank.exceptions.NegativeOrZeroAmountException;
import com.bank.models.Account;
import com.bank.models.Customer;
import com.bank.models.Transaction;
import com.bank.models.TransactionType;
import com.bank.repository.BankRepository;
import com.bank.service.BankServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankServiceImpl_DepositWithdrawTest {

    @Mock
    private BankRepository repo;

    private BankServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new BankServiceImpl(repo);
    }

    // ---------------------------------------------------------------------
    // deposit()
    // ---------------------------------------------------------------------
    @Test
    void deposit_happyPath_appendsTransaction_andUpdatesBalance() {
        Account acc = new Account("A1", new Customer("C1", "Alice"));
        when(repo.getAccountById("A1")).thenReturn(acc);

        double result = service.deposit("A1", 200.0);

        assertEquals(200.0, result);
        assertEquals(200.0, acc.getBalance());

       
        assertTrue(acc.getTransactions().size() >= 1);

        
        Transaction t = acc.getTransactions().get(acc.getTransactions().size() - 1);
        assertEquals(TransactionType.DEPOSIT, t.getType());
        assertEquals(200.0, t.getAmount());
        assertEquals(200.0, t.getBalanceAfter());
        assertNull(t.getSourceAccountId());
        assertEquals("A1", t.getTargetAccountId());
 
        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(repo).getAccountById("A1");
        verify(repo).appendTransaction(eq("A1"), captor.capture());
        assertEquals(TransactionType.DEPOSIT, captor.getValue().getType());
        verifyNoMoreInteractions(repo);
    }

    @Test
    void deposit_throwsForBlankOrNullAccountId() {
        assertThrows(InvalidAccountException.class, () -> service.deposit(" ", 100));
        assertThrows(InvalidAccountException.class, () -> service.deposit(null, 100));
        verifyNoInteractions(repo);
    }

    @Test
    void deposit_throwsForZeroOrNegativeAmount() {
        Account acc = new Account("A1", new Customer("C1", "Alice"));
        when(repo.getAccountById("A1")).thenReturn(acc);

        assertThrows(NegativeOrZeroAmountException.class, () -> service.deposit("A1", 0));
        assertThrows(NegativeOrZeroAmountException.class, () -> service.deposit("A1", -5));

        verify(repo, times(2)).getAccountById("A1");
        verifyNoMoreInteractions(repo);
    }

    // 🔥 Extra negative test (as requested): account not found
    @Test
    void deposit_throwsWhenAccountNotFound() {
        when(repo.getAccountById("X404")).thenReturn(null); // ValidationUtil will throw

        assertThrows(InvalidAccountException.class, () -> service.deposit("X404", 50.0));

        // No transaction should be appended
        verify(repo).getAccountById("X404");
        verify(repo, never()).appendTransaction(anyString(), any(Transaction.class));
        verifyNoMoreInteractions(repo);
    }

    // ---------------------------------------------------------------------
    // withdraw()
    // ---------------------------------------------------------------------
    @Test
    void withdraw_throwsWhenInsufficientFunds() {
        Account acc = new Account("A1", new Customer("C1", "Alice"));

        // Prefund to 100
        acc.appendTransaction(new Transaction(
                UUID.randomUUID().toString(),
                TransactionType.DEPOSIT,
                100.0,
                System.currentTimeMillis(),
                null,
                "A1",
                100.0
        ));
        when(repo.getAccountById("A1")).thenReturn(acc);

        assertThrows(InsufficientFundsException.class, () -> service.withdraw("A1", 150.0));

        // Ensure no extra transaction added; balance remains unchanged
        assertEquals(1, acc.getTransactions().size());
        assertEquals(100.0, acc.getBalance());

        verify(repo).getAccountById("A1");
        verifyNoMoreInteractions(repo);
    }

    @Test
    void withdraw_throwsForBlankOrNullAccountId() {
        assertThrows(InvalidAccountException.class, () -> service.withdraw("", 50));
        assertThrows(InvalidAccountException.class, () -> service.withdraw(null, 50));
        verifyNoInteractions(repo);
    }

    @Test
    void withdraw_throwsForZeroOrNegativeAmount() {
        Account acc = new Account("A1", new Customer("C1", "Alice"));
        when(repo.getAccountById("A1")).thenReturn(acc);

        assertThrows(NegativeOrZeroAmountException.class, () -> service.withdraw("A1", 0));
        assertThrows(NegativeOrZeroAmountException.class, () -> service.withdraw("A1", -10));

        verify(repo, times(2)).getAccountById("A1");
        verifyNoMoreInteractions(repo);
    }
}