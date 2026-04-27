package com.bank.banking_service.service;

import com.bank.models.Account;
import com.bank.models.Customer;
import com.bank.repository.BankRepository;
import com.bank.service.BankServiceImpl;
import com.bank.exceptions.InvalidAccountException;
import com.bank.exceptions.NegativeOrZeroAmountException;
import com.bank.exceptions.InsufficientFundsException;
import com.bank.util.ValidationUtil; // static methods

import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Answers.CALLS_REAL_METHODS;

public class BankServiceImplTransferTest {

    private BankRepository repo;
    private BankServiceImpl service;

    private Account a1;
    private Account a2;

    private MockedStatic<ValidationUtil> validationMock;

    @BeforeEach
    void init() {
        repo = mock(BankRepository.class);
        service = new BankServiceImpl(repo);

        // Build accounts with whatever constructor you DO have; then set balances.
        a1 = new Account("A1", new Customer("C1", "Alice"));
        a2 = new Account("A2", new Customer("C2", "Bob"));
        a1.setBalance(100.0);
        a2.setBalance(50.0);

        // Service likely reads accounts via repo → stub those.
        when(repo.getAccountById("A1")).thenReturn(a1);
        when(repo.getAccountById("A2")).thenReturn(a2);

        // IMPORTANT: mock static ValidationUtil but CALL REAL METHODS by default.
        // This keeps checkPositiveAmount, checkSufficientFunds*, latest10, etc. real,
        // and we override only the account-resolution method where needed.
        validationMock = mockStatic(ValidationUtil.class, CALLS_REAL_METHODS);

        // Default: resolve valid accounts for A1 and A2
        validationMock.when(() -> ValidationUtil.getValidAccountOrThrow(repo, "A1"))
                      .thenReturn(a1);
        validationMock.when(() -> ValidationUtil.getValidAccountOrThrow(repo, "A2"))
                      .thenReturn(a2);
    }

    @AfterEach
    void tearDown() {
        if (validationMock != null) validationMock.close();
    }

    @Test
    void successfulTransfer_updatesBothBalances() {
        // Act
        double srcNew = service.transfer("A1", "A2", 30.0);

        // Assert (use service to read balances, in case service re-fetches objects internally)
        assertEquals(70.0, srcNew, 0.0001);
        assertEquals(70.0, service.getBalance("A1"), 0.0001);
        assertEquals(80.0, service.getBalance("A2"), 0.0001);

        // Two appends for out/in (optional, adjust if your service appends differently)
        verify(repo, atLeast(2)).appendTransaction(anyString(), any());
    }

    @Test
    void selfTransfer_rejected() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> service.transfer("A1", "A1", 20.0)
        );
        assertTrue(ex.getMessage() == null || ex.getMessage().toLowerCase().contains("same"));

        // No side effects
        assertEquals(100.0, service.getBalance("A1"), 0.0001);
        verify(repo, never()).appendTransaction(anyString(), any());
    }

    @Test
    void negativeAmount_rejected() {
        assertThrows(NegativeOrZeroAmountException.class,
            () -> service.transfer("A1", "A2", -10.0));

        // No side effects
        assertEquals(100.0, service.getBalance("A1"), 0.0001);
        assertEquals(50.0,  service.getBalance("A2"), 0.0001);
        verify(repo, never()).appendTransaction(anyString(), any());
    }

    @Test
    void zeroAmount_rejected() {
        assertThrows(NegativeOrZeroAmountException.class,
            () -> service.transfer("A1", "A2", 0.0));

        // No side effects
        assertEquals(100.0, service.getBalance("A1"), 0.0001);
        assertEquals(50.0,  service.getBalance("A2"), 0.0001);
        verify(repo, never()).appendTransaction(anyString(), any());
    }

    @Test
    void invalidSource_throws() {
        // Override only this call to simulate not found
        validationMock.when(() -> ValidationUtil.getValidAccountOrThrow(repo, "BAD"))
                      .thenThrow(new InvalidAccountException("Account not found: BAD"));

        assertThrows(InvalidAccountException.class,
            () -> service.transfer("BAD", "A2", 10.0));

        // No side effects on destination
        assertEquals(50.0, service.getBalance("A2"), 0.0001);
        verify(repo, never()).appendTransaction(anyString(), any());
    }

    @Test
    void insufficientFunds_rejected() {
        a1.setBalance(5.0);

        assertThrows(InsufficientFundsException.class,
            () -> service.transfer("A1", "A2", 20.0));

        // No side effects
        assertEquals(5.0,  service.getBalance("A1"), 0.0001);
        assertEquals(50.0, service.getBalance("A2"), 0.0001);
        verify(repo, never()).appendTransaction(anyString(), any());
    }
}