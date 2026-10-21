package seedu.address.model.lesson;

/** Whether a recurring class meets online or offline. */
public enum ClassMode {
    ONLINE,
    OFFLINE;

    public static final String MESSAGE_CONSTRAINTS = "Class mode must be online or offline.";

    /** Parses a case-insensitive class mode. */
    public static ClassMode fromString(String value) {
        if ("online".equalsIgnoreCase(value)) {
            return ONLINE;
        }
        if ("offline".equalsIgnoreCase(value)) {
            return OFFLINE;
        }
        throw new IllegalArgumentException(MESSAGE_CONSTRAINTS);
    }

    /** Returns the mode shown in student cards and timetable entries. */
    public String toDisplayString() {
        return this == ONLINE ? "Online" : "Offline";
    }
}
