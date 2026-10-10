package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.DayOfWeek;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
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
    public void formatDay_dayOfWeek_returnsCapitalisedEnglishName() {
        assertEquals("Monday", TimetableDayCard.formatDay(DayOfWeek.MONDAY));
        assertEquals("Saturday", TimetableDayCard.formatDay(DayOfWeek.SATURDAY));
    }

    @Test
    public void constructor_emptyAndPopulatedDays_displayCorrectContent() throws Exception {
        Lesson lesson = new Lesson(new Subject("Math"), new Cost("30"),
                new LessonTiming("Monday1800-1930"));
        Person student = new PersonBuilder().build();
        TimetableEntry entry = new TimetableEntry(lesson, List.of(student));

        runOnFxThread(() -> {
            TimetableDayCard emptyCard = new TimetableDayCard(
                    new TimetableDay(DayOfWeek.MONDAY, List.of()));
            TimetableDayCard populatedCard = new TimetableDayCard(
                    new TimetableDay(DayOfWeek.MONDAY, List.of(entry)));

            Label dayHeading = (Label) emptyCard.getRoot().lookup("#dayHeading");
            Label emptyMessage = (Label) emptyCard.getRoot().lookup("#emptyMessage");
            VBox entriesContainer = (VBox) populatedCard.getRoot().lookup("#entriesContainer");
            Label populatedMessage = (Label) populatedCard.getRoot().lookup("#emptyMessage");

            assertNotNull(dayHeading);
            assertEquals("Monday", dayHeading.getText());
            assertTrue(emptyMessage.isVisible());
            assertTrue(emptyMessage.isManaged());
            assertEquals(1, entriesContainer.getChildren().size());
            assertFalse(populatedMessage.isVisible());
            assertFalse(populatedMessage.isManaged());
        });
    }

    private static void runOnFxThread(Runnable assertions) throws Exception {
        FutureTask<Void> task = new FutureTask<>(assertions, null);
        Platform.runLater(task);
        task.get(10, TimeUnit.SECONDS);
    }
}
