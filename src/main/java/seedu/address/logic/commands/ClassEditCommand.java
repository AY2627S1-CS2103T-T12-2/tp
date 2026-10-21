package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CLASS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CLASS_ID;
import static seedu.address.logic.parser.CliSyntax.PREFIX_COST;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;

import java.util.List;
import java.util.Optional;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.lesson.ClassId;
import seedu.address.model.lesson.Cost;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonTiming;
import seedu.address.model.lesson.Subject;
import seedu.address.model.lesson.TuitionClass;

/** Edits a centrally stored class without changing its stable ID. */
public class ClassEditCommand extends ClassCommand {

    public static final String SUBCOMMAND_WORD = "edit";
    public static final String MESSAGE_USAGE = COMMAND_WORD + " " + SUBCOMMAND_WORD
            + ": Edits a class identified by its stable ID.\nParameters: " + PREFIX_CLASS_ID + "CLASS_ID "
            + "[" + PREFIX_SUBJECT + "SUBJECT] [" + PREFIX_COST + "COST] ["
            + PREFIX_CLASS + "DAYHHmm-HHmm]\n"
            + "Example: class edit id/C001 cost/35 c/Tuesday1800-1930";
    public static final String MESSAGE_SUCCESS = "Edited class %1$s: %2$s, %3$s, cost %4$s.";
    public static final String MESSAGE_NOT_FOUND = "No class exists with ID %s.";
    public static final String MESSAGE_DUPLICATE = "Another class already has the same subject and timing.";
    public static final String MESSAGE_NO_CHANGE = "The edited class has the same values as the existing class.";

    private final ClassId classId;
    private final Optional<Subject> subject;
    private final Optional<Cost> cost;
    private final Optional<LessonTiming> timing;

    /** Creates a command containing the optional class fields to replace. */
    public ClassEditCommand(ClassId classId, Optional<Subject> subject, Optional<Cost> cost,
            Optional<LessonTiming> timing) {
        this.classId = requireNonNull(classId);
        this.subject = requireNonNull(subject);
        this.cost = requireNonNull(cost);
        this.timing = requireNonNull(timing);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        if (!model.hasTuitionClass(classId)) {
            throw new CommandException(String.format(MESSAGE_NOT_FOUND, classId));
        }
        TuitionClass original = model.getTuitionClass(classId);
        TuitionClass edited = new TuitionClass(classId, subject.orElse(original.getSubject()),
                cost.orElse(original.getCost()), timing.orElse(original.getTiming()));
        if (edited.equals(original)) {
            throw new CommandException(MESSAGE_NO_CHANGE);
        }
        boolean duplicate = model.getTuitionClassList().stream()
                .anyMatch(item -> !item.getClassId().equals(classId) && item.hasSameDetails(edited));
        if (duplicate) {
            throw new CommandException(MESSAGE_DUPLICATE);
        }
        Lesson candidate = new Lesson(classId, edited.getSubject(), edited.getCost(), edited.getTiming(),
                seedu.address.model.lesson.PaymentStatus.UNPAID);
        List<Lesson> overlaps = model.getTuitionClassList().stream()
                .filter(item -> !item.getClassId().equals(classId))
                .filter(item -> item.getTiming().overlapsWith(edited.getTiming()))
                .map(item -> new Lesson(item.getClassId(), item.getSubject(), item.getCost(), item.getTiming(),
                        seedu.address.model.lesson.PaymentStatus.UNPAID))
                .toList();
        if (!overlaps.isEmpty()) {
            throw new CommandException(Messages.getClassOverlapMessage(candidate, overlaps));
        }
        model.setTuitionClass(original, edited);
        return new CommandResult(String.format(MESSAGE_SUCCESS, classId, edited.getSubject(),
                edited.getTiming(), edited.getCost()));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof ClassEditCommand otherCommand
                && classId.equals(otherCommand.classId) && subject.equals(otherCommand.subject)
                && cost.equals(otherCommand.cost) && timing.equals(otherCommand.timing);
    }
}
