package org.acme.account;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.acme.transaction.Transaction;
import org.acme.transaction.TransactionRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;

@ApplicationScoped
public class AccountService {

    @Inject
    AccountRepository accountRepository;

    @Inject
    TransactionRepository transactionRepository;

    @Transactional
    public List<Account> listAll() {
        List<Account> accounts = accountRepository.listAll();
        accounts.forEach(this::recalculate);
        return accounts;
    }

    @Transactional
    public Account findById(Long id) {
        Account account = accountRepository.findById(id);
        if (account == null) {
            throw new WebApplicationException("Account not found", 404);
        }
        recalculate(account);
        return account;
    }

    @Transactional
    public Account create(Account account) {
        account.currentBalance = account.initialBalance;
        accountRepository.persist(account);
        return account;
    }

    @Transactional
    public Account update(Long id, Account updated) {
        Account account = accountRepository.findById(id);
        if (account == null) {
            throw new WebApplicationException("Account not found", 404);
        }
        account.name = updated.name;
        account.type = updated.type;
        account.currency = updated.currency;
        account.active = updated.active;
        account.updatedAt = LocalDateTime.now();
        return account;
    }

    @Transactional
    public void delete(Long id) {
        Account account = accountRepository.findById(id);
        if (account == null) {
            throw new WebApplicationException("Account not found", 404);
        }
        accountRepository.delete(account);
    }

    public void recalculate(Account account) {
        List<Transaction> transactions = transactionRepository.listByAccountIdUnordered(account.id);
        BigDecimal balance = account.initialBalance;
        for (Transaction tx : transactions) {
            switch (tx.type) {
                case INCOME -> balance = balance.add(tx.amount);
                case EXPENSE -> balance = balance.subtract(tx.amount);
                case TRANSFER -> {
                    if (tx.description != null && tx.description.startsWith("Transfer from")) {
                        balance = balance.add(tx.amount);
                    } else {
                        balance = balance.subtract(tx.amount);
                    }
                }
            }
        }
        if (account.currentBalance.compareTo(balance) != 0) {
            account.currentBalance = balance;
            account.updatedAt = LocalDateTime.now();
        }
    }
}
