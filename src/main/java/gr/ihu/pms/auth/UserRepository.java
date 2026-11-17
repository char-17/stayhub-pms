package gr.ihu.pms.auth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

//Ο αποθηκαριος των χρηστών , interfece
public interface UserRepository extends JpaRepository<User, Long> {

    /** Βρίσκει χρήστη από το όνομα σύνδεσης. */
    Optional<User> findByUsername(String username);
}
