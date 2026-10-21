package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CLASS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CLASS_ID;
import static seedu.address.logic.parser.CliSyntax.PREFIX_COST;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STATUS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.ClassAddCommand;
import seedu.address.logic.commands.ClassCommand;
import seedu.address.logic.commands.ClassDeleteCommand;
import seedu.address.logic.commands.ClassEditCommand;
import seedu.address.logic.commands.ClassEnrolCommand;
import seedu.address.logic.commands.ClassListCommand;
import seedu.address.logic.commands.ClassPaymentCommand;
import seedu.address.logic.commands.ClassUnenrolCommand;
import seedu.address.model.lesson.ClassId;
import seedu.address.model.lesson.Cost;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonTiming;
import seedu.address.model.lesson.OneOffLessonSchedule;
import seedu.address.model.lesson.PaymentStatus;
import seedu.address.model.lesson.Subject;

public class ClassCommandParserTest {

    private static final Subject SUBJECT = new Subject("Math");
    private static final LessonTiming TIMING = new LessonTiming("Monday1800-1930");
    private static final Lesson LESSON = new Lesson(SUBJECT, new Cost("30"), TIMING);

    private final ClassCommandParser parser = new ClassCommandParser();

    @Test
    public void parse_newClassCommands_success() {
        assertParseSuccess(parser, " add sub/Math cost/30 c/Monday1800-1930",
                new ClassAddCommand(SUBJECT, new Cost("30"), TIMING));
        assertParseSuccess(parser, " edit id/c001 cost/35",
                new ClassEditCommand(new ClassId("C001"), Optional.empty(),
                        Optional.of(new Cost("35")), Optional.empty()));
        assertParseSuccess(parser, " list", new ClassListCommand());
        assertParseSuccess(parser, " enrol 1 id/C001",
                new ClassEnrolCommand(Index.fromOneBased(1), new ClassId("C001")));
        assertParseSuccess(parser, " unenrol 1 id/C001",
                new ClassUnenrolCommand(Index.fromOneBased(1), new ClassId("C001")));
        assertParseSuccess(parser, " delete id/C001",
                new ClassDeleteCommand(new ClassId("C001")));
        assertParseSuccess(parser, " payment 1 id/C001 status/paid",
                new ClassPaymentCommand(Index.fromOneBased(1), new ClassId("C001"), PaymentStatus.PAID));
    }

    @Test
    public void parse_addMissingExtraAndDuplicateFields_failure() {
        String expected = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ClassAddCommand.MESSAGE_USAGE);
        assertParseFailure(parser, " add cost/30 c/Monday1800-1930", expected);
        assertParseFailure(parser, " add sub/Math c/Monday1800-1930", expected);
        assertParseFailure(parser, " add sub/Math cost/30", expected);
        assertParseFailure(parser, " add unexpected sub/Math cost/30 c/Monday1800-1930", expected);
        assertParseFailure(parser, " add sub/Math cost/30 cost/31 c/Monday1800-1930",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_COST));
    }

    @Test
    public void parse_editEachOptionalFieldAndAllFields_success() {
        assertParseSuccess(parser, " edit id/C001 sub/Physics",
                new ClassEditCommand(new ClassId("C001"), Optional.of(new Subject("Physics")),
                        Optional.empty(), Optional.empty()));
        assertParseSuccess(parser, " edit id/C001 c/Tuesday1800-1930",
                new ClassEditCommand(new ClassId("C001"), Optional.empty(), Optional.empty(),
                        Optional.of(new LessonTiming("Tuesday1800-1930"))));
        assertParseSuccess(parser, " edit id/C001 sub/Physics cost/40 c/Tuesday1800-1930",
                new ClassEditCommand(new ClassId("C001"), Optional.of(new Subject("Physics")),
                        Optional.of(new Cost("40")), Optional.of(new LessonTiming("Tuesday1800-1930"))));
    }

    @Test
    public void parse_editMissingInvalidAndDuplicateFields_failure() {
        String expected = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ClassEditCommand.MESSAGE_USAGE);
        assertParseFailure(parser, " edit id/C001", expected);
        assertParseFailure(parser, " edit cost/40", expected);
        assertParseFailure(parser, " edit unexpected id/C001 cost/40", expected);
        assertParseFailure(parser, " edit id/C001 id/C002 cost/40",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_CLASS_ID));
        assertParseFailure(parser, " edit id/C001 sub/Math!", Subject.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " edit id/C001 cost/-1", Cost.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " edit id/C001 c/Monday2000-1900", LessonTiming.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_enrolAllFieldsPresent_success() {
        assertParseSuccess(parser, " enrol 1 sub/Math cost/30 c/Monday1800-1930",
                new ClassEnrolCommand(Index.fromOneBased(1), LESSON));
    }

    @Test
    public void parse_oneOffSchedules_success() {
        OneOffLessonSchedule schedule = new OneOffLessonSchedule("2026-10-20T1800-1930");
        Lesson lesson = new Lesson(SUBJECT, new Cost("30"), schedule);

        assertParseSuccess(parser, " enrol 1 sub/Math cost/30 c/2026-10-20T1800-1930",
                new ClassEnrolCommand(Index.fromOneBased(1), lesson));
        assertParseSuccess(parser, " unenrol 1 sub/Math c/2026-10-20T1800-1930",
                new ClassUnenrolCommand(Index.fromOneBased(1), SUBJECT, schedule));
        assertParseSuccess(parser, " delete sub/Math c/2026-10-20T1800-1930",
                new ClassDeleteCommand(SUBJECT, schedule));
        assertParseSuccess(parser, " payment 1 sub/Math c/2026-10-20T1800-1930 status/paid",
                new ClassPaymentCommand(Index.fromOneBased(1), SUBJECT, schedule, PaymentStatus.PAID));
    }

    @Test
    public void parse_enrolExtraWhitespace_success() {
        assertParseSuccess(parser, "  enrol   1   sub/Math   cost/30   c/Monday1800-1930  ",
                new ClassEnrolCommand(Index.fromOneBased(1), LESSON));
    }

    @Test
    public void parse_unenrolAllFieldsPresent_success() {
        assertParseSuccess(parser, " unenrol 1 sub/Math c/Monday1800-1930",
                new ClassUnenrolCommand(Index.fromOneBased(1), SUBJECT, TIMING));
    }

    @Test
    public void parse_deleteAllFieldsPresent_success() {
        assertParseSuccess(parser, " delete sub/Math c/Monday1800-1930",
                new ClassDeleteCommand(SUBJECT, TIMING));
    }

    @Test
    public void parse_paymentAllFieldsPresent_success() {
        assertParseSuccess(parser, " payment 1 sub/Math c/Monday1800-1930 status/paid",
                new ClassPaymentCommand(Index.fromOneBased(1), SUBJECT, TIMING, PaymentStatus.PAID));
        assertParseSuccess(parser, " payment 1 sub/Math c/Monday1800-1930 status/UNPAID",
                new ClassPaymentCommand(Index.fromOneBased(1), SUBJECT, TIMING, PaymentStatus.UNPAID));
    }

    @Test
    public void parse_paymentMissingField_failure() {
        String expected = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ClassPaymentCommand.MESSAGE_USAGE);
        assertParseFailure(parser, " payment 1 sub/Math c/Monday1800-1930", expected);
        assertParseFailure(parser, " payment 1 sub/Math status/paid", expected);
        assertParseFailure(parser, " payment 1 c/Monday1800-1930 status/paid", expected);
        assertParseFailure(parser, " payment sub/Math c/Monday1800-1930 status/paid", expected);
    }

    @Test
    public void parse_paymentInvalidStatus_failure() {
        assertParseFailure(parser, " payment 1 sub/Math c/Monday1800-1930 status/pending",
                PaymentStatus.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_paymentDuplicateStatus_failure() {
        assertParseFailure(parser,
                " payment 1 sub/Math c/Monday1800-1930 status/paid status/unpaid",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_STATUS));
    }

    @Test
    public void parse_enrolMissingCost_failure() {
        String expected = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ClassEnrolCommand.MESSAGE_USAGE);
        assertParseFailure(parser, " enrol 1 sub/Math c/Monday1800-1930", expected);
    }

    @Test
    public void parse_enrolMissingSubject_failure() {
        String expected = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ClassEnrolCommand.MESSAGE_USAGE);
        assertParseFailure(parser, " enrol 1 cost/30 c/Monday1800-1930", expected);
    }

    @Test
    public void parse_enrolMissingTiming_failure() {
        String expected = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ClassEnrolCommand.MESSAGE_USAGE);
        assertParseFailure(parser, " enrol 1 sub/Math cost/30", expected);
    }

    @Test
    public void parse_enrolInvalidIndex_failure() {
        assertParseFailure(parser, " enrol 0 sub/Math cost/30 c/Monday1800-1930",
                ParserUtil.MESSAGE_INVALID_INDEX);
    }

    @Test
    public void parse_enrolInvalidSubject_failure() {
        assertParseFailure(parser, " enrol 1 sub/Math! cost/30 c/Monday1800-1930",
                Subject.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_enrolInvalidCost_failure() {
        assertParseFailure(parser, " enrol 1 sub/Math cost/-30 c/Monday1800-1930",
                Cost.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_enrolInvalidTiming_failure() {
        assertParseFailure(parser, " enrol 1 sub/Math cost/30 c/Monday1930-1800",
                LessonTiming.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_enrolDuplicatePrefix_failure() {
        assertParseFailure(parser,
                " enrol 1 sub/Math sub/English cost/30 c/Monday1800-1930",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_SUBJECT));
    }

    @Test
    public void parse_enrolIdMixedWithLegacyPrefixes_failure() {
        String expected = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ClassEnrolCommand.MESSAGE_USAGE);
        assertParseFailure(parser, " enrol 1 id/C001 sub/Math", expected);
        assertParseFailure(parser, " enrol 1 id/C001 cost/30", expected);
        assertParseFailure(parser, " enrol 1 id/C001 c/Monday1800-1930", expected);
        assertParseFailure(parser, " enrol 1 id/C001 sub/Math cost/30", expected);
        assertParseFailure(parser, " enrol 1 id/C001 sub/Math c/Monday1800-1930", expected);
        assertParseFailure(parser, " enrol 1 id/C001 cost/30 c/Monday1800-1930", expected);
        assertParseFailure(parser, " enrol 1 id/C001 sub/Math cost/30 c/Monday1800-1930", expected);
    }

    @Test
    public void parse_unenrolMissingIndex_failure() {
        String expected = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ClassUnenrolCommand.MESSAGE_USAGE);
        assertParseFailure(parser, " unenrol sub/Math c/Monday1800-1930", expected);
    }

    @Test
    public void parse_unenrolMissingSubject_failure() {
        String expected = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ClassUnenrolCommand.MESSAGE_USAGE);
        assertParseFailure(parser, " unenrol 1 c/Monday1800-1930", expected);
    }

    @Test
    public void parse_unenrolMissingTiming_failure() {
        String expected = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ClassUnenrolCommand.MESSAGE_USAGE);
        assertParseFailure(parser, " unenrol 1 sub/Math", expected);
    }

    @Test
    public void parse_unenrolDuplicatePrefixes_failure() {
        assertParseFailure(parser,
                " unenrol 1 sub/Math sub/English c/Monday1800-1930 c/Tuesday1800-1930",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_SUBJECT, PREFIX_CLASS));
    }

    @Test
    public void parse_unenrolIdMixedWithLegacyPrefixes_failure() {
        String expected = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ClassUnenrolCommand.MESSAGE_USAGE);
        assertParseFailure(parser, " unenrol 1 id/C001 sub/Math", expected);
        assertParseFailure(parser, " unenrol 1 id/C001 c/Monday1800-1930", expected);
        assertParseFailure(parser, " unenrol 1 id/C001 sub/Math c/Monday1800-1930", expected);
    }

    @Test
    public void parse_paymentIdMixedWithLegacyPrefixes_failure() {
        String expected = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ClassPaymentCommand.MESSAGE_USAGE);
        assertParseFailure(parser, " payment 1 id/C001 sub/Math status/paid", expected);
        assertParseFailure(parser, " payment 1 id/C001 c/Monday1800-1930 status/paid", expected);
        assertParseFailure(parser,
                " payment 1 id/C001 sub/Math c/Monday1800-1930 status/paid", expected);
    }

    @Test
    public void parse_deleteWithPreamble_failure() {
        String expected = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ClassDeleteCommand.MESSAGE_USAGE);
        assertParseFailure(parser, " delete 1 sub/Math c/Monday1800-1930", expected);
    }

    @Test
    public void parse_deleteMissingSubject_failure() {
        String expected = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ClassDeleteCommand.MESSAGE_USAGE);
        assertParseFailure(parser, " delete c/Monday1800-1930", expected);
    }

    @Test
    public void parse_deleteMissingTiming_failure() {
        String expected = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ClassDeleteCommand.MESSAGE_USAGE);
        assertParseFailure(parser, " delete sub/Math", expected);
    }

    @Test
    public void parse_deleteIdMixedWithLegacyPrefix_failure() {
        String expected = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ClassDeleteCommand.MESSAGE_USAGE);
        assertParseFailure(parser, " delete id/C001 sub/Math", expected);
        assertParseFailure(parser, " delete id/C001 c/Monday1800-1930", expected);
        assertParseFailure(parser, " delete id/C001 sub/Math c/Monday1800-1930", expected);
    }

    @Test
    public void parse_deleteDuplicateTiming_failure() {
        assertParseFailure(parser,
                " delete sub/Math c/Monday1800-1930 c/Tuesday1800-1930",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_CLASS));
    }

    @Test
    public void parse_unknownSubcommand_failure() {
        String expected = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ClassCommand.MESSAGE_USAGE);
        assertParseFailure(parser, " unknown", expected);
    }

    @Test
    public void parse_noSubcommand_failure() {
        String expected = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ClassCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "   ", expected);
    }
}
