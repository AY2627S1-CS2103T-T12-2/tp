package seedu.address.commons.util;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFileAttributeView;

/**
 * Writes and reads files
 */
public class FileUtil {

    private static final String CHARSET = "UTF-8";

    /**
     * Creates a file if it does not exist along with its missing parent directories.
     * @throws IOException if the file or directory cannot be created.
     */
    public static void createIfMissing(Path file) throws IOException {
        if (Files.exists(file)) {
            return;
        }

        createParentDirsOfFile(file);

        Files.createFile(file);
    }

    /**
     * Creates parent directories of file if it has a parent directory
     */
    private static void createParentDirsOfFile(Path file) throws IOException {
        Path parentDir = file.getParent();

        if (parentDir != null) {
            Files.createDirectories(parentDir);
        }
    }

    /**
     * Assumes file exists
     */
    public static String readFromFile(Path file) throws IOException {
        return new String(Files.readAllBytes(file), CHARSET);
    }

    /**
     * Writes to a sibling temporary file and atomically replaces the destination after the write succeeds.
     * Existing data is retained if staging or replacement fails. Unsupported atomic moves fail safely.
     */
    public static void writeToFile(Path file, String content) throws IOException {
        writeToFile(file, content, Files::write);
    }

    /** Allows a write failure to be injected at the staging boundary without exposing the destination. */
    static void writeToFile(Path file, String content, ContentWriter writer) throws IOException {
        Path target = file.toAbsolutePath().normalize();
        if (Files.exists(target) || Files.isSymbolicLink(target)) {
            target = target.toRealPath();
            if (!Files.isWritable(target)) {
                throw new AccessDeniedException(target.toString());
            }
        }
        createParentDirsOfFile(target);
        Path temporary = Files.createTempFile(target.getParent(), ".tuteasy-", ".tmp");
        try {
            writer.write(temporary, content.getBytes(CHARSET));
            if (Files.exists(target) && Files.getFileAttributeView(target, PosixFileAttributeView.class) != null) {
                Files.setPosixFilePermissions(temporary, Files.getPosixFilePermissions(target));
            }
            Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    @FunctionalInterface
    interface ContentWriter {
        void write(Path file, byte[] content) throws IOException;
    }
}
