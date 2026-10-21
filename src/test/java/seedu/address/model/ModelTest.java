package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.model.lesson.ClassId;
import seedu.address.model.lesson.Cost;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonTiming;
import seedu.address.model.lesson.Subject;
import seedu.address.model.lesson.TuitionClass;
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

    @Test
    public void tuitionClassDefaultMethods_readFromAddressBook() {
        DefaultComparatorModel model = new DefaultComparatorModel();
        TuitionClass math = tuitionClass("C001", "Math", "30", "Monday1800-1930");
        model.addressBook.addTuitionClass(math);

        assertTrue(model.hasTuitionClass(new ClassId("C001")));
        assertFalse(model.hasTuitionClass(new ClassId("C002")));
        assertTrue(model.hasTuitionClassWithDetails(
                tuitionClass("C009", "math", "30.00", "Monday1800-1930")));
        assertSame(math, model.getTuitionClass(new ClassId("C001")));
        assertEquals(List.of(math), model.getTuitionClassList());
        assertThrows(NoSuchElementException.class, () -> model.getTuitionClass(new ClassId("C002")));
    }

    @Test
    public void tuitionClassMutationDefaults_throwUnsupportedOperationException() {
        DefaultComparatorModel model = new DefaultComparatorModel();
        TuitionClass tuitionClass = tuitionClass("C001", "Math", "30", "Monday1800-1930");

        assertThrows(UnsupportedOperationException.class, model::allocateClassId);
        assertThrows(UnsupportedOperationException.class, () -> model.addTuitionClass(tuitionClass));
        assertThrows(UnsupportedOperationException.class, () -> model.setTuitionClass(tuitionClass, tuitionClass));
        assertThrows(UnsupportedOperationException.class, () -> model.deleteTuitionClass(tuitionClass));
    }

    @Test
    public void hasClassCostConflict_sameClassDifferentCost_returnsTrueOnlyForConflict() {
        DefaultComparatorModel model = new DefaultComparatorModel();
        model.addressBook.addTuitionClass(tuitionClass("C001", "Math", "30", "Monday1800-1930"));

        assertTrue(model.hasClassCostConflict(lesson("math", "40", "Monday1800-1930")));
        assertFalse(model.hasClassCostConflict(lesson("Math", "30", "Monday1800-1930")));
        assertFalse(model.hasClassCostConflict(lesson("Math", "40", "Tuesday1800-1930")));
    }

    private static TuitionClass tuitionClass(String id, String subject, String cost, String timing) {
        return new TuitionClass(new ClassId(id), new Subject(subject), new Cost(cost), new LessonTiming(timing));
    }

    private static Lesson lesson(String subject, String cost, String timing) {
        return new Lesson(new Subject(subject), new Cost(cost), new LessonTiming(timing));
    }

    private static class DefaultComparatorModel implements Model {
        private final AddressBook addressBook = new AddressBook();
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
        public void setAddressBook(ReadOnlyAddressBook addressBook) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ReadOnlyAddressBook getAddressBook() {
            return addressBook;
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
        public void setPersons(Map<Person, Person> replacements) {
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

    }
}
