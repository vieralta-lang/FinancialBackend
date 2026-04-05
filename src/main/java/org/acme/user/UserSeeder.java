package org.acme.user;

import java.util.List;

import org.acme.account.Account;
import org.acme.account.AccountRepository;

import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class UserSeeder {

    @Inject
    AppUserRepository appUserRepository;

    @Inject
    AccountRepository accountRepository;

    @Transactional
    void onStart(@Observes StartupEvent ev) {
        AppUser felipe = appUserRepository.findByName("Felipe");
        if (felipe == null) {
            felipe = new AppUser();
            felipe.name = "Felipe";
            felipe.username = "felipe";
            felipe.email = "felipe@email.com";
            felipe.passwordHash = BcryptUtil.bcryptHash("123456");
            appUserRepository.persist(felipe);
        }
        // backfill username/password for existing user without them
        if (felipe.username == null) {
            felipe.username = "felipe";
        }
        if (felipe.passwordHash == null) {
            felipe.passwordHash = BcryptUtil.bcryptHash("123456");
        }

        List<Account> orphanAccounts = accountRepository.findByUserIsNull();
        for (Account account : orphanAccounts) {
            account.user = felipe;
        }
    }
}
