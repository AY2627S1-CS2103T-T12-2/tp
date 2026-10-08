package seedu.address.model.lesson;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Objects;

import seedu.address.commons.util.ToStringBuilder;

/** Represents a recurring weekly tuition lesson. */
public class Lesson {

    private final Subject subject;
    private final Cost cost;
    private final LessonTiming timing;

    /** Creates a lesson with one subject and one weekly timeslot. */
    public Lesson(Subject subject, Cost cost, LessonTiming timing) {
        requireAllNonNull(subject, cost, timing);
        this.subject = subject;
        this.cost = cost;
        this.timing = timing;
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

    /**
     * Returns true if both lessons represent the same class. Cost is excluded because every student
     * in the same class must share one cost.
     */
    public boolean isSameClass(Lesson otherLesson) {
        return otherLesson == this || otherLesson != null
                && subject.value.equalsIgnoreCase(otherLesson.subject.value)
                && timing.equals(otherLesson.timing);
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
                && timing.equals(otherLesson.timing);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subject, cost, timing);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("subject", subject).add("cost", cost)
                .add("timing", timing).toString();
    }
}
