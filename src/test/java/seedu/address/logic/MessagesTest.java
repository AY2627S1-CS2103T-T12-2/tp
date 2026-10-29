package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.lesson.Cost;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonTiming;
import seedu.address.model.lesson.Subject;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class MessagesTest {

    private static final Lesson CANDIDATE = lesson("Chemistry", "40", "Monday1800-1900");
    private static final Lesson EARLIER_CONFLICT = lesson("Physics", "35", "Monday1700-1830");
    private static final Lesson LATER_CONFLICT = lesson("Math", "30", "Monday1800-1930");

    @Test
    public void format_privacyEnabled_masksStudentAndGuardianContactDetails() {
        Person person = new PersonBuilder()
                .withTags("priority")
                .withGuardianContact("Mary Bee", "81234567", "mary@example.com")
                .build();
        String expectedMessage = "Amy Bee; Phone: *******; Email: *******; Address: *******; "
                + "Tags: [priority]; Guardian: *******";

        assertEquals(expectedMessage, Messages.format(person, true));
    }

    @Test
    public void getClassOverlapMessage_multipleConflicts_formatsMessage() {
        String expectedMessage = "Cannot add Chemistry (Monday 18:00-19:00) because it overlaps with:"
                + System.lineSeparator() + "- Physics (Monday 17:00-18:30)"
                + System.lineSeparator() + "- Math (Monday 18:00-19:30)"
                + System.lineSeparator() + "Choose a non-overlapping time.";

        assertEquals(expectedMessage,
                Messages.getClassOverlapMessage(CANDIDATE, List.of(EARLIER_CONFLICT, LATER_CONFLICT)));
    }

    @Test
    public void getClassOverlapMessage_nullCandidate_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () ->
                Messages.getClassOverlapMessage(null, List.of(EARLIER_CONFLICT)));
    }

    @Test
    public void getClassOverlapMessage_nullConflicts_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () ->
                Messages.getClassOverlapMessage(CANDIDATE, null));
    }

    @Test
    public void getClassOverlapMessage_noConflicts_throwsAssertionError() {
        assertThrows(AssertionError.class, () ->
                Messages.getClassOverlapMessage(CANDIDATE, List.of()));
    }

    private static Lesson lesson(String subject, String cost, String timing) {
        return new Lesson(new Subject(subject), new Cost(cost), new LessonTiming(timing));
    }
}
