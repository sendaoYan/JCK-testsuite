/*
 * @test
 * @summary JVMS 4.4.5: referencing the second (unusable) slot of a CONSTANT_Long/Double is rejected with ClassFormatError
 * @run main LongDoubleTwoSlotsTest
 */

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;

public class LongDoubleTwoSlotsTest {
    static int passed;
    static void check(boolean c, String m) { if (c) passed++; else throw new AssertionError(m); }

    static void u1(DataOutputStream d, int v) throws Exception { d.writeByte(v); }
    static void u2(DataOutputStream d, int v) throws Exception { d.writeShort(v); }
    static void u4(DataOutputStream d, long v) throws Exception { d.writeInt((int) v); }
    static void utf8(DataOutputStream d, String s) throws Exception {
        u1(d, 1); byte[] b = s.getBytes("UTF-8"); u2(d, b.length); d.write(b);
    }

    static byte[] readAll(InputStream in) throws Exception {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        byte[] buf = new byte[4096]; int n;
        while ((n = in.read(buf)) >= 0) o.write(buf, 0, n);
        return o.toByteArray();
    }

    static Class<?> load(String name, byte[] b) throws Exception {
        Path dir = Files.createTempDirectory("cls");
        Files.write(dir.resolve(name + ".class"), b);
        URLClassLoader cl = new URLClassLoader(new URL[]{dir.toUri().toURL()}, null);
        return Class.forName(name, true, cl);
    }

    static Throwable loadError(String name, byte[] b) throws Exception {
        try { load(name, b); return null; } catch (Throwable t) { return t; }
    }


    public static void main(String[] args) throws Exception {
        // CONSTANT_Long/Double occupy two constant pool slots; referencing the
        // second (unusable) slot is invalid (JVMS 4.4.5).
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        DataOutputStream d = new DataOutputStream(o);
        d.writeInt(0xCAFEBABE); u2(d, 0); u2(d, 52);
        u2(d, 5);              // cp_count = 5
        u1(d, 5); d.writeLong(1); // #1 Long -> occupies slots 1 and 2
        u1(d, 7); u2(d, 4);       // #3 Class -> #4
        utf8(d, "X");             // #4 Utf8 "X"
        u2(d, 0x0021); u2(d, 2); u2(d, 3); u2(d, 0); u2(d, 0); u2(d, 0); u2(d, 0);
        // this_class = 2 (the hidden second slot of the Long)
        Throwable t = loadError("X", o.toByteArray());
        check(t instanceof ClassFormatError, "second slot of Long/Double rejected: " + t);
        System.out.println("PASS (" + passed + " assertions)");
    }

}
