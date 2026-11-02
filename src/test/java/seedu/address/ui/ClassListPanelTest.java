package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import seedu.address.model.lesson.ClassDetails;
import seedu.address.model.lesson.ClassId;
import seedu.address.model.lesson.ClassMode;
import seedu.address.model.lesson.Cost;
import seedu.address.model.lesson.LessonTiming;
import seedu.address.model.lesson.Subject;
import seedu.address.model.lesson.TuitionClass;

public class ClassListPanelTest {

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
    public void constructor_classes_populatesListAndRendersNonEmptyAndEmptyCells() throws Exception {
        TuitionClass tuitionClass = new TuitionClass(new ClassId("C001"), new Subject("Math"),
                new Cost("30"), new LessonTiming("Monday1800-1930"));

        runOnFxThread(() -> {
            ClassListPanel panel = new ClassListPanel(FXCollections.observableArrayList(tuitionClass));
            @SuppressWarnings("unchecked")
            ListView<TuitionClass> listView = (ListView<TuitionClass>) panel.getRoot().lookup("#classListView");

            assertNotNull(listView);
            assertEquals(java.util.List.of(tuitionClass), listView.getItems());
            StackPane sceneRoot = new StackPane(panel.getRoot());
            new Scene(sceneRoot, 500, 400);
            sceneRoot.applyCss();
            sceneRoot.layout();

            @SuppressWarnings("unchecked")
            ListCell<TuitionClass> populatedCell = (ListCell<TuitionClass>) listView.lookupAll(".list-cell")
                    .stream().map(node -> (ListCell<TuitionClass>) node)
                    .filter(cell -> tuitionClass.equals(cell.getItem())).findFirst().orElseThrow();
            assertNotNull(populatedCell.getGraphic());

            ListCell<TuitionClass> emptyCell = listView.getCellFactory().call(listView);
            assertNull(emptyCell.getGraphic());
            assertNull(emptyCell.getText());
        });
    }

    @Test
    @SuppressWarnings("unchecked")
    public void privacyChanges_maskClassLinksAndLocationsInList() throws Exception {
        TuitionClass tuitionClass = new TuitionClass(new ClassId("C001"), new Subject("Math"),
                new Cost("30"), new LessonTiming("Monday1800-1930"),
                new ClassDetails(ClassMode.ONLINE, "https://example.com/math", "Room 2", "Bring notes"));

        runOnFxThread(() -> {
            SimpleBooleanProperty privacyModeEnabled = new SimpleBooleanProperty();
            ClassListPanel panel = new ClassListPanel(FXCollections.observableArrayList(tuitionClass),
                    privacyModeEnabled);
            ListView<TuitionClass> listView = (ListView<TuitionClass>) panel.getRoot().lookup("#classListView");
            StackPane sceneRoot = new StackPane(panel.getRoot());
            new Scene(sceneRoot, 500, 400);
            sceneRoot.applyCss();
            sceneRoot.layout();

            ListCell<TuitionClass> cell = (ListCell<TuitionClass>) listView.lookupAll(".list-cell").stream()
                    .map(node -> (ListCell<TuitionClass>) node)
                    .filter(candidate -> tuitionClass.equals(candidate.getItem())).findFirst().orElseThrow();
            Text details = (Text) cell.getGraphic().lookup("#details");
            privacyModeEnabled.set(true);
            assertEquals("Mode: Online | Link: " + ClassDetailsFormatter.PRIVACY_MASK + " | Location: "
                    + ClassDetailsFormatter.PRIVACY_MASK + " | Notes: Bring notes", details.getText());
            privacyModeEnabled.set(false);
            assertEquals(tuitionClass.getDetails().toString(), details.getText());
        });
    }

    private static void runOnFxThread(Runnable assertions) throws Exception {
        FutureTask<Void> task = new FutureTask<>(assertions, null);
        Platform.runLater(task);
        task.get(10, TimeUnit.SECONDS);
    }
}
