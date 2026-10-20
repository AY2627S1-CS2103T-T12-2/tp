package seedu.address.storage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.lesson.ClassId;
import seedu.address.model.lesson.Cost;
import seedu.address.model.lesson.LessonTiming;
import seedu.address.model.lesson.Subject;
import seedu.address.model.lesson.TuitionClass;

/** Jackson-friendly representation of an independently stored tuition class. */
class JsonAdaptedTuitionClass {

    private final String id;
    private final String subject;
    private final String cost;
    private final String timing;

    @JsonCreator
    JsonAdaptedTuitionClass(@JsonProperty("id") String id,
            @JsonProperty("subject") String subject, @JsonProperty("cost") String cost,
            @JsonProperty("timing") String timing) {
        this.id = id;
        this.subject = subject;
        this.cost = cost;
        this.timing = timing;
    }

    JsonAdaptedTuitionClass(TuitionClass source) {
        this(source.getClassId().toString(), source.getSubject().value,
                source.getCost().toString(), source.getTiming().toStorageString());
    }

    TuitionClass toModelType() throws IllegalValueException {
        if (!ClassId.isValidClassId(id)) {
            throw new IllegalValueException(ClassId.MESSAGE_CONSTRAINTS);
        }
        if (!Subject.isValidSubject(subject)) {
            throw new IllegalValueException(Subject.MESSAGE_CONSTRAINTS);
        }
        if (!Cost.isValidCost(cost)) {
            throw new IllegalValueException(Cost.MESSAGE_CONSTRAINTS);
        }
        if (!LessonTiming.isValidLessonTiming(timing)) {
            throw new IllegalValueException(LessonTiming.MESSAGE_CONSTRAINTS);
        }
        return new TuitionClass(new ClassId(id), new Subject(subject), new Cost(cost), new LessonTiming(timing));
    }
}
