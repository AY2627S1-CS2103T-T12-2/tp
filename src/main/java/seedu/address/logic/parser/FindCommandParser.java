package seedu.address.logic.parser;

import static seedu.address.logic.parser.CliSyntax.PREFIX_CLASS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.LessonContainsKeywordsPredicate;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.TagContainsKeywordsPredicate;

/**
 * Parses input arguments and creates a new FindCommand object
 */
public class FindCommandParser implements Parser<FindCommand> {
    private static final Set<String> VALID_DAY_NAMES = Set.of("monday", "tuesday", "wednesday", "thursday",
            "friday", "saturday", "sunday");
    private static final int LAST_HOUR = 24;
    private static final int LAST_MINUTE = 59;

    /**
     * Parses the given {@code String} of arguments in the context of the FindCommand
     * and returns a FindCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public FindCommand parse(String args) throws ParseException {
        String trimmedArgs = args.trim();
        if (trimmedArgs.isEmpty()) {
            throw new ParseException(FindCommand.MESSAGE_EMPTY_QUERY);
        }

        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(" " + trimmedArgs, PREFIX_TAG, PREFIX_CLASS);
        boolean hasTag = arguments.getValue(PREFIX_TAG).isPresent();
        boolean hasClass = arguments.getValue(PREFIX_CLASS).isPresent();

        if (hasTag || hasClass) {
            return parsePrefixedFindCommand(arguments, hasTag, hasClass);
        }

        String[] nameKeywords = trimmedArgs.split("\\s+");

        return new FindCommand(new NameContainsKeywordsPredicate(List.of(nameKeywords)));
    }

    private FindCommand parsePrefixedFindCommand(ArgumentMultimap arguments, boolean hasTag, boolean hasClass)
            throws ParseException {
        if (!arguments.getPreamble().isEmpty()) {
            throw new ParseException(FindCommand.MESSAGE_PREFIX_WITH_NAME);
        }

        if (hasTag && hasClass) {
            throw new ParseException(FindCommand.MESSAGE_MULTIPLE_SEARCH_MODES);
        }

        if (arguments.getAllValues(PREFIX_TAG).size() > 1) {
            throw new ParseException(FindCommand.MESSAGE_DUPLICATE_TAG_PREFIX);
        }

        if (arguments.getAllValues(PREFIX_CLASS).size() > 1) {
            throw new ParseException(FindCommand.MESSAGE_DUPLICATE_CLASS_PREFIX);
        }

        if (hasTag) {
            return parseTagFindCommand(arguments.getValue(PREFIX_TAG).get());
        }

        return parseLessonFindCommand(arguments.getValue(PREFIX_CLASS).get());
    }

    private FindCommand parseTagFindCommand(String tagArguments) throws ParseException {
        String trimmedTagArguments = tagArguments.trim();
        if (trimmedTagArguments.isEmpty()) {
            throw new ParseException(FindCommand.MESSAGE_EMPTY_TAG);
        }

        List<String> tagKeywords = List.of(trimmedTagArguments.split("\\s+"));
        for (String keyword : tagKeywords) {
            try {
                ParserUtil.parseTag(keyword);
            } catch (ParseException parseException) {
                throw new ParseException("Invalid tag keyword '" + keyword + "'. "
                        + parseException.getMessage());
            }
        }
        return new FindCommand(new TagContainsKeywordsPredicate(tagKeywords));
    }

    private FindCommand parseLessonFindCommand(String lessonArguments) throws ParseException {
        String trimmedLessonArguments = lessonArguments.trim();
        if (trimmedLessonArguments.isEmpty()) {
            throw new ParseException(FindCommand.MESSAGE_EMPTY_LESSON);
        }

        LessonSearchParts lessonSearchParts = parseLessonSearchParts(trimmedLessonArguments);
        return new FindCommand(new LessonContainsKeywordsPredicate(
                lessonSearchParts.dayKeyword, lessonSearchParts.timeKeyword));
    }

    private LessonSearchParts parseLessonSearchParts(String lessonArguments) throws ParseException {
        String[] tokens = lessonArguments.split("\\s+");
        Optional<String> dayKeyword = Optional.empty();
        Optional<String> timeKeyword = Optional.empty();
        Set<String> seenComponents = new HashSet<>();

        for (String token : tokens) {
            LessonSearchParts tokenParts = parseLessonToken(token);
            if (tokenParts.dayKeyword.isPresent()) {
                if (!seenComponents.add("day")) {
                    throw new ParseException("Lesson search can include only one day keyword. "
                            + FindCommand.MESSAGE_INVALID_LESSON);
                }
                dayKeyword = tokenParts.dayKeyword;
            }
            if (tokenParts.timeKeyword.isPresent()) {
                if (!seenComponents.add("time")) {
                    throw new ParseException("Lesson search can include only one time keyword. "
                            + FindCommand.MESSAGE_INVALID_LESSON);
                }
                timeKeyword = tokenParts.timeKeyword;
            }
        }

        if (dayKeyword.isEmpty() && timeKeyword.isEmpty()) {
            throw new ParseException(FindCommand.MESSAGE_INVALID_LESSON);
        }

        return new LessonSearchParts(dayKeyword, timeKeyword);
    }

    private LessonSearchParts parseLessonToken(String token) throws ParseException {
        int splitIndex = firstDigitIndex(token);
        String dayPart = splitIndex == -1 ? token : token.substring(0, splitIndex);
        String timePart = splitIndex == -1 ? "" : token.substring(splitIndex);

        Optional<String> dayKeyword = Optional.empty();
        Optional<String> timeKeyword = Optional.empty();

        if (!dayPart.isEmpty()) {
            String normalizedDayPart = dayPart.toLowerCase(Locale.ROOT);
            if (!isValidDayPrefix(normalizedDayPart)) {
                throw new ParseException("Invalid lesson day keyword '" + dayPart + "'. "
                        + FindCommand.MESSAGE_INVALID_LESSON);
            }
            dayKeyword = Optional.of(normalizedDayPart);
        }

        if (!timePart.isEmpty()) {
            if (!isValidTimeKeyword(timePart)) {
                throw new ParseException("Invalid lesson time keyword '" + timePart + "'. "
                        + FindCommand.MESSAGE_INVALID_LESSON);
            }
            timeKeyword = Optional.of(timePart);
        }

        return new LessonSearchParts(dayKeyword, timeKeyword);
    }

    private boolean isValidDayPrefix(String dayKeyword) {
        return VALID_DAY_NAMES.stream().anyMatch(day -> day.startsWith(dayKeyword));
    }

    private boolean isValidTimeKeyword(String timeKeyword) {
        if (!timeKeyword.matches("\\d{1,4}")) {
            return false;
        }

        for (int hour = 0; hour <= LAST_HOUR; hour++) {
            int lastMinuteForHour = hour == LAST_HOUR ? 0 : LAST_MINUTE;
            for (int minute = 0; minute <= lastMinuteForHour; minute++) {
                String validTime = String.format("%02d%02d", hour, minute);
                if (validTime.contains(timeKeyword)) {
                    return true;
                }
            }
        }
        return false;
    }

    private int firstDigitIndex(String token) {
        for (int i = 0; i < token.length(); i++) {
            if (Character.isDigit(token.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

    private static class LessonSearchParts {
        private final Optional<String> dayKeyword;
        private final Optional<String> timeKeyword;

        LessonSearchParts(Optional<String> dayKeyword, Optional<String> timeKeyword) {
            this.dayKeyword = dayKeyword;
            this.timeKeyword = timeKeyword;
        }
    }
}
