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
import com.bank.util.SampleDataUtil;
import com.bank.util.ValidationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BankServiceImplTest {

    private BankRepository repo;
    private BankServiceImpl service;

    @BeforeEach
    void setup() {
        repo = mock(BankRepository.class);
        service = new BankServiceImpl(repo);
    }

    // -----------------------------------------------------------------------
    // init()
    // -----------------------------------------------------------------------
    @Test
    void init_shouldLoadSampleAccounts_validateAndSeed() {
        Account[] accounts = {
                new Account("A1", new Customer("C1", "Alice")),
                new Account("A2", new Customer("C2", "Bob"))
        };

        try (MockedStatic<SampleDataUtil> sample =
                     Mockito.mockStatic(SampleDataUtil.class);
             MockedStatic<ValidationUtil> val =
                     Mockito.mockStatic(ValidationUtil.class)) {

            sample.when(SampleDataUtil::createSampleAccounts).thenReturn(accounts);

            service.init();

            sample.verify(SampleDataUtil::createSampleAccounts);
            val.verify(() -> ValidationUtil.checkNoDuplicateCustomerAccount(accounts));
            verify(repo).seedData(Arrays.asList(accounts));
            verifyNoMoreInteractions(repo);
        }
    }

    // -----------------------------------------------------------------------
    // deposit() – ACTUAL behavior (updates balance + appends transaction)
    // -----------------------------------------------------------------------
    @Test
    void deposit_shouldValidate_updateBalance_andAppendTransaction() {
        // Given
        Account acc = new Account("A1", new Customer("C1", "Test"));
        acc.setBalance(100.0);

        try (MockedStatic<ValidationUtil> val = Mockito.mockStatic(ValidationUtil.class)) {
            val.when(() -> ValidationUtil.getValidAccountOrThrow(repo, "A1"))
               .thenReturn(acc);
            val.when(() -> ValidationUtil.checkPositiveAmount(100.0))
               .thenAnswer(inv -> null);

            // When
            double result = service.deposit("A1", 100.0);

            // Then: balance increased
            assertEquals(200.0, result);
            assertEquals(200.0, acc.getBalance());

            // And: a DEPOSIT transaction appended to repo
            ArgumentCaptor<Transaction> txCaptor = ArgumentCaptor.forClass(Transaction.class);
            verify(repo).appendTransaction(eq("A1"), txCaptor.capture());
            Transaction tx = txCaptor.getValue();
            assertEquals(TransactionType.DEPOSIT, tx.getType());
            assertEquals(100.0, tx.getAmount());
            assertEquals(200.0, tx.getBalanceAfter());
            assertNull(tx.getSourceAccountId());
            assertEquals("A1", tx.getTargetAccountId());

            // And: validations were invoked
            val.verify(() -> ValidationUtil.getValidAccountOrThrow(repo, "A1"));
            val.verify(() -> ValidationUtil.checkPositiveAmount(100.0));
            verifyNoMoreInteractions(repo);
        }
    }

    // Negative test for deposit (as requested): positive amount validation fails
    @Test
    void deposit_shouldThrow_whenAmountNotPositive() {
        Account acc = new Account("A1", new Customer("C1", "Test"));
        acc.setBalance(50.0);

        try (MockedStatic<ValidationUtil> val = Mockito.mockStatic(ValidationUtil.class)) {
            val.when(() -> ValidationUtil.getValidAccountOrThrow(repo, "A1"))
               .thenReturn(acc);
            // Simulate validation throwing for zero/negative
            val.when(() -> ValidationUtil.checkPositiveAmount(0.0))
               .thenThrow(new NegativeOrZeroAmountException("Amount must be > 0"));

            assertThrows(NegativeOrZeroAmountException.class,
                    () -> service.deposit("A1", 0.0));

            // Ensure repo is not touched to append a transaction
            verifyNoInteractions(repo);

            // Verify validations called
            val.verify(() -> ValidationUtil.getValidAccountOrThrow(repo, "A1"));
            val.verify(() -> ValidationUtil.checkPositiveAmount(0.0));
        }
    }

    // -----------------------------------------------------------------------
    // withdraw() – insufficient funds
    // -----------------------------------------------------------------------
    @Test
    void withdraw_shouldThrowWhenInsufficientFunds() {
        Account acc = new Account("A1", new Customer("C1", "Test"));
        acc.setBalance(100.0);
 
        try (MockedStatic<ValidationUtil> val = Mockito.mockStatic(ValidationUtil.class)) {
            val.when(() -> ValidationUtil.getValidAccountOrThrow(repo, "A1"))
               .thenReturn(acc);
            val.when(() -> ValidationUtil.checkPositiveAmount(500.0))
               .thenAnswer(inv -> null);
            val.when(() -> ValidationUtil.checkSufficientFunds(acc, 500.0))
               .thenThrow(new InsufficientFundsException("Insufficient"));

            assertThrows(InsufficientFundsException.class,
                    () -> service.withdraw("A1", 500.0));

            // No repo append on failure
            verifyNoInteractions(repo);

            val.verify(() -> ValidationUtil.getValidAccountOrThrow(repo, "A1"));
            val.verify(() -> ValidationUtil.checkPositiveAmount(500.0));
            val.verify(() -> ValidationUtil.checkSufficientFunds(acc, 500.0));
        }
    }

    // -----------------------------------------------------------------------
    // transfer() – same account
    // -----------------------------------------------------------------------
    @Test
    void transfer_shouldThrowWhenSameSourceAndDestination() {
        assertThrows(IllegalArgumentException.class,
                () -> service.transfer("A1", "A1", 100.0));
        verifyNoInteractions(repo);
    }
}