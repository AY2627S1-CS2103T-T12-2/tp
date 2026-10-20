package seedu.address.storage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.lesson.ClassDetails;
import seedu.address.model.lesson.ClassId;
import seedu.address.model.lesson.ClassMode;
import seedu.address.model.lesson.Cost;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonTiming;
import seedu.address.model.lesson.PaymentStatus;
import seedu.address.model.lesson.Subject;

/** Jackson-friendly version of {@link Lesson}. */
class JsonAdaptedLesson {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Lesson's %s field is missing!";

    private final String classId;
    private final String subject;
    private final String cost;
    private final String timing;
    private final String paymentStatus;
    private final String meetingLink;
    private final String mode;
    private final String location;
    private final String notes;

    @JsonCreator
    public JsonAdaptedLesson(@JsonProperty("subject") String subject,
            @JsonProperty("cost") String cost, @JsonProperty("timing") String timing,
            @JsonProperty("paymentStatus") String paymentStatus,
            @JsonProperty("meetingLink") String meetingLink,
            @JsonProperty("location") String location, @JsonProperty("notes") String notes,
            @JsonProperty("mode") String mode, @JsonProperty("classId") String classId) {
        this.classId = classId;
        this.mode = mode;
        this.meetingLink = meetingLink;
        this.location = location;
        this.notes = notes;
        this.subject = subject;
        this.cost = cost;
        this.timing = timing;
        this.paymentStatus = paymentStatus;
    }

    /** Retains the attribute-only storage constructor used before stable class IDs. */
    public JsonAdaptedLesson(String subject, String cost, String timing, String paymentStatus,
            String meetingLink, String location, String notes, String mode) {
        this(subject, cost, timing, paymentStatus, meetingLink, location, notes, mode, null);
    }

    /** Retains the class-ID constructor for records without additional details. */
    public JsonAdaptedLesson(String subject, String cost, String timing, String paymentStatus, String classId) {
        this(subject, cost, timing, paymentStatus, null, null, null, null, classId);
    }

    /** Backwards-compatible constructor for details saved before modes were introduced. */
    public JsonAdaptedLesson(String subject, String cost, String timing, String paymentStatus,
            String meetingLink, String location, String notes) {
        this(subject, cost, timing, paymentStatus, meetingLink, location, notes, null);
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
        classId = source.getClassId() == null ? null : source.getClassId().toString();
        subject = source.getSubject().value;
        cost = source.getCost().toString();
        timing = source.getTiming().toStorageString();
        paymentStatus = source.getPaymentStatus().name();
        meetingLink = source.getDetails().meetingLink();
        mode = source.getDetails().mode().name();
        location = source.getDetails().location();
        notes = source.getDetails().notes();
    }

    /** Returns whether this record explicitly stores additional class fields. */
    boolean hasExplicitDetails() {
        return mode != null || meetingLink != null || location != null || notes != null;
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
        try {
            String link = meetingLink == null ? "" : meetingLink;
            ClassMode classMode = mode == null ? (link.isEmpty() ? ClassMode.OFFLINE : ClassMode.ONLINE)
                    : ClassMode.fromString(mode);
            ClassDetails details = new ClassDetails(classMode, link,
                    location == null ? "" : location, notes == null ? "" : notes);
            return new Lesson(classId == null ? null : new ClassId(classId), new Subject(subject), new Cost(cost),
                    new LessonTiming(timing), modelPaymentStatus, details);
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
