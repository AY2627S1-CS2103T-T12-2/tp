package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import seedu.address.logic.commands.Command;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.lesson.Cost;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonTiming;
import seedu.address.model.lesson.Subject;
import seedu.address.model.person.Person;
import seedu.address.model.timetable.TimetableDay;
import seedu.address.model.timetable.TimetableEntry;
import seedu.address.testutil.PersonBuilder;

public class TimetableDayCardTest {

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
    public void formatDate_date_returnsEnglishHeadingWithoutYear() {
        assertEquals("Tuesday, 20 Oct", TimetableDayCard.formatDate(LocalDate.of(2026, 10, 20)));
        assertEquals("Friday, 1 Jan", TimetableDayCard.formatDate(LocalDate.of(2027, 1, 1)));
    }

    @Test
    public void constructor_emptyAndPopulatedDays_displayCorrectContent() throws Exception {
        Lesson lesson = new Lesson(new Subject("Math"), new Cost("30"),
                new LessonTiming("Monday1800-1930"));
        Person student = new PersonBuilder().build();
        TimetableEntry entry = new TimetableEntry(lesson, List.of(student));

        runOnFxThread(() -> {
            TimetableDayCard emptyCard = new TimetableDayCard(
                    new TimetableDay(LocalDate.of(2026, 10, 19), List.of()), false);
            TimetableDayCard populatedCard = new TimetableDayCard(
                    new TimetableDay(LocalDate.of(2026, 10, 19), List.of(entry)), false);

            Label dayHeading = (Label) emptyCard.getRoot().lookup("#dayHeading");
            Label emptyMessage = (Label) emptyCard.getRoot().lookup("#emptyMessage");
            VBox entriesContainer = (VBox) populatedCard.getRoot().lookup("#entriesContainer");
            Label populatedMessage = (Label) populatedCard.getRoot().lookup("#emptyMessage");

            assertNotNull(dayHeading);
            assertEquals("Monday, 19 Oct", dayHeading.getText());
            assertTrue(emptyMessage.isVisible());
            assertTrue(emptyMessage.isManaged());
            assertEquals(1, entriesContainer.getChildren().size());
            assertFalse(populatedMessage.isVisible());
            assertFalse(populatedMessage.isManaged());
        });
    }

    @Test
    public void constructor_todayAndOtherDay_appliesHighlightOnlyToToday() throws Exception {
        TimetableDay day = new TimetableDay(LocalDate.of(2026, 10, 20), List.of());

        runOnFxThread(() -> {
            TimetableDayCard todayCard = new TimetableDayCard(day, true);
            TimetableDayCard otherDayCard = new TimetableDayCard(day, false);

            assertTrue(todayCard.getRoot().getStyleClass().contains("timetable-today"));
            assertFalse(otherDayCard.getRoot().getStyleClass().contains("timetable-today"));
        });
    }

    @Test
    public void constructor_emptyOneOffCatalogueClass_displaysWithoutStudents() throws Exception {
        ModelManager model = new ModelManager(new AddressBook(), new UserPrefs());
        Command addClass = new AddressBookParser().parseCommand(
                "class add sub/Unenrolled Timetable Check cost/25 c/2026-10-29T2100-2200");
        addClass.execute(model);

        assertTrue(model.getAddressBook().getPersonList().isEmpty());
        assertEquals(1, model.getTuitionClassList().size());
        TimetableDay thursday = model.getTimetable(LocalDate.of(2026, 10, 28)).get(4);
        assertEquals(LocalDate.of(2026, 10, 29), thursday.getDate());
        assertEquals(1, thursday.getEntries().size());
        assertTrue(thursday.getEntries().getFirst().getStudents().isEmpty());

        runOnFxThread(() -> {
            TimetableDayCard card = new TimetableDayCard(thursday, false);
            VBox entriesContainer = (VBox) card.getRoot().lookup("#entriesContainer");
            Label emptyMessage = (Label) card.getRoot().lookup("#emptyMessage");

            assertEquals(1, entriesContainer.getChildren().size());
            assertEquals("21:00-22:00", ((Label) entriesContainer.getChildren().getFirst()
                    .lookup("#time")).getText());
            assertEquals("Unenrolled Timetable Check", ((Label) entriesContainer.getChildren().getFirst()
                    .lookup("#subject")).getText());
            assertEquals("", ((Label) entriesContainer.getChildren().getFirst()
                    .lookup("#students")).getText());
            assertFalse(emptyMessage.isVisible());
            assertFalse(emptyMessage.isManaged());
        });
    }

    private static void runOnFxThread(Runnable assertions) throws Exception {
        FutureTask<Void> task = new FutureTask<>(assertions, null);
        Platform.runLater(task);
        task.get(10, TimeUnit.SECONDS);
    }
}
