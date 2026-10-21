package seedu.address.storage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.lesson.ClassId;
import seedu.address.model.lesson.Cost;
import seedu.address.model.lesson.LessonSchedule;
import seedu.address.model.lesson.LessonTiming;
import seedu.address.model.lesson.OneOffLessonSchedule;
import seedu.address.model.lesson.ScheduleType;
import seedu.address.model.lesson.Subject;
import seedu.address.model.lesson.TuitionClass;

/** Jackson-friendly representation of an independently stored tuition class. */
class JsonAdaptedTuitionClass {

    private final String id;
    private final String subject;
    private final String cost;
    private final String timing;
    private final String scheduleType;

    @JsonCreator
    JsonAdaptedTuitionClass(@JsonProperty("id") String id,
            @JsonProperty("subject") String subject, @JsonProperty("cost") String cost,
            @JsonProperty("timing") String timing,
            @JsonProperty("scheduleType") String scheduleType) {
        this.id = id;
        this.subject = subject;
        this.cost = cost;
        this.timing = timing;
        this.scheduleType = scheduleType;
    }

    /** Backwards-compatible constructor for weekly classes without an explicit schedule type. */
    JsonAdaptedTuitionClass(String id, String subject, String cost, String timing) {
        this(id, subject, cost, timing, null);
    }

    JsonAdaptedTuitionClass(TuitionClass source) {
        this(source.getClassId().toString(), source.getSubject().value,
                source.getCost().toString(), source.getSchedule().toStorageString(),
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
            if (ScheduleType.ONE_OFF.name().equals(scheduleType)) {
                throw new IllegalValueException(OneOffLessonSchedule.MESSAGE_CONSTRAINTS);
            }
            throw new IllegalValueException(LessonTiming.MESSAGE_CONSTRAINTS);
        }
        return new TuitionClass(new ClassId(id), new Subject(subject), new Cost(cost), parseSchedule());
    }

    private LessonSchedule parseSchedule() throws IllegalValueException {
        ScheduleType modelScheduleType;
        if (scheduleType == null) {
            modelScheduleType = ScheduleType.WEEKLY;
        } else {
            try {
                modelScheduleType = ScheduleType.valueOf(scheduleType);
            } catch (IllegalArgumentException exception) {
                throw new IllegalValueException("Unknown lesson schedule type: " + scheduleType);
            }
        }
        if (modelScheduleType == ScheduleType.WEEKLY) {
            if (!LessonTiming.isValidLessonTiming(timing)) {
                throw new IllegalValueException(LessonTiming.MESSAGE_CONSTRAINTS);
            }
            return new LessonTiming(timing);
        }
        if (!OneOffLessonSchedule.isValidOneOffLessonSchedule(timing)) {
            throw new IllegalValueException(OneOffLessonSchedule.MESSAGE_CONSTRAINTS);
        }
        return new OneOffLessonSchedule(timing);
    }
}
