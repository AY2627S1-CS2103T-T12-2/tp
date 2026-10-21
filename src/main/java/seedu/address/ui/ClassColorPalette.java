package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Person;

/** Assigns presentation colors consistently to logical classes. */
final class ClassColorPalette {

    static final int COLOR_COUNT = 8;
    static final String STYLE_CLASS_PREFIX = "enrolled-class-color-";

    private final Map<ClassIdentity, Integer> colorByClass = new HashMap<>();

    /** Creates an initially empty palette. */
    ClassColorPalette() {
    }

    /**
     * Creates a palette pre-populated from {@code persons} in a deterministic order.
     */
    ClassColorPalette(Iterable<Person> persons) {
        requireNonNull(persons);
        List<ClassIdentity> identities = new ArrayList<>();
        for (Person person : persons) {
            requireNonNull(person);
            person.getLessons().stream()
                    .map(ClassIdentity::from)
                    .forEach(identities::add);
        }
        identities.stream()
                .distinct()
                .sorted()
                .forEach(this::assignColor);
    }

    /** Returns the CSS style class assigned to {@code lesson}. */
    String getStyleClass(Lesson lesson) {
        requireNonNull(lesson);
        return STYLE_CLASS_PREFIX + assignColor(ClassIdentity.from(lesson));
    }

    private int assignColor(ClassIdentity identity) {
        Integer existingColor = colorByClass.get(identity);
        if (existingColor != null) {
            return existingColor;
        }

        int preferredColor = Math.floorMod(identity.hashCode(), COLOR_COUNT);
        if (colorByClass.size() < COLOR_COUNT) {
            int candidateColor = preferredColor;
            while (colorByClass.containsValue(candidateColor)) {
                candidateColor = (candidateColor + 1) % COLOR_COUNT;
            }
            colorByClass.put(identity, candidateColor);
            return candidateColor;
        }

        colorByClass.put(identity, preferredColor);
        return preferredColor;
    }

    /** The fields that define a logical class in {@link Lesson#isSameClass(Lesson)}. */
    private record ClassIdentity(String normalizedSubject, String timing) implements Comparable<ClassIdentity> {

        private static ClassIdentity from(Lesson lesson) {
            String subject = lesson.getSubject().toString().toLowerCase(Locale.ROOT);
            return new ClassIdentity(subject, lesson.getSchedule().toStorageString());
        }

        @Override
        public int compareTo(ClassIdentity other) {
            int subjectComparison = normalizedSubject.compareTo(other.normalizedSubject);
            return subjectComparison != 0 ? subjectComparison : timing.compareTo(other.timing);
        }
    }
}
