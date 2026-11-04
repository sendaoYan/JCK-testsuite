/*
 * @test
 * @summary JVMS 4.8: a truncated class file (constant pool count exceeds available data) is rejected with ClassFormatError
 * @run main TruncatedClassFileTest
 */

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;

public class TruncatedClassFileTest {
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
        // A class file that declares a constant_pool_count larger than the data
        // present is truncated and rejected (JVMS 4.8).
        byte[] b = new byte[]{ (byte)0xCA,(byte)0xFE,(byte)0xBA,(byte)0xBE, 0,0, 0,52, 0,8, 0 };
        Throwable t = loadError("X", b);
        check(t instanceof ClassFormatError, "truncated class file rejected: " + t);
        System.out.println("PASS (" + passed + " assertions)");
    }

}
