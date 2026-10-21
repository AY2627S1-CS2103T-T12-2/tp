package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.lesson.ClassId;
import seedu.address.model.lesson.Cost;
import seedu.address.model.lesson.LessonTiming;
import seedu.address.model.lesson.Subject;
import seedu.address.model.lesson.TuitionClass;

public class JsonAdaptedTuitionClassTest {

    private static final TuitionClass VALID_CLASS = new TuitionClass(new ClassId("C001"),
            new Subject("Math"), new Cost("30"), new LessonTiming("Monday1800-1930"));

    @Test
    public void toModelType_validFields_returnsTuitionClass() throws Exception {
        assertEquals(VALID_CLASS,
                new JsonAdaptedTuitionClass("C001", "Math", "30", "Monday1800-1930").toModelType());
        assertEquals(VALID_CLASS, new JsonAdaptedTuitionClass(VALID_CLASS).toModelType());
    }

    @Test
    public void toModelType_invalidClassId_throwsIllegalValueException() {
        assertThrows(IllegalValueException.class, ClassId.MESSAGE_CONSTRAINTS, () ->
                new JsonAdaptedTuitionClass(null, "Math", "30", "Monday1800-1930").toModelType());
        assertThrows(IllegalValueException.class, ClassId.MESSAGE_CONSTRAINTS, () ->
                new JsonAdaptedTuitionClass("1", "Math", "30", "Monday1800-1930").toModelType());
    }

    @Test
    public void toModelType_invalidSubject_throwsIllegalValueException() {
        assertThrows(IllegalValueException.class, Subject.MESSAGE_CONSTRAINTS, () ->
                new JsonAdaptedTuitionClass("C001", null, "30", "Monday1800-1930").toModelType());
        assertThrows(IllegalValueException.class, Subject.MESSAGE_CONSTRAINTS, () ->
                new JsonAdaptedTuitionClass("C001", "Math!", "30", "Monday1800-1930").toModelType());
    }

    @Test
    public void toModelType_invalidCost_throwsIllegalValueException() {
        assertThrows(IllegalValueException.class, Cost.MESSAGE_CONSTRAINTS, () ->
                new JsonAdaptedTuitionClass("C001", "Math", null, "Monday1800-1930").toModelType());
        assertThrows(IllegalValueException.class, Cost.MESSAGE_CONSTRAINTS, () ->
                new JsonAdaptedTuitionClass("C001", "Math", "-1", "Monday1800-1930").toModelType());
    }

    @Test
    public void toModelType_oneOffClass_requiresMatchingScheduleTypeAndTiming() throws Exception {
        assertEquals("2026-10-24T1800-1930", new JsonAdaptedTuitionClass("C001", "Math", "30",
                "2026-10-24T1800-1930", "ONE_OFF").toModelType().getSchedule().toStorageString());
        assertThrows(IllegalValueException.class, () -> new JsonAdaptedTuitionClass("C001", "Math", "30",
                null, "ONE_OFF").toModelType());
    }

    @Test
    public void toModelType_invalidTiming_throwsIllegalValueException() {
        assertThrows(IllegalValueException.class, LessonTiming.MESSAGE_CONSTRAINTS, () ->
                new JsonAdaptedTuitionClass("C001", "Math", "30", null).toModelType());
        assertThrows(IllegalValueException.class, LessonTiming.MESSAGE_CONSTRAINTS, () ->
                new JsonAdaptedTuitionClass("C001", "Math", "30", "Monday2500-2600").toModelType());
    }
}
