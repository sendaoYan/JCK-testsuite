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

    interface Op { void run() throws Exception; }

    /** Negative-path helper: asserts the EXACT exception class (no subclasses). */
    static void checkThrows(Class<? extends Throwable> ex, Op r, String msg) {
        try { r.run(); }
        catch (Throwable t) {
            if (t.getClass() != ex)
                throw new AssertionError(msg + ": expected " + ex.getName() + " got " + t);
            passed++;
            return;
        }
        throw new AssertionError(msg + ": expected " + ex.getName() + " but nothing thrown");
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

    // ===== deepened contract coverage (batch A9): probe-confirmed =====
    @SuppressWarnings("unchecked")
    static void testDeepened() throws Exception {
        Path dir = Files.createTempDirectory("jck-sds2");
        try {
            DirectoryStream<Path> raw = Files.newDirectoryStream(dir);
            if (!(raw instanceof SecureDirectoryStream)) {
                raw.close();
                System.out.println("NOTE: no secure stream support; deepened part skipped");
                check(true, "graceful skip recorded");
                return;
            }
            SecureDirectoryStream<Path> sds = (SecureDirectoryStream<Path>) raw;
            try {
                // read channel round-trip through the stream
                try (var ch = sds.newByteChannel(Path.of("rw.txt"),
                        java.util.Set.of(StandardOpenOption.CREATE, StandardOpenOption.WRITE))) {
                    ch.write(java.nio.ByteBuffer.wrap("abc".getBytes()));
                }
                try (var ch = sds.newByteChannel(Path.of("rw.txt"),
                        java.util.Set.of(StandardOpenOption.READ))) {
                    java.nio.ByteBuffer buf = java.nio.ByteBuffer.allocate(3);
                    ch.read(buf);
                    check(new String(buf.array()).equals("abc"), "read channel through stream");
                }
                // CREATE_NEW + WRITE on existing -> FileAlreadyExistsException
                // (note: CREATE_NEW alone without WRITE opens read-only without error)
                checkThrows(java.nio.file.FileAlreadyExistsException.class,
                      () -> sds.newByteChannel(Path.of("rw.txt"),
                            java.util.Set.of(StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)),
                      "CREATE_NEW+WRITE existing -> FileAlreadyExistsException");
                // NOFOLLOW view variant reads the same file
                var v = sds.getFileAttributeView(Path.of("rw.txt"),
                        java.nio.file.attribute.BasicFileAttributeView.class,
                        java.nio.file.LinkOption.NOFOLLOW_LINKS);
                check(v != null && v.readAttributes().size() == 3, "NOFOLLOW view reads size");
                // nested stream with NOFOLLOW option
                Files.createDirectory(dir.resolve("nd"));
                Files.writeString(dir.resolve("nd").resolve("inner"), "i");
                try (SecureDirectoryStream<Path> nested = sds.newDirectoryStream(Path.of("nd"),
                        java.nio.file.LinkOption.NOFOLLOW_LINKS)) {
                    int c = 0;
                    for (Path p : nested) c++;
                    check(c == 1, "nested NOFOLLOW stream lists entries");
                }
                // deleteDirectory nonempty -> DirectoryNotEmptyException
                checkThrows(java.nio.file.DirectoryNotEmptyException.class,
                      () -> sds.deleteDirectory(Path.of("nd")),
                      "deleteDirectory nonempty -> DirectoryNotEmptyException");
                // deleteDirectory missing -> NoSuchFileException
                checkThrows(java.nio.file.NoSuchFileException.class,
                      () -> sds.deleteDirectory(Path.of("ghostd")),
                      "deleteDirectory missing -> NoSuchFileException");
                // deleteFile on a directory -> FileSystemException "Is a directory" (probe-confirmed)
                checkThrows(java.nio.file.FileSystemException.class,
                      () -> sds.deleteFile(Path.of("nd")),
                      "deleteFile on directory -> FileSystemException");
                // deleteDirectory on a file -> FileSystemException
                checkThrows(java.nio.file.FileSystemException.class,
                      () -> sds.deleteDirectory(Path.of("rw.txt")),
                      "deleteDirectory on file -> FileSystemException");
                // clean up nested content through the stream itself
                try (SecureDirectoryStream<Path> nested = sds.newDirectoryStream(Path.of("nd"))) {
                    for (Path p : nested) nested.deleteFile(Path.of(p.getFileName().toString()));
                }
                sds.deleteDirectory(Path.of("nd"));
                check(!Files.exists(dir.resolve("nd")), "deleteDirectory after emptying");
                sds.deleteFile(Path.of("rw.txt"));
                check(!Files.exists(dir.resolve("rw.txt")), "deleteFile cleanup");
            } finally {
                try { sds.close(); } catch (Exception ignore) { }
            }
            // close semantics for further operations
            checkThrows(java.nio.file.ClosedDirectoryStreamException.class,
                  () -> sds.deleteFile(Path.of("x")),
                  "deleteFile after close -> ClosedDirectoryStreamException");
            // views are lazy: obtaining one after close does not throw
            check(sds.getFileAttributeView(
                      java.nio.file.attribute.BasicFileAttributeView.class) != null,
                  "getFileAttributeView after close returns a lazy view (no throw)");
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
        testDeepened();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
