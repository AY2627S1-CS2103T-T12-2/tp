package seedu.address.model.lesson;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Objects;

import seedu.address.commons.util.ToStringBuilder;

/** Represents a recurring weekly tuition lesson. */
public class Lesson {

    private final Subject subject;
    private final Cost cost;
    private final LessonTiming timing;
    private final PaymentStatus paymentStatus;

    /** Creates an unpaid lesson enrolment with one subject and one weekly timeslot. */
    public Lesson(Subject subject, Cost cost, LessonTiming timing) {
        this(subject, cost, timing, PaymentStatus.UNPAID);
    }

    /** Creates a lesson enrolment with the supplied payment status. */
    public Lesson(Subject subject, Cost cost, LessonTiming timing, PaymentStatus paymentStatus) {
        requireAllNonNull(subject, cost, timing, paymentStatus);
        this.subject = subject;
        this.cost = cost;
        this.timing = timing;
        this.paymentStatus = paymentStatus;
    }

    public Subject getSubject() {
        return subject;
    }

    public Cost getCost() {
        return cost;
    }

    public LessonTiming getTiming() {
        return timing;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    /** Returns a copy of this lesson enrolment with {@code updatedPaymentStatus}. */
    public Lesson withPaymentStatus(PaymentStatus updatedPaymentStatus) {
        requireAllNonNull(updatedPaymentStatus);
        return new Lesson(subject, cost, timing, updatedPaymentStatus);
    }

    /**
     * Returns true if both lessons represent the same class. Cost is excluded because every student
     * in the same class must share one cost.
     */
    public boolean isSameClass(Lesson otherLesson) {
        return otherLesson == this || otherLesson != null
                && subject.value.equalsIgnoreCase(otherLesson.subject.value)
                && timing.equals(otherLesson.timing);
    }

    /** Returns true if this lesson has the supplied class identity. */
    public boolean isSameClass(Subject expectedSubject, LessonTiming expectedTiming) {
        requireAllNonNull(expectedSubject, expectedTiming);
        return subject.equals(expectedSubject) && timing.equals(expectedTiming);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Lesson otherLesson)) {
            return false;
        }
        return subject.equals(otherLesson.subject) && cost.equals(otherLesson.cost)
                && timing.equals(otherLesson.timing) && paymentStatus == otherLesson.paymentStatus;
    }

    @Override
    public int hashCode() {
        return Objects.hash(subject, cost, timing, paymentStatus);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("subject", subject).add("cost", cost)
                .add("timing", timing).add("paymentStatus", paymentStatus).toString();
    }
}
