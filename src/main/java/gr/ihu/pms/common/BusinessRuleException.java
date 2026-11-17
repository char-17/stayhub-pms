package gr.ihu.pms.common;

/** Παραβίαση επιχειρησιακού κανόνα. Το μήνυμά της εμφανίζεται στον χρήστη. */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
