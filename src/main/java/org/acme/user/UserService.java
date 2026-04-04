package org.acme.user;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;

@ApplicationScoped
public class UserService {

    @Inject
    AppUserRepository appUserRepository;

    public List<AppUser> listAll() {
        return appUserRepository.listAll();
    }

    public AppUser findById(Long id) {
        AppUser user = appUserRepository.findById(id);
        if (user == null) {
            throw new WebApplicationException("User not found", 404);
        }
        return user;
    }

    @Transactional
    public AppUser create(AppUser user) {
        appUserRepository.persist(user);
        return user;
    }

    @Transactional
    public AppUser update(Long id, AppUser updated) {
        AppUser user = appUserRepository.findById(id);
        if (user == null) {
            throw new WebApplicationException("User not found", 404);
        }
        user.name = updated.name;
        user.email = updated.email;
        user.updatedAt = LocalDateTime.now();
        return user;
    }

    @Transactional
    public void delete(Long id) {
        AppUser user = appUserRepository.findById(id);
        if (user == null) {
            throw new WebApplicationException("User not found", 404);
        }
        appUserRepository.delete(user);
    }
}
