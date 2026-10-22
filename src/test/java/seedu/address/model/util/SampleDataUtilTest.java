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
import seedu.address.model.lesson.Cost;
import seedu.address.model.lesson.Enrollment;
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
        assertTrue(sample.getPersonList().stream().flatMap(person -> person.getEnrollments().stream())
                .anyMatch(enrollment -> enrollment.getPaymentStatus() == PaymentStatus.PAID));
        assertTrue(sample.getPersonList().stream().flatMap(person -> person.getEnrollments().stream())
                .anyMatch(enrollment -> enrollment.getPaymentStatus() == PaymentStatus.UNPAID));
        assertTrue(sample.getPersonList().stream().flatMap(person -> person.getEnrollments().stream())
                .anyMatch(enrollment -> enrollment.getAgreedRate().equals(new Cost("42"))));
        assertTrue(sample.getPersonList().stream().allMatch(person -> person.getEnrollments().stream()
                .allMatch(enrollment -> enrollment.getStudentId().equals(person.getStudentId()))));

        Set<ClassId> enrolledClassIds = sample.getPersonList().stream()
                .flatMap(person -> person.getEnrollments().stream())
                .map(Enrollment::getClassId)
                .collect(Collectors.toSet());
        assertFalse(enrolledClassIds.contains(new ClassId("C005")));
    }
}
