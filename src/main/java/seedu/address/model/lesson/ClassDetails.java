package seedu.address.model.lesson;

import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Shared details for a scheduled class. Empty fields mean not provided. */
public record ClassDetails(ClassMode mode, String meetingLink, String location, String notes) {

    public static final ClassDetails EMPTY = new ClassDetails("", "", "");
    public static final String MESSAGE_LINK_CONSTRAINTS =
            "Meeting link must be an absolute http:// or https:// URL with a host, or empty to clear it.";
    public static final String MESSAGE_TEXT_CONSTRAINTS =
            "Class location and notes must be single-line text of at most 500 characters each.";

    public static final String MESSAGE_OFFLINE_LINK = "Only online classes can have a meeting link.";

    /** Retains compatibility with details saved before class modes were introduced. */
    public ClassDetails(String meetingLink, String location, String notes) {
        this(meetingLink.isEmpty() ? ClassMode.OFFLINE : ClassMode.ONLINE, meetingLink, location, notes);
    }

    /** Creates validated class details. */
    public ClassDetails {
        requireAllNonNull(mode, meetingLink, location, notes);
        checkArgument(isValidMeetingLink(meetingLink), MESSAGE_LINK_CONSTRAINTS);
        checkArgument(mode == ClassMode.ONLINE || meetingLink.isEmpty(), MESSAGE_OFFLINE_LINK);
        checkArgument(isValidText(location) && isValidText(notes), MESSAGE_TEXT_CONSTRAINTS);
    }

    /** Returns whether the link is empty or a valid web URL. */
    public static boolean isValidMeetingLink(String value) {
        if (value.isEmpty()) {
            return true;
        }
        try {
            URI uri = new URI(value);
            return ("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
                    && uri.getHost() != null && uri.getUserInfo() == null
                    && (uri.getPort() == -1 || uri.getPort() > 0 && uri.getPort() <= 65535);
        } catch (URISyntaxException e) {
            return false;
        }
    }

    private static boolean isValidText(String value) {
        return value.length() <= 500 && value.chars().noneMatch(Character::isISOControl);
    }

    /** Returns whether this class has no extra details. */
    public boolean isEmpty() {
        return equals(EMPTY);
    }

    @Override
    public String toString() {
        return Stream.of("Mode: " + mode.toDisplayString(), meetingLink.isEmpty() ? "" : "Link: " + meetingLink,
                location.isEmpty() ? "" : "Location: " + location,
                notes.isEmpty() ? "" : "Notes: " + notes)
                .filter(value -> !value.isEmpty()).collect(Collectors.joining(" | "));
    }
}
