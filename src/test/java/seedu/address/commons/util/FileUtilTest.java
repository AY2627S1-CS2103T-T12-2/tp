package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class FileUtilTest {
    @TempDir
    public Path directory;

    @Test
    public void writeToFile_createsNestedFileAndReplacesContents() throws Exception {
        Path target = directory.resolve("nested/students.json");
        FileUtil.writeToFile(target, "First — 初");
        assertEquals("First — 初", Files.readString(target));
        FileUtil.writeToFile(target, "Second");
        assertEquals("Second", Files.readString(target));
        assertOnlyEntry(target.getParent(), target);
    }

    @Test
    public void writeToFile_partialStagingFailure_preservesOriginalAndCleansTemporaryFile() throws Exception {
        Path target = directory.resolve("students.json");
        Files.writeString(target, "original data");
        assertThrows(IOException.class, () -> FileUtil.writeToFile(target, "replacement", (staging, bytes) -> {
            assertEquals(target.getParent().toRealPath(), staging.getParent().toRealPath());
            assertFalse(target.equals(staging));
            Files.writeString(staging, "partial");
            throw new IOException("Simulated disk-write failure");
        }));
        assertEquals("original data", Files.readString(target));
        assertOnlyEntry(directory, target);
    }

    @Test
    public void writeToFile_failedFirstWrite_doesNotCreateEmptyDestination() throws Exception {
        Path target = directory.resolve("students.json");
        assertThrows(IOException.class, () -> FileUtil.writeToFile(target, "new data", (staging, bytes) -> {
            Files.writeString(staging, "partial");
            throw new IOException("Simulated disk-write failure");
        }));
        assertFalse(Files.exists(target));
        try (var entries = Files.list(directory)) {
            assertEquals(0, entries.count());
        }
    }

    @Test
    public void writeToFile_failedReplacement_preservesDestinationAndCleansTemporaryFile() throws Exception {
        Path target = Files.createDirectory(directory.resolve("destination"));
        Path child = Files.writeString(target.resolve("keep.txt"), "original data");
        assertThrows(IOException.class, () -> FileUtil.writeToFile(target, "replacement"));
        assertEquals("original data", Files.readString(child));
        assertOnlyEntry(directory, target);
    }

    @Test
    public void writeToFile_symbolicLink_retainsLinkAndUpdatesItsTarget() throws Exception {
        assumeFalse(System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("windows"));
        Path target = Files.writeString(directory.resolve("real.json"), "original data");
        Path link = Files.createSymbolicLink(directory.resolve("link.json"), target.getFileName());
        FileUtil.writeToFile(link, "replacement");
        assertTrue(Files.isSymbolicLink(link));
        assertEquals("replacement", Files.readString(target));
        assertEquals("replacement", Files.readString(link));
    }

    @Test
    public void writeToFile_existingPermissions_preserved() throws Exception {
        Path target = Files.writeString(directory.resolve("students.json"), "original data");
        assumeTrue(Files.getFileAttributeView(target, PosixFileAttributeView.class) != null);
        var permissions = PosixFilePermissions.fromString("rw-r-----");
        Files.setPosixFilePermissions(target, permissions);
        FileUtil.writeToFile(target, "replacement");
        assertEquals(permissions, Files.getPosixFilePermissions(target));
    }

    @Test
    public void writeToFile_readOnlyDestination_rejectedWithoutChangingData() throws Exception {
        Path target = Files.writeString(directory.resolve("students.json"), "original data");
        assumeTrue(Files.getFileAttributeView(target, PosixFileAttributeView.class) != null);
        var originalPermissions = Files.getPosixFilePermissions(target);
        try {
            Files.setPosixFilePermissions(target, PosixFilePermissions.fromString("r--------"));
            assumeFalse(Files.isWritable(target));
            assertThrows(AccessDeniedException.class, () -> FileUtil.writeToFile(target, "replacement"));
            assertEquals("original data", Files.readString(target));
        } finally {
            Files.setPosixFilePermissions(target, originalPermissions);
        }
    }

    @Test
    public void failedAtomicSave_logicRestoresModelAndRetainsReadableSavedData() throws Exception {
        Path file = directory.resolve("students.json");
        ModelManager model = new ModelManager();
        JsonAddressBookStorage book = new JsonAddressBookStorage(file);
        JsonUserPrefsStorage prefs = new JsonUserPrefsStorage(directory.resolve("prefs.json"));
        LogicManager logic = new LogicManager(model, new StorageManager(book, prefs));
        logic.execute("add n/Alice p/91234567 e/alice@example.com a/One gp/99999999"
                + " sub/Math cost/30 c/Monday1800-1900 notes/Review");
        logic.execute("add n/Bob p/81234567 e/bob@example.com a/Two");
        logic.execute("find sub/Math");
        AddressBook original = new AddressBook(model.getAddressBook());
        var originalResults = List.copyOf(model.getFilteredPersonList());
        String originalJson = Files.readString(file);
        JsonAddressBookStorage failing = new JsonAddressBookStorage(file) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                FileUtil.writeToFile(file, "replacement", (staging, bytes) -> {
                    Files.writeString(staging, "partial");
                    throw new IOException("Simulated disk-write failure");
                });
            }
        };
        LogicManager failingLogic = new LogicManager(model, new StorageManager(failing, prefs));
        assertThrows(CommandException.class, () -> failingLogic.execute("edit 1 a/Changed"));
        assertEquals(original, model.getAddressBook());
        assertEquals(originalResults, model.getFilteredPersonList());
        assertEquals(originalJson, Files.readString(file));
        assertEquals(original, book.readAddressBook().orElseThrow());
        assertOnlyEntry(directory, file);
    }

    private void assertOnlyEntry(Path parent, Path expected) throws IOException {
        try (var entries = Files.list(parent)) {
            assertEquals(List.of(expected), entries.toList());
        }
    }
}
