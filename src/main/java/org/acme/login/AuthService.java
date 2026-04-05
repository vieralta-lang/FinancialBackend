package org.acme.login;

import java.time.Duration;
import java.util.Set;

import org.acme.user.AppUser;
import org.acme.user.AppUserRepository;

import io.quarkus.elytron.security.common.BcryptUtil;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class AuthService {

    @Inject
    AppUserRepository appUserRepository;

    public String authenticate(String username, String password) {
        AppUser user = appUserRepository.findByUsername(username);
        if (user == null || !BcryptUtil.matches(password, user.passwordHash)) {
            return null;
        }
        return generateToken(user);
    }

    @Transactional
    public AppUser register(String name, String username, String email, String password) {
        if (appUserRepository.findByUsername(username) != null) {
            throw new IllegalArgumentException("Username already exists");
        }
        AppUser user = new AppUser();
        user.name = name;
        user.username = username;
        user.email = email;
        user.passwordHash = BcryptUtil.bcryptHash(password);
        appUserRepository.persist(user);
        return user;
    }

    @Transactional
    public void changePassword(String username, String currentPassword, String newPassword) {
        AppUser user = appUserRepository.findByUsername(username);
        if (user == null || !BcryptUtil.matches(currentPassword, user.passwordHash)) {
            throw new SecurityException("Current password is incorrect");
        }
        user.passwordHash = BcryptUtil.bcryptHash(newPassword);
        appUserRepository.persist(user);
    }

    private String generateToken(AppUser user) {
        return Jwt.issuer("myagenda")
                .upn(user.username)
                .claim("userId", user.id)
                .claim("name", user.name)
                .groups(Set.of("user"))
                .expiresIn(Duration.ofHours(24))
                .sign();
    }
}
