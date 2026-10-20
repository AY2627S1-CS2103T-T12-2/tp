package seedu.address.model.person;

import java.util.function.Predicate;

/**
 * A {@code Predicate} that can rank matching persons by relevance.
 */
public interface ScoredPersonPredicate extends Predicate<Person> {

    /**
     * Returns a positive relevance score for matching persons and zero for non-matching persons.
     */
    int score(Person person);
}
