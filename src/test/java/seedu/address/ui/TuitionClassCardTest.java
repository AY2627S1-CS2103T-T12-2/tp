package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.control.Label;
import seedu.address.model.lesson.ClassDetails;
import seedu.address.model.lesson.ClassId;
import seedu.address.model.lesson.ClassMode;
import seedu.address.model.lesson.Cost;
import seedu.address.model.lesson.LessonTiming;
import seedu.address.model.lesson.Subject;
import seedu.address.model.lesson.TuitionClass;

public class TuitionClassCardTest {

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
    public void constructor_validClass_displaysAllClassDetails() throws Exception {
        TuitionClass tuitionClass = new TuitionClass(new ClassId("C001"), new Subject("Math"),
                new Cost("30"), new LessonTiming("Monday1800-1930"));

        runOnFxThread(() -> {
            TuitionClassCard card = new TuitionClassCard(tuitionClass);

            assertLabel(card, "classId", "C001");
            assertLabel(card, "subject", "Math");
            assertLabel(card, "schedule", "Monday 18:00–19:30");
            assertLabel(card, "cost", "Cost: 30");
        });
    }

    @Test
    public void constructor_onlineClass_displaysStoredAttributes() throws Exception {
        ClassDetails details = new ClassDetails(ClassMode.ONLINE, "https://example.com/math", "Room 2", "Bring notes");
        TuitionClass tuitionClass = new TuitionClass(new ClassId("C001"), new Subject("Math"),
                new Cost("30"), new LessonTiming("Monday1800-1930"), details);
        runOnFxThread(() -> {
            TuitionClassCard card = new TuitionClassCard(tuitionClass);
            assertLabel(card, "details", details.toString());
        });
    }

    private static void assertLabel(TuitionClassCard card, String id, String expectedText) {
        Label label = (Label) card.getRoot().lookup("#" + id);
        assertNotNull(label);
        assertEquals(expectedText, label.getText());
    }

    private static void runOnFxThread(Runnable assertions) throws Exception {
        FutureTask<Void> task = new FutureTask<>(assertions, null);
        Platform.runLater(task);
        task.get(10, TimeUnit.SECONDS);
    }
}
