/*
 * @test
 * @summary java.nio.file.SecureDirectoryStream conformance: white-box structure + black-box
 *          behavior (relative open/move/delete through the stream, attribute views, close).
 * @run main SecureDirectoryStreamTest
 */

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SecureDirectoryStream;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributeView;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

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
        check(c.isInterface(), "is interface");
        check(DirectoryStream.class.isAssignableFrom(c), "extends DirectoryStream");
        check(c.getTypeParameters().length == 1, "generic in path type");
        check(c.getTypeParameters()[0].getName().equals("T"), "type parameter T");
        Set<String> names = new HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods())
            if (m.getDeclaringClass() == c) names.add(m.getName());
        String[] required = { "newDirectoryStream", "newByteChannel", "deleteFile",
                "deleteDirectory", "move", "getFileAttributeView" };
        for (String n : required) check(names.contains(n), "has method " + n);
        // relative open takes (T, Set<? extends OpenOption>, FileAttribute...)
        boolean sigOk = false;
        for (java.lang.reflect.Method m : c.getMethods())
            if (m.getDeclaringClass() == c && m.getName().equals("newByteChannel"))
                sigOk = m.getParameterTypes().length == 3;
        check(sigOk, "newByteChannel(T, Set, FileAttribute...) signature");
    }

    // ---- Part 2: black-box (behavioral) ----
    // The default (sun.nio.fs.Unix) provider yields a SecureDirectoryStream on Linux.
    static void testBlackBox() throws Exception {
        Path dir = Files.createTempDirectory("jck-sds");
        try {
            DirectoryStream<Path> raw = Files.newDirectoryStream(dir);
            if (!(raw instanceof SecureDirectoryStream)) {
                raw.close();
                System.out.println("NOTE: no secure stream support; behavioral part skipped");
                check(true, "graceful skip recorded");
                return;
            }
            @SuppressWarnings("unchecked")
            SecureDirectoryStream<Path> sds = (SecureDirectoryStream<Path>) raw;
            try {
                // create + write a file relative to the stream
                Path rel = Paths.get("s.txt");
                try (java.nio.channels.SeekableByteChannel ch = sds.newByteChannel(rel,
                        EnumSet.of(StandardOpenOption.CREATE, StandardOpenOption.WRITE))) {
                    ch.write(java.nio.ByteBuffer.wrap("sec".getBytes("UTF-8")));
                }
                check(Files.exists(dir.resolve("s.txt")), "file created through the stream");
                check(Files.size(dir.resolve("s.txt")) == 3, "size via Files matches channel write");
                // read back through a relative READ channel
                try (java.nio.channels.SeekableByteChannel ch = sds.newByteChannel(rel,
                        EnumSet.of(StandardOpenOption.READ))) {
                    java.nio.ByteBuffer buf = java.nio.ByteBuffer.allocate(3);
                    ch.read(buf);
                    check(new String(buf.array(), "UTF-8").equals("sec"), "read through stream channel");
                }
                // attribute views: path-bound and directory-bound
                BasicFileAttributeView v = sds.getFileAttributeView(rel,
                        BasicFileAttributeView.class);
                check(v != null, "path-bound view non-null");
                check(v.readAttributes().isRegularFile(), "path view sees regular file");
                check(v.readAttributes().size() == 3, "path view size");
                BasicFileAttributeView dv = sds.getFileAttributeView(BasicFileAttributeView.class);
                check(dv != null, "directory-bound view non-null");
                check(dv.readAttributes().isDirectory(), "dir view sees directory");
                // nested secure stream + entry visibility
                try (SecureDirectoryStream<Path> nested = sds.newDirectoryStream(Paths.get("."))) {
                    boolean found = false;
                    for (Path p : nested)
                        if (p.getFileName().toString().equals("s.txt")) found = true;
                    check(found, "nested stream lists entries");
                }
                // move within the same directory tree via two streams
                Path dir2 = Files.createDirectory(dir.resolveSibling("jck-sds2"));
                try (SecureDirectoryStream<Path> other =
                        (SecureDirectoryStream<Path>) Files.newDirectoryStream(dir2)) {
                    sds.move(Paths.get("s.txt"), other, Paths.get("r.txt"));
                    check(Files.exists(dir2.resolve("r.txt")), "moved into other stream's dir");
                    check(!Files.exists(dir.resolve("s.txt")), "source gone after stream move");
                    check(other.getFileAttributeView(Paths.get("r.txt"), BasicFileAttributeView.class)
                          .readAttributes().size() == 3, "moved content intact");
                    other.deleteFile(Paths.get("r.txt"));
                    check(!Files.exists(dir2.resolve("r.txt")), "deleteFile through other stream");
                } finally {
                    Files.deleteIfExists(dir2);
                }
                // deleteFile / deleteDirectory through the stream + exact negatives
                try (java.nio.channels.SeekableByteChannel e = sds.newByteChannel(Paths.get("e"),
                        EnumSet.of(StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE))) {
                    e.write(java.nio.ByteBuffer.wrap(new byte[] {1}));
                }
                checkThrows(java.nio.file.FileAlreadyExistsException.class,
                      () -> sds.newByteChannel(Paths.get("e"),
                            EnumSet.of(StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)),
                      "CREATE_NEW+WRITE existing -> FileAlreadyExistsException");
                Files.createDirectory(dir.resolve("subd"));
                Files.write(dir.resolve("subd").resolve("x"), new byte[] {1});
                checkThrows(java.nio.file.DirectoryNotEmptyException.class,
                      () -> sds.deleteDirectory(Paths.get("subd")),
                      "deleteDirectory nonempty -> DirectoryNotEmptyException");
                checkThrows(java.nio.file.NoSuchFileException.class,
                      () -> sds.deleteFile(Paths.get("ghost")),
                      "deleteFile missing -> NoSuchFileException");
                checkThrows(java.nio.file.NoSuchFileException.class,
                      () -> sds.deleteDirectory(Paths.get("ghostd")),
                      "deleteDirectory missing -> NoSuchFileException");
                Files.delete(dir.resolve("subd").resolve("x"));
                sds.deleteDirectory(Paths.get("subd"));
                check(!Files.exists(dir.resolve("subd")), "deleteDirectory through stream");
                sds.deleteFile(Paths.get("e"));
                check(!Files.exists(dir.resolve("e")), "deleteFile through stream");
            } finally {
                try { sds.close(); } catch (Exception ignore) { }
            }
            // operations after close -> ClosedDirectoryStreamException
            checkThrows(java.nio.file.ClosedDirectoryStreamException.class,
                  () -> sds.newByteChannel(Paths.get("x"), EnumSet.of(StandardOpenOption.READ)),
                  "newByteChannel after close -> ClosedDirectoryStreamException");
            checkThrows(java.nio.file.ClosedDirectoryStreamException.class,
                  () -> sds.newDirectoryStream(Paths.get(".")),
                  "newDirectoryStream after close -> ClosedDirectoryStreamException");
            checkThrows(java.nio.file.ClosedDirectoryStreamException.class,
                  () -> sds.deleteFile(Paths.get("x")),
                  "deleteFile after close -> ClosedDirectoryStreamException");
        } finally {
            try (java.util.stream.Stream<Path> s = Files.walk(dir)) {
                for (Path p : s.sorted(java.util.Comparator.reverseOrder())
                        .collect(java.util.stream.Collectors.toList()))
                    try { Files.deleteIfExists(p); } catch (Exception ignore) { }
            }
            Files.deleteIfExists(dir.resolveSibling("jck-sds2"));
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
