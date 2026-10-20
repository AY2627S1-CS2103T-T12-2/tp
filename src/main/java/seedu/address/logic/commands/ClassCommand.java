package seedu.address.logic.commands;

/** Base type for commands that manage classes and enrolments. */
public abstract class ClassCommand extends Command {

    public static final String COMMAND_WORD = "class";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Manages classes and enrolments.\n"
            + "Subcommands:\n"
            + "  class enrol STUDENT_INDEX sub/SUBJECT cost/COST c/DAYHHmm-HHmm\n"
            + "  class unenrol STUDENT_INDEX sub/SUBJECT c/DAYHHmm-HHmm\n"
            + "  class delete sub/SUBJECT c/DAYHHmm-HHmm\n"
            + "  class payment STUDENT_INDEX sub/SUBJECT c/DAYHHmm-HHmm status/paid|unpaid";
}
