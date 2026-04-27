package com.bank.models;

public class Transaction {

    private final String transactionId;
    private final TransactionType type;
    private final double amount;
    private final long timestamp;
    private final String sourceAccountId;
    private final String targetAccountId;
    private final double balanceAfter;

    public Transaction(String transactionId, TransactionType type, double amount,
                       long timestamp, String sourceAccountId,
                       String targetAccountId, double balanceAfter) {

        if (transactionId == null || transactionId.trim().isEmpty()) {
            throw new IllegalArgumentException("transactionId cannot be blank");
        }
        if (type == null) {
            throw new IllegalArgumentException("type cannot be null");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }

        this.transactionId = transactionId;
        this.type = type;
        this.amount = amount;
        this.timestamp = timestamp;
        this.sourceAccountId = sourceAccountId;
        this.targetAccountId = targetAccountId;
        this.balanceAfter = balanceAfter;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public TransactionType getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getSourceAccountId() {
        return sourceAccountId;
    }

    public String getTargetAccountId() {
        return targetAccountId;
    }

    public double getBalanceAfter() {
        return balanceAfter;
    }
    @Override
    public String toString() {
        return String.format(
                "[%s] %s | amount=%.2f | src=%s | dst=%s | balAfter=%.2f | ts=%d",
                transactionId,
                type,
                amount,
                sourceAccountId,
                targetAccountId,
                balanceAfter,
                timestamp
        );
    }
}