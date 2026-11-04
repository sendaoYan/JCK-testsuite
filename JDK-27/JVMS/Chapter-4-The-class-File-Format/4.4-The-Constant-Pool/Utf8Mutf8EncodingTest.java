/*
 * @test
 * @summary JVMS 4.4.7: a CONSTANT_Utf8 that is not well-formed modified UTF-8 (overlong two-byte encoding of U+0000) is rejected with ClassFormatError
 * @run main Utf8Mutf8EncodingTest
 */

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;

public class Utf8Mutf8EncodingTest {
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
        // A CONSTANT_Utf8 that encodes U+0000 as the overlong two-byte sequence
        // 0xC0 0x80 (instead of 0x00) is not well-formed modified UTF-8 and must
        // be rejected (JVMS 4.4.7).
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        DataOutputStream d = new DataOutputStream(o);
        d.writeInt(0xCAFEBABE); u2(d, 0); u2(d, 52);
        u2(d, 2);              // cp_count = 2
        u1(d, 1);              // CONSTANT_Utf8
        u2(d, 2); u1(d, 0xC0); u1(d, 0x80);   // overlong encoding of U+0000
        Throwable t = loadError("X", o.toByteArray());
        check(t instanceof ClassFormatError, "overlong MUTF-8 encoding rejected: " + t);
        System.out.println("PASS (" + passed + " assertions)");
    }

}
