package seedu.address.ui;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import seedu.address.model.lesson.ClassDetails;

/** Formats shared class details according to the current privacy display state. */
final class ClassDetailsFormatter {

    static final String PRIVACY_MASK = "*******";

    private ClassDetailsFormatter() {}

    static String format(ClassDetails details, boolean privacyModeEnabled) {
        String mode = "Mode: " + details.mode().toDisplayString();
        String supplementaryDetails = formatSupplementary(details, privacyModeEnabled);
        return supplementaryDetails.isEmpty() ? mode : mode + " | " + supplementaryDetails;
    }

    static String formatSupplementary(ClassDetails details, boolean privacyModeEnabled) {
        return Stream.of(
                        formatSensitiveField("Link", details.meetingLink(), privacyModeEnabled),
                        formatSensitiveField("Location", details.location(), privacyModeEnabled),
                        formatSensitiveField("Notes", details.notes(), false))
                .filter(value -> !value.isEmpty())
                .collect(Collectors.joining(" | "));
    }

    private static String formatSensitiveField(String name, String value, boolean privacyModeEnabled) {
        return value.isEmpty() ? "" : name + ": " + (privacyModeEnabled ? PRIVACY_MASK : value);
    }
}
