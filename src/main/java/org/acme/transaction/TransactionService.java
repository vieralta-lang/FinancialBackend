package org.acme.transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.acme.account.Account;
import org.acme.account.AccountRepository;
import org.acme.category.CategoryRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class TransactionService {

    @Inject
    TransactionRepository transactionRepository;

    @Inject
    AccountRepository accountRepository;

    @Inject
    CategoryRepository categoryRepository;

    public List<Transaction> list(Long accountId, TransactionType type, LocalDate from, LocalDate to) {
        if (accountId != null && from != null && to != null) {
            return transactionRepository.listByAccountAndDateRange(accountId, from, to);
        }
        if (accountId != null) {
            return transactionRepository.listByAccountId(accountId);
        }
        if (type != null) {
            return transactionRepository.listByType(type);
        }
        if (from != null && to != null) {
            return transactionRepository.listByDateRange(from, to);
        }
        return transactionRepository.listAllOrderByDateDesc();
    }

    public Transaction findById(Long id) {
        Transaction transaction = transactionRepository.findById(id);
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction not found");
        }
        return transaction;
    }

    @Transactional
    public Transaction createTransaction(Transaction transaction) {
        Account account = accountRepository.findById(transaction.account.id);
        if (account == null) {
            throw new IllegalArgumentException("Account not found: " + transaction.account.id);
        }
        transaction.account = account;

        if (transaction.category != null && transaction.category.id != null) {
            transaction.category = categoryRepository.findById(transaction.category.id);
        }

        adjustBalance(account, transaction.type, transaction.amount);
        account.updatedAt = LocalDateTime.now();
        transactionRepository.persist(transaction);
        return transaction;
    }

    @Transactional
    public Transaction updateTransaction(Long id, Transaction updated) {
        Transaction existing = transactionRepository.findById(id);
        if (existing == null) {
            throw new IllegalArgumentException("Transaction not found");
        }

        reverseBalance(existing.account, existing.type, existing.amount);
        existing.account.updatedAt = LocalDateTime.now();

        if (updated.account != null && updated.account.id != null) {
            Account newAccount = accountRepository.findById(updated.account.id);
            if (newAccount == null) {
                throw new IllegalArgumentException("Account not found: " + updated.account.id);
            }
            existing.account = newAccount;
        }
        if (updated.category != null && updated.category.id != null) {
            existing.category = categoryRepository.findById(updated.category.id);
        } else {
            existing.category = null;
        }
        existing.type = updated.type;
        existing.amount = updated.amount;
        existing.date = updated.date;
        existing.description = updated.description;
        existing.updatedAt = LocalDateTime.now();

        adjustBalance(existing.account, existing.type, existing.amount);
        existing.account.updatedAt = LocalDateTime.now();

        return existing;
    }

    @Transactional
    public TransferResult createTransfer(Long fromAccountId, Long toAccountId, BigDecimal amount,
            String description, LocalDate date) {
        Account from = accountRepository.findById(fromAccountId);
        Account to = accountRepository.findById(toAccountId);
        if (from == null || to == null) {
            throw new IllegalArgumentException("One or both accounts not found");
        }

        long transferId = System.nanoTime();

        String debitDesc = "Transfer to " + to.name;
        String creditDesc = "Transfer from " + from.name;
        if (description != null && !description.isBlank()) {
            debitDesc += " - " + description;
            creditDesc += " - " + description;
        }

        Transaction debit = new Transaction();
        debit.account = from;
        debit.type = TransactionType.TRANSFER;
        debit.amount = amount;
        debit.description = debitDesc;
        debit.date = date;
        debit.transferId = transferId;
        transactionRepository.persist(debit);

        Transaction credit = new Transaction();
        credit.account = to;
        credit.type = TransactionType.TRANSFER;
        credit.amount = amount;
        credit.description = creditDesc;
        credit.date = date;
        credit.transferId = transferId;
        transactionRepository.persist(credit);

        from.currentBalance = from.currentBalance.subtract(amount);
        from.updatedAt = LocalDateTime.now();
        to.currentBalance = to.currentBalance.add(amount);
        to.updatedAt = LocalDateTime.now();

        return new TransferResult(debit, credit);
    }

    @Transactional
    public void deleteTransaction(Long id) {
        Transaction transaction = transactionRepository.findById(id);
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction not found");
        }

        Account account = transaction.account;
        reverseBalance(account, transaction.type, transaction.amount);
        account.updatedAt = LocalDateTime.now();

        if (transaction.transferId != null) {
            List<Transaction> linked = transactionRepository
                    .findByTransferIdExcluding(transaction.transferId, transaction.id);
            for (Transaction t : linked) {
                reverseBalance(t.account, t.type, t.amount);
                t.account.updatedAt = LocalDateTime.now();
                transactionRepository.delete(t);
            }
        }

        transactionRepository.delete(transaction);
    }

    private void adjustBalance(Account account, TransactionType type, BigDecimal amount) {
        switch (type) {
            case INCOME -> account.currentBalance = account.currentBalance.add(amount);
            case EXPENSE -> account.currentBalance = account.currentBalance.subtract(amount);
            case TRANSFER -> {
            }
        }
    }

    private void reverseBalance(Account account, TransactionType type, BigDecimal amount) {
        switch (type) {
            case INCOME -> account.currentBalance = account.currentBalance.subtract(amount);
            case EXPENSE -> account.currentBalance = account.currentBalance.add(amount);
            case TRANSFER -> {
            }
        }
    }

    public record TransferResult(Transaction debit, Transaction credit) {
    }
}
