/*
 * @test
 * @summary javax.imageio.stream.FileCacheImageOutputStream conformance: white-box structure + black-box behavior.
 * @run main FileCacheImageOutputStreamTest
 */

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.stream.FileCacheImageOutputStream;

public class FileCacheImageOutputStreamTest {
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
        Class<?> c = Class.forName("javax.imageio.stream.FileCacheImageOutputStream");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "not interface");
        check(!c.isAnnotation(), "not annotation");
        check(!c.isEnum(), "not enum");
        check(Modifier.isPublic(mods), "public");
        check(!Modifier.isAbstract(mods), "concrete");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.imageio.stream.ImageOutputStreamImpl"), "superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Path dir = Files.createTempDirectory("jck-fcios");
        FileCacheImageOutputStream out = null;
        try {
            out = new FileCacheImageOutputStream(bos, dir.toFile());
            check(out.isCached(), "isCached");
            check(out.isCachedFile(), "isCachedFile");
            check(!out.isCachedMemory(), "not isCachedMemory");
            out.writeByte(0x11);
            out.writeInt(0x12345678);
            out.writeBoolean(true);
            out.writeUTF("hello");
            out.writeShort(0x2233);
            check(out.length() == 15L, "length after writes");
            check(out.getStreamPosition() == 15L, "position after writes");
            out.seek(0L);
            out.mark();
            out.readInt();
            out.readInt();
            out.reset();
            check(out.getStreamPosition() == 0L, "reset to mark");
            out.seek(0L);
            check(out.readByte() == 0x11, "readByte back");
            check(out.readInt() == 0x12345678, "readInt back");
            check(out.readBoolean(), "readBoolean back");
            check("hello".equals(out.readUTF()), "readUTF back");
            check(out.readShort() == 0x2233, "readShort back");
            out.seek(5L);
            out.flushBefore(5L);
            check(out.getFlushedPosition() == 5L, "flushedPos after flushBefore");
            byte[] flushed = bos.toByteArray();
            check(flushed.length == 5, "flushed 5 bytes to underlying");
            check(flushed[0] == 0x11 && flushed[1] == 0x12 && flushed[2] == 0x34 && flushed[3] == 0x56 && flushed[4] == 0x78, "flushed bytes content");
            final FileCacheImageOutputStream stream = out;
            expectThrows(IndexOutOfBoundsException.class, () -> stream.seek(4L), "seek below flushedPos");
            out.close();
            out = null;
            check(bos.size() == 15, "close flushes all bytes");
        } finally {
            if (out != null) { try { out.close(); } catch (IOException ignore) {} }
            deleteRecursive(dir.toFile());
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
