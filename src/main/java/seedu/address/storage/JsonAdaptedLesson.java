package seedu.address.storage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.lesson.ClassId;
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
    private final String classId;
    private final String cost;
    private final String schedule;
    private final String paymentStatus;
    private final String scheduleType;

    @JsonCreator
    public JsonAdaptedLesson(@JsonProperty("subject") String subject,
            @JsonProperty("cost") String cost, @JsonProperty("timing") String schedule,
            @JsonProperty("paymentStatus") String paymentStatus,
            @JsonProperty("classId") String classId,
            @JsonProperty("scheduleType") String scheduleType) {
        this.subject = subject;
        this.classId = classId;
        this.cost = cost;
        this.schedule = schedule;
        this.paymentStatus = paymentStatus;
        this.scheduleType = scheduleType;
    }

    /** Backwards-compatible constructor for enrolments without payment status. */
    public JsonAdaptedLesson(String subject, String cost, String timing) {
        this(subject, cost, timing, null, null, null);
    }

    /** Constructor retained for existing tests and callers. */
    public JsonAdaptedLesson(String subject, String cost, String schedule, String paymentStatus) {
        this(subject, cost, schedule, paymentStatus, null, null);
    }

    /** Backwards-compatible constructor for an explicit schedule type without a class ID. */
    public JsonAdaptedLesson(String subject, String cost, String schedule, String paymentStatus,
            String scheduleType) {
        this(subject, cost, schedule, paymentStatus, null, scheduleType);
    }

    public JsonAdaptedLesson(Lesson source) {
        subject = source.getSubject().value;
        classId = source.getClassId() == null ? null : source.getClassId().toString();
        cost = source.getCost().toString();
        schedule = source.getSchedule().toStorageString();
        paymentStatus = source.getPaymentStatus().name();
        scheduleType = source.getSchedule().getType().name();
    }

    public Lesson toModelType() throws IllegalValueException {
        requireField(subject, Subject.class);
        requireField(cost, Cost.class);
        requireField(schedule, LessonSchedule.class);
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
        if (classId != null && !ClassId.isValidClassId(classId)) {
            throw new IllegalValueException(ClassId.MESSAGE_CONSTRAINTS);
        }
        return new Lesson(classId == null ? null : new ClassId(classId), new Subject(subject),
                new Cost(cost), modelSchedule, modelPaymentStatus);
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
            if (!LessonTiming.isValidLessonTiming(schedule)) {
                throw new IllegalValueException(LessonTiming.MESSAGE_CONSTRAINTS);
            }
            return new LessonTiming(schedule);
        }
        if (!OneOffLessonSchedule.isValidOneOffLessonSchedule(schedule)) {
            throw new IllegalValueException(OneOffLessonSchedule.MESSAGE_CONSTRAINTS);
        }
        return new OneOffLessonSchedule(schedule);
    }

    private static void requireField(String value, Class<?> fieldClass) throws IllegalValueException {
        if (value == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, fieldClass.getSimpleName()));
        }
    }
}
