package gr.ihu.pms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Σημείο εκκίνησης της εφαρμογής (Spring Boot). */
@SpringBootApplication
public class PmsApplication {

    /** Ξεκινά την εφαρμογή. */
    public static void main(String[] args) {
        SpringApplication.run(PmsApplication.class, args);
    }
}
