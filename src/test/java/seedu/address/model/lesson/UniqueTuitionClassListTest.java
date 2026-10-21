package seedu.address.model.lesson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

public class UniqueTuitionClassListTest {

    private static final TuitionClass MATH = tuitionClass("C001", "Math", "30", "Monday1800-1930");
    private static final TuitionClass PHYSICS = tuitionClass("C002", "Physics", "35", "Tuesday1800-1930");

    @Test
    public void addGetAndRemove_validClasses_maintainsSortedUnmodifiableList() {
        UniqueTuitionClassList classes = new UniqueTuitionClassList();
        classes.add(PHYSICS);
        classes.add(MATH);

        assertTrue(classes.containsId(new ClassId("C001")));
        assertTrue(classes.containsDetails(MATH));
        assertEquals(MATH, classes.get(new ClassId("C001")));
        assertEquals(List.of(MATH, PHYSICS), classes.asUnmodifiableObservableList());
        assertThrows(UnsupportedOperationException.class, () ->
                classes.asUnmodifiableObservableList().add(MATH));

        classes.remove(MATH);
        assertFalse(classes.containsId(new ClassId("C001")));
        assertThrows(NoSuchElementException.class, () -> classes.remove(MATH));
        assertThrows(NoSuchElementException.class, () -> classes.get(new ClassId("C099")));
    }

    @Test
    public void add_duplicates_rejectedByIdOrDetails() {
        UniqueTuitionClassList classes = new UniqueTuitionClassList();
        classes.add(MATH);
        assertThrows(IllegalArgumentException.class, () ->
                classes.add(tuitionClass("C001", "Physics", "35", "Tuesday1800-1930")));
        assertThrows(IllegalArgumentException.class, () ->
                classes.add(tuitionClass("C009", "math", "99", "Monday1800-1930")));
    }

    @Test
    public void set_validAndInvalidReplacements_enforcesInvariants() {
        UniqueTuitionClassList classes = new UniqueTuitionClassList();
        classes.setClasses(List.of(MATH, PHYSICS));
        TuitionClass editedMath = tuitionClass("C001", "Advanced Math", "40", "Wednesday1800-1930");
        classes.set(MATH, editedMath);
        assertEquals(editedMath, classes.get(new ClassId("C001")));

        assertThrows(NoSuchElementException.class, () ->
                classes.set(MATH, tuitionClass("C001", "English", "30", "Friday1800-1930")));
        assertThrows(IllegalArgumentException.class, () ->
                classes.set(editedMath, tuitionClass("C003", "English", "30", "Friday1800-1930")));
        assertThrows(IllegalArgumentException.class, () ->
                classes.set(editedMath, tuitionClass("C001", "Physics", "99", "Tuesday1800-1930")));
    }

    @Test
    public void equalityAndHashCode_useContainedClasses() {
        UniqueTuitionClassList first = new UniqueTuitionClassList();
        UniqueTuitionClassList second = new UniqueTuitionClassList();
        first.add(MATH);
        second.add(MATH);
        assertEquals(first, first);
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        second.add(PHYSICS);
        assertNotEquals(first, second);
        assertNotEquals(first, null);
    }

    private static TuitionClass tuitionClass(String id, String subject, String cost, String timing) {
        return new TuitionClass(new ClassId(id), new Subject(subject), new Cost(cost), new LessonTiming(timing));
    }
}
