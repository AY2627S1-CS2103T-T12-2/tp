package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.AddClassCommand;
import seedu.address.model.lesson.Cost;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonTiming;
import seedu.address.model.lesson.Subject;

public class AddClassCommandParserTest {

    private final AddClassCommandParser parser = new AddClassCommandParser();

    @Test
    public void parse_allFieldsPresent_success() {
        Lesson lesson = new Lesson(new Subject("Math"), new Cost("30"),
                new LessonTiming("Monday1800-1930"));
        assertParseSuccess(parser, " 1 sub/Math cost/30 c/Monday1800-1930",
                new AddClassCommand(Index.fromOneBased(1), lesson));
    }

    @Test
    public void parse_missingField_failure() {
        String expected = String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddClassCommand.MESSAGE_USAGE);
        assertParseFailure(parser, " 1 sub/Math c/Monday1800-1930", expected);
    }
}
