package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.control.Label;
import seedu.address.model.lesson.Cost;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonTiming;
import seedu.address.model.lesson.Subject;
import seedu.address.model.person.Person;
import seedu.address.model.timetable.TimetableEntry;
import seedu.address.testutil.PersonBuilder;

public class TimetableEntryCardTest {

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
    public void formatters_validEntry_returnDisplayText() {
        Lesson lesson = new Lesson(new Subject("Math"), new Cost("30"),
                new LessonTiming("Monday1800-1930"));
        Person zoe = new PersonBuilder().withName("Zoe Tan").build();
        Person alice = new PersonBuilder().withName("Alice Tan").withEmail("alice@example.com")
                .withPhone("91234567").build();
        TimetableEntry entry = new TimetableEntry(lesson, List.of(zoe, alice));

        assertEquals("18:00-19:30", TimetableEntryCard.formatTimeRange(entry));
        assertEquals("Alice Tan, Zoe Tan", TimetableEntryCard.formatStudents(entry));
    }

    @Test
    public void constructor_validEntry_displaysLessonDetails() throws Exception {
        Lesson lesson = new Lesson(new Subject("Math"), new Cost("30"),
                new LessonTiming("Monday1800-1930"));
        Person student = new PersonBuilder().withName("Alice Tan").build();
        TimetableEntry entry = new TimetableEntry(lesson, List.of(student));

        runOnFxThread(() -> {
            TimetableEntryCard card = new TimetableEntryCard(entry);

            assertLabel(card, "time", "18:00-19:30");
            assertLabel(card, "subject", "Math");
            assertLabel(card, "students", "Alice Tan");
        });
    }

    private static void assertLabel(TimetableEntryCard card, String id, String expectedText) {
        Label label = (Label) card.getRoot().lookup("#" + id);
        assertNotNull(label, "Missing FXML label: " + id);
        assertEquals(expectedText, label.getText());
    }

    private static void runOnFxThread(Runnable assertions) throws Exception {
        FutureTask<Void> task = new FutureTask<>(assertions, null);
        Platform.runLater(task);
        task.get(10, TimeUnit.SECONDS);
    }
}
