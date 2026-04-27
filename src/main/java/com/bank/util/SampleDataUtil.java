
package com.bank.util;


import com.bank.models.Account;
import com.bank.models.Customer;

public class SampleDataUtil {

    
        public static Account[] createSampleAccounts() {

            Customer c1 = new Customer("C1", "Alice");
            Customer c2 = new Customer("C2", "Bob");
            Customer c3 = new Customer("C3", "Charlie");
            Customer c4 = new Customer("C4", "David");
            Customer c5 = new Customer("C5", "Emma");
            Customer c6 = new Customer("C6", "Frank");
            Customer c7 = new Customer("C7", "Grace");
            Customer c8 = new Customer("C8", "Helen");
            Customer c9 = new Customer("C9", "Ian");
            Customer c10 = new Customer("C10", "Jack");

            Account a1 = new Account("A1", c1);
            Account a2 = new Account("A2", c2);
            Account a3 = new Account("A3", c3);
            Account a4 = new Account("A4", c4);
            Account a5 = new Account("A5", c5);
            Account a6 = new Account("A6", c6);
            Account a7 = new Account("A7", c7);
            Account a8 = new Account("A8", c8);
            Account a9 = new Account("A9", c9);
            Account a10 = new Account("A10", c10);

            a1.setBalance(1200.50);
            a2.setBalance(250.00);
            a3.setBalance(9800.00);
            a4.setBalance(1000.00);
            a5.setBalance(500.00);
            a6.setBalance(300.00);
            a7.setBalance(5000.00);
            a8.setBalance(150.99);
            a9.setBalance(720.00);
            a10.setBalance(220.00);


            return new Account[]{
                    a1, a2, a3, a4, a5,
                    a6, a7, a8, a9, a10
            };
        }
    }




