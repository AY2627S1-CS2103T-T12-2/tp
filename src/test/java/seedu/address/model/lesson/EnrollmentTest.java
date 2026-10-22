package seedu.address.model.lesson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.StudentId;

public class EnrollmentTest {

    private static final EnrollmentId ENROLLMENT_ID =
            new EnrollmentId("E-00000000-0000-0000-0000-000000000001");
    private static final StudentId STUDENT_ID =
            new StudentId("S-00000000-0000-0000-0000-000000000001");
    private static final ClassId CLASS_ID = new ClassId("C001");
    private static final TuitionClass TUITION_CLASS = new TuitionClass(CLASS_ID,
            new Subject("Math"), new Cost("30"), new LessonTiming("Monday1800-1930"));

    @Test
    public void constructors_validValues_preserveAssociationData() {
        Enrollment defaultRate = new Enrollment(STUDENT_ID, TUITION_CLASS);
        Enrollment agreedRate = new Enrollment(STUDENT_ID, TUITION_CLASS,
                new Cost("25"), PaymentStatus.PAID);
        Enrollment persisted = new Enrollment(ENROLLMENT_ID, STUDENT_ID, CLASS_ID,
                new Cost("20"), PaymentStatus.UNPAID);

        assertEquals(STUDENT_ID, defaultRate.getStudentId());
        assertEquals(CLASS_ID, defaultRate.getClassId());
        assertEquals(TUITION_CLASS.getDefaultRate(), defaultRate.getAgreedRate());
        assertEquals(PaymentStatus.UNPAID, defaultRate.getPaymentStatus());
        assertTrue(defaultRate.isResolved());
        assertEquals(new Cost("25"), agreedRate.getAgreedRate());
        assertEquals(PaymentStatus.PAID, agreedRate.getPaymentStatus());
        assertEquals(ENROLLMENT_ID, persisted.getEnrollmentId());
        assertFalse(persisted.isResolved());
        assertThrows(IllegalStateException.class, persisted::getTuitionClass);
    }

    @Test
    public void resolveWith_matchingClass_preservesStudentSpecificData() {
        Enrollment persisted = enrollment(ENROLLMENT_ID, STUDENT_ID, CLASS_ID,
                new Cost("20"), PaymentStatus.PAID);

        Enrollment resolved = persisted.resolveWith(TUITION_CLASS);
        Lesson view = resolved.toLessonView();

        assertEquals(ENROLLMENT_ID, resolved.getEnrollmentId());
        assertEquals(new Cost("20"), resolved.getAgreedRate());
        assertEquals(PaymentStatus.PAID, resolved.getPaymentStatus());
        assertEquals(TUITION_CLASS, resolved.getTuitionClass());
        assertEquals(TUITION_CLASS.getSubject(), view.getSubject());
        assertEquals(TUITION_CLASS.getSchedule(), view.getSchedule());
        assertEquals(new Cost("20"), view.getCost());
    }

    @Test
    public void resolveWith_differentClass_throwsIllegalArgumentException() {
        Enrollment persisted = enrollment(ENROLLMENT_ID, STUDENT_ID, CLASS_ID,
                new Cost("20"), PaymentStatus.PAID);
        TuitionClass otherClass = new TuitionClass(new ClassId("C002"), new Subject("Physics"),
                new Cost("40"), new LessonTiming("Tuesday1800-1930"));

        assertThrows(NullPointerException.class, () -> persisted.resolveWith(null));
        assertThrows(IllegalArgumentException.class, () -> persisted.resolveWith(otherClass));
    }

    @Test
    public void withPaymentStatus_replacesOnlyPaymentStatus() {
        Enrollment original = enrollment(ENROLLMENT_ID, STUDENT_ID, CLASS_ID,
                new Cost("20"), PaymentStatus.UNPAID).resolveWith(TUITION_CLASS);

        Enrollment paid = original.withPaymentStatus(PaymentStatus.PAID);

        assertEquals(ENROLLMENT_ID, paid.getEnrollmentId());
        assertEquals(STUDENT_ID, paid.getStudentId());
        assertEquals(CLASS_ID, paid.getClassId());
        assertEquals(new Cost("20"), paid.getAgreedRate());
        assertEquals(PaymentStatus.PAID, paid.getPaymentStatus());
        assertEquals(TUITION_CLASS, paid.getTuitionClass());
        assertThrows(NullPointerException.class, () -> original.withPaymentStatus(null));
    }

    @Test
    public void equalsHashCodeAndToString_followAllAssociationFields() {
        Enrollment original = enrollment(ENROLLMENT_ID, STUDENT_ID, CLASS_ID,
                new Cost("20"), PaymentStatus.UNPAID);
        Enrollment copy = enrollment(ENROLLMENT_ID, STUDENT_ID, CLASS_ID,
                new Cost("20.00"), PaymentStatus.UNPAID);

        assertEquals(original, original);
        assertEquals(original, copy);
        assertEquals(original.hashCode(), copy.hashCode());
        assertNotEquals(original, enrollment(
                new EnrollmentId("E-00000000-0000-0000-0000-000000000002"),
                STUDENT_ID, CLASS_ID, new Cost("20"), PaymentStatus.UNPAID));
        assertNotEquals(original, enrollment(ENROLLMENT_ID,
                new StudentId("S-00000000-0000-0000-0000-000000000002"),
                CLASS_ID, new Cost("20"), PaymentStatus.UNPAID));
        assertNotEquals(original, enrollment(ENROLLMENT_ID, STUDENT_ID,
                new ClassId("C002"), new Cost("20"), PaymentStatus.UNPAID));
        assertNotEquals(original, enrollment(ENROLLMENT_ID, STUDENT_ID,
                CLASS_ID, new Cost("21"), PaymentStatus.UNPAID));
        assertNotEquals(original, enrollment(ENROLLMENT_ID, STUDENT_ID,
                CLASS_ID, new Cost("20"), PaymentStatus.PAID));
        assertNotEquals(original, null);
        assertNotEquals(original, "enrollment");
        assertTrue(original.toString().contains(ENROLLMENT_ID.toString()));
    }

    private static Enrollment enrollment(EnrollmentId enrollmentId, StudentId studentId,
            ClassId classId, Cost agreedRate, PaymentStatus paymentStatus) {
        return new Enrollment(enrollmentId, studentId, classId, agreedRate, paymentStatus);
    }
}
