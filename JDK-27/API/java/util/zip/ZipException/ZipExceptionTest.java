/*
 * @test
 * @summary java.util.zip.ZipException conformance: white-box structure + black-box constructors/real trigger.
 * @run main ZipExceptionTest
 */

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class ZipExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static <T extends Throwable> T expectThrows(Class<T> type, ThrowingRunnable r) {
        try { r.run(); } catch (Throwable t) {
            if (type.isInstance(t)) return type.cast(t);
            throw new AssertionError("Expected " + type.getName() + " but got " + t, t);
        }
        throw new AssertionError("Expected " + type.getName() + " but none thrown");
    }

    static Class<?> clazz(String n) {
        try { return Class.forName(n); } catch (ClassNotFoundException e) { throw new AssertionError(e); }
    }

    static void deleteTree(Path root) throws IOException {
        if (root == null || !Files.exists(root)) return;
        Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
            public FileVisitResult visitFile(Path f, BasicFileAttributes a) throws IOException {
                Files.delete(f);
                return FileVisitResult.CONTINUE;
            }
            public FileVisitResult postVisitDirectory(Path d, IOException e) throws IOException {
                if (e != null) throw e;
                Files.delete(d);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = clazz("java.util.zip.ZipException");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        check(c.getSuperclass() == IOException.class, "extends IOException");
        c.getConstructor();
        c.getConstructor(String.class);
        check(true, "no-arg and String constructors present");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ZipException e = new ZipException();
        check(e.getMessage() == null, "no-arg message null");
        check(e.getCause() == null, "no-arg cause null");
        check(e instanceof IOException, "is an IOException");

        ZipException e2 = new ZipException("bad zip");
        check("bad zip".equals(e2.getMessage()), "message ctor");

        // real trigger: ZipFile over a non-zip byte file
        Path dir = Files.createTempDirectory("jck");
        try {
            Path f = dir.resolve("notazip.bin");
            Files.write(f, new byte[] { 'n', 'o', 't', ' ', 'a', ' ', 'z', 'i', 'p' });
            ZipException ze = expectThrows(ZipException.class, () -> new ZipFile(f.toFile()));
            check(ze.getMessage() != null, "real ZipException from non-zip ZipFile has message");
        } finally {
            deleteTree(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
