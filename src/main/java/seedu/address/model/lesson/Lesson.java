package seedu.address.model.lesson;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Objects;

import seedu.address.commons.util.ToStringBuilder;

/** Represents one student's enrolment in a scheduled tuition class. */
public class Lesson {

    private final Subject subject;
    private final Cost cost;
    private final LessonSchedule schedule;
    private final PaymentStatus paymentStatus;

    /** Creates an unpaid lesson enrolment with one subject and one weekly timeslot. */
    public Lesson(Subject subject, Cost cost, LessonSchedule schedule) {
        this(subject, cost, schedule, PaymentStatus.UNPAID);
    }

    /** Creates a lesson enrolment with the supplied payment status. */
    public Lesson(Subject subject, Cost cost, LessonSchedule schedule, PaymentStatus paymentStatus) {
        requireAllNonNull(subject, cost, schedule, paymentStatus);
        this.subject = subject;
        this.cost = cost;
        this.schedule = schedule;
        this.paymentStatus = paymentStatus;
    }

    public Subject getSubject() {
        return subject;
    }

    public Cost getCost() {
        return cost;
    }

    public LessonSchedule getSchedule() {
        return schedule;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    /** Returns a copy of this lesson enrolment with {@code updatedPaymentStatus}. */
    public Lesson withPaymentStatus(PaymentStatus updatedPaymentStatus) {
        requireAllNonNull(updatedPaymentStatus);
        return new Lesson(subject, cost, schedule, updatedPaymentStatus);
    }

    /**
     * Returns true if both lessons represent the same class. Cost is excluded because every student
     * in the same class must share one cost.
     */
    public boolean isSameClass(Lesson otherLesson) {
        return otherLesson == this || otherLesson != null
                && subject.value.equalsIgnoreCase(otherLesson.subject.value)
                && schedule.equals(otherLesson.schedule);
    }

    /** Returns true if this lesson has the supplied class identity. */
    public boolean isSameClass(Subject expectedSubject, LessonSchedule expectedSchedule) {
        requireAllNonNull(expectedSubject, expectedSchedule);
        return subject.equals(expectedSubject) && schedule.equals(expectedSchedule);
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
                && schedule.equals(otherLesson.schedule) && paymentStatus == otherLesson.paymentStatus;
    }

    @Override
    public int hashCode() {
        return Objects.hash(subject, cost, schedule, paymentStatus);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("subject", subject).add("cost", cost)
                .add("schedule", schedule).add("paymentStatus", paymentStatus).toString();
    }
}
