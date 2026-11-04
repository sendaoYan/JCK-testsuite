/*
 * @test
 * @summary javax.imageio.stream.FileImageInputStream conformance: white-box structure + black-box behavior.
 * @run main FileImageInputStreamTest
 */

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Modifier;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.stream.FileImageInputStream;

public class FileImageInputStreamTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.imageio.stream.FileImageInputStream");
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
        byte[] data = new byte[16];
        for (int i = 0; i < data.length; i++) data[i] = (byte) (i + 1);
        Path tmp = Files.createTempFile("jck-fiis", ".img");
        Files.write(tmp, data);
        File f = tmp.toFile();
        FileImageInputStream in = null;
        try {
            in = new FileImageInputStream(f);
            check(in.length() == 16L, "length");
            check(in.getStreamPosition() == 0L, "initial position");
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
            check(in.getStreamPosition() == 8L, "position after readFully");
            in.reset();
            check(in.getStreamPosition() == 4L, "reset to mark");
            long skipped = in.skipBytes(10);
            check(skipped == 10L, "skipBytes count");
            in.flushBefore(4L);
            check(in.getFlushedPosition() == 4L, "flushBefore flushedPos");
            in.close();
            in = null;
        } finally {
            if (in != null) { try { in.close(); } catch (IOException ignore) {} }
            Files.deleteIfExists(tmp);
        }

        // RandomAccessFile constructor round-trip
        Path tmp2 = Files.createTempFile("jck-fiis-raf", ".img");
        Files.write(tmp2, data);
        try (RandomAccessFile raf = new RandomAccessFile(tmp2.toFile(), "r")) {
            FileImageInputStream in2 = new FileImageInputStream(raf);
            check(in2.length() == 16L, "raf ctor length");
            check(in2.read() == 1, "raf ctor read");
            in2.close();
        } finally {
            Files.deleteIfExists(tmp2);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
