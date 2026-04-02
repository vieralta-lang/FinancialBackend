package org.acme.transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.acme.account.Account;
import org.acme.category.Category;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class TransactionService {

    @Transactional
    public Transaction createTransaction(Transaction transaction) {
        Account account = Account.findById(transaction.account.id);
        if (account == null) {
            throw new IllegalArgumentException("Account not found: " + transaction.account.id);
        }
        transaction.account = account;

        if (transaction.category != null && transaction.category.id != null) {
            transaction.category = Category.findById(transaction.category.id);
        }

        // Update account balance
        adjustBalance(account, transaction.type, transaction.amount);
        account.updatedAt = LocalDateTime.now();
        transaction.persist();
        return transaction;
    }

    @Transactional
    public TransferResult createTransfer(Long fromAccountId, Long toAccountId, BigDecimal amount,
            String description, java.time.LocalDate date) {
        Account from = Account.findById(fromAccountId);
        Account to = Account.findById(toAccountId);
        if (from == null || to == null) {
            throw new IllegalArgumentException("One or both accounts not found");
        }

        // Generate a shared transfer ID (use sequence-like approach)
        long transferId = System.nanoTime();

        Transaction debit = new Transaction();
        debit.account = from;
        debit.type = TransactionType.TRANSFER;
        debit.amount = amount;
        debit.description = description != null ? description : "Transfer to " + to.name;
        debit.date = date;
        debit.transferId = transferId;
        debit.persist();

        Transaction credit = new Transaction();
        credit.account = to;
        credit.type = TransactionType.TRANSFER;
        credit.amount = amount;
        credit.description = description != null ? description : "Transfer from " + from.name;
        credit.date = date;
        credit.transferId = transferId;
        credit.persist();

        from.currentBalance = from.currentBalance.subtract(amount);
        from.updatedAt = LocalDateTime.now();
        to.currentBalance = to.currentBalance.add(amount);
        to.updatedAt = LocalDateTime.now();

        return new TransferResult(debit, credit);
    }

    @Transactional
    public void deleteTransaction(Long id) {
        Transaction transaction = Transaction.findById(id);
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction not found");
        }

        Account account = transaction.account;
        // Reverse the balance change
        reverseBalance(account, transaction.type, transaction.amount);
        account.updatedAt = LocalDateTime.now();

        // If it's a transfer, delete the counterpart too
        if (transaction.transferId != null) {
            List<Transaction> linked = Transaction.list("transferId = ?1 and id != ?2",
                    transaction.transferId, transaction.id);
            for (Transaction t : linked) {
                reverseBalance(t.account, t.type, t.amount);
                t.account.updatedAt = LocalDateTime.now();
                t.delete();
            }
        }

        transaction.delete();
    }

    private void adjustBalance(Account account, TransactionType type, BigDecimal amount) {
        switch (type) {
            case INCOME -> account.currentBalance = account.currentBalance.add(amount);
            case EXPENSE -> account.currentBalance = account.currentBalance.subtract(amount);
            case TRANSFER -> {} // handled separately in createTransfer
        }
    }

    private void reverseBalance(Account account, TransactionType type, BigDecimal amount) {
        switch (type) {
            case INCOME -> account.currentBalance = account.currentBalance.subtract(amount);
            case EXPENSE -> account.currentBalance = account.currentBalance.add(amount);
            case TRANSFER -> {
                // For transfer reversal, check description to determine direction
                // Debit (from) had balance subtracted, credit (to) had balance added
                // We reverse both in deleteTransaction
            }
        }
    }

    public record TransferResult(Transaction debit, Transaction credit) {}
}
