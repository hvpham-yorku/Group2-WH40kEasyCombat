package eecs2311.group2.wh40k_easycombat.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

public final class BundledResourceManager {
    private static final ClassLoader CLASS_LOADER = BundledResourceManager.class.getClassLoader();
    private static final Module MODULE = BundledResourceManager.class.getModule();

    private BundledResourceManager() {
    }

    public static synchronized void ensureBundledResourcesAvailable() throws IOException {
        AppPaths.ensureRuntimeDirectories();
        syncDirectory("csv", AppPaths.getBundledCsvDirectory());
        syncDirectory("dsl", AppPaths.getBundledDslDirectory());
    }

    private static void syncDirectory(String resourceRoot, Path targetDirectory) throws IOException {
        List<String> relativeFiles = listRelativeResourceFiles(resourceRoot);
        if (relativeFiles.isEmpty()) {
            throw new IOException("Bundled resource folder is empty or missing: " + resourceRoot);
        }

        Files.createDirectories(targetDirectory);
        Set<Path> expectedFiles = new HashSet<>();

        for (String relativeFile : relativeFiles) {
            Path relativePath = Path.of(relativeFile.replace('/', java.io.File.separatorChar));
            Path targetFile = targetDirectory.resolve(relativePath);
            expectedFiles.add(relativePath.normalize());

            Path parent = targetFile.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            syncFile(resourceRoot, relativeFile, targetFile);
        }

        deleteUnexpectedFiles(targetDirectory, expectedFiles);
    }

    private static void syncFile(String resourceRoot, String relativeFile, Path targetFile) throws IOException {
        byte[] bundledBytes = readBundledBytes(resourceRoot, relativeFile);
        if (Files.exists(targetFile) && hasSameContent(targetFile, bundledBytes)) {
            return;
        }

        try {
            writeFile(targetFile, bundledBytes);
        } catch (IOException e) {
            if (Files.exists(targetFile)) {
                System.err.println("[WARN] Keeping existing bundled resource because it could not be updated: "
                        + targetFile + " (" + e.getMessage() + ")");
                return;
            }
            throw e;
        }
    }

    private static byte[] readBundledBytes(String resourceRoot, String relativeFile) throws IOException {
        try (InputStream stream = MODULE.getResourceAsStream(resourceRoot + "/" + relativeFile)) {
            if (stream == null) {
                throw new IOException("Bundled resource not found: " + resourceRoot + "/" + relativeFile);
            }
            return stream.readAllBytes();
        }
    }

    private static boolean hasSameContent(Path targetFile, byte[] bundledBytes) throws IOException {
        return Arrays.equals(Files.readAllBytes(targetFile), bundledBytes);
    }

    private static void writeFile(Path targetFile, byte[] bundledBytes) throws IOException {
        Path tempFile = Files.createTempFile(targetFile.getParent(), targetFile.getFileName().toString(), ".tmp");
        try {
            Files.write(tempFile, bundledBytes);
            try {
                Files.move(tempFile, targetFile,
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException firstMoveFailure) {
                Files.move(tempFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    private static void deleteUnexpectedFiles(Path targetDirectory, Set<Path> expectedFiles) throws IOException {
        if (!Files.isDirectory(targetDirectory)) {
            return;
        }

        try (Stream<Path> paths = Files.walk(targetDirectory)) {
            List<Path> filesToDelete = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> !expectedFiles.contains(targetDirectory.relativize(path).normalize()))
                    .toList();

            for (Path file : filesToDelete) {
                try {
                    Files.deleteIfExists(file);
                } catch (IOException e) {
                    System.err.println("[WARN] Failed to delete outdated bundled resource: "
                            + file + " (" + e.getMessage() + ")");
                }
            }
        }

        try (Stream<Path> paths = Files.walk(targetDirectory)) {
            List<Path> directories = paths
                    .filter(Files::isDirectory)
                    .sorted(Comparator.reverseOrder())
                    .toList();

            for (Path directory : directories) {
                if (!directory.equals(targetDirectory) && isDirectoryEmpty(directory)) {
                    try {
                        Files.deleteIfExists(directory);
                    } catch (IOException e) {
                        System.err.println("[WARN] Failed to delete outdated bundled resource directory: "
                                + directory + " (" + e.getMessage() + ")");
                    }
                }
            }
        }
    }

    private static boolean isDirectoryEmpty(Path directory) throws IOException {
        try (Stream<Path> paths = Files.list(directory)) {
            return paths.findAny().isEmpty();
        }
    }

    private static List<String> listRelativeResourceFiles(String resourceRoot) throws IOException {
        URL rootUrl = CLASS_LOADER.getResource(resourceRoot);
        if (rootUrl == null) {
            return List.of();
        }

        return switch (rootUrl.getProtocol()) {
            case "file" -> listRelativeFilesFromDirectory(rootUrl);
            case "jar" -> listRelativeFilesFromJar(rootUrl);
            default -> throw new IOException("Unsupported resource protocol: " + rootUrl.getProtocol());
        };
    }

    private static List<String> listRelativeFilesFromDirectory(URL rootUrl) throws IOException {
        try {
            Path rootPath = Paths.get(rootUrl.toURI());
            try (Stream<Path> paths = Files.walk(rootPath)) {
                return paths
                        .filter(Files::isRegularFile)
                        .map(rootPath::relativize)
                        .map(path -> path.toString().replace('\\', '/'))
                        .sorted()
                        .toList();
            }
        } catch (URISyntaxException e) {
            throw new IOException("Failed to resolve bundled resource directory: " + rootUrl, e);
        }
    }

    private static List<String> listRelativeFilesFromJar(URL rootUrl) throws IOException {
        JarURLConnection connection = (JarURLConnection) rootUrl.openConnection();
        JarFile jarFile = connection.getJarFile();
        String prefix = connection.getEntryName();
        if (prefix == null || prefix.isBlank()) {
            return List.of();
        }

        if (!prefix.endsWith("/")) {
            prefix += "/";
        }

        List<String> results = new ArrayList<>();
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (!entry.isDirectory() && entry.getName().startsWith(prefix)) {
                results.add(entry.getName().substring(prefix.length()));
            }
        }

        results.sort(String::compareTo);
        return results;
    }
}
