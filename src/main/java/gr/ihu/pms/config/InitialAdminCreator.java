package gr.ihu.pms.config;

import gr.ihu.pms.auth.Role;
import gr.ihu.pms.auth.User;
import gr.ihu.pms.auth.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(2)
public class InitialAdminCreator implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(InitialAdminCreator.class);

    private static final int MIN_PASSWORD_LENGTH = 8;

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final String username;
    private final String password;

    public InitialAdminCreator(UserRepository users, PasswordEncoder encoder,
                               @Value("${pms.admin.username:}") String username,
                               @Value("${pms.admin.password:}") String password) {
        this.users = users;
        this.encoder = encoder;
        this.username = username;
        this.password = password;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (users.count() > 0) {
            return;
        }
        if (username.isBlank() || password.isBlank()) {
            log.warn("No users exist. Set PMS_ADMIN_USERNAME and PMS_ADMIN_PASSWORD to create the first manager account.");
            return;
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalStateException(
                    "PMS_ADMIN_PASSWORD must be at least " + MIN_PASSWORD_LENGTH + " characters long");
        }
        users.save(new User(username, encoder.encode(password), "Administrator", Role.MANAGER));
        log.info("Created initial manager account '{}'", username);
    }
}
