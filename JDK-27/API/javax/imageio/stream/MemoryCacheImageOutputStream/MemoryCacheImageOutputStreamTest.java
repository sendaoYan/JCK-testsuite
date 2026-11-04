/*
 * @test
 * @summary javax.imageio.stream.MemoryCacheImageOutputStream conformance: white-box structure + black-box behavior.
 * @run main MemoryCacheImageOutputStreamTest
 */

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Modifier;

import javax.imageio.stream.MemoryCacheImageOutputStream;

public class MemoryCacheImageOutputStreamTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.imageio.stream.MemoryCacheImageOutputStream");
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
        MemoryCacheImageOutputStream out = null;
        try {
            out = new MemoryCacheImageOutputStream(bos);
            check(out.isCached(), "isCached");
            check(out.isCachedMemory(), "isCachedMemory");
            check(!out.isCachedFile(), "not isCachedFile");
            out.writeByte(0x11);
            out.writeInt(0x12345678);
            out.writeBoolean(true);
            out.writeUTF("hi");
            out.writeShort(0x2233);
            check(out.length() == 12L, "length after writes");
            check(out.getStreamPosition() == 12L, "position after writes");
            out.seek(0L);
            check(out.readByte() == 0x11, "readByte back");
            check(out.readInt() == 0x12345678, "readInt back");
            check(out.readBoolean(), "readBoolean back");
            check("hi".equals(out.readUTF()), "readUTF back");
            check(out.readShort() == 0x2233, "readShort back");
            out.seek(5L);
            out.flushBefore(5L);
            check(out.getFlushedPosition() == 5L, "flushedPos after flushBefore");
            byte[] flushed = bos.toByteArray();
            check(flushed.length == 5, "flushed 5 bytes to underlying");
            check(flushed[0] == 0x11 && flushed[1] == 0x12 && flushed[2] == 0x34 && flushed[3] == 0x56 && flushed[4] == 0x78, "flushed bytes content");
            out.close();
            out = null;
            check(bos.size() == 12, "close flushes all bytes");
        } finally {
            if (out != null) { try { out.close(); } catch (IOException ignore) {} }
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
