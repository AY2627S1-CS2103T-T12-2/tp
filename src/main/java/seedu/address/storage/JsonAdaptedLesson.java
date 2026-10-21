package seedu.address.storage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.lesson.ClassId;
import seedu.address.model.lesson.Cost;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonTiming;
import seedu.address.model.lesson.PaymentStatus;
import seedu.address.model.lesson.Subject;

/** Jackson-friendly version of {@link Lesson}. */
class JsonAdaptedLesson {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Lesson's %s field is missing!";

    private final String subject;
    private final String classId;
    private final String cost;
    private final String timing;
    private final String paymentStatus;

    @JsonCreator
    public JsonAdaptedLesson(@JsonProperty("subject") String subject,
            @JsonProperty("cost") String cost, @JsonProperty("timing") String timing,
            @JsonProperty("paymentStatus") String paymentStatus,
            @JsonProperty("classId") String classId) {
        this.subject = subject;
        this.classId = classId;
        this.cost = cost;
        this.timing = timing;
        this.paymentStatus = paymentStatus;
    }

    /** Backwards-compatible constructor for enrolments without payment status. */
    public JsonAdaptedLesson(String subject, String cost, String timing) {
        this(subject, cost, timing, null, null);
    }

    /** Constructor retained for existing tests and callers. */
    public JsonAdaptedLesson(String subject, String cost, String timing, String paymentStatus) {
        this(subject, cost, timing, paymentStatus, null);
    }

    public JsonAdaptedLesson(Lesson source) {
        subject = source.getSubject().value;
        classId = source.getClassId() == null ? null : source.getClassId().toString();
        cost = source.getCost().toString();
        timing = source.getTiming().toStorageString();
        paymentStatus = source.getPaymentStatus().name();
    }

    public Lesson toModelType() throws IllegalValueException {
        requireField(subject, Subject.class);
        requireField(cost, Cost.class);
        requireField(timing, LessonTiming.class);
        if (!Subject.isValidSubject(subject)) {
            throw new IllegalValueException(Subject.MESSAGE_CONSTRAINTS);
        }
        if (!Cost.isValidCost(cost)) {
            throw new IllegalValueException(Cost.MESSAGE_CONSTRAINTS);
        }
        if (!LessonTiming.isValidLessonTiming(timing)) {
            throw new IllegalValueException(LessonTiming.MESSAGE_CONSTRAINTS);
        }
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
                new Cost(cost), new LessonTiming(timing), modelPaymentStatus);
    }

    private static void requireField(String value, Class<?> fieldClass) throws IllegalValueException {
        if (value == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, fieldClass.getSimpleName()));
        }
    }
}
