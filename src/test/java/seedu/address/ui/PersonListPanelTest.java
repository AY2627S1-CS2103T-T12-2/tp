package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.lesson.Cost;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonTiming;
import seedu.address.model.lesson.Subject;
import seedu.address.model.person.Person;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.PersonBuilder;

public class PersonListPanelTest {

    @BeforeAll
    public static void startJavaFx() throws InterruptedException {
        CountDownLatch ready = new CountDownLatch(1);
        Runnable initialize = () -> {
            Platform.setImplicitExit(false);
            ready.countDown();
        };
        try {
            Platform.startup(initialize);
        } catch (IllegalStateException alreadyStarted) {
            Platform.runLater(initialize);
        }
        if (!ready.await(10, TimeUnit.SECONDS)) {
            throw new AssertionError("JavaFX did not start within 10 seconds");
        }
    }

    @Test
    public void constructor_personsWithSharedClass_populatesListAndCreatesCards() throws Exception {
        Lesson sharedLesson = new Lesson(new Subject("Math"), new Cost("30"),
                new LessonTiming("Monday1800-1930"));
        Person firstStudent = new PersonBuilder().withLessons(sharedLesson).build();
        Person secondStudent = new PersonBuilder().withName("Ben Tan").withPhone("91234567")
                .withEmail("ben@example.com").withLessons(sharedLesson).build();
        ObservableList<Person> persons = FXCollections.observableArrayList(firstStudent, secondStudent);

        runOnFxThread(() -> {
            PersonListPanel panel = new PersonListPanel(persons);
            @SuppressWarnings("unchecked")
            ListView<Person> listView = (ListView<Person>) panel.getRoot().lookup("#personListView");

            assertNotNull(listView);
            assertEquals(List.of(firstStudent, secondStudent), listView.getItems());
            PersonListPanel.PersonListViewCell cell =
                    (PersonListPanel.PersonListViewCell) listView.getCellFactory().call(listView);
            assertNotNull(cell);
            cell.updateItem(firstStudent, false);
            assertNotNull(cell.getGraphic());
            cell.updateItem(null, true);
            assertNull(cell.getGraphic());
            assertNull(cell.getText());
            cell.updateItem(null, false);
            assertNull(cell.getGraphic());
            assertNull(cell.getText());
        });
    }

    @Test
    public void setPrivacyModeEnabled_existingCardMasksAndRestoresContactDetails() throws Exception {
        Person person = new PersonBuilder().withPhone("91234567")
                .withEmail("student@example.com").withAddress("123 Privacy Street").build();
        ObservableList<Person> persons = FXCollections.observableArrayList(person);

        runOnFxThread(() -> {
            PersonListPanel panel = new PersonListPanel(persons, false);
            @SuppressWarnings("unchecked")
            ListView<Person> listView = (ListView<Person>) panel.getRoot().lookup("#personListView");
            PersonListPanel.PersonListViewCell cell =
                    (PersonListPanel.PersonListViewCell) listView.getCellFactory().call(listView);
            cell.updateItem(person, false);

            panel.setPrivacyModeEnabled(false);

            assertContactLabelShown(cell, "phone", "91234567");
            assertContactLabelShown(cell, "email", "student@example.com");
            assertContactLabelShown(cell, "address", "123 Privacy Street");

            panel.setPrivacyModeEnabled(true);

            assertContactLabelMasked(cell, "phone", "Phone");
            assertContactLabelMasked(cell, "email", "Email");
            assertContactLabelMasked(cell, "address", "Address");

            panel.setPrivacyModeEnabled(false);

            assertContactLabelShown(cell, "phone", "91234567");
            assertContactLabelShown(cell, "email", "student@example.com");
            assertContactLabelShown(cell, "address", "123 Privacy Street");
        });
    }

    private static Label getCardLabel(PersonListPanel.PersonListViewCell cell, String id) {
        Label label = (Label) cell.getGraphic().lookup("#" + id);
        assertNotNull(label);
        return label;
    }

    private static void assertContactLabelMasked(PersonListPanel.PersonListViewCell cell, String id,
            String fieldName) {
        Label label = getCardLabel(cell, id);
        assertEquals(fieldName + ": " + PersonCard.PRIVACY_MASK, label.getText());
        assertTrue(label.isVisible());
        assertTrue(label.isManaged());
    }

    private static void assertContactLabelShown(PersonListPanel.PersonListViewCell cell, String id, String text) {
        Label label = getCardLabel(cell, id);
        assertEquals(text, label.getText());
        assertTrue(label.isVisible());
        assertTrue(label.isManaged());
    }

    @Test
    public void personList_pinningStudent_reordersItemsAndUpdatesDisplayedIndex() throws Exception {
        Person firstStudent = new PersonBuilder().withName("Alice Tan").build();
        Person secondStudent = new PersonBuilder().withName("Ben Tan").withPhone("91234567")
                .withEmail("ben@example.com").build();
        AddressBook addressBook = new AddressBookBuilder()
                .withPerson(firstStudent)
                .withPerson(secondStudent)
                .build();
        ModelManager model = new ModelManager(addressBook, new UserPrefs());

        runOnFxThread(() -> {
            PersonListPanel panel = new PersonListPanel(model.getFilteredPersonList());
            @SuppressWarnings("unchecked")
            ListView<Person> listView = (ListView<Person>) panel.getRoot().lookup("#personListView");
            Person pinnedSecondStudent = secondStudent.withPinned(true);

            model.setPerson(secondStudent, pinnedSecondStudent);

            assertEquals(List.of(pinnedSecondStudent, firstStudent), listView.getItems());
            PersonListPanel.PersonListViewCell cell =
                    (PersonListPanel.PersonListViewCell) listView.getCellFactory().call(listView);
            cell.updateIndex(0);
            cell.updateItem(listView.getItems().get(0), false);
            Label displayedIndex = (Label) cell.getGraphic().lookup("#id");
            assertEquals("1. ", displayedIndex.getText());
        });
    }

    private static void runOnFxThread(Runnable assertions) throws Exception {
        FutureTask<Void> task = new FutureTask<>(assertions, null);
        Platform.runLater(task);
        task.get(10, TimeUnit.SECONDS);
    }
}
