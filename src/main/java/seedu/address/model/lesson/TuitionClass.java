package seedu.address.model.lesson;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Objects;

import seedu.address.commons.util.ToStringBuilder;

/** An independently stored scheduled tuition class. */
public final class TuitionClass {

    private final ClassId classId;
    private final Subject subject;
    private final Cost cost;
    private final LessonSchedule schedule;

    /** Creates an immutable class with a stable ID. */
    public TuitionClass(ClassId classId, Subject subject, Cost cost, LessonSchedule schedule) {
        requireAllNonNull(classId, subject, cost, schedule);
        this.classId = classId;
        this.subject = subject;
        this.cost = cost;
        this.schedule = schedule;
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

    public LessonSchedule getSchedule() {
        return schedule;
    }

    /** Returns true when subject and schedule identify the same logical class. */
    public boolean hasSameDetails(TuitionClass other) {
        return other != null && subject.value.equalsIgnoreCase(other.subject.value)
                && schedule.equals(other.schedule);
    }

    public boolean overlapsWith(TuitionClass other) {
        return other != null && schedule.overlapsWith(other.schedule);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof TuitionClass otherClass
                && classId.equals(otherClass.classId)
                && subject.equals(otherClass.subject)
                && cost.equals(otherClass.cost)
                && schedule.equals(otherClass.schedule);
    }

    @Override
    public int hashCode() {
        return Objects.hash(classId, subject, cost, schedule);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("id", classId).add("subject", subject)
                .add("cost", cost).add("schedule", schedule).toString();
    }
}
