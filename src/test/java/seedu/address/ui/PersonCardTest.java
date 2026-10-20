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
import seedu.address.model.lesson.OneOffLessonSchedule;
import seedu.address.model.lesson.PaymentStatus;
import seedu.address.model.lesson.Subject;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests the actual student card and its optional fields using the JavaFX application thread.
 */
public class PersonCardTest {

    @Test
    public void formatClassBadge_oneOffSchedule_displaysExactDate() {
        Lesson oneOff = new Lesson(new Subject("Math"), new Cost("30"),
                new OneOffLessonSchedule("2026-10-20T1800-1930"));

        assertEquals("Math · 20 Oct 2026 18:00–19:30", PersonCard.formatClassBadge(oneOff));
    }

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
    public void constructor_enrolledClasses_displaysSortedGroupedBadgesBelowEmail() throws Exception {
        runOnFxThread(() -> {
            Lesson physics = lesson("Physics", "35", "Wednesday1700-1830")
                    .withPaymentStatus(PaymentStatus.PAID);
            Lesson math = lesson("Math", "30", "Monday1800-1930");
            Person person = new PersonBuilder().withTags("zeta", "alpha")
                    .withLessons(physics, math).build();
            ClassColorPalette palette = new ClassColorPalette(List.of(person));
            PersonCard card = new PersonCard(person, 2, palette);

            assertEquals(person, card.person);
            assertLabel(card, "id", "2. ", true);
            assertLabel(card, "name", person.getName().fullName, true);
            assertLabel(card, "balance", "Balance: 30", true);
            assertLabel(card, "phone", person.getPhone().value, true);
            assertLabel(card, "email", person.getEmail().value, true);
            assertLabel(card, "address", person.getAddress().value, true);
            assertEquals(List.of("Math · Mon 18:00–19:30", "Physics · Wed 17:00–18:30"),
                    getClassGroups(card).stream().map(PersonCardTest::getClassDetails).toList());
            assertEquals(List.of("Unpaid", "Paid"),
                    getClassGroups(card).stream().map(PersonCardTest::getPaymentStatus).toList());
            assertTrue(getClassPane(card).isVisible());
            assertTrue(getClassPane(card).isManaged());
            assertTrue(getClassPane(card).getParent() instanceof VBox);
            VBox details = (VBox) getClassPane(card).getParent();
            assertEquals(details.getChildren().indexOf(email(card)) + 1,
                    details.getChildren().indexOf(getClassPane(card)));
            assertEquals(3.0, getClassPane(card).getSpacing());
            getClassGroups(card).forEach(group -> {
                assertTrue(group.getStyleClass().contains("enrolled-class-group"));
                assertEquals(5, group.getSpacing());
                assertTrue(getClassBadge(group).getStyleClass().contains("enrolled-class-badge"));
                assertTrue(getColorStyleClass(group).startsWith(ClassColorPalette.STYLE_CLASS_PREFIX));
            });
            assertTrue(getPaymentLabel(getClassGroups(card).get(0)).getStyleClass()
                    .contains("payment-status-unpaid"));
            assertTrue(getPaymentLabel(getClassGroups(card).get(1)).getStyleClass()
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
    public void constructor_sameClassAcrossStudents_usesSameBadgeColor() throws Exception {
        runOnFxThread(() -> {
            Lesson firstLesson = lesson("Math", "30", "Monday1800-1930");
            Lesson equivalentLesson = lesson("math", "30.00", "Monday1800-1930");
            Person firstStudent = new PersonBuilder().withLessons(firstLesson).build();
            Person secondStudent = new PersonBuilder().withName("Ben Tan").withPhone("91234567")
                    .withEmail("ben@example.com").withLessons(equivalentLesson).build();
            ClassColorPalette palette = new ClassColorPalette(List.of(firstStudent, secondStudent));

            PersonCard firstCard = new PersonCard(firstStudent, 1, palette);
            PersonCard secondCard = new PersonCard(secondStudent, 2, palette);

            assertEquals(getColorStyleClass(getClassGroups(firstCard).get(0)),
                    getColorStyleClass(getClassGroups(secondCard).get(0)));
        });
    }

    @Test
    public void constructor_noStudentDetails_hidesOptionalFieldsAndTheirLayoutSpace() throws Exception {
        runOnFxThread(() -> {
            PersonCard card = new PersonCard(new PersonBuilder().build(), 1);
            assertTrue(getClassGroups(card).isEmpty());
            assertFalse(getClassPane(card).isVisible());
            assertFalse(getClassPane(card).isManaged());
            assertLabel(card, "balance", "Balance: 0", true);
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
                    getClassGroups(card).stream().map(PersonCardTest::getClassDetails).toList());
            assertEquals(List.of("Unpaid", "Unpaid"),
                    getClassGroups(card).stream().map(PersonCardTest::getPaymentStatus).toList());
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
            assertTrue(getClassGroups(card).isEmpty());
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
            assertTrue(getClassGroups(card).isEmpty());
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
            assertTrue(getClassGroups(card).isEmpty());
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

    private static Label email(PersonCard card) {
        Label email = (Label) card.getRoot().lookup("#email");
        assertNotNull(email);
        return email;
    }

    private static List<HBox> getClassGroups(PersonCard card) {
        return getClassPane(card).getChildren().stream().map(node -> (HBox) node).toList();
    }

    private static String getClassDetails(HBox classGroup) {
        return getClassBadge(classGroup).getText();
    }

    private static Label getClassBadge(HBox classGroup) {
        return (Label) classGroup.getChildren().get(0);
    }

    private static String getColorStyleClass(HBox classGroup) {
        return getClassBadge(classGroup).getStyleClass().stream()
                .filter(styleClass -> styleClass.startsWith(ClassColorPalette.STYLE_CLASS_PREFIX))
                .findFirst()
                .orElseThrow();
    }

    private static String getPaymentStatus(HBox classGroup) {
        return getPaymentLabel(classGroup).getText();
    }

    private static Label getPaymentLabel(HBox classGroup) {
        return (Label) classGroup.getChildren().get(1);
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
