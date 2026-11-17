package gr.ihu.pms.common;

/** Κάτι που ζητήθηκε δεν υπάρχει. Οδηγεί στη σελίδα 404. */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String what, Object id) {
        super("unknown " + what + ": " + id);
    }
}
