package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class ApplicationPathsTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void migrateLegacyFiles_destinationBlocked_failsAndRetainsOriginal() throws Exception {
        Path home = temporaryFolder.resolve("app");
        Path legacy = temporaryFolder.resolve("launch");
        Files.createDirectories(home);
        Files.writeString(home.resolve("data"), "blocked directory");
        Files.createDirectories(legacy.resolve("data"));
        Files.writeString(legacy.resolve("data/addressbook.json"), "original data");
        assertThrows(IOException.class, () -> ApplicationPaths.migrateLegacyFiles(home, legacy));
        assertEquals("original data", Files.readString(legacy.resolve("data/addressbook.json")));
    }

    @Test
    public void resolveHome_jarWithSpaces_ignoresLaunchDirectory() {
        Path home = temporaryFolder.resolve("app folder");
        assertEquals(home, ApplicationPaths.resolveHome(home.resolve("tuteasy.jar"),
                temporaryFolder.resolve("other")));
        assertEquals(temporaryFolder, ApplicationPaths.resolveHome(home.resolve("classes"), temporaryFolder));
    }

    @Test
    public void migrateLegacyFiles_copiesWithoutDeletingOrOverwriting() throws Exception {
        Path home = temporaryFolder.resolve("app");
        Path legacy = temporaryFolder.resolve("launch");
        Files.createDirectories(legacy.resolve("data"));
        Files.writeString(legacy.resolve("data/addressbook.json"), "original data");
        Files.writeString(legacy.resolve("preferences.json"), "original preferences");
        ApplicationPaths.migrateLegacyFiles(home, legacy);
        assertEquals("original data", Files.readString(home.resolve("data/addressbook.json")));
        assertEquals("original preferences", Files.readString(home.resolve("preferences.json")));
        assertEquals("original data", Files.readString(legacy.resolve("data/addressbook.json")));
        Files.writeString(home.resolve("data/addressbook.json"), "new data");
        Files.writeString(home.resolve("preferences.json"), "new preferences");
        ApplicationPaths.migrateLegacyFiles(home, legacy);
        assertEquals("new data", Files.readString(home.resolve("data/addressbook.json")));
        assertEquals("new preferences", Files.readString(home.resolve("preferences.json")));
    }

    @Test
    public void migrateLegacyFiles_noLegacyFiles_doesNotCreateFiles() throws Exception {
        ApplicationPaths.migrateLegacyFiles(temporaryFolder.resolve("app"), temporaryFolder);
        assertFalse(Files.exists(temporaryFolder.resolve("app/data/addressbook.json")));
        ApplicationPaths.migrateLegacyFiles(temporaryFolder, temporaryFolder);
    }
}
