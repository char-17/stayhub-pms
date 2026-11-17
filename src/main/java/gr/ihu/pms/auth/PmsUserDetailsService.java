package gr.ihu.pms.auth;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

//Η γεφυρα αναμεσα στους δικους μας χρηστες και τους χρηστες του pring Security

//Το Spring Security ξέρει να ελέγχει κωδικούς και ρόλους,
//        αλλά δεν ξέρει πού βρίσκονται οι δικοί σου χρήστες.
//Αυτή η κλάση του το λέει.

/** Γέφυρα με το Spring Security: του δίνει τους χρήστες της βάσης μας στη σύνδεση. */
@Service
public class PmsUserDetailsService implements UserDetailsService {

    private final UserRepository users;

    public PmsUserDetailsService(UserRepository users) {
        this.users = users;
    }

    /** Φέρνει τον χρήστη και τον μετατρέπει στη μορφή του Spring Security (ρόλος → ROLE_...). */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User u = users.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("unknown user: " + username));
        return org.springframework.security.core.userdetails.User.builder()
                .username(u.getUsername())
                .password(u.getPasswordHash())
                .disabled(!u.isEnabled())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole().name())))
                .build();
    }
}
