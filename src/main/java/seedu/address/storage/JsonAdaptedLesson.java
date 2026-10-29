package seedu.address.storage;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.lesson.ClassDetails;
import seedu.address.model.lesson.ClassId;
import seedu.address.model.lesson.ClassMode;
import seedu.address.model.lesson.Cost;
import seedu.address.model.lesson.Enrollment;
import seedu.address.model.lesson.EnrollmentId;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonSchedule;
import seedu.address.model.lesson.LessonTiming;
import seedu.address.model.lesson.OneOffLessonSchedule;
import seedu.address.model.lesson.PaymentStatus;
import seedu.address.model.lesson.ScheduleType;
import seedu.address.model.lesson.Subject;
import seedu.address.model.person.StudentId;

/** Jackson-friendly version of {@link Lesson}. */
class JsonAdaptedLesson {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Lesson's %s field is missing!";

    private final String scheduleType;
    private final String enrollmentId;
    private final String studentId;
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
            @JsonProperty("scheduleType") String scheduleType, @JsonProperty("schedule") String savedSchedule,
            @JsonProperty("enrollmentId") String enrollmentId,
            @JsonProperty("studentId") String studentId) {
        this.scheduleType = scheduleType;
        this.enrollmentId = enrollmentId;
        this.studentId = studentId;
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

    /** Retains the pre-association JSON constructor. */
    public JsonAdaptedLesson(String subject, String cost, String timing, String paymentStatus,
            String meetingLink, String location, String notes, String mode, String classId,
            String scheduleType, String savedSchedule) {
        this(subject, cost, timing, paymentStatus, meetingLink, location, notes, mode, classId,
                scheduleType, savedSchedule, null, null);
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
        enrollmentId = null;
        studentId = null;
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

    /** Converts a canonical enrolment association into its compact storage representation. */
    public JsonAdaptedLesson(Enrollment source) {
        enrollmentId = source.getEnrollmentId().toString();
        studentId = source.getStudentId().toString();
        classId = source.getClassId().toString();
        cost = source.getAgreedRate().toString();
        paymentStatus = source.getPaymentStatus().name();
        scheduleType = null;
        subject = null;
        timing = null;
        meetingLink = null;
        mode = null;
        location = null;
        notes = null;
    }

    boolean isAssociationRecord() {
        return enrollmentId != null || studentId != null;
    }

    /** Returns whether this record explicitly stores additional class fields. */
    boolean hasExplicitDetails() {
        return mode != null || meetingLink != null || location != null || notes != null;
    }

    /** Retains which individual fields were supplied, including explicitly empty values. */
    ExplicitDetails getExplicitDetails() {
        Optional<ClassMode> suppliedMode = Optional.ofNullable(mode).map(ClassMode::fromString);
        if (suppliedMode.isEmpty() && meetingLink != null && !meetingLink.isEmpty()) {
            suppliedMode = Optional.of(ClassMode.ONLINE);
        }
        return new ExplicitDetails(suppliedMode, Optional.ofNullable(meetingLink), Optional.ofNullable(location),
                Optional.ofNullable(notes));
    }

    /** Field provenance used while reconciling partially populated legacy class records. */
    record ExplicitDetails(Optional<ClassMode> mode, Optional<String> meetingLink,
            Optional<String> location, Optional<String> notes) {
        static final ExplicitDetails NONE = new ExplicitDetails(
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());

        static ExplicitDetails complete(ClassDetails details) {
            return new ExplicitDetails(Optional.of(details.mode()), Optional.of(details.meetingLink()),
                    Optional.of(details.location()), Optional.of(details.notes()));
        }

        boolean conflictsWith(ExplicitDetails other) {
            return conflicts(mode, other.mode) || conflicts(meetingLink, other.meetingLink)
                    || conflicts(location, other.location) || conflicts(notes, other.notes);
        }

        private static boolean conflicts(Optional<?> first, Optional<?> second) {
            return first.isPresent() && second.isPresent() && !first.equals(second);
        }

        ExplicitDetails withProvided(ExplicitDetails provided) {
            return new ExplicitDetails(provided.mode.or(() -> mode), provided.meetingLink.or(() -> meetingLink),
                    provided.location.or(() -> location), provided.notes.or(() -> notes));
        }

        ClassDetails applyTo(ClassDetails base) {
            return new ClassDetails(mode.orElse(base.mode()), meetingLink.orElse(base.meetingLink()),
                    location.orElse(base.location()), notes.orElse(base.notes()));
        }
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

    /** Converts a compact association record without resolving its referenced class. */
    public Enrollment toEnrollmentModelType() throws IllegalValueException {
        requireField(enrollmentId, EnrollmentId.class);
        requireField(studentId, StudentId.class);
        requireField(classId, ClassId.class);
        requireField(cost, Cost.class);
        requireField(paymentStatus, PaymentStatus.class);
        if (!EnrollmentId.isValidEnrollmentId(enrollmentId)) {
            throw new IllegalValueException(EnrollmentId.MESSAGE_CONSTRAINTS);
        }
        if (!StudentId.isValidStudentId(studentId)) {
            throw new IllegalValueException(StudentId.MESSAGE_CONSTRAINTS);
        }
        if (!ClassId.isValidClassId(classId)) {
            throw new IllegalValueException(ClassId.MESSAGE_CONSTRAINTS);
        }
        if (!Cost.isValidCost(cost)) {
            throw new IllegalValueException(Cost.MESSAGE_CONSTRAINTS);
        }
        if (!PaymentStatus.isValidPaymentStatus(paymentStatus)) {
            throw new IllegalValueException(PaymentStatus.MESSAGE_CONSTRAINTS);
        }
        return new Enrollment(new EnrollmentId(enrollmentId), new StudentId(studentId),
                new ClassId(classId), new Cost(cost), PaymentStatus.fromString(paymentStatus));
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
