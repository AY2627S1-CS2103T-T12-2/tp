package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.TagCommand;
import seedu.address.model.tag.Tag;

public class TagCommandParserTest {

    private final TagCommandParser parser = new TagCommandParser();

    @Test
    public void parse_validArgs_returnsTagCommand() {
        Set<Tag> tags = new LinkedHashSet<>();
        tags.add(new Tag("exam-prep"));
        tags.add(new Tag("needs-follow-up"));

        assertParseSuccess(parser, "1 exam-prep needs-follow-up", new TagCommand(INDEX_FIRST_PERSON, tags));
    }

    @Test
    public void parse_missingTag_throwsParseException() {
        assertParseFailure(parser, "1", String.format(MESSAGE_INVALID_COMMAND_FORMAT, TagCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_invalidIndex_throwsParseException() {
        assertParseFailure(parser, "a exam-prep",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, TagCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_invalidTag_throwsParseException() {
        assertParseFailure(parser, "1 ExamPrep", Tag.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_duplicateTag_throwsParseException() {
        assertParseFailure(parser, "1 exam-prep exam-prep", Tag.MESSAGE_DUPLICATE_TAGS);
    }
}
