package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CLASS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_COST;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;

import java.util.List;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.lesson.ClassId;
import seedu.address.model.lesson.Cost;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonSchedule;
import seedu.address.model.lesson.Subject;
import seedu.address.model.lesson.TuitionClass;

/** Adds an independently stored tuition class. */
public class ClassAddCommand extends ClassCommand {

    public static final String SUBCOMMAND_WORD = "add";
    public static final String MESSAGE_USAGE = COMMAND_WORD + " " + SUBCOMMAND_WORD
            + ": Adds a class independently of any student.\n"
            + "Parameters: " + PREFIX_SUBJECT + "SUBJECT " + PREFIX_COST + "COST "
            + PREFIX_CLASS + "SCHEDULE\n"
            + "Example: class add sub/Math cost/30 c/Monday1800-1930";
    public static final String MESSAGE_SUCCESS = "Added class %1$s: %2$s, %3$s, cost %4$s.";
    public static final String MESSAGE_DUPLICATE = "A class with the same subject and timing already exists.";

    private final Subject subject;
    private final Cost cost;
    private final LessonSchedule schedule;

    /** Creates a command for the supplied class details. */
    public ClassAddCommand(Subject subject, Cost cost, LessonSchedule schedule) {
        this.subject = requireNonNull(subject);
        this.cost = requireNonNull(cost);
        this.schedule = requireNonNull(schedule);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Lesson candidate = new Lesson(subject, cost, schedule);
        TuitionClass comparison = new TuitionClass(new ClassId("C1"), subject, cost, schedule);
        if (model.hasTuitionClassWithDetails(comparison)) {
            throw new CommandException(MESSAGE_DUPLICATE);
        }
        List<Lesson> overlaps = model.findOverlappingClasses(candidate);
        if (!overlaps.isEmpty()) {
            throw new CommandException(Messages.getClassOverlapMessage(candidate, overlaps));
        }
        TuitionClass tuitionClass = new TuitionClass(model.allocateClassId(), subject, cost, schedule);
        model.addTuitionClass(tuitionClass);
        return new CommandResult(String.format(MESSAGE_SUCCESS, tuitionClass.getClassId(), subject, schedule, cost));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof ClassAddCommand otherCommand
                && subject.equals(otherCommand.subject) && cost.equals(otherCommand.cost)
                && schedule.equals(otherCommand.schedule);
    }
}
