package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
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
    public void parse_batchArgs_returnsTagCommand() {
        assertParseSuccess(parser, "1 2 3 t/math-2026",
                new TagCommand(List.of(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON, Index.fromOneBased(3)),
                        new Tag("math-2026")));
    }

    @Test
    public void parse_batchNumericTag_returnsTagCommand() {
        assertParseSuccess(parser, "1 2 t/2026",
                new TagCommand(List.of(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON), new Tag("2026")));
    }

    @Test
    public void parse_legacyNumericTag_returnsTagCommand() {
        assertParseSuccess(parser, "1 2026",
                new TagCommand(INDEX_FIRST_PERSON, Set.of(new Tag("2026"))));
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

    @Test
    public void parse_batchDuplicateIndex_throwsParseException() {
        assertParseFailure(parser, "1 2 1 t/exam-prep", TagCommand.MESSAGE_DUPLICATE_INDEXES);
    }

    @Test
    public void parse_batchInvalidIndex_throwsParseException() {
        assertParseFailure(parser, "1 student t/exam-prep", ParserUtil.MESSAGE_INVALID_INDEX);
    }

    @Test
    public void parse_batchMissingIndexes_throwsParseException() {
        assertParseFailure(parser, " t/exam-prep",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, TagCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_batchMissingTag_throwsParseException() {
        assertParseFailure(parser, "1 2 t/", Tag.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_batchMultipleTags_throwsParseException() {
        assertParseFailure(parser, "1 2 t/exam-prep urgent", Tag.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_batchDuplicateTagPrefix_throwsParseException() {
        assertParseFailure(parser, "1 2 t/exam-prep t/urgent",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_TAG));
    }
}
