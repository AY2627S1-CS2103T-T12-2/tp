package seedu.address.storage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.lesson.ClassDetails;
import seedu.address.model.lesson.Cost;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonTiming;
import seedu.address.model.lesson.PaymentStatus;
import seedu.address.model.lesson.Subject;

/** Jackson-friendly version of {@link Lesson}. */
class JsonAdaptedLesson {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Lesson's %s field is missing!";

    private final String subject;
    private final String cost;
    private final String timing;
    private final String paymentStatus;
    private final String meetingLink;
    private final String location;
    private final String notes;

    @JsonCreator
    public JsonAdaptedLesson(@JsonProperty("subject") String subject,
            @JsonProperty("cost") String cost, @JsonProperty("timing") String timing,
            @JsonProperty("paymentStatus") String paymentStatus,
            @JsonProperty("meetingLink") String meetingLink,
            @JsonProperty("location") String location, @JsonProperty("notes") String notes) {
        this.meetingLink = meetingLink;
        this.location = location;
        this.notes = notes;
        this.subject = subject;
        this.cost = cost;
        this.timing = timing;
        this.paymentStatus = paymentStatus;
    }

    /** Backwards-compatible constructor for enrolments without shared details. */
    public JsonAdaptedLesson(String subject, String cost, String timing, String paymentStatus) {
        this(subject, cost, timing, paymentStatus, null, null, null);
    }

    /** Backwards-compatible constructor for enrolments without payment status. */
    public JsonAdaptedLesson(String subject, String cost, String timing) {
        this(subject, cost, timing, null);
    }

    public JsonAdaptedLesson(Lesson source) {
        subject = source.getSubject().value;
        cost = source.getCost().toString();
        timing = source.getTiming().toStorageString();
        paymentStatus = source.getPaymentStatus().name();
        meetingLink = source.getDetails().meetingLink();
        location = source.getDetails().location();
        notes = source.getDetails().notes();
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
        try {
            ClassDetails details = new ClassDetails(meetingLink == null ? "" : meetingLink,
                    location == null ? "" : location, notes == null ? "" : notes);
            return new Lesson(new Subject(subject), new Cost(cost), new LessonTiming(timing), modelPaymentStatus,
                    details);
        } catch (IllegalArgumentException e) {
            throw new IllegalValueException(e.getMessage());
        }
    }

    private static void requireField(String value, Class<?> fieldClass) throws IllegalValueException {
        if (value == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, fieldClass.getSimpleName()));
        }
    }
}
