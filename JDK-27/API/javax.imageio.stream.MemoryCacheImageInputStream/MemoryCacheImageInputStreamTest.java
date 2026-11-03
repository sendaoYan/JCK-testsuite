/*
 * @test
 * @summary javax.imageio.stream.MemoryCacheImageInputStream conformance: white-box structure + black-box behavior.
 * @run main MemoryCacheImageInputStreamTest
 */

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.ByteOrder;

import javax.imageio.stream.MemoryCacheImageInputStream;

public class MemoryCacheImageInputStreamTest {
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

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.imageio.stream.MemoryCacheImageInputStream");
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
        for (int i = 0; i < data.length; i++) data[i] = (byte) (i + 1);
        MemoryCacheImageInputStream in = null;
        try {
            in = new MemoryCacheImageInputStream(new ByteArrayInputStream(data));
            check(in.isCached(), "isCached");
            check(in.isCachedMemory(), "isCachedMemory");
            check(!in.isCachedFile(), "not isCachedFile");
            check(in.length() == -1L, "length unknown -1");
            check(in.getStreamPosition() == 0L, "initial position 0");
            check(in.read() == 1, "read first byte");
            check(in.readByte() == 2, "readByte");
            check(in.readUnsignedByte() == 3, "readUnsignedByte");
            in.seek(0L);
            check(in.readInt() == 0x01020304, "readInt big-endian");
            in.seek(0L);
            in.setByteOrder(ByteOrder.LITTLE_ENDIAN);
            check(in.readInt() == 0x04030201, "readInt little-endian");
            in.setByteOrder(ByteOrder.BIG_ENDIAN);
            in.seek(4L);
            in.mark();
            byte[] four = new byte[4];
            in.readFully(four);
            check(four[0] == 5 && four[3] == 8, "readFully values");
            in.reset();
            check(in.getStreamPosition() == 4L, "reset to mark");
            long skipped = in.skipBytes(10);
            check(skipped == 10L, "skipBytes count");
            check(in.getStreamPosition() == 14L, "position after skip");
            in.flushBefore(5L);
            check(in.getFlushedPosition() == 5L, "flushBefore flushedPos");
            final MemoryCacheImageInputStream stream = in;
            expectThrows(IndexOutOfBoundsException.class, () -> stream.seek(4L), "seek below flushedPos");
            in.close();
            in = null;
        } finally {
            if (in != null) { try { in.close(); } catch (IOException ignore) {} }
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
