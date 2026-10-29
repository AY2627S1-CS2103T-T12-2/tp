package seedu.address.logic.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Tokenizes arguments string of the form: {@code preamble <prefix>value <prefix>value ...}<br>
 *     e.g. {@code some preamble text t/ 11.00 t/12.00 k/ m/ July}  where prefixes are {@code t/ k/ m/}.<br>
 * 1. An argument's value can be an empty string e.g. the value of {@code k/} in the above example.<br>
 * 2. Leading and trailing whitespaces of an argument value will be discarded.<br>
 * 3. An argument may be repeated and all its values will be accumulated e.g. the value of {@code t/}
 *    in the above example.<br>
 */
public class ArgumentTokenizer {

    public static final String MESSAGE_INVALID_QUOTED_VALUE =
            "Use matching double quotes around the whole address, notes, or location value. "
                    + "Separate any following fields with spaces.";
    public static final String MESSAGE_UNKNOWN_PREFIX = "Unrecognised parameter-like token: %s";
    private static final Pattern PREFIX_LIKE_TOKEN =
            Pattern.compile("(?<!\\S)([A-Za-z][A-Za-z0-9-]*/)");

    /**
     * Tokenizes an arguments string and returns an {@code ArgumentMultimap} object that maps prefixes to their
     * respective argument values. Only the given prefixes will be recognized in the arguments string.
     *
     * @param argsString Arguments string of the form: {@code preamble <prefix>value <prefix>value ...}
     * @param prefixes   Prefixes to tokenize the arguments string with
     * @return           ArgumentMultimap object that maps prefixes to their arguments
     */
    public static ArgumentMultimap tokenize(String argsString, Prefix... prefixes) {
        List<PrefixPosition> positions = findAllPrefixPositions(argsString, prefixes);
        return extractArguments(argsString, positions);
    }

    /**
     * Tokenizes free-text fields without hiding subsequent fields. Quoted free-text values preserve every prefix
     * literally. In unquoted free text, an already supplied prefix from {@code literalRepeats} is treated as text.
     * All other prefixes remain field boundaries, so fields can appear in any order.
     */
    public static ArgumentMultimap tokenizeWithFreeTextFields(String argsString, Set<Prefix> freeTextPrefixes,
            Set<Prefix> literalRepeats, Prefix... prefixes) throws ParseException {
        List<PrefixPosition> positions = findAllPrefixPositions(argsString, prefixes);
        positions.sort((first, second) -> Integer.compare(first.getStartPosition(), second.getStartPosition()));
        List<PrefixPosition> fields = new ArrayList<>();
        List<QuotedRange> quotedRanges = new ArrayList<>();
        Set<Prefix> seen = new HashSet<>();
        boolean inFreeText = false;
        int quotedEnd = -1;
        for (PrefixPosition position : positions) {
            if (position.getStartPosition() <= quotedEnd) {
                continue;
            }
            Prefix prefix = position.getPrefix();
            if (inFreeText && literalRepeats.contains(prefix) && seen.contains(prefix)) {
                continue;
            }
            fields.add(position);
            seen.add(prefix);
            inFreeText = freeTextPrefixes.contains(prefix);
            int valueStart = position.getStartPosition() + prefix.getPrefix().length();
            while (valueStart < argsString.length() && Character.isWhitespace(argsString.charAt(valueStart))) {
                valueStart++;
            }
            if (inFreeText && valueStart < argsString.length() && argsString.charAt(valueStart) == '"') {
                quotedEnd = findClosingQuote(argsString, valueStart);
                quotedRanges.add(new QuotedRange(valueStart, quotedEnd));
                inFreeText = false;
            }
        }
        verifyNoUnknownPrefixes(argsString, quotedRanges, prefixes);
        ArgumentMultimap rawArguments = extractArguments(argsString, fields);
        ArgumentMultimap arguments = new ArgumentMultimap();
        arguments.put(new Prefix(""), rawArguments.getPreamble());
        for (Prefix prefix : Arrays.stream(prefixes).distinct().toList()) {
            for (String value : rawArguments.getAllValues(prefix)) {
                arguments.put(prefix, freeTextPrefixes.contains(prefix) ? unquote(value) : value);
            }
        }
        return arguments;
    }

    private static void verifyNoUnknownPrefixes(String argsString, List<QuotedRange> quotedRanges,
            Prefix... prefixes) throws ParseException {
        Set<String> knownPrefixes = Arrays.stream(prefixes)
                .map(Prefix::getPrefix)
                .collect(Collectors.toSet());
        Matcher matcher = PREFIX_LIKE_TOKEN.matcher(argsString);
        while (matcher.find()) {
            String candidate = matcher.group(1);
            boolean isQuoted = quotedRanges.stream().anyMatch(range -> range.contains(matcher.start(1)));
            if (!isQuoted && !knownPrefixes.contains(candidate)) {
                throw new ParseException(String.format(MESSAGE_UNKNOWN_PREFIX, candidate));
            }
        }
    }

    private static int findClosingQuote(String value, int openingQuote) throws ParseException {
        for (int index = openingQuote + 1; index < value.length(); index++) {
            char current = value.charAt(index);
            if (current == '\\' && index + 1 < value.length()
                    && (value.charAt(index + 1) == '\\' || value.charAt(index + 1) == '"')) {
                index++;
            } else if (current == '"') {
                return index;
            }
        }
        throw new ParseException(MESSAGE_INVALID_QUOTED_VALUE);
    }

    private static String unquote(String value) throws ParseException {
        if (!value.startsWith("\"")) {
            return value;
        }
        int closingQuote = findClosingQuote(value, 0);
        if (closingQuote != value.length() - 1) {
            throw new ParseException(MESSAGE_INVALID_QUOTED_VALUE);
        }
        StringBuilder result = new StringBuilder();
        for (int index = 1; index < closingQuote; index++) {
            char current = value.charAt(index);
            if (current == '\\' && index + 1 < closingQuote
                    && (value.charAt(index + 1) == '\\' || value.charAt(index + 1) == '"')) {
                current = value.charAt(++index);
            }
            result.append(current);
        }
        return result.toString();
    }

    /**
     * Finds all zero-based prefix positions in the given arguments string.
     *
     * @param argsString Arguments string of the form: {@code preamble <prefix>value <prefix>value ...}
     * @param prefixes   Prefixes to find in the arguments string
     * @return           List of zero-based prefix positions in the given arguments string
     */
    private static List<PrefixPosition> findAllPrefixPositions(String argsString, Prefix... prefixes) {
        return Arrays.stream(prefixes)
                .flatMap(prefix -> findPrefixPositions(argsString, prefix).stream())
                .collect(Collectors.toList());
    }

    /**
     * @see #findAllPrefixPositions(String, Prefix...)
     */
    private static List<PrefixPosition> findPrefixPositions(String argsString, Prefix prefix) {
        List<PrefixPosition> positions = new ArrayList<>();

        int prefixPosition = findPrefixPosition(argsString, prefix.getPrefix(), 0);
        while (prefixPosition != -1) {
            PrefixPosition extendedPrefix = new PrefixPosition(prefix, prefixPosition);
            positions.add(extendedPrefix);
            prefixPosition = findPrefixPosition(argsString, prefix.getPrefix(), prefixPosition);
        }

        return positions;
    }

    /**
     * Returns the index of the first occurrence of {@code prefix} in
     * {@code argsString} starting from index {@code fromIndex}. An occurrence
     * is valid if there is a whitespace before {@code prefix}. Returns -1 if no
     * such occurrence can be found.
     *
     * E.g if {@code argsString} = "e/hip/900", {@code prefix} = "p/" and
     * {@code fromIndex} = 0, this method returns -1 as there are no valid
     * occurrences of "p/" with whitespace before it. However, if
     * {@code argsString} = "e/hi p/900", {@code prefix} = "p/" and
     * {@code fromIndex} = 0, this method returns 5.
     */
    private static int findPrefixPosition(String argsString, String prefix, int fromIndex) {
        int searchFrom = fromIndex + 1;
        while (searchFrom < argsString.length()) {
            int prefixIndex = argsString.indexOf(prefix, searchFrom);
            if (prefixIndex == -1) {
                return -1;
            }
            if (Character.isWhitespace(argsString.charAt(prefixIndex - 1))) {
                return prefixIndex;
            }
            searchFrom = prefixIndex + 1;
        }
        return -1;
    }

    /**
     * Extracts prefixes and their argument values, and returns an {@code ArgumentMultimap} object that maps the
     * extracted prefixes to their respective arguments. Prefixes are extracted based on their zero-based positions in
     * {@code argsString}.
     *
     * @param argsString      Arguments string of the form: {@code preamble <prefix>value <prefix>value ...}
     * @param prefixPositions Zero-based positions of all prefixes in {@code argsString}
     * @return                ArgumentMultimap object that maps prefixes to their arguments
     */
    private static ArgumentMultimap extractArguments(String argsString, List<PrefixPosition> prefixPositions) {

        // Sort by start position
        prefixPositions.sort((prefix1, prefix2) -> prefix1.getStartPosition() - prefix2.getStartPosition());

        // Insert a PrefixPosition to represent the preamble
        PrefixPosition preambleMarker = new PrefixPosition(new Prefix(""), 0);
        prefixPositions.addFirst(preambleMarker);

        // Add a dummy PrefixPosition to represent the end of the string
        PrefixPosition endPositionMarker = new PrefixPosition(new Prefix(""), argsString.length());
        prefixPositions.add(endPositionMarker);

        // Map prefixes to their argument values (if any)
        ArgumentMultimap argMultimap = new ArgumentMultimap();
        for (int i = 0; i < prefixPositions.size() - 1; i++) {
            // Extract and store prefixes and their arguments
            Prefix argPrefix = prefixPositions.get(i).getPrefix();
            String argValue = extractArgumentValue(argsString, prefixPositions.get(i), prefixPositions.get(i + 1));
            argMultimap.put(argPrefix, argValue);
        }

        return argMultimap;
    }

    /**
     * Returns the trimmed value of the argument in the arguments string specified by {@code currentPrefixPosition}.
     * The end position of the value is determined by {@code nextPrefixPosition}.
     */
    private static String extractArgumentValue(String argsString,
                                        PrefixPosition currentPrefixPosition,
                                        PrefixPosition nextPrefixPosition) {
        Prefix prefix = currentPrefixPosition.getPrefix();

        int valueStartPos = currentPrefixPosition.getStartPosition() + prefix.getPrefix().length();
        String value = argsString.substring(valueStartPos, nextPrefixPosition.getStartPosition());

        return value.trim();
    }

    /**
     * Represents a prefix's position in an arguments string.
     */
    private static class PrefixPosition {
        private int startPosition;
        private final Prefix prefix;

        PrefixPosition(Prefix prefix, int startPosition) {
            this.prefix = prefix;
            this.startPosition = startPosition;
        }

        int getStartPosition() {
            return startPosition;
        }

        Prefix getPrefix() {
            return prefix;
        }
    }

    private static class QuotedRange {
        private final int startPosition;
        private final int endPosition;

        QuotedRange(int startPosition, int endPosition) {
            this.startPosition = startPosition;
            this.endPosition = endPosition;
        }

        boolean contains(int position) {
            return startPosition <= position && position <= endPosition;
        }
    }

}
