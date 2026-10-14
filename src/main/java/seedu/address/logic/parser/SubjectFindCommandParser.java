package seedu.address.logic.parser;

import static seedu.address.logic.parser.CliSyntax.PREFIX_CLASS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.List;

import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.SubjectContainsKeywordsPredicate;

/** Parses subject searches and delegates the other find modes to their existing parser. */
public class SubjectFindCommandParser implements Parser<FindCommand> {

    public static final String MESSAGE_USAGE = "Find students by subject: find sub/KEYWORD [MORE_KEYWORDS]...\n"
            + "Example: find sub/Math Physics";
    public static final String MESSAGE_EMPTY_SUBJECT = "Enter at least one subject keyword after sub/.\n"
            + MESSAGE_USAGE;
    public static final String MESSAGE_MIXED_MODES = "Find can search only one category at a time. "
            + "Put sub/ immediately after find and use subject keywords only.\n" + MESSAGE_USAGE;

    @Override
    public FindCommand parse(String args) throws ParseException {
        String normalizedArgs = " " + args.trim().replaceAll("\\s+", " ");
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(normalizedArgs,
                PREFIX_SUBJECT, PREFIX_TAG, PREFIX_CLASS);
        if (arguments.getValue(PREFIX_SUBJECT).isEmpty()) {
            return new FindCommandParser().parse(args);
        }

        if (!arguments.getPreamble().isEmpty() || arguments.getValue(PREFIX_TAG).isPresent()
                || arguments.getValue(PREFIX_CLASS).isPresent()) {
            throw new ParseException(MESSAGE_MIXED_MODES);
        }
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_SUBJECT);
        String subjectQuery = arguments.getValue(PREFIX_SUBJECT).get();
        if (subjectQuery.isEmpty()) {
            throw new ParseException(MESSAGE_EMPTY_SUBJECT);
        }
        List<String> keywords = List.of(subjectQuery.split("\\s+"));
        for (String keyword : keywords) {
            ParserUtil.parseLessonSubject(keyword);
        }
        return new FindCommand(new SubjectContainsKeywordsPredicate(keywords));
    }
}
