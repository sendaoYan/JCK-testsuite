/*
 * @test
 * @summary JVMS 4.11: code_length == 65535 is the legal upper bound; the method loads and executes (65534 nops + return)
 * @run main CodeLengthPositiveTest
 */

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;

public class CodeLengthPositiveTest {
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
        // code_length == 65535 (65534 nops + return) is the legal upper bound.
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        DataOutputStream d = new DataOutputStream(o);
        d.writeInt(0xCAFEBABE); u2(d, 0); u2(d, 52);
        u2(d, 8);                  // cp_count = 8
        u1(d, 7); u2(d, 2); utf8(d, "CodeMax");     // #1,#2
        u1(d, 7); u2(d, 4); utf8(d, "java/lang/Object"); // #3,#4
        utf8(d, "m");              // #5
        utf8(d, "()V");            // #6
        utf8(d, "Code");           // #7
        u2(d, 0x0021); u2(d, 1); u2(d, 3); u2(d, 0);
        u2(d, 0);                  // fields_count
        u2(d, 1);                  // methods_count = 1
        u2(d, 0x0009); u2(d, 5); u2(d, 6); u2(d, 1); // public static m()V + Code
        u2(d, 7); u4(d, 12 + 65535); u2(d, 0); u2(d, 0); // Code attr len, max_stack, max_locals
        u4(d, 65535);              // code_length = 65535
        for (int i = 0; i < 65534; i++) u1(d, 0x00);  // nop
        u1(d, 0xB1);               // return
        u2(d, 0);                  // exception_table_length
        u2(d, 0);                  // code attributes_count
        u2(d, 0);                  // class attributes_count
        return o.toByteArray();
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = load("CodeMax", build());
        check(c != null, "class with code_length 65535 loads");
        java.lang.reflect.Method m = c.getMethod("m");
        m.invoke(null);            // execute the 65535-byte method successfully
        check(true, "method with code_length 65535 executes");
        System.out.println("PASS (" + passed + " assertions)");
    }

}
