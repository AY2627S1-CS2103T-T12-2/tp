package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CLASS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CLASS_ID;
import static seedu.address.logic.parser.CliSyntax.PREFIX_COST;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.lesson.ClassId;
import seedu.address.model.lesson.Enrollment;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonSchedule;
import seedu.address.model.lesson.Subject;
import seedu.address.model.lesson.TuitionClass;
import seedu.address.model.person.Person;

/** Enrols one or more existing students in a scheduled class. */
public class ClassEnrolCommand extends ClassCommand {

    public static final String SUBCOMMAND_WORD = "enrol";
    public static final String MESSAGE_USAGE = COMMAND_WORD + " " + SUBCOMMAND_WORD
            + ": Enrols one or more indexed students in a class.\n"
            + "To use a class ID: STUDENT_INDEX " + PREFIX_CLASS_ID + "CLASS_ID\n"
            + "To use an existing class: STUDENT_INDEX [STUDENT_INDEX]... "
            + PREFIX_SUBJECT + "SUBJECT " + PREFIX_CLASS + "SCHEDULE\n"
            + "To supply a class cost: STUDENT_INDEX " + PREFIX_SUBJECT + "SUBJECT "
            + PREFIX_COST + "COST " + PREFIX_CLASS + "SCHEDULE\n"
            + "Examples:\n"
            + COMMAND_WORD + " " + SUBCOMMAND_WORD + " 1 " + PREFIX_CLASS_ID + "C001\n"
            + COMMAND_WORD + " " + SUBCOMMAND_WORD + " 1 3 5 "
            + PREFIX_SUBJECT + "Math " + PREFIX_CLASS + "Monday1800-1930\n"
            + COMMAND_WORD + " " + SUBCOMMAND_WORD + " 1 "
            + PREFIX_SUBJECT + "Math " + PREFIX_COST + "30 " + PREFIX_CLASS + "Monday1800-1930";
    public static final String MESSAGE_SUCCESS = "Enrolled %1$s in class %2$s: %3$s, %4$s.";
    public static final String MESSAGE_BATCH_SUCCESS = "Enrolled %1$s in class: %2$s, %3$s.";
    public static final String MESSAGE_BATCH_PARTIAL_SUCCESS =
            "Enrolled %1$s in class: %2$s, %3$s. Already enrolled and skipped: %4$s.";
    public static final String MESSAGE_BATCH_ALL_ALREADY_ENROLLED =
            "No students were enrolled in class: %1$s, %2$s. Already enrolled: %3$s. No changes were made.";
    public static final String MESSAGE_DUPLICATE_CLASS = "This student is already enrolled in the specified class.";
    public static final String MESSAGE_EXISTING_CLASS_NOT_FOUND =
            "The specified class does not exist. No students were enrolled.";
    public static final String MESSAGE_CLASS_NOT_FOUND = "No class exists with ID %s.";
    public static final String MESSAGE_BATCH_COST_NOT_ALLOWED =
            "Do not provide cost/ when enrolling multiple students. Use an existing class's subject and schedule.";
    private final List<Index> personIndexes;
    private final ClassId classId;
    private final Lesson legacyLesson;
    private final Subject subject;
    private final LessonSchedule schedule;

    /** Creates an ID-based enrolment command for one student. */
    public ClassEnrolCommand(Index personIndex, ClassId classId) {
        personIndexes = List.of(requireNonNull(personIndex));
        this.classId = requireNonNull(classId);
        legacyLesson = null;
        subject = null;
        schedule = null;
    }

    /** Creates a command that enrols one student while supplying the class details and cost. */
    public ClassEnrolCommand(Index personIndex, Lesson lesson) {
        personIndexes = List.of(requireNonNull(personIndex));
        legacyLesson = requireNonNull(lesson);
        classId = lesson.getClassId();
        subject = null;
        schedule = null;
    }

    /** Creates a command that enrols the indexed students in an existing class. */
    public ClassEnrolCommand(List<Index> personIndexes, Subject subject, LessonSchedule schedule) {
        requireAllNonNull(personIndexes);
        checkArgument(!personIndexes.isEmpty(), "At least one student index must be provided.");
        checkArgument(personIndexes.stream().map(Index::getZeroBased).distinct().count() == personIndexes.size(),
                "Student indexes must not be repeated.");
        this.personIndexes = List.copyOf(personIndexes);
        this.subject = requireNonNull(subject);
        this.schedule = requireNonNull(schedule);
        classId = null;
        legacyLesson = null;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> persons = model.getFilteredPersonList();
        if (personIndexes.stream().anyMatch(index -> index.getZeroBased() >= persons.size())) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        List<Person> selectedPersons = personIndexes.stream()
                .map(index -> persons.get(index.getZeroBased()))
                .toList();
        TuitionClass tuitionClass = resolveTuitionClass(model);
        List<Person> alreadyEnrolledPersons = selectedPersons.stream()
                .filter(person -> person.getEnrollments().stream()
                        .anyMatch(existing -> existing.getClassId().equals(tuitionClass.getClassId())))
                .toList();
        if (!isBatchExistingClassCommand() && !alreadyEnrolledPersons.isEmpty()) {
            throw new CommandException(MESSAGE_DUPLICATE_CLASS);
        }

        List<Person> personsToEnrol = selectedPersons.stream()
                .filter(person -> person.getEnrollments().stream()
                        .noneMatch(existing -> existing.getClassId().equals(tuitionClass.getClassId())))
                .toList();
        if (personsToEnrol.isEmpty()) {
            return new CommandResult(String.format(MESSAGE_BATCH_ALL_ALREADY_ENROLLED,
                    tuitionClass.getSubject(), tuitionClass.getSchedule(), formatStudentNames(alreadyEnrolledPersons)));
        }

        for (Person person : personsToEnrol) {
            Set<Enrollment> updatedEnrollments = new LinkedHashSet<>(person.getEnrollments());
            seedu.address.model.lesson.Cost agreedRate = legacyLesson == null
                    ? tuitionClass.getCost() : legacyLesson.getCost();
            updatedEnrollments.add(new Enrollment(person.getStudentId(), tuitionClass, agreedRate,
                    seedu.address.model.lesson.PaymentStatus.UNPAID));
            model.setPerson(person, person.withEnrollments(updatedEnrollments));
        }

        if (!isBatchExistingClassCommand()) {
            return new CommandResult(String.format(MESSAGE_SUCCESS, selectedPersons.get(0).getName(),
                    tuitionClass.getClassId(), tuitionClass.getSubject(), tuitionClass.getSchedule()));
        }
        String enrolledStudentNames = formatStudentNames(personsToEnrol);
        if (alreadyEnrolledPersons.isEmpty()) {
            return new CommandResult(String.format(MESSAGE_BATCH_SUCCESS,
                    enrolledStudentNames, tuitionClass.getSubject(), tuitionClass.getSchedule()));
        }
        return new CommandResult(String.format(MESSAGE_BATCH_PARTIAL_SUCCESS,
                enrolledStudentNames, tuitionClass.getSubject(), tuitionClass.getSchedule(),
                formatStudentNames(alreadyEnrolledPersons)));
    }

    private boolean isBatchExistingClassCommand() {
        return classId == null && legacyLesson == null;
    }

    private static String formatStudentNames(List<Person> persons) {
        return persons.stream()
                .map(person -> person.getName().toString())
                .collect(Collectors.joining(", "));
    }

    private TuitionClass resolveTuitionClass(Model model) throws CommandException {
        if (classId != null) {
            if (!model.hasTuitionClass(classId)) {
                throw new CommandException(String.format(MESSAGE_CLASS_NOT_FOUND, classId));
            }
            return model.getTuitionClass(classId);
        }
        if (legacyLesson == null) {
            return model.findTuitionClass(subject, schedule)
                    .orElseThrow(() -> new CommandException(MESSAGE_EXISTING_CLASS_NOT_FOUND));
        }
        TuitionClass existingClass = model.findTuitionClass(legacyLesson.getSubject(), legacyLesson.getSchedule())
                .orElse(null);
        if (existingClass != null) {
            return existingClass;
        }
        List<Lesson> overlappingClasses = model.findOverlappingClasses(legacyLesson);
        if (!overlappingClasses.isEmpty()) {
            throw new CommandException(Messages.getClassOverlapMessage(legacyLesson, overlappingClasses));
        }
        TuitionClass createdClass = new TuitionClass(model.allocateClassId(), legacyLesson.getSubject(),
                legacyLesson.getCost(), legacyLesson.getSchedule());
        model.addTuitionClass(createdClass);
        return createdClass;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof ClassEnrolCommand otherCommand
                && personIndexes.equals(otherCommand.personIndexes)
                && Objects.equals(classId, otherCommand.classId)
                && Objects.equals(legacyLesson, otherCommand.legacyLesson)
                && Objects.equals(subject, otherCommand.subject)
                && Objects.equals(schedule, otherCommand.schedule);
    }
}
