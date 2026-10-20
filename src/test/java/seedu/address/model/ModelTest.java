package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertSame;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Person;
import seedu.address.model.timetable.TimetableDay;

public class ModelTest {

    @Test
    public void updateFilteredPersonList_withComparator_usesPredicateFallback() {
        DefaultComparatorModel model = new DefaultComparatorModel();
        Predicate<Person> predicate = person -> true;
        Comparator<Person> comparator = Comparator.comparing(person -> person.getName().fullName);

        model.updateFilteredPersonList(predicate, comparator);

        assertSame(predicate, model.predicate);
    }

    private static class DefaultComparatorModel implements Model {
        private Predicate<Person> predicate;

        @Override
        public ReadOnlyUserPrefs getUserPrefs() {
            throw new UnsupportedOperationException();
        }

        @Override
        public GuiSettings getGuiSettings() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setGuiSettings(GuiSettings guiSettings) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isPrivacyModeEnabled() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setPrivacyModeEnabled(boolean privacyModeEnabled) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setAddressBook(ReadOnlyAddressBook addressBook) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ReadOnlyAddressBook getAddressBook() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean hasPerson(Person person) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void deletePerson(Person target) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addPerson(Person person) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setPerson(Person target, Person editedPerson) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ObservableList<Person> getFilteredPersonList() {
            return FXCollections.emptyObservableList();
        }

        @Override
        public void updateFilteredPersonList(Predicate<Person> predicate) {
            this.predicate = predicate;
        }

        @Override
        public List<TimetableDay> getTimetable(LocalDate today) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean hasClassCostConflict(Lesson candidate) {
            throw new UnsupportedOperationException();
        }
    }
}
