package com.bank.banking_service;

import com.bank.models.Account;
import com.bank.models.Transaction;
import com.bank.repository.BankRepositoryImpl;
import com.bank.service.BankServiceImpl;

import java.util.List;
import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        BankRepositoryImpl repo = new BankRepositoryImpl();
        BankServiceImpl service = new BankServiceImpl(repo);
        service.init(); 
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Welcome to the CLI Banking App");
            boolean running = true;

            while (running) {
                printMenu();
                System.out.print("Choose an option: ");
                System.out.flush(); 
                String choice = scanner.nextLine().trim();

                switch (choice) {
                    case "1" -> {
                        Account[] accounts = repo.getAllAccounts();
                        if (accounts == null || accounts.length == 0) {
                            System.out.println("No accounts available.");
                        } else {
                            System.out.println("Accounts:");
                            for (Account a : accounts) {
                                System.out.printf(
                                    "%s | Customer: %s | Balance: %.2f%n",
                                    a.getAccountId(),
                                    a.getCustomer().getName(),
                                    a.getBalance()
                                );
                            }
                        }
                    }

                    case "2" -> {
                        System.out.print("Enter account id: ");
                        String accId = scanner.nextLine().trim();
                        try {
                            double bal = service.getBalance(accId);
                            System.out.printf("Balance of %s: %.2f%n", accId, bal);
                        } catch (Exception e) {                            
                        	System.out.println(e.getMessage());
                        }
                    }

                    case "3" -> {
                        System.out.print("Enter account id: ");
                        String dAcc = scanner.nextLine().trim();
                        System.out.print("Enter amount to deposit: ");
                        String damt = scanner.nextLine().trim();
                        try {
                            double amount = Double.parseDouble(damt);
                            service.deposit(dAcc, amount);
                            System.out.printf(
                                "Deposited %.2f into %s. New balance: %.2f%n",
                                amount, dAcc, service.getBalance(dAcc)
                            );
                        } catch (NumberFormatException nfe) {
                            System.out.println("Invalid amount.");
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                        }
                    }

                    case "4" -> {
                        System.out.print("Enter account id: ");
                        String wAcc = scanner.nextLine().trim();
                        System.out.print("Enter amount to withdraw: ");
                        String wamt = scanner.nextLine().trim();
                        try {
                            double amount = Double.parseDouble(wamt);
                            service.withdraw(wAcc, amount);
                            System.out.printf(
                                "Withdrew %.2f from %s. New balance: %.2f%n",
                                amount, wAcc, service.getBalance(wAcc)
                            );
                        } catch (NumberFormatException nfe) {
                            System.out.println("Invalid amount.");
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                        }
                    }

                    case "5" -> {
                        System.out.print("Enter source account id: ");
                        String src = scanner.nextLine().trim();
                        System.out.print("Enter target account id: ");
                        String tgt = scanner.nextLine().trim();
                        System.out.print("Enter amount to transfer: ");
                        String tamt = scanner.nextLine().trim();
                        try {
                            double amount = Double.parseDouble(tamt);
                            service.transfer(src, tgt, amount);
                            System.out.printf(
                                "Transferred %.2f from %s to %s.%n",
                                amount, src, tgt
                            );
                            System.out.printf(
                                "New balance %s: %.2f | %s: %.2f%n",
                                src, service.getBalance(src), tgt, service.getBalance(tgt)
                            );
                        } catch (NumberFormatException nfe) {
                            System.out.println("Invalid amount.");
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                        }
                    }

                    case "6" -> {                        
                    	System.out.print("Enter account id: ");
                        String txAcc = scanner.nextLine().trim();
                        try {
                            List<Transaction> txns = service.getLast10Transactions(txAcc);
                            if (txns == null || txns.size() == 0) {
                                System.out.println("No transactions found.");
                            } else {
                                System.out.println("Latest 10 transactions:");
                                for (Transaction t : txns) {
                                    System.out.println(t);
                                }
                            }
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                        }
                    }

                    case "0" -> {
                        running = false;
                        System.out.println("Exiting. Goodbye.");
                    }

                    default -> System.out.println("Unknown option. Please try again.");
                }

                System.out.println();
            }
        }
    }

    private static void printMenu() {
        System.out.println("---- Menu ----");
        System.out.println("1) List all accounts");
        System.out.println("2) Get account balance");
        System.out.println("3) Deposit");
        System.out.println("4) Withdraw");
        System.out.println("5) Transfer");
        System.out.println("6) Show last N transactions");
        System.out.println("0) Exit");
    }
}