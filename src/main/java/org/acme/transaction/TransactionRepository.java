package org.acme.transaction;

import java.time.LocalDate;
import java.util.List;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TransactionRepository implements PanacheRepository<Transaction> {

    public List<Transaction> listByAccountId(Long accountId) {
        return list("account.id = ?1 order by date desc", accountId);
    }

    public List<Transaction> listByType(TransactionType type) {
        return list("type = ?1 order by date desc", type);
    }

    public List<Transaction> listByDateRange(LocalDate from, LocalDate to) {
        return list("date >= ?1 and date <= ?2 order by date desc", from, to);
    }

    public List<Transaction> listByAccountAndDateRange(Long accountId, LocalDate from, LocalDate to) {
        return list("account.id = ?1 and date >= ?2 and date <= ?3 order by date desc", accountId, from, to);
    }

    public List<Transaction> listAllOrderByDateDesc() {
        return list("order by date desc");
    }

    public List<Transaction> listByAccountIdUnordered(Long accountId) {
        return list("account.id", accountId);
    }

    public List<Transaction> findByTransferIdExcluding(Long transferId, Long excludeId) {
        return list("transferId = ?1 and id != ?2", transferId, excludeId);
    }

    public List<Transaction> listByCategoryAndTypeAndDateRange(Long categoryId, TransactionType type,
            LocalDate from, LocalDate to) {
        return list("category.id = ?1 and type = ?2 and date >= ?3 and date <= ?4",
                categoryId, type, from, to);
    }

    public List<Transaction> listByTypeAndDateRange(TransactionType type, LocalDate from, LocalDate to) {
        return list("date >= ?1 and date <= ?2 and type = ?3", from, to, type);
    }

    public List<Transaction> listByAccountAndDateRangeAsc(Long accountId, LocalDate from, LocalDate to) {
        return list("account.id = ?1 and date >= ?2 and date <= ?3 order by date asc", accountId, from, to);
    }

    public List<Transaction> listByDateRangeAsc(LocalDate from, LocalDate to) {
        return list("date >= ?1 and date <= ?2 order by date asc", from, to);
    }

    public long deleteByAccountAndDescriptionAndAmount(
            org.acme.account.Account account, String description, java.math.BigDecimal amount) {
        return delete("account = ?1 and description = ?2 and amount = ?3", account, description, amount);
    }
}
