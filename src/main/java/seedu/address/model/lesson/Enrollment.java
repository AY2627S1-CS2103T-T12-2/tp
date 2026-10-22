package seedu.address.model.lesson;

import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Objects;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.person.StudentId;

/**
 * The immutable association between one student and one independently stored tuition class.
 * Shared class details are deliberately excluded; only student-specific enrolment data is stored.
 */
public final class Enrollment {

    private final EnrollmentId enrollmentId;
    private final StudentId studentId;
    private final ClassId classId;
    private final Cost agreedRate;
    private final PaymentStatus paymentStatus;
    private final TuitionClass resolvedClass;

    /** Creates a new unpaid enrolment at the class's current default rate. */
    public Enrollment(StudentId studentId, TuitionClass tuitionClass) {
        this(EnrollmentId.generate(), studentId, tuitionClass.getClassId(), tuitionClass.getDefaultRate(),
                PaymentStatus.UNPAID, tuitionClass);
    }

    /** Creates a new enrolment at an explicitly agreed rate and payment status. */
    public Enrollment(StudentId studentId, TuitionClass tuitionClass, Cost agreedRate,
            PaymentStatus paymentStatus) {
        this(EnrollmentId.generate(), studentId, tuitionClass.getClassId(), agreedRate,
                paymentStatus, tuitionClass);
    }

    /** Recreates a persisted association before its class reference is resolved. */
    public Enrollment(EnrollmentId enrollmentId, StudentId studentId, ClassId classId,
            Cost agreedRate, PaymentStatus paymentStatus) {
        this(enrollmentId, studentId, classId, agreedRate, paymentStatus, null);
    }

    private Enrollment(EnrollmentId enrollmentId, StudentId studentId, ClassId classId,
            Cost agreedRate, PaymentStatus paymentStatus, TuitionClass resolvedClass) {
        requireAllNonNull(enrollmentId, studentId, classId, agreedRate, paymentStatus);
        this.enrollmentId = enrollmentId;
        this.studentId = studentId;
        this.classId = classId;
        this.agreedRate = agreedRate;
        this.paymentStatus = paymentStatus;
        this.resolvedClass = resolvedClass;
    }

    public EnrollmentId getEnrollmentId() {
        return enrollmentId;
    }

    public StudentId getStudentId() {
        return studentId;
    }

    public ClassId getClassId() {
        return classId;
    }

    public Cost getAgreedRate() {
        return agreedRate;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public boolean isResolved() {
        return resolvedClass != null;
    }

    /** Returns the referenced class after model-level referential-integrity validation. */
    public TuitionClass getTuitionClass() {
        if (resolvedClass == null) {
            throw new IllegalStateException("Enrollment has not been resolved to a tuition class.");
        }
        return resolvedClass;
    }

    /** Returns this association resolved against the supplied current class record. */
    public Enrollment resolveWith(TuitionClass tuitionClass) {
        requireAllNonNull(tuitionClass);
        checkArgument(classId.equals(tuitionClass.getClassId()),
                "Cannot resolve an enrollment against a different class.");
        return new Enrollment(enrollmentId, studentId, classId, agreedRate, paymentStatus, tuitionClass);
    }

    /** Returns a copy with only its payment status changed. */
    public Enrollment withPaymentStatus(PaymentStatus updatedStatus) {
        requireAllNonNull(updatedStatus);
        return new Enrollment(enrollmentId, studentId, classId, agreedRate, updatedStatus, resolvedClass);
    }

    /** Builds a read-only legacy view for presentation and backward-compatible APIs. */
    public Lesson toLessonView() {
        TuitionClass tuitionClass = getTuitionClass();
        return new Lesson(classId, tuitionClass.getSubject(), agreedRate, tuitionClass.getSchedule(),
                paymentStatus, tuitionClass.getDetails());
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof Enrollment otherEnrollment
                && enrollmentId.equals(otherEnrollment.enrollmentId)
                && studentId.equals(otherEnrollment.studentId)
                && classId.equals(otherEnrollment.classId)
                && agreedRate.equals(otherEnrollment.agreedRate)
                && paymentStatus == otherEnrollment.paymentStatus;
    }

    @Override
    public int hashCode() {
        return Objects.hash(enrollmentId, studentId, classId, agreedRate, paymentStatus);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("id", enrollmentId).add("studentId", studentId)
                .add("classId", classId).add("agreedRate", agreedRate)
                .add("paymentStatus", paymentStatus).toString();
    }
}
