package org.acme.recurring;

import java.time.LocalDateTime;
import java.util.List;

import org.acme.account.AccountRepository;
import org.acme.category.CategoryRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;

@ApplicationScoped
public class RecurringTransactionService {

    @Inject
    RecurringTransactionRepository recurringTransactionRepository;

    @Inject
    AccountRepository accountRepository;

    @Inject
    CategoryRepository categoryRepository;

    public List<RecurringTransaction> listAll() {
        return recurringTransactionRepository.listAll();
    }

    public RecurringTransaction findById(Long id) {
        RecurringTransaction rt = recurringTransactionRepository.findById(id);
        if (rt == null) {
            throw new WebApplicationException("Recurring transaction not found", 404);
        }
        return rt;
    }

    @Transactional
    public RecurringTransaction create(RecurringTransaction rt) {
        if (rt.account != null && rt.account.id != null) {
            rt.account = accountRepository.findById(rt.account.id);
        }
        if (rt.category != null && rt.category.id != null) {
            rt.category = categoryRepository.findById(rt.category.id);
        }
        recurringTransactionRepository.persist(rt);
        return rt;
    }

    @Transactional
    public RecurringTransaction update(Long id, RecurringTransaction updated) {
        RecurringTransaction rt = recurringTransactionRepository.findById(id);
        if (rt == null) {
            throw new WebApplicationException("Recurring transaction not found", 404);
        }
        rt.amount = updated.amount;
        rt.description = updated.description;
        rt.frequency = updated.frequency;
        rt.nextDueDate = updated.nextDueDate;
        rt.endDate = updated.endDate;
        rt.active = updated.active;
        rt.type = updated.type;
        if (updated.account != null && updated.account.id != null) {
            rt.account = accountRepository.findById(updated.account.id);
        }
        if (updated.category != null && updated.category.id != null) {
            rt.category = categoryRepository.findById(updated.category.id);
        }
        rt.updatedAt = LocalDateTime.now();
        return rt;
    }

    @Transactional
    public void delete(Long id) {
        RecurringTransaction rt = recurringTransactionRepository.findById(id);
        if (rt == null) {
            throw new WebApplicationException("Recurring transaction not found", 404);
        }
        recurringTransactionRepository.delete(rt);
    }
}
