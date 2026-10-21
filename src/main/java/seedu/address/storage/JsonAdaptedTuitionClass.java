package seedu.address.storage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.lesson.ClassId;
import seedu.address.model.lesson.Cost;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonTiming;
import seedu.address.model.lesson.OneOffLessonSchedule;
import seedu.address.model.lesson.ScheduleType;
import seedu.address.model.lesson.Subject;
import seedu.address.model.lesson.TuitionClass;

/** Jackson-friendly representation of an independently stored tuition class. */
class JsonAdaptedTuitionClass {

    private final String scheduleType;
    private final String mode;
    private final String meetingLink;
    private final String location;
    private final String notes;
    private final String id;
    private final String subject;
    private final String cost;
    private final String timing;

    @JsonCreator
    JsonAdaptedTuitionClass(@JsonProperty("id") String id,
            @JsonProperty("subject") String subject, @JsonProperty("cost") String cost,
            @JsonProperty("timing") String timing, @JsonProperty("mode") String mode,
            @JsonProperty("meetingLink") String meetingLink, @JsonProperty("location") String location,
            @JsonProperty("notes") String notes, @JsonProperty("scheduleType") String scheduleType) {
        this.scheduleType = scheduleType;
        this.mode = mode;
        this.meetingLink = meetingLink;
        this.location = location;
        this.notes = notes;
        this.id = id;
        this.subject = subject;
        this.cost = cost;
        this.timing = timing;
    }

    /** Retains the weekly class constructor with additional details. */
    JsonAdaptedTuitionClass(String id, String subject, String cost, String timing, String mode,
            String meetingLink, String location, String notes) {
        this(id, subject, cost, timing, mode, meetingLink, location, notes, null);
    }

    /** Retains the schedule-aware class constructor. */
    JsonAdaptedTuitionClass(String id, String subject, String cost, String timing, String scheduleType) {
        this(id, subject, cost, timing, null, null, null, null, scheduleType);
    }

    /** Retains the previous class storage constructor. */
    JsonAdaptedTuitionClass(String id, String subject, String cost, String timing) {
        this(id, subject, cost, timing, null, null, null, null);
    }

    JsonAdaptedTuitionClass(TuitionClass source) {
        this(source.getClassId().toString(), source.getSubject().value,
                source.getCost().toString(), source.getSchedule().toStorageString(), source.getDetails().mode().name(),
                source.getDetails().meetingLink(), source.getDetails().location(), source.getDetails().notes(),
                source.getSchedule().getType().name());
    }

    TuitionClass toModelType() throws IllegalValueException {
        if (!ClassId.isValidClassId(id)) {
            throw new IllegalValueException(ClassId.MESSAGE_CONSTRAINTS);
        }
        if (subject == null || !Subject.isValidSubject(subject)) {
            throw new IllegalValueException(Subject.MESSAGE_CONSTRAINTS);
        }
        if (cost == null || !Cost.isValidCost(cost)) {
            throw new IllegalValueException(Cost.MESSAGE_CONSTRAINTS);
        }
        if (timing == null) {
            throw new IllegalValueException(ScheduleType.ONE_OFF.name().equals(scheduleType)
                    ? OneOffLessonSchedule.MESSAGE_CONSTRAINTS : LessonTiming.MESSAGE_CONSTRAINTS);
        }
        Lesson validated = new JsonAdaptedLesson(subject, cost, timing, null,
                meetingLink, location, notes, mode, id, scheduleType).toModelType();
        return new TuitionClass(new ClassId(id), new Subject(subject), new Cost(cost), validated.getSchedule(),
                validated.getDetails());
    }
}
