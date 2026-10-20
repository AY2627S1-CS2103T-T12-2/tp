package seedu.address.storage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.lesson.Cost;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonSchedule;
import seedu.address.model.lesson.LessonTiming;
import seedu.address.model.lesson.OneOffLessonSchedule;
import seedu.address.model.lesson.PaymentStatus;
import seedu.address.model.lesson.ScheduleType;
import seedu.address.model.lesson.Subject;

/** Jackson-friendly version of {@link Lesson}. */
class JsonAdaptedLesson {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Lesson's %s field is missing!";

    private final String subject;
    private final String cost;
    private final String timing;
    private final String paymentStatus;
    private final String scheduleType;

    @JsonCreator
    public JsonAdaptedLesson(@JsonProperty("subject") String subject,
            @JsonProperty("cost") String cost, @JsonProperty("timing") String timing,
            @JsonProperty("paymentStatus") String paymentStatus,
            @JsonProperty("scheduleType") String scheduleType) {
        this.subject = subject;
        this.cost = cost;
        this.timing = timing;
        this.paymentStatus = paymentStatus;
        this.scheduleType = scheduleType;
    }

    /** Backwards-compatible constructor for enrolments without payment status. */
    public JsonAdaptedLesson(String subject, String cost, String timing) {
        this(subject, cost, timing, null, null);
    }

    /** Backwards-compatible constructor for tests and enrolments with an explicit payment status. */
    public JsonAdaptedLesson(String subject, String cost, String timing, String paymentStatus) {
        this(subject, cost, timing, paymentStatus, null);
    }

    public JsonAdaptedLesson(Lesson source) {
        subject = source.getSubject().value;
        cost = source.getCost().toString();
        timing = source.getSchedule().toStorageString();
        paymentStatus = source.getPaymentStatus().name();
        scheduleType = source.getSchedule().getType().name();
    }

    public Lesson toModelType() throws IllegalValueException {
        requireField(subject, Subject.class);
        requireField(cost, Cost.class);
        requireField(timing, LessonSchedule.class);
        if (!Subject.isValidSubject(subject)) {
            throw new IllegalValueException(Subject.MESSAGE_CONSTRAINTS);
        }
        if (!Cost.isValidCost(cost)) {
            throw new IllegalValueException(Cost.MESSAGE_CONSTRAINTS);
        }
        LessonSchedule modelSchedule = parseSchedule();
        PaymentStatus modelPaymentStatus = PaymentStatus.UNPAID;
        if (paymentStatus != null) {
            if (!PaymentStatus.isValidPaymentStatus(paymentStatus)) {
                throw new IllegalValueException(PaymentStatus.MESSAGE_CONSTRAINTS);
            }
            modelPaymentStatus = PaymentStatus.fromString(paymentStatus);
        }
        return new Lesson(new Subject(subject), new Cost(cost), modelSchedule, modelPaymentStatus);
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

    private static void requireField(String value, Class<?> fieldClass) throws IllegalValueException {
        if (value == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, fieldClass.getSimpleName()));
        }
    }
}
