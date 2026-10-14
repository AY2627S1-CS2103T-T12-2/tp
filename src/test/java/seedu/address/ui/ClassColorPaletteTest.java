package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.lesson.Cost;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonTiming;
import seedu.address.model.lesson.Subject;
import seedu.address.testutil.PersonBuilder;

public class ClassColorPaletteTest {

    private static final Lesson MATH = lesson("Math", "30", "Monday1800-1930");
    private static final Lesson SAME_MATH_CLASS = lesson("math", "30.00", "Monday1800-1930");
    private static final Lesson PHYSICS = lesson("Physics", "35", "Wednesday1700-1830");

    @Test
    public void getStyleClass_sameLogicalClass_returnsSameStyleClass() {
        ClassColorPalette palette = new ClassColorPalette(List.of(
                new PersonBuilder().withLessons(MATH).build(),
                new PersonBuilder().withLessons(SAME_MATH_CLASS).build()));

        assertEquals(palette.getStyleClass(MATH), palette.getStyleClass(SAME_MATH_CLASS));
    }

    @Test
    public void getStyleClass_differentClassesBeforePaletteExhausted_returnsDifferentStyleClasses() {
        ClassColorPalette palette = new ClassColorPalette(List.of(
                new PersonBuilder().withLessons(MATH, PHYSICS).build()));

        assertNotEquals(palette.getStyleClass(MATH), palette.getStyleClass(PHYSICS));
    }

    @Test
    public void getStyleClass_nullLesson_throwsNullPointerException() {
        ClassColorPalette palette = new ClassColorPalette();

        assertThrows(NullPointerException.class, () -> palette.getStyleClass(null));
    }

    private static Lesson lesson(String subject, String cost, String timing) {
        return new Lesson(new Subject(subject), new Cost(cost), new LessonTiming(timing));
    }
}
