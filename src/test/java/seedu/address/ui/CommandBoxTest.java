package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.TextField;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.parser.exceptions.ParseException;

public class CommandBoxTest {

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
    public void getCommandSuggestion_findInputs_returnsExpectedSuggestion() {
        assertEquals("", CommandBox.getCommandSuggestion(null));
        assertEquals("", CommandBox.getCommandSuggestion(""));
        assertEquals("find", CommandBox.getCommandSuggestion("fin"));
        assertEquals("find KEYWORD | find t/TAG | find c/DAY, DATE, or TIME",
                CommandBox.getCommandSuggestion("find"));
        assertEquals("find KEYWORD | find t/TAG | find c/DAY, DATE, or TIME",
                CommandBox.getCommandSuggestion("find "));
        assertEquals("find t/TAG (e.g. priority)", CommandBox.getCommandSuggestion("find t/"));
        assertEquals("find c/DAY, DATE, or TIME (e.g. Monday 1800; 2026-10-20; time uses HHmm)",
                CommandBox.getCommandSuggestion("find c/"));
        assertEquals("t/TAG (e.g. priority)", CommandBox.getCommandSuggestion("t/"));
        assertEquals("c/DAY, DATE, or TIME (e.g. Monday 1800; 2026-10-20; time uses HHmm)",
                CommandBox.getCommandSuggestion("c/"));
        assertEquals("", CommandBox.getCommandSuggestion("list"));
    }

    @Test
    public void isFindCommand() {
        assertTrue(CommandBox.isFindCommand("find"));
        assertTrue(CommandBox.isFindCommand(" find Alex "));
        assertFalse(CommandBox.isFindCommand("findAlex"));
        assertFalse(CommandBox.isFindCommand("list"));
    }

    @Test
    public void commandBox_textChanges_updatesFindSuggestion() throws Exception {
        runOnFxThread(() -> {
            CommandBox commandBox = new CommandBox(commandText -> new CommandResult(""));
            TextField commandTextField = getCommandTextField(commandBox);
            TextField commandSuggestionTextField = getCommandSuggestionTextField(commandBox);

            assertEquals("", commandSuggestionTextField.getText());

            commandTextField.setText("fin");
            assertEquals("find", commandSuggestionTextField.getText());

            commandTextField.setText("find t/");
            assertEquals("find t/TAG (e.g. priority)", commandSuggestionTextField.getText());
        });
    }

    @Test
    public void handleCommandEntered_emptyCommand_doesNotExecute() throws Exception {
        runOnFxThread(() -> {
            AtomicInteger executeCount = new AtomicInteger();
            CommandBox commandBox = new CommandBox(commandText -> {
                executeCount.incrementAndGet();
                return new CommandResult("");
            });

            getCommandTextField(commandBox).fireEvent(new ActionEvent());

            assertEquals(0, executeCount.get());
        });
    }

    @Test
    public void handleCommandEntered_findCommand_showsReturnHomeSuggestion() throws Exception {
        runOnFxThread(() -> {
            AtomicReference<String> executedCommand = new AtomicReference<>();
            CommandBox commandBox = new CommandBox(commandText -> {
                executedCommand.set(commandText);
                return new CommandResult("");
            });
            TextField commandTextField = getCommandTextField(commandBox);
            TextField commandSuggestionTextField = getCommandSuggestionTextField(commandBox);

            commandTextField.setText("find Alex");
            commandTextField.fireEvent(new ActionEvent());

            assertEquals("find Alex", executedCommand.get());
            assertEquals("", commandTextField.getText());
            assertEquals("Type 'list' to return to the home page.", commandSuggestionTextField.getText());
        });
    }

    @Test
    public void handleCommandEntered_nonFindCommand_clearsSuggestion() throws Exception {
        runOnFxThread(() -> {
            CommandBox commandBox = new CommandBox(commandText -> new CommandResult(""));
            TextField commandTextField = getCommandTextField(commandBox);
            TextField commandSuggestionTextField = getCommandSuggestionTextField(commandBox);

            commandTextField.setText("list");
            commandTextField.fireEvent(new ActionEvent());

            assertEquals("", commandTextField.getText());
            assertEquals("", commandSuggestionTextField.getText());
        });
    }

    @Test
    public void handleCommandEntered_commandFailure_marksAndClearsErrorStyle() throws Exception {
        runOnFxThread(() -> {
            CommandBox commandBox = new CommandBox(commandText -> {
                throw new ParseException("bad command");
            });
            TextField commandTextField = getCommandTextField(commandBox);

            commandTextField.setText("find /");
            commandTextField.fireEvent(new ActionEvent());
            commandTextField.fireEvent(new ActionEvent());

            assertTrue(commandTextField.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));

            commandTextField.setText("find a");

            assertFalse(commandTextField.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
        });
    }

    private TextField getCommandTextField(CommandBox commandBox) {
        return (TextField) commandBox.getRoot().lookup("#commandTextField");
    }

    private TextField getCommandSuggestionTextField(CommandBox commandBox) {
        return (TextField) commandBox.getRoot().lookup("#commandSuggestionTextField");
    }

    private static void runOnFxThread(Runnable assertions) throws Exception {
        FutureTask<Void> task = new FutureTask<>(assertions, null);
        Platform.runLater(task);
        task.get(10, TimeUnit.SECONDS);
    }
}
