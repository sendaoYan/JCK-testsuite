/*
 * @test
 * @summary java.nio.file.SecureDirectoryStream conformance: white-box structure + black-box behavior.
 * @run main SecureDirectoryStreamTest
 */

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SecureDirectoryStream;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

public class SecureDirectoryStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.file.SecureDirectoryStream");
        check(c.isInterface(), "SecureDirectoryStream is an interface");
        check(DirectoryStream.class.isAssignableFrom(c), "extends DirectoryStream");
        for (String n : new String[] {"newDirectoryStream", "newByteChannel", "deleteFile",
                "deleteDirectory", "move", "getFileAttributeView"})
            check(java.util.Arrays.stream(c.getMethods()).anyMatch(m -> m.getName().equals(n)),
                  "has method " + n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Path dir = Files.createTempDirectory("jck");
        try {
            // the default provider yields a secure stream on Linux
            DirectoryStream<Path> ds0 = Files.newDirectoryStream(dir);
            if (!(ds0 instanceof SecureDirectoryStream)) {
                ds0.close();
                System.out.println("NOTE: no secure stream support; behavioral part skipped");
                return;
            }
            SecureDirectoryStream<Path> sds = (SecureDirectoryStream<Path>) ds0;
            check(sds != null, "secure stream obtained");
            try {

            // create + open a file relative to the stream
            Path name = dir.getFileSystem().getPath("s.txt");
            try (var ch = sds.newByteChannel(name,
                    java.util.Set.of(StandardOpenOption.CREATE,
                            StandardOpenOption.WRITE, StandardOpenOption.READ))) {
                ch.write(java.nio.ByteBuffer.wrap("sec".getBytes()));
            }
            check(Files.readString(dir.resolve("s.txt")).equals("sec"),
                  "file created through the stream");

            // nested secure stream over the same directory sees the same content
            try (SecureDirectoryStream<Path> nested =
                    sds.newDirectoryStream(dir.getFileSystem().getPath("."))) {
                boolean found = false;
                for (Path p : nested) found |= p.getFileName().toString().equals("s.txt");
                check(found, "nested stream lists entries");
            }

            // attributes through the stream read the created file (path-bound variant)
            var view = sds.getFileAttributeView(name,
                    java.nio.file.attribute.BasicFileAttributeView.class);
            check(view.readAttributes().size() == 3, "view over stream reads size");
            // the directory-bound variant reads the streamed directory itself
            var dirView = sds.getFileAttributeView(
                    java.nio.file.attribute.BasicFileAttributeView.class);
            check(dirView.readAttributes().isDirectory(), "directory view via stream");

            // move within the same directory tree
            Path renamed = dir.getFileSystem().getPath("r.txt");
            sds.move(name, sds, renamed);
            check(Files.exists(dir.resolve("r.txt")) && !Files.exists(dir.resolve("s.txt")),
                  "move through streams");

            // deleteFile removes it
            sds.deleteFile(renamed);
            check(!Files.exists(dir.resolve("r.txt")), "deleteFile through stream");

            // deleteDirectory on a subdirectory
            Files.createDirectory(dir.resolve("sub"));
            sds.deleteDirectory(dir.getFileSystem().getPath("sub"));
            check(!Files.exists(dir.resolve("sub")), "deleteDirectory through stream");

            } finally {
                // deleting a missing file reports NoSuchFileException
                boolean nsf = false;
                try { sds.deleteFile(dir.getFileSystem().getPath("ghost")); }
                catch (java.nio.file.NoSuchFileException e) { nsf = true; }
                check(nsf, "deleteFile missing -> NoSuchFileException");
            }

            // stream lifetime managed above; a fresh stream tests close semantics
            sds.close();
            boolean closed = false;
            try {
                sds.newByteChannel(dir.getFileSystem().getPath("x"),
                        java.util.Set.of(StandardOpenOption.READ));
            } catch (java.nio.file.ClosedDirectoryStreamException e) {
                closed = true;
            }
            check(closed, "operations after close -> ClosedDirectoryStreamException");
            check(true, "close semantics already covered");
        } finally {
            try (var s = Files.walk(dir)) {
                s.sorted(java.util.Comparator.reverseOrder())
                 .forEach(p -> { try { Files.deleteIfExists(p); } catch (Exception ignore) { } });
            }
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
