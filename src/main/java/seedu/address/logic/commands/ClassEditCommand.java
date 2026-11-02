package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CLASS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CLASS_ID;
import static seedu.address.logic.parser.CliSyntax.PREFIX_COST;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LINK;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LOCATION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MODE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NOTES;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;

import java.util.List;
import java.util.Optional;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.lesson.ClassDetails;
import seedu.address.model.lesson.ClassId;
import seedu.address.model.lesson.Cost;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonSchedule;
import seedu.address.model.lesson.Subject;
import seedu.address.model.lesson.TuitionClass;

/** Edits a centrally stored class without changing its stable ID. */
public class ClassEditCommand extends ClassCommand {

    public static final String SUBCOMMAND_WORD = "edit";
    public static final String MESSAGE_USAGE = COMMAND_WORD + " " + SUBCOMMAND_WORD
            + ": Edits a class identified by its stable ID.\nParameters: " + PREFIX_CLASS_ID + "CLASS_ID "
            + "[" + PREFIX_SUBJECT + "SUBJECT] [" + PREFIX_COST + "COST] ["
            + PREFIX_CLASS + "SCHEDULE] [" + PREFIX_MODE + "online|offline] [" + PREFIX_LINK + "URL] ["
            + PREFIX_LOCATION + "LOCATION] [" + PREFIX_NOTES + "NOTES]\n"
            + "Example: class edit id/C001 mode/offline location/Room 2 notes/Bring worksheet";
    public static final String MESSAGE_SUCCESS = "Edited class %1$s: %2$s, %3$s, cost %4$s.";
    public static final String MESSAGE_NOT_FOUND = "No class exists with ID %s.";
    public static final String MESSAGE_DUPLICATE = "Another class already has the same subject and timing.";
    public static final String MESSAGE_NO_CHANGE = "The edited class has the same values as the existing class.";

    private final ClassId classId;
    private final Optional<Subject> subject;
    private final Optional<Cost> cost;
    private final Optional<LessonSchedule> schedule;
    private final ClassDetailsUpdate detailsUpdate;

    /** Creates a command containing the optional class fields to replace. */
    public ClassEditCommand(ClassId classId, Optional<Subject> subject, Optional<Cost> cost,
            Optional<LessonSchedule> schedule) {
        this(classId, subject, cost, schedule, ClassDetailsUpdate.NONE);
    }

    /** Creates a command containing optional class fields and shared details to replace. */
    public ClassEditCommand(ClassId classId, Optional<Subject> subject, Optional<Cost> cost,
            Optional<LessonSchedule> schedule, ClassDetailsUpdate detailsUpdate) {
        this.classId = requireNonNull(classId);
        this.subject = requireNonNull(subject);
        this.cost = requireNonNull(cost);
        this.schedule = requireNonNull(schedule);
        this.detailsUpdate = requireNonNull(detailsUpdate);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        if (!model.hasTuitionClass(classId)) {
            throw new CommandException(String.format(MESSAGE_NOT_FOUND, classId));
        }
        TuitionClass original = model.getTuitionClass(classId);
        ClassDetails details;
        try {
            details = detailsUpdate.applyTo(original.getDetails());
        } catch (IllegalArgumentException e) {
            throw new CommandException(e.getMessage());
        }
        TuitionClass edited = new TuitionClass(classId, subject.orElse(original.getSubject()),
                cost.orElse(original.getCost()), schedule.orElse(original.getSchedule()), details);
        if (edited.equals(original) && edited.getSubject().value.equals(original.getSubject().value)) {
            throw new CommandException(MESSAGE_NO_CHANGE);
        }
        boolean duplicate = model.getTuitionClassList().stream()
                .anyMatch(item -> !item.getClassId().equals(classId) && item.hasSameDetails(edited));
        if (duplicate) {
            throw new CommandException(MESSAGE_DUPLICATE);
        }
        Lesson candidate = new Lesson(classId, edited.getSubject(), edited.getCost(), edited.getSchedule(),
                seedu.address.model.lesson.PaymentStatus.UNPAID);
        List<Lesson> overlaps = model.getTuitionClassList().stream()
                .filter(item -> !item.getClassId().equals(classId))
                .filter(item -> item.getSchedule().overlapsWith(edited.getSchedule()))
                .map(item -> new Lesson(item.getClassId(), item.getSubject(), item.getCost(), item.getSchedule(),
                        seedu.address.model.lesson.PaymentStatus.UNPAID))
                .toList();
        if (!overlaps.isEmpty()) {
            throw new CommandException(Messages.getClassEditOverlapMessage(candidate, overlaps));
        }
        model.setTuitionClass(original, edited);
        return new CommandResult(String.format(MESSAGE_SUCCESS, classId, edited.getSubject(),
                edited.getSchedule(), edited.getCost()));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof ClassEditCommand otherCommand
                && classId.equals(otherCommand.classId) && subject.equals(otherCommand.subject)
                && cost.equals(otherCommand.cost) && schedule.equals(otherCommand.schedule)
                && detailsUpdate.equals(otherCommand.detailsUpdate);
    }
}
