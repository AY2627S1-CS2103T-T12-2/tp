package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CLASS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_COST;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.lesson.Cost;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonTiming;
import seedu.address.model.lesson.Subject;
import seedu.address.model.person.Person;

/** Enrols one or more existing students in a recurring weekly class. */
public class ClassEnrolCommand extends ClassCommand {

    public static final String SUBCOMMAND_WORD = "enrol";
    public static final String MESSAGE_USAGE = COMMAND_WORD + " " + SUBCOMMAND_WORD
            + ": Enrols one or more indexed students in a class.\n"
            + "To use an existing class: STUDENT_INDEX [STUDENT_INDEX]... "
            + PREFIX_SUBJECT + "SUBJECT " + PREFIX_CLASS + "DAYHHmm-HHmm\n"
            + "To supply a class cost: STUDENT_INDEX " + PREFIX_SUBJECT + "SUBJECT "
            + PREFIX_COST + "COST " + PREFIX_CLASS + "DAYHHmm-HHmm\n"
            + "Examples:\n"
            + COMMAND_WORD + " " + SUBCOMMAND_WORD + " 1 3 5 sub/Math c/Monday1800-1930\n"
            + COMMAND_WORD + " " + SUBCOMMAND_WORD + " 1 sub/Math cost/30 c/Monday1800-1930";
    public static final String MESSAGE_SUCCESS = "Enrolled %1$s in class: %2$s";
    public static final String MESSAGE_BATCH_SUCCESS = "Enrolled %1$s in class: %2$s, %3$s.";
    public static final String MESSAGE_BATCH_PARTIAL_SUCCESS =
            "Enrolled %1$s in class: %2$s, %3$s. Already enrolled and skipped: %4$s.";
    public static final String MESSAGE_BATCH_ALL_ALREADY_ENROLLED =
            "No students were enrolled in class: %1$s, %2$s. Already enrolled: %3$s. No changes were made.";
    public static final String MESSAGE_DUPLICATE_CLASS = "This student is already enrolled in the specified class.";
    public static final String MESSAGE_CLASS_NOT_FOUND =
            "The specified class does not exist. No students were enrolled.";
    public static final String MESSAGE_BATCH_COST_NOT_ALLOWED =
            "Do not provide cost/ when enrolling multiple students. Use an existing class's subject and timing.";
    public static final String MESSAGE_CLASS_COST_CONFLICT =
            "This class already exists with a different cost. Students in the same class must have the same cost.";

    private final List<Index> personIndexes;
    private final Subject subject;
    private final LessonTiming timing;
    private final Optional<Cost> suppliedCost;

    /** Creates a command that enrols the indexed student in {@code lesson}. */
    public ClassEnrolCommand(Index personIndex, Lesson lesson) {
        personIndexes = List.of(requireNonNull(personIndex));
        Lesson checkedLesson = requireNonNull(lesson);
        subject = checkedLesson.getSubject();
        timing = checkedLesson.getTiming();
        suppliedCost = Optional.of(checkedLesson.getCost());
    }

    /** Creates a command that enrols the indexed students in an existing class. */
    public ClassEnrolCommand(List<Index> personIndexes, Subject subject, LessonTiming timing) {
        requireAllNonNull(personIndexes);
        checkArgument(!personIndexes.isEmpty(), "At least one student index must be provided.");
        checkArgument(personIndexes.stream().map(Index::getZeroBased).distinct().count() == personIndexes.size(),
                "Student indexes must not be repeated.");
        this.personIndexes = List.copyOf(personIndexes);
        this.subject = requireNonNull(subject);
        this.timing = requireNonNull(timing);
        suppliedCost = Optional.empty();
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
        Lesson lesson = resolveLesson(model);
        if (suppliedCost.isPresent()) {
            if (model.hasClassCostConflict(lesson)) {
                throw new CommandException(MESSAGE_CLASS_COST_CONFLICT);
            }
        }

        List<Person> alreadyEnrolledPersons = selectedPersons.stream()
                .filter(person -> person.getLessons().stream().anyMatch(existing -> existing.isSameClass(lesson)))
                .toList();
        if (suppliedCost.isPresent() && !alreadyEnrolledPersons.isEmpty()) {
            throw new CommandException(MESSAGE_DUPLICATE_CLASS);
        }
        if (suppliedCost.isPresent()) {
            List<Lesson> overlappingClasses = model.findOverlappingClasses(lesson);
            if (!overlappingClasses.isEmpty()) {
                throw new CommandException(Messages.getClassOverlapMessage(lesson, overlappingClasses));
            }
        }

        List<Person> personsToEnrol = selectedPersons.stream()
                .filter(person -> person.getLessons().stream()
                        .noneMatch(existing -> existing.isSameClass(lesson)))
                .toList();
        if (personsToEnrol.isEmpty()) {
            return new CommandResult(String.format(MESSAGE_BATCH_ALL_ALREADY_ENROLLED,
                    lesson.getSubject(), lesson.getTiming(), formatStudentNames(alreadyEnrolledPersons)));
        }

        for (Person person : personsToEnrol) {
            Set<Lesson> updatedLessons = new LinkedHashSet<>(person.getLessons());
            updatedLessons.add(lesson);
            model.setPerson(person, person.withLessons(updatedLessons));
        }

        if (suppliedCost.isPresent()) {
            return new CommandResult(String.format(MESSAGE_SUCCESS, selectedPersons.get(0).getName(), lesson));
        }
        String enrolledStudentNames = formatStudentNames(personsToEnrol);
        if (alreadyEnrolledPersons.isEmpty()) {
            return new CommandResult(String.format(MESSAGE_BATCH_SUCCESS,
                    enrolledStudentNames, lesson.getSubject(), lesson.getTiming()));
        }
        return new CommandResult(String.format(MESSAGE_BATCH_PARTIAL_SUCCESS,
                enrolledStudentNames, lesson.getSubject(), lesson.getTiming(),
                formatStudentNames(alreadyEnrolledPersons)));
    }

    private static String formatStudentNames(List<Person> persons) {
        return persons.stream()
                .map(person -> person.getName().toString())
                .collect(Collectors.joining(", "));
    }

    private Lesson resolveLesson(Model model) throws CommandException {
        if (suppliedCost.isPresent()) {
            return new Lesson(subject, suppliedCost.get(), timing);
        }
        Lesson existingClass = model.getAddressBook().getPersonList().stream()
                .flatMap(person -> person.getLessons().stream())
                .filter(lesson -> lesson.isSameClass(subject, timing))
                .findFirst()
                .orElseThrow(() -> new CommandException(MESSAGE_CLASS_NOT_FOUND));
        return new Lesson(existingClass.getSubject(), existingClass.getCost(), existingClass.getTiming());
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof ClassEnrolCommand otherCommand
                && personIndexes.equals(otherCommand.personIndexes)
                && subject.equals(otherCommand.subject)
                && timing.equals(otherCommand.timing)
                && suppliedCost.equals(otherCommand.suppliedCost);
    }
}
