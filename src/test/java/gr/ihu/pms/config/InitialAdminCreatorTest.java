package gr.ihu.pms.config;

import gr.ihu.pms.auth.Role;
import gr.ihu.pms.auth.User;
import gr.ihu.pms.auth.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InitialAdminCreatorTest {

    private final UserRepository users = mock(UserRepository.class);
    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    @Test
    void doesNothingWhenUsersAlreadyExist() {
        when(users.count()).thenReturn(2L);

        new InitialAdminCreator(users, encoder, "owner", "a-long-password").run();

        verify(users, never()).save(any());
    }

    @Test
    void createsNothingWhenCredentialsAreNotConfigured() {
        when(users.count()).thenReturn(0L);

        new InitialAdminCreator(users, encoder, "", "").run();

        verify(users, never()).save(any());
    }

    @Test
    void refusesToStartWithTooShortPassword() {
        when(users.count()).thenReturn(0L);
        InitialAdminCreator creator = new InitialAdminCreator(users, encoder, "owner", "short");

        assertThatThrownBy(creator::run)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at least 8");
        verify(users, never()).save(any());
    }

    @Test
    void createsManagerWithHashedPasswordOnEmptyDatabase() {
        when(users.count()).thenReturn(0L);

        new InitialAdminCreator(users, encoder, "owner", "a-long-password").run();

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(users).save(saved.capture());
        assertThat(saved.getValue().getUsername()).isEqualTo("owner");
        assertThat(saved.getValue().getRole()).isEqualTo(Role.MANAGER);
        assertThat(saved.getValue().getPasswordHash()).isNotEqualTo("a-long-password");
        assertThat(encoder.matches("a-long-password", saved.getValue().getPasswordHash())).isTrue();
    }
}
