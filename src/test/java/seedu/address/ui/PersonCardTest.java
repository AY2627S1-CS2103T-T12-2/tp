package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import seedu.address.model.lesson.Cost;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonTiming;
import seedu.address.model.lesson.PaymentStatus;
import seedu.address.model.lesson.Subject;
import seedu.address.model.person.Person;
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
    public void constructor_enrolledClasses_displaysSortedRowsBelowContactDetails() throws Exception {
        runOnFxThread(() -> {
            Lesson physics = lesson("Physics", "35", "Wednesday1700-1830")
                    .withPaymentStatus(PaymentStatus.PAID);
            Lesson math = lesson("Math", "30", "Monday1800-1930");
            Person person = new PersonBuilder().withTags("zeta", "alpha")
                    .withLessons(physics, math).build();
            PersonCard card = new PersonCard(person, 2);

            assertEquals(person, card.person);
            assertLabel(card, "id", "2. ", true);
            assertLabel(card, "name", person.getName().fullName, true);
            assertLabel(card, "phone", person.getPhone().value, true);
            assertLabel(card, "email", person.getEmail().value, true);
            assertLabel(card, "address", person.getAddress().value, true);
            assertEquals(List.of("Math · Mon 18:00–19:30", "Physics · Wed 17:00–18:30"),
                    getClassRows(card).stream().map(PersonCardTest::getClassDetails).toList());
            assertEquals(List.of("Unpaid", "Paid"),
                    getClassRows(card).stream().map(PersonCardTest::getPaymentStatus).toList());
            assertTrue(getClassPane(card).isVisible());
            assertTrue(getClassPane(card).isManaged());
            assertTrue(getClassPane(card).getParent() instanceof VBox);
            VBox details = (VBox) getClassPane(card).getParent();
            assertTrue(details.getChildren().indexOf(card.getRoot().lookup("#email"))
                    < details.getChildren().indexOf(getClassPane(card)));
            getClassRows(card).forEach(row -> {
                assertTrue(row.getStyleClass().contains("enrolled-class-row"));
                assertEquals(8, row.getSpacing());
                assertTrue(getClassDetailsLabel(row).getStyleClass().contains("cell_small_label"));
            });
            assertTrue(getPaymentLabel(getClassRows(card).get(0)).getStyleClass()
                    .contains("payment-status-unpaid"));
            assertTrue(getPaymentLabel(getClassRows(card).get(1)).getStyleClass()
                    .contains("payment-status-paid"));
            assertLabel(card, "subjects", "Subjects: ", false);
            assertLabel(card, "cost", "", false);
            assertLabel(card, "lesson", "", false);

            FlowPane tags = (FlowPane) card.getRoot().lookup("#tags");
            assertNotNull(tags);
            assertEquals(List.of("alpha", "zeta"), tags.getChildren().stream()
                    .map(node -> ((Label) node).getText()).toList());
        });
    }

    @Test
    public void constructor_noStudentDetails_hidesOptionalFieldsAndTheirLayoutSpace() throws Exception {
        runOnFxThread(() -> {
            PersonCard card = new PersonCard(new PersonBuilder().build(), 1);
            assertTrue(getClassRows(card).isEmpty());
            assertFalse(getClassPane(card).isVisible());
            assertFalse(getClassPane(card).isManaged());
            assertLabel(card, "subjects", "Subjects: ", false);
            assertLabel(card, "cost", "", false);
            assertLabel(card, "lesson", "", false);
        });
    }

    @Test
    public void constructor_completeLegacyDetails_displaysMigratedClassesWithoutDuplicates() throws Exception {
        runOnFxThread(() -> {
            Person person = legacyStudent(Set.of(new seedu.address.model.person.Subject("Physics"),
                            new seedu.address.model.person.Subject("Math")),
                    Optional.of(new seedu.address.model.person.Cost("30")),
                    Optional.of(new seedu.address.model.person.Lesson("Monday1800")));
            PersonCard card = new PersonCard(person, 1);

            assertEquals(List.of("Math · Mon 18:00–19:00", "Physics · Mon 18:00–19:00"),
                    getClassRows(card).stream().map(PersonCardTest::getClassDetails).toList());
            assertEquals(List.of("Unpaid", "Unpaid"),
                    getClassRows(card).stream().map(PersonCardTest::getPaymentStatus).toList());
            assertLabel(card, "subjects", "Subjects: Math, Physics", false);
            assertLabel(card, "cost", "Cost/hour: 30", false);
            assertLabel(card, "lesson", "Lesson: Monday1800", false);
        });
    }

    @Test
    public void constructor_legacySubjectsOnly_hidesCostAndLesson() throws Exception {
        runOnFxThread(() -> {
            PersonCard card = new PersonCard(legacyStudent(
                    Set.of(new seedu.address.model.person.Subject("A")), Optional.empty(), Optional.empty()), 1);
            assertTrue(getClassRows(card).isEmpty());
            assertLabel(card, "subjects", "Subjects: A", true);
            assertLabel(card, "cost", "", false);
            assertLabel(card, "lesson", "", false);
        });
    }

    @Test
    public void constructor_legacyCostOnly_hidesSubjectsAndLesson() throws Exception {
        runOnFxThread(() -> {
            PersonCard card = new PersonCard(legacyStudent(Set.of(),
                    Optional.of(new seedu.address.model.person.Cost("1")), Optional.empty()), 1);
            assertTrue(getClassRows(card).isEmpty());
            assertLabel(card, "subjects", "Subjects: ", false);
            assertLabel(card, "cost", "Cost/hour: 1", true);
            assertLabel(card, "lesson", "", false);
        });
    }

    @Test
    public void constructor_legacyLessonOnly_hidesSubjectsAndCost() throws Exception {
        runOnFxThread(() -> {
            PersonCard card = new PersonCard(legacyStudent(Set.of(), Optional.empty(),
                    Optional.of(new seedu.address.model.person.Lesson("Sunday2359"))), 1);
            assertTrue(getClassRows(card).isEmpty());
            assertLabel(card, "subjects", "Subjects: ", false);
            assertLabel(card, "cost", "", false);
            assertLabel(card, "lesson", "Lesson: Sunday2359", true);
        });
    }

    private static Lesson lesson(String subject, String cost, String timing) {
        return new Lesson(new Subject(subject), new Cost(cost), new LessonTiming(timing));
    }

    private static Person legacyStudent(Set<seedu.address.model.person.Subject> subjects,
            Optional<seedu.address.model.person.Cost> cost,
            Optional<seedu.address.model.person.Lesson> lesson) {
        Person base = new PersonBuilder().withTags("zeta", "alpha").build();
        return new Person(base.getName(), base.getPhone(), base.getEmail(), base.getAddress(),
                base.getTags(), subjects, cost, lesson);
    }

    private static VBox getClassPane(PersonCard card) {
        VBox classes = (VBox) card.getRoot().lookup("#classes");
        assertNotNull(classes);
        return classes;
    }

    private static List<HBox> getClassRows(PersonCard card) {
        return getClassPane(card).getChildren().stream().map(node -> (HBox) node).toList();
    }

    private static String getClassDetails(HBox classRow) {
        return getClassDetailsLabel(classRow).getText();
    }

    private static Label getClassDetailsLabel(HBox classRow) {
        return (Label) classRow.getChildren().get(0);
    }

    private static String getPaymentStatus(HBox classRow) {
        return getPaymentLabel(classRow).getText();
    }

    private static Label getPaymentLabel(HBox classRow) {
        return (Label) classRow.getChildren().get(1);
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
