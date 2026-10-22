package seedu.address.model.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.lesson.ClassId;
import seedu.address.model.lesson.ClassMode;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.OneOffLessonSchedule;
import seedu.address.model.lesson.PaymentStatus;

public class SampleDataUtilTest {

    @Test
    public void getSampleAddressBook_containsRepresentativeFeatureData() {
        ReadOnlyAddressBook sample = SampleDataUtil.getSampleAddressBook();

        assertEquals(6, sample.getPersonList().size());
        assertEquals(5, sample.getTuitionClassList().size());
        assertEquals(6, sample.getNextClassSequence());
        assertTrue(sample.getTuitionClassList().stream()
                .anyMatch(item -> item.getSchedule() instanceof OneOffLessonSchedule));
        assertTrue(sample.getTuitionClassList().stream()
                .anyMatch(item -> item.getDetails().mode() == ClassMode.ONLINE));
        assertTrue(sample.getTuitionClassList().stream()
                .anyMatch(item -> item.getDetails().mode() == ClassMode.OFFLINE));
        assertTrue(sample.getPersonList().stream().anyMatch(person -> person.isPinned()));
        assertTrue(sample.getPersonList().stream()
                .anyMatch(person -> !person.getGuardianContact().isEmpty()));
        assertTrue(sample.getPersonList().stream().flatMap(person -> person.getLessons().stream())
                .anyMatch(lesson -> lesson.getPaymentStatus() == PaymentStatus.PAID));
        assertTrue(sample.getPersonList().stream().flatMap(person -> person.getLessons().stream())
                .anyMatch(lesson -> lesson.getPaymentStatus() == PaymentStatus.UNPAID));

        Set<ClassId> enrolledClassIds = sample.getPersonList().stream()
                .flatMap(person -> person.getLessons().stream())
                .map(Lesson::getClassId)
                .collect(Collectors.toSet());
        assertFalse(enrolledClassIds.contains(new ClassId("C005")));
    }
}
