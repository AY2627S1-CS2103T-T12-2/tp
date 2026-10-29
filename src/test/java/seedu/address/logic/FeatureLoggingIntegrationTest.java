package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.ModelManager;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/** Checks that diagnostics remain useful without copying private inputs into logs. */
public class FeatureLoggingIntegrationTest {

    @TempDir
    public Path temporaryFolder;

    @Test
    public void featureCommands_logEventsWithoutContactDetailsOrSearchTerms() throws Exception {
        ModelManager model = new ModelManager();
        JsonAddressBookStorage bookStorage = new JsonAddressBookStorage(temporaryFolder.resolve("students.json"));
        LogicManager logic = new LogicManager(model, new StorageManager(bookStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json"))));
        Logger logger = Logger.getLogger("tuteasy");
        Level originalLevel = logger.getLevel();
        List<String> messages = new ArrayList<>();
        Handler capture = new Handler() {
            @Override
            public void publish(LogRecord record) {
                messages.add(record.getMessage());
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        };
        logger.addHandler(capture);
        logger.setLevel(Level.ALL);
        try {
            String contact = " n/Private Student p/98887777 e/private@example.com a/Secret Street"
                    + " gn/Private Guardian gp/97776666 ge/guardian@example.com";
            logic.execute("add" + contact + " sub/PrivateSubject cost/30 c/Thursday1000-1100"
                    + " mode/online link/https://private.example.com location/Secret Room notes/Secret Notes");
            assertThrows(CommandException.class, () -> logic.execute("add" + contact));
            logic.execute("find sub/PrivateSubject");
            bookStorage.readAddressBook();

            String logged = String.join("\n", messages);
            assertTrue(logged.contains("Student created"));
            assertTrue(logged.contains("duplicate name"));
            assertTrue(logged.contains("Parsed subject search"));
            assertTrue(logged.contains("Loaded 1 student"));
            for (String secret : List.of("Private Student", "98887777", "private@example.com", "Secret Street",
                    "Private Guardian", "97776666", "guardian@example.com", "PrivateSubject",
                    "https://private.example.com", "Secret Room", "Secret Notes")) {
                assertFalse(logged.contains(secret), "Private value appeared in logs: " + secret);
            }
        } finally {
            logger.removeHandler(capture);
            logger.setLevel(originalLevel);
        }
    }
}
