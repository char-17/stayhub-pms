package gr.ihu.pms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/** Ρυθμίσεις ασφάλειας: ποιος ρόλος μπαίνει πού, σελίδα σύνδεσης, hashing κωδικών. */
@Configuration
public class SecurityConfig {

    /** Οι κωδικοί αποθηκεύονται ως hash με BCrypt. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Κανόνες πρόσβασης: /billing και /reports μόνο MANAGER, όλα τα άλλα απαιτούν σύνδεση. */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/css/**", "/js/**", "/webjars/**").permitAll()
                .requestMatchers("/billing/**", "/reports/**").hasRole("MANAGER")
                .anyRequest().authenticated())
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .permitAll())
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll());
        return http.build();
    }
}
