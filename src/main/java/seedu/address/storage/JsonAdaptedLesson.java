package seedu.address.storage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.lesson.ClassDetails;
import seedu.address.model.lesson.ClassId;
import seedu.address.model.lesson.ClassMode;
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

    private final String scheduleType;
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
            @JsonProperty("mode") String mode, @JsonProperty("classId") String classId,
            @JsonProperty("scheduleType") String scheduleType, @JsonProperty("schedule") String savedSchedule) {
        this.scheduleType = scheduleType;
        this.classId = classId;
        this.mode = mode;
        this.meetingLink = meetingLink;
        this.location = location;
        this.notes = notes;
        this.subject = subject;
        this.cost = cost;
        this.timing = timing == null ? savedSchedule : timing;
        this.paymentStatus = paymentStatus;
    }

    /** Retains the canonical timing-based constructor. */
    public JsonAdaptedLesson(String subject, String cost, String timing, String paymentStatus,
            String meetingLink, String location, String notes, String mode, String classId, String scheduleType) {
        this(subject, cost, timing, paymentStatus, meetingLink, location, notes, mode, classId, scheduleType, null);
    }

    /** Retains the attribute and class-ID constructor for weekly lessons. */
    public JsonAdaptedLesson(String subject, String cost, String timing, String paymentStatus,
            String meetingLink, String location, String notes, String mode, String classId) {
        this(subject, cost, timing, paymentStatus, meetingLink, location, notes, mode, classId, null);
    }

    /** Retains the schedule-type constructor introduced by one-off schedules. */
    public JsonAdaptedLesson(String subject, String cost, String timing, String paymentStatus,
            String classId, String scheduleType) {
        this(subject, cost, timing, paymentStatus, null, null, null, null, classId, scheduleType);
    }

    /** Retains the attribute-only storage constructor used before stable class IDs. */
    public JsonAdaptedLesson(String subject, String cost, String timing, String paymentStatus,
            String meetingLink, String location, String notes, String mode) {
        this(subject, cost, timing, paymentStatus, meetingLink, location, notes, mode, null);
    }

    /** Retains the schedule-type constructor for records without additional details. */
    public JsonAdaptedLesson(String subject, String cost, String timing, String paymentStatus, String scheduleType) {
        this(subject, cost, timing, paymentStatus, null, scheduleType);
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
        scheduleType = source.getSchedule().getType().name();
        classId = source.getClassId() == null ? null : source.getClassId().toString();
        subject = source.getSubject().value;
        cost = source.getCost().toString();
        timing = source.getSchedule().toStorageString();
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
                    modelSchedule, modelPaymentStatus, details);
        } catch (IllegalArgumentException e) {
            throw new IllegalValueException(e.getMessage());
        }
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
