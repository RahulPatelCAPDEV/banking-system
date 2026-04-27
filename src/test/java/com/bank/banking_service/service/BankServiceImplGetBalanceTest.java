package com.bank.banking_service.service;

import com.bank.models.*;
import com.bank.repository.BankRepository;
import com.bank.service.BankServiceImpl;
import com.bank.util.ValidationUtil;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
        import static org.mockito.Mockito.*;

class BankServiceImplGetBalanceTest {

    private final BankRepository repository = mock(BankRepository.class);
    private final BankServiceImpl service = new BankServiceImpl(repository);

    // -------------------------------------------------------------------------
    // 1. Happy Path - Valid Account Returns Positive Balance
    // -------------------------------------------------------------------------
    @Test
    void shouldReturnBalance_WhenAccountIsValid() {

        Customer customer = new Customer("C001", "John Doe");
        Account account = new Account("ACC123", customer);

        long now = System.currentTimeMillis();
        account.appendTransaction(new Transaction(
                "T001",
                TransactionType.DEPOSIT,
                1000.0,
                now,
                "ACC123",
                null,
                1000.0
        ));

        try (MockedStatic<ValidationUtil> mocked = Mockito.mockStatic(ValidationUtil.class)) {

            mocked.when(() -> ValidationUtil.getValidAccountOrThrow(repository, "ACC123"))
                    .thenReturn(account);

            double result = service.getBalance("ACC123");

            assertEquals(1000.0, result);
        }
    }

    // -------------------------------------------------------------------------
    // 2. Happy Path - Zero Balance
    // -------------------------------------------------------------------------
    @Test
    void shouldReturnZeroBalance_WhenNoTransactions() {

        Customer customer = new Customer("C002", "Alice");
        Account account = new Account("ACC999", customer); // default balance = 0

        try (MockedStatic<ValidationUtil> mocked = Mockito.mockStatic(ValidationUtil.class)) {
            mocked.when(() -> ValidationUtil.getValidAccountOrThrow(repository, "ACC999"))
                    .thenReturn(account);

            assertEquals(0.0, service.getBalance("ACC999"));
        }
    }

    // -------------------------------------------------------------------------
    // 3. Negative Balance (Overdraft)
    // -------------------------------------------------------------------------
    @Test
    void shouldReturnNegativeBalance_WhenOverdraft() {

        Customer customer = new Customer("C003", "Sam");
        Account account = new Account("ACC777", customer);

        long now = System.currentTimeMillis();
        // Withdrawal leading to negative balance
        account.appendTransaction(new Transaction(
                "T002",
                TransactionType.WITHDRAWAL,
                1500.0,
                now,
                "ACC777",
                null,
                -500.0
        ));

        try (MockedStatic<ValidationUtil> mocked = Mockito.mockStatic(ValidationUtil.class)) {
            mocked.when(() -> ValidationUtil.getValidAccountOrThrow(repository, "ACC777"))
                    .thenReturn(account);

            assertEquals(-500.0, service.getBalance("ACC777"));
        }
    }

    // -------------------------------------------------------------------------
    // 4. accountId = null → expect exception
    // -------------------------------------------------------------------------
    @Test
    void shouldThrowException_WhenAccountIdIsNull() {

        try (MockedStatic<ValidationUtil> mocked = Mockito.mockStatic(ValidationUtil.class)) {

            mocked.when(() -> ValidationUtil.getValidAccountOrThrow(repository, null))
                    .thenThrow(new IllegalArgumentException("accountId cannot be null"));

            assertThrows(IllegalArgumentException.class,
                    () -> service.getBalance(null));
        }
    }

    // -------------------------------------------------------------------------
    // 5. accountId = blank → expect exception
    // -------------------------------------------------------------------------
    @Test
    void shouldThrowException_WhenAccountIdIsBlank() {

        try (MockedStatic<ValidationUtil> mocked = Mockito.mockStatic(ValidationUtil.class)) {

            mocked.when(() -> ValidationUtil.getValidAccountOrThrow(repository, " "))
                    .thenThrow(new IllegalArgumentException("accountId cannot be blank"));

            assertThrows(IllegalArgumentException.class,
                    () -> service.getBalance(" "));
        }
    }

    // -------------------------------------------------------------------------
    // 6. Account does not exist → NotFoundException (or your exception type)
    // -------------------------------------------------------------------------
    @Test
    void shouldThrowException_WhenAccountNotFound() {

        try (MockedStatic<ValidationUtil> mocked = Mockito.mockStatic(ValidationUtil.class)) {

            mocked.when(() -> ValidationUtil.getValidAccountOrThrow(repository, "ACC404"))
                    .thenThrow(new RuntimeException("Account not found"));

            assertThrows(RuntimeException.class,
                    () -> service.getBalance("ACC404"));
        }
    }

    // -------------------------------------------------------------------------
    // 7. Unexpected exception from ValidationUtil should propagate
    // -------------------------------------------------------------------------
    @Test
    void shouldPropagateUnexpectedException() {

        try (MockedStatic<ValidationUtil> mocked = Mockito.mockStatic(ValidationUtil.class)) {

            mocked.when(() -> ValidationUtil.getValidAccountOrThrow(repository, "ACCERR"))
                    .thenThrow(new IllegalStateException("DB failure"));

            assertThrows(IllegalStateException.class,
                    () -> service.getBalance("ACCERR"));
        }
    }

    // -------------------------------------------------------------------------
    // 8. Very large balance
    // -------------------------------------------------------------------------
    @Test
    void shouldReturnVeryLargeBalance() {

        Customer customer = new Customer("C100", "Rich Guy");
        Account account = new Account("BIG123", customer);

        long now = System.currentTimeMillis();
        account.appendTransaction(new Transaction(
                "T100",
                TransactionType.DEPOSIT,
                1.0,
                now,
                "BIG123",
                null,
                Double.MAX_VALUE
        ));

        try (MockedStatic<ValidationUtil> mocked = Mockito.mockStatic(ValidationUtil.class)) {
            mocked.when(() -> ValidationUtil.getValidAccountOrThrow(repository, "BIG123"))
                    .thenReturn(account);

            assertEquals(Double.MAX_VALUE, service.getBalance("BIG123"));
        }
    }
}