package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CLASS_ID;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.lesson.ClassId;
import seedu.address.model.lesson.Enrollment;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.TuitionClass;
import seedu.address.model.person.Person;

/** Enrols an existing student in a scheduled class. */
public class ClassEnrolCommand extends ClassCommand {

    public static final String SUBCOMMAND_WORD = "enrol";
    public static final String MESSAGE_USAGE = COMMAND_WORD + " " + SUBCOMMAND_WORD
            + ": Enrols the indexed student in a class.\n"
            + "Parameters: STUDENT_INDEX " + PREFIX_CLASS_ID + "CLASS_ID\n"
            + "Example: " + COMMAND_WORD + " " + SUBCOMMAND_WORD + " 1 id/C001";
    public static final String MESSAGE_SUCCESS = "Enrolled %1$s in class: %2$s";
    public static final String MESSAGE_DUPLICATE_CLASS = "This student is already enrolled in the specified class.";
    public static final String MESSAGE_CLASS_COST_CONFLICT =
            "This class already exists with a different cost. Students in the same class must have the same cost.";
    public static final String MESSAGE_CLASS_NOT_FOUND = "No class exists with ID %s.";

    private final Index personIndex;
    private final ClassId classId;
    private final Lesson legacyLesson;

    /** Creates an ID-based enrolment command. */
    public ClassEnrolCommand(Index personIndex, ClassId classId) {
        this.personIndex = requireNonNull(personIndex);
        this.classId = requireNonNull(classId);
        this.legacyLesson = null;
    }

    /** Creates a command that enrols the indexed student in {@code lesson}. */
    public ClassEnrolCommand(Index personIndex, Lesson lesson) {
        this.personIndex = requireNonNull(personIndex);
        this.legacyLesson = requireNonNull(lesson);
        this.classId = lesson.getClassId();
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> persons = model.getFilteredPersonList();
        if (personIndex.getZeroBased() >= persons.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        TuitionClass tuitionClass;
        if (classId != null) {
            if (!model.hasTuitionClass(classId)) {
                throw new CommandException(String.format(MESSAGE_CLASS_NOT_FOUND, classId));
            }
            tuitionClass = model.getTuitionClass(classId);
        } else {
            if (model.hasClassCostConflict(legacyLesson)) {
                throw new CommandException(MESSAGE_CLASS_COST_CONFLICT);
            }
            tuitionClass = model.getTuitionClassList().stream()
                    .filter(item -> item.getSubject().value.equalsIgnoreCase(legacyLesson.getSubject().value)
                            && item.getSchedule().equals(legacyLesson.getSchedule()))
                    .findFirst().orElse(null);
            if (tuitionClass == null) {
                List<Lesson> overlappingClasses = model.findOverlappingClasses(legacyLesson);
                if (!overlappingClasses.isEmpty()) {
                    throw new CommandException(Messages.getClassOverlapMessage(legacyLesson, overlappingClasses));
                }
                tuitionClass = new TuitionClass(model.allocateClassId(), legacyLesson.getSubject(),
                        legacyLesson.getCost(), legacyLesson.getSchedule());
                model.addTuitionClass(tuitionClass);
            }
        }
        Person person = persons.get(personIndex.getZeroBased());
        TuitionClass selectedClass = tuitionClass;
        if (person.getLessons().stream()
                .anyMatch(existing -> selectedClass.getClassId().equals(existing.getClassId()))) {
            throw new CommandException(MESSAGE_DUPLICATE_CLASS);
        }
        Set<Lesson> updatedLessons = new HashSet<>(person.getLessons());
        Lesson enrollment = new Enrollment(tuitionClass);
        updatedLessons.add(enrollment);
        model.setPerson(person, person.withLessons(updatedLessons));
        return new CommandResult(String.format(MESSAGE_SUCCESS, person.getName(),
                legacyLesson != null ? legacyLesson : enrollment));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof ClassEnrolCommand otherCommand
                && personIndex.equals(otherCommand.personIndex)
                && java.util.Objects.equals(classId, otherCommand.classId)
                && java.util.Objects.equals(legacyLesson, otherCommand.legacyLesson);
    }
}
