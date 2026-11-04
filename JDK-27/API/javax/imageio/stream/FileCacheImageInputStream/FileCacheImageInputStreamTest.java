/*
 * @test
 * @summary javax.imageio.stream.FileCacheImageInputStream conformance: white-box structure + black-box behavior.
 * @run main FileCacheImageInputStreamTest
 */

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.stream.FileCacheImageInputStream;

public class FileCacheImageInputStreamTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    interface ThrowingRunnable { void run() throws Exception; }
    static void expectThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try { r.run(); } catch (Throwable t) {
            check(expected.isAssignableFrom(t.getClass()),
                  msg + " expected " + expected.getSimpleName() + " but got " + t.getClass().getSimpleName());
            return;
        }
        check(false, msg + " should throw " + expected.getSimpleName());
    }
    static void deleteRecursive(File f) {
        if (f == null || !f.exists()) return;
        if (f.isDirectory()) {
            File[] kids = f.listFiles();
            if (kids != null) for (File k : kids) deleteRecursive(k);
        }
        f.delete();
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.imageio.stream.FileCacheImageInputStream");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "not interface");
        check(!c.isAnnotation(), "not annotation");
        check(!c.isEnum(), "not enum");
        check(Modifier.isPublic(mods), "public");
        check(!Modifier.isAbstract(mods), "concrete");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.imageio.stream.ImageInputStreamImpl"), "superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        byte[] data = new byte[24];
        for (int i = 0; i < data.length; i++) data[i] = (byte) i;
        Path dir = Files.createTempDirectory("jck-fciis");
        FileCacheImageInputStream in = null;
        try {
            in = new FileCacheImageInputStream(new ByteArrayInputStream(data), dir.toFile());
            check(in.isCached(), "isCached");
            check(in.isCachedFile(), "isCachedFile");
            check(!in.isCachedMemory(), "not isCachedMemory");
            check(in.length() == -1L, "length unknown -1");
            check(in.getStreamPosition() == 0L, "initial position 0");
            check(in.read() == 0, "read first byte");
            byte[] buf = new byte[8];
            int n = in.read(buf, 1, 3);
            check(n == 3, "read(buf,off,len) count");
            check(buf[1] == 1 && buf[2] == 2 && buf[3] == 3, "read(buf,off,len) values");
            check(in.getStreamPosition() == 4L, "position after reads");
            in.mark();
            in.readFully(buf, 0, 4);
            check(buf[0] == 4 && buf[3] == 7, "readFully values");
            in.reset();
            check(in.getStreamPosition() == 4L, "reset to mark");
            in.seek(10L);
            check(in.readByte() == 10, "seek then readByte");
            long skipped = in.skipBytes(3);
            check(skipped == 3L, "skipBytes count");
            check(in.getStreamPosition() == 14L, "position after skip");
            in.flushBefore(5L);
            check(in.getFlushedPosition() == 5L, "flushBefore flushedPos");
            final FileCacheImageInputStream stream = in;
            expectThrows(IndexOutOfBoundsException.class, () -> stream.seek(4L), "seek below flushedPos");
            in.close();
            in = null;
        } finally {
            if (in != null) { try { in.close(); } catch (IOException ignore) {} }
            deleteRecursive(dir.toFile());
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
