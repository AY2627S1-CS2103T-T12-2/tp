package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
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
    public void getStyleClass_classesWithCollidingPreferredColors_usesDifferentAvailableColors() {
        Lesson firstLesson = lesson("A", "30", "Monday1800-1930");
        Lesson collidingLesson = lesson("I", "30", "Monday1800-1930");
        ClassColorPalette palette = new ClassColorPalette();

        assertNotEquals(palette.getStyleClass(firstLesson), palette.getStyleClass(collidingLesson));
    }

    @Test
    public void constructor_sameSubjectAtDifferentTimes_assignsDifferentColors() {
        Lesson mondayMath = lesson("Math", "30", "Monday1800-1930");
        Lesson tuesdayMath = lesson("Math", "30", "Tuesday1800-1930");
        ClassColorPalette palette = new ClassColorPalette(List.of(
                new PersonBuilder().withLessons(tuesdayMath, mondayMath).build()));

        assertNotEquals(palette.getStyleClass(mondayMath), palette.getStyleClass(tuesdayMath));
    }

    @Test
    public void getStyleClass_moreClassesThanColors_reusesPaletteColor() {
        Lesson[] lessons = new Lesson[ClassColorPalette.COLOR_COUNT + 1];
        for (int index = 0; index < lessons.length; index++) {
            lessons[index] = lesson("Subject" + index, "30",
                    String.format("Monday%02d00-%02d30", index, index));
        }
        ClassColorPalette palette = new ClassColorPalette(List.of(
                new PersonBuilder().withLessons(lessons).build()));
        List<String> styleClasses = Arrays.stream(lessons)
                .map(palette::getStyleClass)
                .toList();

        assertEquals(ClassColorPalette.COLOR_COUNT,
                styleClasses.subList(0, ClassColorPalette.COLOR_COUNT).stream().distinct().count());
        assertTrue(styleClasses.subList(0, ClassColorPalette.COLOR_COUNT)
                .contains(styleClasses.get(ClassColorPalette.COLOR_COUNT)));
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
