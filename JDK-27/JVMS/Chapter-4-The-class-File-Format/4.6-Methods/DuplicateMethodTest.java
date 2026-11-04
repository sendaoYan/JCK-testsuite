/*
 * @test
 * @summary JVMS 4.6: two methods with the same name and descriptor are rejected
 *          with ClassFormatError
 * @run main DuplicateMethodTest
 */

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;

public class DuplicateMethodTest {
    static int passed;
    static void check(boolean c, String m) { if (c) passed++; else throw new AssertionError(m); }

    static void u1(DataOutputStream d, int v) throws Exception { d.writeByte(v); }
    static void u2(DataOutputStream d, int v) throws Exception { d.writeShort(v); }
    static void u4(DataOutputStream d, long v) throws Exception { d.writeInt((int) v); }
    static void utf8(DataOutputStream d, String s) throws Exception {
        u1(d, 1); byte[] b = s.getBytes("UTF-8"); u2(d, b.length); d.write(b);
    }

    static Throwable loadError(String name, byte[] b) throws Exception {
        Path dir = Files.createTempDirectory("cls");
        Files.write(dir.resolve(name + ".class"), b);
        try {
            URLClassLoader cl = new URLClassLoader(new URL[]{dir.toUri().toURL()}, null);
            Class.forName(name, true, cl);
            return null;
        } catch (Throwable t) { return t; }
    }

    static byte[] build() throws Exception {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        DataOutputStream d = new DataOutputStream(o);
        d.writeInt(0xCAFEBABE); u2(d, 0); u2(d, 52);
        u2(d, 8);                                     // cp_count = 8
        u1(d, 7); u2(d, 2); utf8(d, "X");             // #1,#2
        u1(d, 7); u2(d, 4); utf8(d, "java/lang/Object"); // #3,#4
        utf8(d, "m");                                 // #5
        utf8(d, "()V");                               // #6
        utf8(d, "Code");                              // #7
        u2(d, 0x0021); u2(d, 1); u2(d, 3); u2(d, 0);
        u2(d, 0);                                     // fields_count
        u2(d, 2);                                     // methods_count = 2
        for (int i = 0; i < 2; i++) {
            u2(d, 0x0009); u2(d, 5); u2(d, 6); u2(d, 1);   // public static m()V + Code
            u2(d, 7); u4(d, 12 + 1); u2(d, 0); u2(d, 0); u4(d, 1); u1(d, 0xB1); u2(d, 0); u2(d, 0);
        }
        u2(d, 0);                                     // class attributes_count
        return o.toByteArray();
    }

    public static void main(String[] args) throws Exception {
        Throwable t = loadError("X", build());
        check(t instanceof ClassFormatError, "duplicate method name+descriptor rejected: " + t);
        System.out.println("PASS (" + passed + " assertions)");
    }
}
