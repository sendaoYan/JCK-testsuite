/*
 * @test
 * @summary javax.imageio.stream.FileImageOutputStream conformance: white-box structure + black-box behavior.
 * @run main FileImageOutputStreamTest
 */

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.FileImageOutputStream;

public class FileImageOutputStreamTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.imageio.stream.FileImageOutputStream");
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
        Path tmp = Files.createTempFile("jck-fios", ".img");
        File f = tmp.toFile();
        FileImageOutputStream out = null;
        try {
            out = new FileImageOutputStream(f);
            check(out.length() == 0L, "initial length 0");
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
            out.close();
            out = null;

            FileImageInputStream in = new FileImageInputStream(f);
            check(in.length() == 12L, "file length after close");
            in.seek(0L);
            check(in.readByte() == 0x11, "file readByte");
            check(in.readInt() == 0x12345678, "file readInt");
            in.close();
        } finally {
            if (out != null) { try { out.close(); } catch (IOException ignore) {} }
            Files.deleteIfExists(tmp);
        }

        // RandomAccessFile constructor round-trip
        Path tmp2 = Files.createTempFile("jck-fios-raf", ".img");
        try (RandomAccessFile raf = new RandomAccessFile(tmp2.toFile(), "rw")) {
            FileImageOutputStream out2 = new FileImageOutputStream(raf);
            out2.write(0x7E);
            out2.close();
            byte[] wrote = Files.readAllBytes(tmp2);
            check(wrote.length == 1, "raf ctor wrote 1 byte");
            check(wrote[0] == (byte) 0x7E, "raf ctor byte value");
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
