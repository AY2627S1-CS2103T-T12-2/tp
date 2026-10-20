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
        for (String link : List.of("example.com", "https://", "ftp://example.com", "javascript:alert(1)",
                "https://user:pass@example.com", "https://example.com/with space", "https://example.com:99999")) {
            assertThrows(IllegalArgumentException.class, () -> new ClassDetails(link, "", ""), link);
        }
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
