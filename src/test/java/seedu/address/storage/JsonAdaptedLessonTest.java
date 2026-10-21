package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.lesson.ClassId;
import seedu.address.model.lesson.Cost;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonTiming;
import seedu.address.model.lesson.OneOffLessonSchedule;
import seedu.address.model.lesson.PaymentStatus;
import seedu.address.model.lesson.Subject;

public class JsonAdaptedLessonTest {

    private static final Lesson VALID_LESSON = new Lesson(new Subject("Math"), new Cost("30"),
            new LessonTiming("Monday1800-1930"));

    @Test
    public void toModelType_validLessonDetails_returnsLesson() throws Exception {
        assertEquals(VALID_LESSON, new JsonAdaptedLesson("Math", "30", "Monday1800-1930").toModelType());
        assertEquals(VALID_LESSON, new JsonAdaptedLesson(VALID_LESSON).toModelType());
        Lesson paidLesson = VALID_LESSON.withPaymentStatus(PaymentStatus.PAID);
        assertEquals(paidLesson,
                new JsonAdaptedLesson("Math", "30", "Monday1800-1930", "PAID").toModelType());
        Lesson linkedLesson = new JsonAdaptedLesson(
                "Math", "30", "Monday1800-1930", "PAID", "C001", null).toModelType();
        assertEquals(new ClassId("C001"), linkedLesson.getClassId());
        Lesson oneOffLesson = new Lesson(new Subject("Math"), new Cost("30"),
                new OneOffLessonSchedule("2026-10-20T1800-1930"));
        assertEquals(oneOffLesson, new JsonAdaptedLesson(oneOffLesson).toModelType());
        assertEquals(oneOffLesson, new JsonAdaptedLesson(
                "Math", "30", "2026-10-20T1800-1930", null, "ONE_OFF").toModelType());
    }

    @Test
    public void explicitDetailPresence_distinguishesEachOptionalFieldFromOmission() throws Exception {
        assertFalse(new JsonAdaptedLesson("Math", "30", "Monday1800-1930").hasExplicitDetails());
        for (int index = 0; index < 4; index++) {
            String[] fields = new String[4];
            fields[index] = index == 3 ? "offline" : "";
            JsonAdaptedLesson adapted = new JsonAdaptedLesson("Math", "30", "Monday1800-1930", null,
                    fields[0], fields[1], fields[2], fields[3]);
            assertTrue(adapted.hasExplicitDetails());
            assertEquals(VALID_LESSON, adapted.toModelType());
        }
        assertThrows(IllegalValueException.class, () -> new JsonAdaptedLesson("Math", "30", "Monday1800-1930",
                null, null, null, null, null, "invalid").toModelType());
    }

    @Test
    public void toModelType_missingField_throwsIllegalValueException() {
        assertThrows(IllegalValueException.class, () -> new JsonAdaptedLesson(
                null, "30", "Monday1800-1930").toModelType());
        assertThrows(IllegalValueException.class, () -> new JsonAdaptedLesson(
                "Math", null, "Monday1800-1930").toModelType());
        assertThrows(IllegalValueException.class, () -> new JsonAdaptedLesson(
                "Math", "30", null).toModelType());
    }

    @Test
    public void toModelType_invalidField_throwsIllegalValueException() {
        assertThrows(IllegalValueException.class, () -> new JsonAdaptedLesson(
                "Math!", "30", "Monday1800-1930").toModelType());
        assertThrows(IllegalValueException.class, () -> new JsonAdaptedLesson(
                "Math", "-1", "Monday1800-1930").toModelType());
        assertThrows(IllegalValueException.class, () -> new JsonAdaptedLesson(
                "Math", "30", "Monday2400-0100").toModelType());
        assertThrows(IllegalValueException.class, PaymentStatus.MESSAGE_CONSTRAINTS, () -> new JsonAdaptedLesson(
                "Math", "30", "Monday1800-1930", "pending").toModelType());
        assertThrows(IllegalValueException.class, ClassId.MESSAGE_CONSTRAINTS, () -> new JsonAdaptedLesson(
                "Math", "30", "Monday1800-1930", "PAID", "C000", null).toModelType());
        assertThrows(IllegalValueException.class, () -> new JsonAdaptedLesson(
                "Math", "30", "Monday1800-1930", null, "MONTHLY").toModelType());
        assertThrows(IllegalValueException.class, () -> new JsonAdaptedLesson(
                "Math", "30", "2026-10-20T1800-1930", null, "WEEKLY").toModelType());
        assertThrows(IllegalValueException.class, () -> new JsonAdaptedLesson(
                "Math", "30", "Monday1800-1930", null, "ONE_OFF").toModelType());
    }
}
