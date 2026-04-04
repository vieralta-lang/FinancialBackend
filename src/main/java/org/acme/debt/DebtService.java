package org.acme.debt;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.acme.account.Account;
import org.acme.account.AccountRepository;
import org.acme.transaction.Transaction;
import org.acme.transaction.TransactionRepository;
import org.acme.transaction.TransactionType;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;

@ApplicationScoped
public class DebtService {

    @Inject
    DebtRepository debtRepository;

    @Inject
    AccountRepository accountRepository;

    @Inject
    TransactionRepository transactionRepository;

    public List<Debt> listAll() {
        return debtRepository.listAll();
    }

    public Debt findById(Long id) {
        Debt debt = debtRepository.findById(id);
        if (debt == null) {
            throw new WebApplicationException("Debt not found", 404);
        }
        return debt;
    }

    @Transactional
    public Debt create(Debt debt) {
        debtRepository.persist(debt);
        return debt;
    }

    @Transactional
    public Debt update(Long id, Debt updated) {
        Debt debt = debtRepository.findById(id);
        if (debt == null) {
            throw new WebApplicationException("Debt not found", 404);
        }
        debt.person = updated.person;
        debt.direction = updated.direction;
        debt.amount = updated.amount;
        debt.description = updated.description;
        debt.dueDate = updated.dueDate;
        debt.paid = updated.paid;
        debt.updatedAt = LocalDateTime.now();
        return debt;
    }

    @Transactional
    public void delete(Long id) {
        Debt debt = debtRepository.findById(id);
        if (debt == null) {
            throw new WebApplicationException("Debt not found", 404);
        }
        debtRepository.delete(debt);
    }

    @Transactional
    public Debt pay(Long id, Long accountId) {
        Debt debt = debtRepository.findById(id);
        if (debt == null) {
            throw new WebApplicationException("Debt not found", 404);
        }
        if (debt.paid) {
            throw new WebApplicationException("Debt is already paid", 400);
        }
        Account account = accountRepository.findById(accountId);
        if (account == null) {
            throw new WebApplicationException("Account not found", 404);
        }

        Transaction tx = new Transaction();
        tx.account = account;
        tx.amount = debt.amount;
        tx.date = LocalDate.now();
        tx.type = (debt.direction == DebtDirection.I_OWE) ? TransactionType.EXPENSE : TransactionType.INCOME;
        tx.description = (debt.direction == DebtDirection.I_OWE)
                ? "Pagamento dívida: " + debt.person
                : "Recebimento dívida: " + debt.person;
        transactionRepository.persist(tx);

        debt.paid = true;
        debt.paidWithAccount = account;
        debt.updatedAt = LocalDateTime.now();
        return debt;
    }

    @Transactional
    public Debt reopen(Long id) {
        Debt debt = debtRepository.findById(id);
        if (debt == null) {
            throw new WebApplicationException("Debt not found", 404);
        }
        if (!debt.paid) {
            throw new WebApplicationException("Debt is not paid", 400);
        }

        if (debt.paidWithAccount != null) {
            Account account = debt.paidWithAccount;
            String descPrefix = (debt.direction == DebtDirection.I_OWE)
                    ? "Pagamento dívida: " + debt.person
                    : "Recebimento dívida: " + debt.person;
            transactionRepository.deleteByAccountAndDescriptionAndAmount(account, descPrefix, debt.amount);
        }

        debt.paid = false;
        debt.paidWithAccount = null;
        debt.updatedAt = LocalDateTime.now();
        return debt;
    }
}
