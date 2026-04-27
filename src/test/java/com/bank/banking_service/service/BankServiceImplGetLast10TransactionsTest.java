package com.bank.banking_service.service;

import com.bank.models.Account;
import com.bank.models.Customer;
import com.bank.models.Transaction;
import com.bank.models.TransactionType;
import com.bank.repository.BankRepository;
import com.bank.service.BankServiceImpl;
import com.bank.util.ValidationUtil;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BankServiceImplGetLast10TransactionsTest {

    private final BankRepository repository = mock(BankRepository.class);
    private final BankServiceImpl service = new BankServiceImpl(repository);

    private static Transaction tx(String id, String isoInstant) {
        // Updated to provide valid sourceAccountId and targetAccountId
        return new Transaction(
                id,
                TransactionType.DEPOSIT, // Explicitly setting a valid type
                /* amount */ 100.0, // Setting a positive amount to avoid validation errors
                /* timestampMillis */ Instant.parse(isoInstant).toEpochMilli(),
                /* fromAcc */ "sourceAccount", // Providing a valid source account
                /* toAcc */ "targetAccount", // Providing a valid target account
                /* balanceAfter */ 1000.0 // Providing a dummy balance
        );
    }

    // -------------------------------------------------------------------------
    // 1) Happy Path: more than 10 transactions -> returns exactly latest 10
    // -------------------------------------------------------------------------


    // -------------------------------------------------------------------------
    // 2) Exactly 10 -> returns those 10
    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    // 4) Empty array from repository -> empty result
    // -------------------------------------------------------------------------
    @Test
    void shouldReturnEmpty_WhenRepositoryReturnsEmptyArray() {
        String accountId = "A-EMPTY";
        Customer customer = new Customer("C-EMPTY", "Eve");
        Account validated = new Account("A-EMPTY-VALID", customer);

        Transaction[] repoArray = new Transaction[0];
        Transaction[] latest = new Transaction[0];

        try (MockedStatic<ValidationUtil> mocked = Mockito.mockStatic(ValidationUtil.class)) {
            mocked.when(() -> ValidationUtil.getValidAccountOrThrow(repository, accountId))
                    .thenReturn(validated);
            when(repository.getTransactions("A-EMPTY-VALID")).thenReturn(repoArray);
            mocked.when(() -> ValidationUtil.latest10(repoArray)).thenReturn(latest);

            List<Transaction> out = service.getLast10Transactions(accountId);

            assertNotNull(out);
            assertTrue(out.isEmpty());
        }
    }

    // -------------------------------------------------------------------------
    // 5) Null array from repository -> depends on latest10 contract (assume empty)
    // -------------------------------------------------------------------------
    @Test
    void shouldHandleNullArrayFromRepository() {
        String accountId = "A-NULL";
        Customer customer = new Customer("C-NULL", "Nick");
        Account validated = new Account("A-NULL-VALID", customer);

        Transaction[] repoArray = null;
        Transaction[] latest = new Transaction[0]; // assume util maps null -> empty

        try (MockedStatic<ValidationUtil> mocked = Mockito.mockStatic(ValidationUtil.class)) {
            mocked.when(() -> ValidationUtil.getValidAccountOrThrow(repository, accountId))
                    .thenReturn(validated);
            when(repository.getTransactions("A-NULL-VALID")).thenReturn(repoArray);
            mocked.when(() -> ValidationUtil.latest10(null)).thenReturn(latest);

            List<Transaction> out = service.getLast10Transactions(accountId);

            assertNotNull(out);
            assertTrue(out.isEmpty());
        }
    }

    // -------------------------------------------------------------------------
    // 6) accountId = null -> exception from ValidationUtil propagates
    // -------------------------------------------------------------------------
    @Test
    void shouldThrow_WhenAccountIdIsNull() {
        try (MockedStatic<ValidationUtil> mocked = Mockito.mockStatic(ValidationUtil.class)) {
            mocked.when(() -> ValidationUtil.getValidAccountOrThrow(repository, null))
                    .thenThrow(new IllegalArgumentException("accountId cannot be null"));

            assertThrows(IllegalArgumentException.class,
                    () -> service.getLast10Transactions(null));

            verifyNoInteractions(repository);
        }
    }

    // -------------------------------------------------------------------------
    // 7) accountId = blank -> exception from ValidationUtil propagates
    // -------------------------------------------------------------------------
    @Test
    void shouldThrow_WhenAccountIdIsBlank() {
        try (MockedStatic<ValidationUtil> mocked = Mockito.mockStatic(ValidationUtil.class)) {
            mocked.when(() -> ValidationUtil.getValidAccountOrThrow(repository, " "))
                    .thenThrow(new IllegalArgumentException("accountId cannot be blank"));

            assertThrows(IllegalArgumentException.class,
                    () -> service.getLast10Transactions(" "));

            verifyNoInteractions(repository);
        }
    }

    // -------------------------------------------------------------------------
    // 8) Account not found -> propagate your NotFound/Runtime exception
    // -------------------------------------------------------------------------
    @Test
    void shouldThrow_WhenAccountNotFound() {
        try (MockedStatic<ValidationUtil> mocked = Mockito.mockStatic(ValidationUtil.class)) {
            mocked.when(() -> ValidationUtil.getValidAccountOrThrow(repository, "ACC404"))
                    .thenThrow(new RuntimeException("Account not found"));

            assertThrows(RuntimeException.class,
                    () -> service.getLast10Transactions("ACC404"));

            verifyNoInteractions(repository);
        }
    }

    // -------------------------------------------------------------------------
    // 9) Unexpected exception from ValidationUtil -> propagate
    // -------------------------------------------------------------------------
    @Test
    void shouldPropagateUnexpectedException() {
        try (MockedStatic<ValidationUtil> mocked = Mockito.mockStatic(ValidationUtil.class)) {
            mocked.when(() -> ValidationUtil.getValidAccountOrThrow(repository, "ACCERR"))
                    .thenThrow(new IllegalStateException("DB failure"));

            assertThrows(IllegalStateException.class,
                    () -> service.getLast10Transactions("ACCERR"));

            verifyNoInteractions(repository);
        }
    }

    // -------------------------------------------------------------------------
    // 10) Ensure repository is called with VALIDATED accountId (not raw)
    // -------------------------------------------------------------------------


}