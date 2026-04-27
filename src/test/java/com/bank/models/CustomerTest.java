package com.bank.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CustomerTest {

    @Test
    void testValidCustomerCreation() {
        Customer c = new Customer("C001", "Alice");
        assertEquals("C001", c.getCustomerId());
        assertEquals("Alice", c.getName());
    }

    @Test
    void testCustomerIdCannotBeBlank() {
        assertThrows(IllegalArgumentException.class, () -> new Customer("", "Alice"));
    }

    @Test
    void testNameCannotBeBlank() {
        assertThrows(IllegalArgumentException.class, () -> new Customer("C001", ""));
    }

    @Test
    void testSetName() {
        Customer c = new Customer("C001", "Alice");
        c.setName("Bob");
        assertEquals("Bob", c.getName());
    }
}