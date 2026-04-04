package org.acme.subscription;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;

@ApplicationScoped
public class SubscriptionService {

    @Inject
    SubscriptionRepository subscriptionRepository;

    public List<Subscription> listAll() {
        return subscriptionRepository.listAll();
    }

    public Subscription findById(Long id) {
        Subscription sub = subscriptionRepository.findById(id);
        if (sub == null) {
            throw new WebApplicationException("Subscription not found", 404);
        }
        return sub;
    }

    @Transactional
    public Subscription create(Subscription subscription) {
        subscriptionRepository.persist(subscription);
        return subscription;
    }

    @Transactional
    public Subscription update(Long id, Subscription updated) {
        Subscription sub = subscriptionRepository.findById(id);
        if (sub == null) {
            throw new WebApplicationException("Subscription not found", 404);
        }
        sub.name = updated.name;
        sub.amount = updated.amount;
        sub.frequency = updated.frequency;
        sub.category = updated.category;
        sub.nextBillingDate = updated.nextBillingDate;
        sub.active = updated.active;
        sub.notes = updated.notes;
        sub.updatedAt = LocalDateTime.now();
        return sub;
    }

    @Transactional
    public void delete(Long id) {
        Subscription sub = subscriptionRepository.findById(id);
        if (sub == null) {
            throw new WebApplicationException("Subscription not found", 404);
        }
        subscriptionRepository.delete(sub);
    }
}
