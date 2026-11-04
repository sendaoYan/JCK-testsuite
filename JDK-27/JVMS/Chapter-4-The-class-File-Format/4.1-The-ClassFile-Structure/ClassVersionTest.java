/*
 * @test
 * @summary JVMS 4.1: class file version -- magic 0xCAFEBABE, supported major/minor range (major 71 accepted, too-high/too-low rejected with UnsupportedClassVersionError, nonzero minor for major<56 rejected)
 * @run main ClassVersionTest
 */

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;

public class ClassVersionTest {
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


    static byte[] versionOnly(int major, int minor) throws Exception {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        DataOutputStream d = new DataOutputStream(o);
        d.writeInt(0xCAFEBABE); u2(d, minor); u2(d, major); u2(d, 1);
        return o.toByteArray();
    }

    public static void main(String[] args) throws Exception {
        byte[] self;
        InputStream in = ClassVersionTest.class.getResourceAsStream("ClassVersionTest.class");
        self = readAll(in);

        DataInputStream r = new DataInputStream(new java.io.ByteArrayInputStream(self));
        int magic = r.readInt(); int minor = r.readUnsignedShort(); int major = r.readUnsignedShort();
        check(magic == 0xCAFEBABE, "magic == 0xCAFEBABE");
        check(minor == 0, "minor_version == 0");
        check(major == 71, "major_version == 71");

        // A class file whose major_version exceeds the JVM's supported range is
        // rejected with UnsupportedClassVersionError (JVMS 4.1 / 4.8).
        Throwable t1 = loadError("V", versionOnly(72, 0));
        check(t1 instanceof UnsupportedClassVersionError, "too-high major -> UnsupportedClassVersionError: " + t1);

        // major_version < 45 is likewise an unsupported version.
        Throwable t2 = loadError("V", versionOnly(44, 0));
        check(t2 instanceof UnsupportedClassVersionError, "major 44 -> UnsupportedClassVersionError: " + t2);

        // For a class file with major_version in 45..55, minor_version must be 0.
        Throwable t3 = loadError("V", versionOnly(52, 1));
        check(t3 instanceof ClassFormatError, "nonzero minor for major 52 -> ClassFormatError: " + t3);

        System.out.println("PASS (" + passed + " assertions)");
    }

}
