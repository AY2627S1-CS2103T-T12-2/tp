package seedu.address.model.lesson;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Objects;

import seedu.address.commons.util.ToStringBuilder;

/** An independently stored recurring tuition class. */
public final class TuitionClass {

    private final ClassDetails details;
    private final ClassId classId;
    private final Subject subject;
    private final Cost defaultRate;
    private final LessonSchedule schedule;

    /** Creates an immutable class with a stable ID. */
    public TuitionClass(ClassId classId, Subject subject, Cost cost, LessonSchedule schedule) {
        this(classId, subject, cost, schedule, ClassDetails.EMPTY);
    }

    /** Creates a shared class with its optional online/offline attributes. */
    public TuitionClass(ClassId classId, Subject subject, Cost cost, LessonSchedule schedule, ClassDetails details) {
        requireAllNonNull(classId, subject, cost, schedule, details);
        this.details = details;
        this.classId = classId;
        this.subject = subject;
        this.defaultRate = cost;
        this.schedule = schedule;
    }

    public ClassDetails getDetails() {
        return details;
    }

    /** Returns the same class ID and schedule with additional details replaced. */
    public TuitionClass withDetails(ClassDetails updatedDetails) {
        return new TuitionClass(classId, subject, defaultRate, schedule, updatedDetails);
    }

    public ClassId getClassId() {
        return classId;
    }

    public Subject getSubject() {
        return subject;
    }

    public Cost getCost() {
        return defaultRate;
    }

    /** Returns the rate offered by default to students who enrol in this class. */
    public Cost getDefaultRate() {
        return defaultRate;
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
                && defaultRate.equals(otherClass.defaultRate)
                && schedule.equals(otherClass.schedule) && details.equals(otherClass.details);
    }

    @Override
    public int hashCode() {
        return Objects.hash(classId, subject, defaultRate, schedule, details);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("id", classId).add("subject", subject)
                .add("cost", defaultRate).add("schedule", schedule).toString();
    }
}
