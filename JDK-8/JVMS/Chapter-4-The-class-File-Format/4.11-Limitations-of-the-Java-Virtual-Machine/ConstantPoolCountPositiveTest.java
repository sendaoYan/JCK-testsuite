/*
 * @test
 * @summary JVMS 4.11: constant_pool_count == 65535 (65534 entries) is the legal upper bound and the class loads
 * @run main ConstantPoolCountPositiveTest
 */

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConstantPoolCountPositiveTest {
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
        // constant_pool_count = 65535 encodes 65534 entries (indices 1..65534),
        // the legal upper bound. Entries 5..65534 are filler Utf8 "x".
        int CP_ENTRIES = 65534;
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        DataOutputStream d = new DataOutputStream(o);
        d.writeInt(0xCAFEBABE); u2(d, 0); u2(d, 52);
        u2(d, 65535);            // constant_pool_count (u2 max) -> 65534 entries
        u1(d, 7); u2(d, 2); utf8(d, "CpMax");       // #1,#2
        u1(d, 7); u2(d, 4); utf8(d, "java/lang/Object"); // #3,#4
        for (int i = 5; i <= CP_ENTRIES; i++) { u1(d, 1); u2(d, 1); u1(d, 'x'); }
        u2(d, 0x0021); u2(d, 1); u2(d, 3); u2(d, 0); u2(d, 0); u2(d, 0); u2(d, 0);
        return o.toByteArray();
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = load("CpMax", build());
        check(c != null, "class with 65534-entry constant pool loads");
        System.out.println("PASS (" + passed + " assertions)");
    }

}
