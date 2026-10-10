package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.lesson.LessonStart;
import seedu.address.model.lesson.LessonTiming;
import seedu.address.model.person.Address;
import seedu.address.model.person.Cost;
import seedu.address.model.person.Email;
import seedu.address.model.person.Lesson;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Subject;
import seedu.address.model.tag.Tag;

/**
 * Contains utility methods used for parsing strings in the various *Parser classes.
 */
public class ParserUtil {

    public static final String MESSAGE_INVALID_INDEX = "Index must be a positive integer.";

    /**
     * Parses {@code oneBasedIndex} into an {@code Index} and returns it. Leading and trailing whitespaces will be
     * trimmed.
     * @throws ParseException if the specified index is invalid (not a non-zero unsigned integer).
     */
    public static Index parseIndex(String oneBasedIndex) throws ParseException {
        String trimmedIndex = oneBasedIndex.trim();
        if (!StringUtil.isNonZeroUnsignedInteger(trimmedIndex)) {
            throw new ParseException(MESSAGE_INVALID_INDEX);
        }
        return Index.fromOneBased(Integer.parseInt(trimmedIndex));
    }

    /**
     * Parses a {@code String name} into a {@code Name}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code name} is invalid.
     */
    public static Name parseName(String name) throws ParseException {
        requireNonNull(name);
        String trimmedName = name.trim();
        if (!Name.isValidName(trimmedName)) {
            throw new ParseException(Name.MESSAGE_CONSTRAINTS);
        }
        return new Name(trimmedName);
    }

    /**
     * Parses a {@code String phone} into a {@code Phone}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code phone} is invalid.
     */
    public static Phone parsePhone(String phone) throws ParseException {
        requireNonNull(phone);
        String trimmedPhone = phone.trim();
        if (!Phone.isValidPhone(trimmedPhone)) {
            throw new ParseException(Phone.MESSAGE_CONSTRAINTS);
        }
        return new Phone(trimmedPhone);
    }

    /**
     * Parses a {@code String address} into an {@code Address}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code address} is invalid.
     */
    public static Address parseAddress(String address) throws ParseException {
        requireNonNull(address);
        String trimmedAddress = address.trim();
        if (!Address.isValidAddress(trimmedAddress)) {
            throw new ParseException(Address.MESSAGE_CONSTRAINTS);
        }
        return new Address(trimmedAddress);
    }

    /**
     * Parses a {@code String email} into an {@code Email}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code email} is invalid.
     */
    public static Email parseEmail(String email) throws ParseException {
        requireNonNull(email);
        String trimmedEmail = email.trim();
        if (!Email.isValidEmail(trimmedEmail)) {
            throw new ParseException(Email.MESSAGE_CONSTRAINTS);
        }
        return new Email(trimmedEmail);
    }

    /**
     * Parses a {@code String tag} into a {@code Tag}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code tag} is invalid.
     */
    public static Tag parseTag(String tag) throws ParseException {
        requireNonNull(tag);
        String trimmedTag = tag.trim();
        if (!Tag.isValidTagName(trimmedTag)) {
            throw new ParseException(Tag.MESSAGE_CONSTRAINTS);
        }
        return new Tag(trimmedTag);
    }

    /**
     * Parses {@code Collection<String> tags} into a {@code Set<Tag>}.
     */
    public static Set<Tag> parseTags(Collection<String> tags) throws ParseException {
        requireNonNull(tags);
        final Set<Tag> tagSet = new LinkedHashSet<>();
        for (String tagName : tags) {
            if (!tagSet.add(parseTag(tagName))) {
                throw new ParseException(Tag.MESSAGE_DUPLICATE_TAGS);
            }
        }
        if (tagSet.size() > Tag.MAX_TAGS_PER_PERSON) {
            throw new ParseException(Tag.MESSAGE_TAG_LIMIT);
        }
        return tagSet;
    }

    /** Parses a subject. */
    public static Subject parseSubject(String subject) throws ParseException {
        requireNonNull(subject);
        String trimmedSubject = subject.trim();
        if (!Subject.isValidSubject(trimmedSubject)) {
            throw new ParseException(Subject.MESSAGE_CONSTRAINTS);
        }
        return new Subject(trimmedSubject);
    }

    /** Parses multiple subjects used by the previous student model. */
    public static Set<Subject> parseSubjects(Collection<String> subjects) throws ParseException {
        requireNonNull(subjects);
        Set<Subject> parsedSubjects = new LinkedHashSet<>();
        for (String subject : subjects) {
            parsedSubjects.add(parseSubject(subject));
        }
        return parsedSubjects;
    }

    /** Parses a lesson cost. */
    public static Cost parseCost(String cost) throws ParseException {
        requireNonNull(cost);
        String trimmedCost = cost.trim();
        if (!Cost.isValidCost(trimmedCost)) {
            throw new ParseException(Cost.MESSAGE_CONSTRAINTS);
        }
        return new Cost(trimmedCost);
    }

    /** Parses a start-only lesson value used by the previous student model. */
    public static Lesson parseLesson(String lesson) throws ParseException {
        requireNonNull(lesson);
        String trimmedLesson = lesson.trim();
        if (!Lesson.isValidLesson(trimmedLesson)) {
            throw new ParseException(Lesson.MESSAGE_CONSTRAINTS);
        }
        return new Lesson(trimmedLesson);
    }

    /** Parses a subject for the recurring lesson model. */
    public static seedu.address.model.lesson.Subject parseLessonSubject(String subject) throws ParseException {
        requireNonNull(subject);
        String trimmedSubject = subject.trim();
        if (!seedu.address.model.lesson.Subject.isValidSubject(trimmedSubject)) {
            throw new ParseException(seedu.address.model.lesson.Subject.MESSAGE_CONSTRAINTS);
        }
        return new seedu.address.model.lesson.Subject(trimmedSubject);
    }

    /** Parses a cost for the recurring lesson model. */
    public static seedu.address.model.lesson.Cost parseLessonCost(String cost) throws ParseException {
        requireNonNull(cost);
        String trimmedCost = cost.trim();
        if (!seedu.address.model.lesson.Cost.isValidCost(trimmedCost)) {
            throw new ParseException(seedu.address.model.lesson.Cost.MESSAGE_CONSTRAINTS);
        }
        return new seedu.address.model.lesson.Cost(trimmedCost);
    }

    /** Parses a complete weekly lesson timing. */
    public static LessonTiming parseLessonTiming(String timing) throws ParseException {
        requireNonNull(timing);
        String trimmedTiming = timing.trim();
        if (!LessonTiming.isValidLessonTiming(trimmedTiming)) {
            throw new ParseException(LessonTiming.MESSAGE_CONSTRAINTS);
        }
        return new LessonTiming(trimmedTiming);
    }

    /** Parses the day and start time used for class searching. */
    public static LessonStart parseLessonStart(String start) throws ParseException {
        requireNonNull(start);
        String trimmedStart = start.trim();
        if (!LessonStart.isValidLessonStart(trimmedStart)) {
            throw new ParseException(LessonStart.MESSAGE_CONSTRAINTS);
        }
        return new LessonStart(trimmedStart);
    }
}
