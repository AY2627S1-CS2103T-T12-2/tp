package seedu.address.commons.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Resolves persistent files independently of the launch directory for packaged applications. */
public final class ApplicationPaths {
    private ApplicationPaths() {
    }

    /** Uses the JAR's parent directory, or the working directory during development. */
    public static Path resolveHome(Path codeLocation, Path workingDirectory) {
        Path location = codeLocation.toAbsolutePath().normalize();
        return location.getFileName().toString().endsWith(".jar")
                ? location.getParent() : workingDirectory.toAbsolutePath().normalize();
    }

    /**
     * Copies legacy files only when their destinations are absent, preserving the originals.
     * Failure aborts startup rather than silently opening an unrelated empty data set.
     */
    public static void migrateLegacyFiles(Path applicationHome, Path workingDirectory) throws IOException {
        Path home = applicationHome.toAbsolutePath().normalize();
        Path legacy = workingDirectory.toAbsolutePath().normalize();
        if (home.equals(legacy)) {
            return;
        }
        for (String relative : List.of("data/addressbook.json", "preferences.json")) {
            Path source = legacy.resolve(relative);
            Path target = home.resolve(relative);
            if (!Files.exists(target) && Files.isRegularFile(source)) {
                Files.createDirectories(target.getParent());
                Files.copy(source, target);
            }
        }
    }
}
