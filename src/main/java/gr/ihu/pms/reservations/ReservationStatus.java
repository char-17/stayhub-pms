package gr.ihu.pms.reservations;

/** Κύκλος ζωής κράτησης: BOOKED → CHECKED_IN → CHECKED_OUT, ή BOOKED → CANCELLED. */
public enum ReservationStatus {
    BOOKED,
    CHECKED_IN,
    CHECKED_OUT,
    CANCELLED
}
