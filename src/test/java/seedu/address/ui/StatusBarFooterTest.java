package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.control.Label;

public class StatusBarFooterTest {

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
    public void setPrivacyModeEnabled_updatesPersistentIndicator() throws Exception {
        runOnFxThread(() -> {
            StatusBarFooter footer = new StatusBarFooter(Path.of("data", "addressbook.json"), false);
            Label privacyStatus = (Label) footer.getRoot().lookup("#privacyStatus");

            assertNotNull(privacyStatus);
            assertEquals(StatusBarFooter.PRIVACY_STATUS_TEXT, privacyStatus.getText());
            assertFalse(privacyStatus.isVisible());
            assertFalse(privacyStatus.isManaged());

            footer.setPrivacyModeEnabled(true);

            assertTrue(privacyStatus.isVisible());
            assertTrue(privacyStatus.isManaged());

            footer.setPrivacyModeEnabled(false);

            assertFalse(privacyStatus.isVisible());
            assertFalse(privacyStatus.isManaged());
        });
    }

    @Test
    public void constructor_withoutPrivacyState_hidesIndicator() throws Exception {
        runOnFxThread(() -> {
            StatusBarFooter footer = new StatusBarFooter(Path.of("data", "addressbook.json"));
            Label privacyStatus = (Label) footer.getRoot().lookup("#privacyStatus");

            assertNotNull(privacyStatus);
            assertFalse(privacyStatus.isVisible());
            assertFalse(privacyStatus.isManaged());
        });
    }

    private static void runOnFxThread(Runnable assertions) throws Exception {
        FutureTask<Void> task = new FutureTask<>(assertions, null);
        Platform.runLater(task);
        task.get(10, TimeUnit.SECONDS);
    }
}
