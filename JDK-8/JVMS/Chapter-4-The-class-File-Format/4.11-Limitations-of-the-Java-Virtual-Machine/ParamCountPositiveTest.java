/*
 * @test
 * @summary JVMS 4.11: a method descriptor with 255 parameters is the legal upper
 *          bound and the class loads
 * @run main ParamCountPositiveTest
 */

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;

public class ParamCountPositiveTest {
    static int passed;
    static void check(boolean c, String m) { if (c) passed++; else throw new AssertionError(m); }

    static void u1(DataOutputStream d, int v) throws Exception { d.writeByte(v); }
    static void u2(DataOutputStream d, int v) throws Exception { d.writeShort(v); }
    static void u4(DataOutputStream d, long v) throws Exception { d.writeInt((int) v); }
    static void utf8(DataOutputStream d, String s) throws Exception {
        u1(d, 1); byte[] b = s.getBytes("UTF-8"); u2(d, b.length); d.write(b);
    }

    static Class<?> load(String name, byte[] b) throws Exception {
        Path dir = Files.createTempDirectory("cls");
        Files.write(dir.resolve(name + ".class"), b);
        URLClassLoader cl = new URLClassLoader(new URL[]{dir.toUri().toURL()}, null);
        return Class.forName(name, true, cl);
    }

    static byte[] build() throws Exception {
        StringBuilder sb = new StringBuilder(); for (int i = 0; i < 255; i++) sb.append('I');
        String desc = "(" + sb + ")V";
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        DataOutputStream d = new DataOutputStream(o);
        d.writeInt(0xCAFEBABE); u2(d, 0); u2(d, 52);
        u2(d, 8);
        u1(d, 7); u2(d, 2); utf8(d, "ArgsMax");
        u1(d, 7); u2(d, 4); utf8(d, "java/lang/Object");
        utf8(d, "m");
        utf8(d, desc);
        utf8(d, "Code");
        u2(d, 0x0021); u2(d, 1); u2(d, 3); u2(d, 0);
        u2(d, 0);
        u2(d, 1);                                    // methods_count = 1
        u2(d, 0x0009); u2(d, 5); u2(d, 6); u2(d, 1); // public static m + Code
        u2(d, 7); u4(d, 12 + 1); u2(d, 0); u2(d, 255); // Code attr, max_stack, max_locals=255
        u4(d, 1); u1(d, 0xB1);                       // return
        u2(d, 0); u2(d, 0); u2(d, 0);
        return o.toByteArray();
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = load("ArgsMax", build());
        check(c != null, "class with 255 parameters loads");
        java.lang.reflect.Method m = null;
        for (java.lang.reflect.Method mm : c.getDeclaredMethods())
            if (mm.getName().equals("m")) m = mm;
        check(m != null, "method m found");
        check(m.getParameterTypes().length == 255, "parameter count == 255");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
