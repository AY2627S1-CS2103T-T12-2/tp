package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import seedu.address.model.person.Cost;
import seedu.address.model.person.Lesson;
import seedu.address.model.person.Person;
import seedu.address.model.person.Subject;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests the actual student card and its optional fields using the JavaFX application thread.
 */
public class PersonCardTest {

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
    public void constructor_allDetails_displaysContactAndSortedStudentDetails() throws Exception {
        runOnFxThread(() -> {
            Person person = student(Set.of(new Subject("Physics"), new Subject("Math")),
                    Optional.of(new Cost("30")), Optional.of(new Lesson("Monday1800")));
            PersonCard card = new PersonCard(person, 2);
            assertEquals(person, card.person);
            assertLabel(card, "id", "2. ", true);
            assertLabel(card, "name", person.getName().fullName, true);
            assertLabel(card, "phone", person.getPhone().value, true);
            assertLabel(card, "email", person.getEmail().value, true);
            assertLabel(card, "address", person.getAddress().value, true);
            assertLabel(card, "subjects", "Subjects: Math, Physics", true);
            assertLabel(card, "cost", "Cost/hour: 30", true);
            assertLabel(card, "lesson", "Lesson: Monday1800", true);
            FlowPane tags = (FlowPane) card.getRoot().lookup("#tags");
            assertNotNull(tags);
            assertEquals(List.of("alpha", "zeta"), tags.getChildren().stream()
                    .map(node -> ((Label) node).getText()).toList());
        });
    }

    @Test
    public void constructor_noStudentDetails_hidesOptionalLabelsAndTheirLayoutSpace() throws Exception {
        runOnFxThread(() -> {
            PersonCard card = new PersonCard(new PersonBuilder().build(), 1);
            assertLabel(card, "subjects", "Subjects: ", false);
            assertLabel(card, "cost", "", false);
            assertLabel(card, "lesson", "", false);
        });
    }

    @Test
    public void constructor_subjectsOnly_hidesCostAndLesson() throws Exception {
        runOnFxThread(() -> {
            PersonCard card = new PersonCard(student(Set.of(new Subject("A")),
                    Optional.empty(), Optional.empty()), 1);
            assertLabel(card, "subjects", "Subjects: A", true);
            assertLabel(card, "cost", "", false);
            assertLabel(card, "lesson", "", false);
        });
    }

    @Test
    public void constructor_costOnly_hidesSubjectsAndLesson() throws Exception {
        runOnFxThread(() -> {
            PersonCard card = new PersonCard(student(Set.of(),
                    Optional.of(new Cost("1")), Optional.empty()), 1);
            assertLabel(card, "subjects", "Subjects: ", false);
            assertLabel(card, "cost", "Cost/hour: 1", true);
            assertLabel(card, "lesson", "", false);
        });
    }

    @Test
    public void constructor_lessonOnly_hidesSubjectsAndCost() throws Exception {
        runOnFxThread(() -> {
            PersonCard card = new PersonCard(student(Set.of(),
                    Optional.empty(), Optional.of(new Lesson("Sunday2359"))), 1);
            assertLabel(card, "subjects", "Subjects: ", false);
            assertLabel(card, "cost", "", false);
            assertLabel(card, "lesson", "Lesson: Sunday2359", true);
        });
    }

    private static Person student(Set<Subject> subjects, Optional<Cost> cost, Optional<Lesson> lesson) {
        Person base = new PersonBuilder().withTags("zeta", "alpha").build();
        return new Person(base.getName(), base.getPhone(), base.getEmail(), base.getAddress(),
                base.getTags(), subjects, cost, lesson);
    }

    private static void assertLabel(PersonCard card, String id, String text, boolean shown) {
        Label label = (Label) card.getRoot().lookup("#" + id);
        assertNotNull(label, "Missing FXML label: " + id);
        assertEquals(text, label.getText(), id + " text");
        assertEquals(shown, label.isVisible(), id + " visibility");
        assertEquals(shown, label.isManaged(), id + " layout participation");
    }

    private static void runOnFxThread(Runnable assertions) throws Exception {
        FutureTask<Void> task = new FutureTask<>(assertions, null);
        Platform.runLater(task);
        task.get(10, TimeUnit.SECONDS);
    }
}
