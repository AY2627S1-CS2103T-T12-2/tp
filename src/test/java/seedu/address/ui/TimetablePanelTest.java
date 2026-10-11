package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.DayOfWeek;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import seedu.address.model.timetable.TimetableDay;

public class TimetablePanelTest {

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
    public void constructor_timetableDays_populatesListAndCellFactory() throws Exception {
        TimetableDay monday = new TimetableDay(DayOfWeek.MONDAY, List.of());

        runOnFxThread(() -> {
            TimetablePanel panel = new TimetablePanel(List.of(monday));
            @SuppressWarnings("unchecked")
            ListView<TimetableDay> listView = (ListView<TimetableDay>) panel.getRoot()
                    .lookup("#timetableDayListView");

            assertNotNull(listView);
            assertEquals(List.of(monday), listView.getItems());
            ListCell<TimetableDay> cell = listView.getCellFactory().call(listView);
            assertNotNull(cell);
            cell.updateIndex(0);
        });
    }

    private static void runOnFxThread(Runnable assertions) throws Exception {
        FutureTask<Void> task = new FutureTask<>(assertions, null);
        Platform.runLater(task);
        task.get(10, TimeUnit.SECONDS);
    }
}
