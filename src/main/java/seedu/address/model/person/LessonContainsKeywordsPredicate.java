package seedu.address.model.person;

import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.lesson.LessonTiming;

/**
 * Tests that a {@code Person}'s lessons match the given day and/or time keywords.
 */
public class LessonContainsKeywordsPredicate implements ScoredPersonPredicate {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HHmm");

    private final Optional<String> dayKeyword;
    private final Optional<String> timeKeyword;

    /**
     * Creates a predicate from a single lesson-search input.
     */
    public LessonContainsKeywordsPredicate(String lessonSearchInput) {
        this(LessonSearchParts.from(lessonSearchInput));
    }

    /**
     * Creates a predicate from lesson-search input tokens.
     */
    public LessonContainsKeywordsPredicate(List<String> keywords) {
        this(String.join(" ", keywords));
    }

    /**
     * Creates a predicate that matches lesson timings by optional day and time keywords.
     */
    public LessonContainsKeywordsPredicate(Optional<String> dayKeyword, Optional<String> timeKeyword) {
        this.dayKeyword = dayKeyword.map(keyword -> keyword.toLowerCase(Locale.ROOT));
        this.timeKeyword = timeKeyword;
    }

    private LessonContainsKeywordsPredicate(LessonSearchParts lessonSearchParts) {
        this(lessonSearchParts.dayKeyword, lessonSearchParts.timeKeyword);
    }

    @Override
    public boolean test(Person person) {
        return score(person) > 0;
    }

    @Override
    public int score(Person person) {
        int lessonScore = person.getLessons().stream()
                .mapToInt(this::scoreLesson)
                .sum();

        if (lessonScore > 0) {
            return lessonScore;
        }

        return person.getLesson()
                .map(lesson -> scoreLegacyLesson(lesson.toString()))
                .orElse(0);
    }

    private int scoreLesson(seedu.address.model.lesson.Lesson lesson) {
        LessonTiming timing = lesson.getTiming();
        boolean dayMatches = dayKeyword.map(keyword -> dayMatches(timing.getDay(), keyword)).orElse(true);
        boolean timeMatches = timeKeyword.map(keyword -> timeMatches(timing, keyword)).orElse(true);

        if (!dayMatches || !timeMatches) {
            return 0;
        }

        return (int) dayKeyword.stream().count() + (int) timeKeyword.stream().count();
    }

    private boolean dayMatches(DayOfWeek day, String keyword) {
        String dayName = day.toString().toLowerCase(Locale.ROOT);
        return dayName.startsWith(keyword);
    }

    private boolean timeMatches(LessonTiming timing, String keyword) {
        return timing.getStartTime().format(TIME_FORMAT).contains(keyword)
                || timing.getEndTime().format(TIME_FORMAT).contains(keyword);
    }

    private int scoreLegacyLesson(String lesson) {
        String lessonLowerCase = lesson.toLowerCase(Locale.ROOT);
        boolean dayMatches = dayKeyword.map(lessonLowerCase::startsWith).orElse(true);
        boolean timeMatches = timeKeyword.map(lessonLowerCase::contains).orElse(true);

        if (!dayMatches || !timeMatches) {
            return 0;
        }

        return (int) dayKeyword.stream().count() + (int) timeKeyword.stream().count();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof LessonContainsKeywordsPredicate otherLessonContainsKeywordsPredicate)) {
            return false;
        }

        return dayKeyword.equals(otherLessonContainsKeywordsPredicate.dayKeyword)
                && timeKeyword.equals(otherLessonContainsKeywordsPredicate.timeKeyword);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("dayKeyword", dayKeyword)
                .add("timeKeyword", timeKeyword)
                .toString();
    }

    private static class LessonSearchParts {
        private final Optional<String> dayKeyword;
        private final Optional<String> timeKeyword;

        LessonSearchParts(Optional<String> dayKeyword, Optional<String> timeKeyword) {
            this.dayKeyword = dayKeyword;
            this.timeKeyword = timeKeyword;
        }

        private static LessonSearchParts from(String lessonSearchInput) {
            String[] tokens = lessonSearchInput.trim().split("\\s+");
            String day = null;
            String time = null;

            for (String token : tokens) {
                int splitIndex = firstDigitIndex(token);
                String dayPart = splitIndex == -1 ? token : token.substring(0, splitIndex);
                String timePart = splitIndex == -1 ? "" : token.substring(splitIndex);

                if (!dayPart.isEmpty()) {
                    day = dayPart;
                }
                if (!timePart.isEmpty()) {
                    time = timePart;
                }
            }

            return new LessonSearchParts(Optional.ofNullable(day), Optional.ofNullable(time));
        }

        private static int firstDigitIndex(String token) {
            for (int i = 0; i < token.length(); i++) {
                if (Character.isDigit(token.charAt(i))) {
                    return i;
                }
            }
            return -1;
        }
    }
}
