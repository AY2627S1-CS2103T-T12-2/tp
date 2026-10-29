package seedu.address.model.lesson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

public class ClassDetailsTest {

    @Test
    public void constructor_validDetailsAndTextBoundaries_acceptsInput() {
        assertTrue(ClassDetails.EMPTY.isEmpty());
        ClassDetails details = new ClassDetails("https://meet.example.com/path?room=1#join", "x".repeat(500), "");
        assertFalse(details.isEmpty());
        assertEquals(500, details.location().length());
        assertTrue(ClassDetails.isValidMeetingLink("HTTP://example.com"));
        assertTrue(ClassDetails.isValidMeetingLink("https://example.com:443"));
        assertThrows(IllegalArgumentException.class, () -> new ClassDetails("", "x".repeat(501), ""));
        assertThrows(IllegalArgumentException.class, () -> new ClassDetails("", "", "x".repeat(501)));
        assertThrows(IllegalArgumentException.class, () -> new ClassDetails("", "", "line\nbreak"));
        assertThrows(NullPointerException.class, () -> new ClassDetails(null, "", ""));
    }

    @Test
    public void constructor_invalidLinks_rejectsInput() {
        for (String link : List.of("example.com", "https://", "http:/path", "ftp://example.com", "javascript:alert(1)",
                "https://user:pass@example.com", "https://example.com/with space", "https://example.com:99999",
                "https://example.com:0", "https://example.com:-2", "https://example.com:",
                "https://example.com:/room", "http://[::1]:/room")) {
            assertThrows(IllegalArgumentException.class, () -> new ClassDetails(link, "", ""), link);
        }
    }

    @Test
    public void mode_offlineRejectsLinksAndOnlineAllowsOptionalLinks() {
        assertEquals(ClassMode.OFFLINE, ClassDetails.EMPTY.mode());
        assertEquals(ClassMode.ONLINE, new ClassDetails(ClassMode.ONLINE, "", "", "").mode());
        assertThrows(IllegalArgumentException.class, () -> new ClassDetails(
                ClassMode.OFFLINE, "https://example.com", "", ""));
        assertEquals(ClassMode.ONLINE, ClassMode.fromString("OnLiNe"));
        assertEquals(ClassMode.OFFLINE, ClassMode.fromString("OFFLINE"));
        assertThrows(IllegalArgumentException.class, () -> ClassMode.fromString("hybrid"));
    }

    @Test
    public void optionalFieldsAndValidPortBoundaries_remainMeaningfulDetails() {
        assertTrue(ClassDetails.isValidMeetingLink("https://example.com:1"));
        assertTrue(ClassDetails.isValidMeetingLink("https://example.com:65535"));
        assertFalse(new ClassDetails(ClassMode.ONLINE, "", "", "").isEmpty());
        assertFalse(new ClassDetails("", "Room 2", "").isEmpty());
        assertFalse(new ClassDetails("", "", "Bring notes").isEmpty());
        assertEquals("Mode: Offline | Location: Room 2", new ClassDetails("", "Room 2", "").toString());
        assertEquals("Mode: Offline | Notes: Bring notes", new ClassDetails("", "", "Bring notes").toString());
    }

    @Test
    public void lesson_detailUpdates_preserveIdentityAndPayment() {
        Lesson original = new Lesson(new Subject("Math"), new Cost("30"), new LessonTiming("Monday1800-1930"),
                PaymentStatus.PAID);
        ClassDetails details = new ClassDetails("https://example.com", "Room 2", "Bring calculator");
        Lesson updated = original.withDetails(details);
        assertTrue(original.isSameClass(updated));
        assertNotEquals(original, updated);
        assertEquals(PaymentStatus.PAID, updated.getPaymentStatus());
        assertEquals(details, updated.withPaymentStatus(PaymentStatus.UNPAID).getDetails());
        assertEquals(ClassDetails.EMPTY, original.getDetails());
        assertEquals(updated.hashCode(), original.withDetails(details).hashCode());
    }
}
