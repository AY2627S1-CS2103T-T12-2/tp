package seedu.address.model.lesson;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Objects;

import seedu.address.commons.util.ToStringBuilder;

/** An independently stored recurring tuition class. */
public final class TuitionClass {

    private final ClassId classId;
    private final Subject subject;
    private final Cost cost;
    private final LessonTiming timing;

    /** Creates an immutable class with a stable ID. */
    public TuitionClass(ClassId classId, Subject subject, Cost cost, LessonTiming timing) {
        requireAllNonNull(classId, subject, cost, timing);
        this.classId = classId;
        this.subject = subject;
        this.cost = cost;
        this.timing = timing;
    }

    public ClassId getClassId() {
        return classId;
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

    /** Returns true when subject and timing identify the same logical class. */
    public boolean hasSameDetails(TuitionClass other) {
        return other != null && subject.value.equalsIgnoreCase(other.subject.value)
                && timing.equals(other.timing);
    }

    public boolean overlapsWith(TuitionClass other) {
        return other != null && timing.overlapsWith(other.timing);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof TuitionClass otherClass
                && classId.equals(otherClass.classId)
                && subject.equals(otherClass.subject)
                && cost.equals(otherClass.cost)
                && timing.equals(otherClass.timing);
    }

    @Override
    public int hashCode() {
        return Objects.hash(classId, subject, cost, timing);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("id", classId).add("subject", subject)
                .add("cost", cost).add("timing", timing).toString();
    }
}
