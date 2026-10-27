package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.FindCommand;
import seedu.address.model.person.LessonContainsKeywordsPredicate;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.TagContainsKeywordsPredicate;

public class FindCommandParserTest {

    private FindCommandParser parser = new FindCommandParser();

    @Test
    public void parse_emptyArg_throwsParseException() {
        assertParseFailure(parser, "     ", FindCommand.MESSAGE_EMPTY_QUERY);
    }

    @Test
    public void parse_validArgs_returnsFindCommand() {
        // no leading and trailing whitespaces
        FindCommand expectedFindCommand =
                new FindCommand(new NameContainsKeywordsPredicate(List.of("Alice", "Bob")));
        assertParseSuccess(parser, "Alice Bob", expectedFindCommand);

        // multiple whitespaces between keywords
        assertParseSuccess(parser, " \n Alice \n \t Bob  \t", expectedFindCommand);
    }

    @Test
    public void parse_tag_success() {
        assertParseSuccess(parser, " t/priority",
                new FindCommand(new TagContainsKeywordsPredicate(List.of("priority"))));
        assertParseSuccess(parser, " t/PRI Exam-Prep",
                new FindCommand(new TagContainsKeywordsPredicate(List.of("pri", "exam-prep"))));
        assertParseSuccess(parser, " t/exam- -PREP",
                new FindCommand(new TagContainsKeywordsPredicate(List.of("exam-", "-prep"))));
    }

    @Test
    public void parse_lesson_success() {
        assertParseSuccess(parser, " c/Monday1800",
                new FindCommand(new LessonContainsKeywordsPredicate(List.of("Monday1800"))));
        assertParseSuccess(parser, " c/Monday",
                new FindCommand(new LessonContainsKeywordsPredicate(List.of("Monday"))));
        assertParseSuccess(parser, " c/1800",
                new FindCommand(new LessonContainsKeywordsPredicate(List.of("1800"))));
        assertParseSuccess(parser, " c/2359",
                new FindCommand(new LessonContainsKeywordsPredicate(List.of("2359"))));
        assertParseSuccess(parser, " c/Mon 18",
                new FindCommand(new LessonContainsKeywordsPredicate(List.of("Mon", "18"))));
        assertParseSuccess(parser, " c/Mon 2359",
                new FindCommand(new LessonContainsKeywordsPredicate(List.of("Mon", "2359"))));
        assertParseSuccess(parser, " c/2026-10-20",
                new FindCommand(new LessonContainsKeywordsPredicate(List.of("2026-10-20"))));
        assertParseSuccess(parser, " c/2026-10-20 1800",
                new FindCommand(new LessonContainsKeywordsPredicate(List.of("2026-10-20", "1800"))));
    }

    @Test
    public void parse_mixedModes_failure() {
        assertParseFailure(parser, " t/priority c/Monday1800", FindCommand.MESSAGE_MULTIPLE_SEARCH_MODES);
    }

    @Test
    public void parse_prefixedSearchWithNamePreamble_failure() {
        assertParseFailure(parser, "Alice t/priority", FindCommand.MESSAGE_PREFIX_WITH_NAME);
    }

    @Test
    public void parse_duplicateTagPrefix_failure() {
        assertParseFailure(parser, "t/priority t/follow-up", FindCommand.MESSAGE_DUPLICATE_TAG_PREFIX);
    }

    @Test
    public void parse_duplicateClassPrefix_failure() {
        assertParseFailure(parser, "c/Monday c/1800", FindCommand.MESSAGE_DUPLICATE_CLASS_PREFIX);
    }

    @Test
    public void parse_emptyTagKeyword_failure() {
        assertParseFailure(parser, "t/  ", FindCommand.MESSAGE_EMPTY_TAG);
    }

    @Test
    public void parse_invalidTagKeyword_failure() {
        assertParseFailure(parser, "t/Bad*",
                "Invalid tag keyword 'Bad*'. " + seedu.address.model.tag.Tag.MESSAGE_SEARCH_KEYWORD_CONSTRAINTS);
        assertParseFailure(parser, "t/--prep",
                "Invalid tag keyword '--prep'. " + seedu.address.model.tag.Tag.MESSAGE_SEARCH_KEYWORD_CONSTRAINTS);
    }

    @Test
    public void parse_emptyLessonKeyword_failure() {
        assertParseFailure(parser, "c/  ", FindCommand.MESSAGE_EMPTY_LESSON);
    }

    @Test
    public void parse_invalidLessonKeyword_failure() {
        assertParseFailure(parser, "c/Funday",
                "Invalid lesson day keyword 'Funday'. " + FindCommand.MESSAGE_INVALID_LESSON);
        assertParseFailure(parser, "c/Monday Tuesday",
                "Lesson search can include only one day keyword. " + FindCommand.MESSAGE_INVALID_LESSON);
        assertParseFailure(parser, "c/1800 1900",
                "Lesson search can include only one time keyword. " + FindCommand.MESSAGE_INVALID_LESSON);
        assertParseFailure(parser, "c/Mon18pm",
                "Invalid lesson time keyword '18pm'. " + FindCommand.MESSAGE_INVALID_LESSON);
        assertParseFailure(parser, "c/Monday 24000",
                "Invalid lesson time keyword '24000'. " + FindCommand.MESSAGE_INVALID_LESSON);
        assertParseFailure(parser, "c/Monday 2400",
                "Invalid lesson time keyword '2400'. " + FindCommand.MESSAGE_INVALID_LESSON);
        assertParseFailure(parser, "c/2026-02-29",
                "Invalid lesson date keyword '2026-02-29'. " + FindCommand.MESSAGE_INVALID_LESSON);
        assertParseFailure(parser, "c/2026-10-20 2026-10-21",
                "Lesson search can include only one date keyword. " + FindCommand.MESSAGE_INVALID_LESSON);
        assertParseFailure(parser, "c/Mon 2026-10-20",
                "Lesson search cannot combine day and date keywords. " + FindCommand.MESSAGE_INVALID_LESSON);
    }

}
