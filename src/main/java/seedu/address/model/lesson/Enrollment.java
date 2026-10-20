package seedu.address.model.lesson;

/**
 * The association between one student and one independently stored tuition class.
 * Payment status belongs to this association, not to the shared class.
 */
public final class Enrollment extends Lesson {

    /** Creates an unpaid enrolment in the supplied class. */
    public Enrollment(TuitionClass tuitionClass) {
        this(tuitionClass, PaymentStatus.UNPAID);
    }

    /** Creates an enrolment in the supplied class with an explicit payment status. */
    public Enrollment(TuitionClass tuitionClass, PaymentStatus paymentStatus) {
        super(tuitionClass.getClassId(), tuitionClass.getSubject(), tuitionClass.getCost(),
                tuitionClass.getTiming(), paymentStatus, tuitionClass.getDetails());
    }
}
