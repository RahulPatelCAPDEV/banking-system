package com.bank.util;

import com.bank.exceptions.DuplicateCustomerAccountException;
import com.bank.exceptions.InsufficientFundsException;
import com.bank.exceptions.InvalidAccountException;
import com.bank.exceptions.NegativeOrZeroAmountException;
import com.bank.models.Account;
import com.bank.models.Customer;
import com.bank.models.Transaction;
import com.bank.repository.BankRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ValidationUtilTest {

    @Mock
    private BankRepository bankRepository;

    /* -------------------------------------------------
       getValidAccountOrThrow
       ------------------------------------------------- */

    @Test
    void getValidAccountOrThrow_returnsAccountWhenPresent() {
        Account account = new Account("A001", new Customer("C001", "Alice"));
        when(bankRepository.getAccountById("A001")).thenReturn(account);

        Account result = ValidationUtil.getValidAccountOrThrow(bankRepository, "A001");

        assertSame(account, result);
        verify(bankRepository).getAccountById("A001");
        verifyNoMoreInteractions(bankRepository);
    }

    @Test
    void getValidAccountOrThrow_throwsForNullOrBlankId() {
        assertThrows(InvalidAccountException.class,
                () -> ValidationUtil.getValidAccountOrThrow(bankRepository, null));

        assertThrows(InvalidAccountException.class,
                () -> ValidationUtil.getValidAccountOrThrow(bankRepository, " "));

        verifyNoInteractions(bankRepository);
    } 

    @Test
    void getValidAccountOrThrow_throwsWhenAccountNotFound() {
        when(bankRepository.getAccountById("X999")).thenReturn(null);

        assertThrows(InvalidAccountException.class,
                () -> ValidationUtil.getValidAccountOrThrow(bankRepository, "X999"));

        verify(bankRepository).getAccountById("X999");
        verifyNoMoreInteractions(bankRepository);
    }

    /* -------------------------------------------------
       checkPositiveAmount
       ------------------------------------------------- */

    @Test
    void checkPositiveAmount_acceptsPositiveAmount() {
        assertDoesNotThrow(() -> ValidationUtil.checkPositiveAmount(0.01));
        assertDoesNotThrow(() -> ValidationUtil.checkPositiveAmount(100.0));
    }

    @Test
    void checkPositiveAmount_throwsForNullZeroOrNegative() {
        assertThrows(NegativeOrZeroAmountException.class,
                () -> ValidationUtil.checkPositiveAmount(null));

        assertThrows(NegativeOrZeroAmountException.class,
                () -> ValidationUtil.checkPositiveAmount(0.0));

        assertThrows(NegativeOrZeroAmountException.class,
                () -> ValidationUtil.checkPositiveAmount(-10.0));
    }

    /* -------------------------------------------------
       checkSufficientFundsOrThrow (Double version)
       ------------------------------------------------- */

    @Test
    void checkSufficientFundsOrThrow_passesWhenBalanceIsEnough() {
        assertDoesNotThrow(() ->
                ValidationUtil.checkSufficientFundsOrThrow(
                        500.0, 200.0, "A001"));
    }

    @Test
    void checkSufficientFundsOrThrow_throwsWhenInsufficientBalance() {
        assertThrows(InsufficientFundsException.class,
                () -> ValidationUtil.checkSufficientFundsOrThrow(
                        100.0, 150.0, "A001"));
    }

    /* -------------------------------------------------
       checkNoDuplicateCustomerAccount
       ------------------------------------------------- */

    @Test
    void checkNoDuplicateCustomerAccount_throwsWhenDuplicateCustomerFound() {
        Account[] accounts = {
                new Account("A1", new Customer("C1", "John")),
                new Account("A2", new Customer("C1", "John"))
        };

        assertThrows(DuplicateCustomerAccountException.class,
                () -> ValidationUtil.checkNoDuplicateCustomerAccount(accounts));
    }

    @Test
    void checkNoDuplicateCustomerAccount_passesWhenCustomersAreUnique() {
        Account[] accounts = {
                new Account("A1", new Customer("C1", "John")),
                new Account("A2", new Customer("C2", "Jane"))
        };

        assertDoesNotThrow(() ->
                ValidationUtil.checkNoDuplicateCustomerAccount(accounts));
    }

    
    @Test
    void latest10_handlesNullInput() {
        Transaction[] result = ValidationUtil.latest10(null);
        assertNotNull(result);
        assertEquals(0, result.length);
    }

}