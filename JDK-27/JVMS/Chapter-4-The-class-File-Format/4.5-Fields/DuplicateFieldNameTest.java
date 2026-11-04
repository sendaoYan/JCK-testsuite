/*
 * @test
 * @summary JVMS 4.5: two fields with the same name and descriptor are rejected with ClassFormatError
 * @run main DuplicateFieldNameTest
 */

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;

public class DuplicateFieldNameTest {
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


    static byte[] build() throws Exception {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        DataOutputStream d = new DataOutputStream(o);
        d.writeInt(0xCAFEBABE); u2(d, 0); u2(d, 52);
        u2(d, 7);                  // cp_count = 7
        u1(d, 7); u2(d, 2); utf8(d, "X");              // #1,#2
        u1(d, 7); u2(d, 4); utf8(d, "java/lang/Object"); // #3,#4
        utf8(d, "a");               // #5 name
        utf8(d, "I");               // #6 descriptor
        u2(d, 0x0021); u2(d, 1); u2(d, 3); u2(d, 0);
        u2(d, 2);                   // fields_count = 2
        u2(d, 0x0001); u2(d, 5); u2(d, 6); u2(d, 0); // public a:I
        u2(d, 0x0001); u2(d, 5); u2(d, 6); u2(d, 0); // public a:I (duplicate)
        u2(d, 0); u2(d, 0);
        return o.toByteArray();
    }
    public static void main(String[] args) throws Exception {
        Throwable t = loadError("X", build());
        check(t instanceof ClassFormatError, "duplicate field name+descriptor rejected: " + t);
        System.out.println("PASS (" + passed + " assertions)");
    }

}
