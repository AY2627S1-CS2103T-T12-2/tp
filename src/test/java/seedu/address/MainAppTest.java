package seedu.address;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.model.AddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.testutil.PersonBuilder;

/** Verifies startup persistence wiring without writing to the user's application files. */
public class MainAppTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void defaultLocations_useActualCodeSourceAndLaunchDirectory() throws Exception {
        MainApp app = new MainApp();
        assertEquals(Path.of(MainApp.class.getProtectionDomain().getCodeSource().getLocation().toURI()),
                app.getCodeLocation());
        assertEquals(Path.of(""), app.getWorkingDirectory());
    }

    @Test
    public void init_packagedApplication_migratesAndReopensSameDataFromDifferentLaunchDirectory() throws Exception {
        Path home = temporaryFolder.resolve("app folder");
        Path launch = temporaryFolder.resolve("old launch");
        AddressBook data = new AddressBook();
        data.addPerson(new PersonBuilder().withName("Migrated Student").build());
        new JsonAddressBookStorage(launch.resolve("data/addressbook.json")).saveAddressBook(data);
        UserPrefs prefs = new UserPrefs();
        prefs.setPrivacyModeEnabled(true);
        new JsonUserPrefsStorage(launch.resolve("preferences.json")).saveUserPrefs(prefs);

        MainApp app = appAt(home.resolve("tuteasy.jar"), launch);
        app.init();

        assertEquals(home.resolve("data/addressbook.json"), app.storage.getAddressBookFilePath());
        assertEquals(home.resolve("preferences.json"), app.storage.getUserPrefsFilePath());
        assertEquals(data, app.model.getAddressBook());
        assertTrue(app.logic.isPrivacyModeEnabled());
        assertNotNull(app.ui);
        assertTrue(Files.exists(launch.resolve("data/addressbook.json")));
        app.stop();

        MainApp restarted = appAt(home.resolve("tuteasy.jar"), temporaryFolder.resolve("new launch"));
        restarted.init();
        assertEquals(data, restarted.model.getAddressBook());
        assertTrue(restarted.logic.isPrivacyModeEnabled());
        assertEquals(app.storage.getAddressBookFilePath(), restarted.storage.getAddressBookFilePath());
    }

    @Test
    public void init_developmentApplication_usesLaunchDirectoryAndCreatesDefaultPreferences() throws Exception {
        Path launch = temporaryFolder.resolve("development");
        MainApp app = appAt(temporaryFolder.resolve("classes"), launch);
        app.init();
        assertEquals(launch.resolve("data/addressbook.json"), app.storage.getAddressBookFilePath());
        assertTrue(app.storage.readUserPrefs().isPresent());
        assertFalse(app.model.getAddressBook().getPersonList().isEmpty());
    }

    private MainApp appAt(Path codeLocation, Path launchDirectory) {
        return new MainApp() {
            @Override
            Path getCodeLocation() {
                return codeLocation;
            }

            @Override
            Path getWorkingDirectory() {
                return launchDirectory;
            }
        };
    }
}
