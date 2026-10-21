package seedu.address.logic.commands;

/** Base type for commands that manage classes and enrolments. */
public abstract class ClassCommand extends Command {

    public static final String COMMAND_WORD = "class";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Manages classes and enrolments.\n"
            + "Subcommands:\n"
            + "  class add sub/SUBJECT cost/COST c/DAYHHmm-HHmm\n"
            + "  class edit id/CLASS_ID [sub/SUBJECT] [cost/COST] [c/DAYHHmm-HHmm]\n"
            + "  class list\n"
            + "  class enrol STUDENT_INDEX id/CLASS_ID\n"
            + "  class unenrol STUDENT_INDEX id/CLASS_ID\n"
            + "  class delete id/CLASS_ID\n"
            + "  class payment STUDENT_INDEX id/CLASS_ID status/paid|unpaid";
}
