package gr.ihu.pms.common;

public class NotFoundException extends RuntimeException {

    public NotFoundException(String what, Object id) {
        super("unknown " + what + ": " + id);
    }
}
