/*
 * @test
 * @summary JVMS 4.1: class access_flags -- public/super reflected from the class
 *          file bytes, interface+final and interface-without-abstract are illegal
 *          and rejected with ClassFormatError
 * @run main ClassAccessFlagsTest
 */

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;

public class ClassAccessFlagsTest {
    static int passed;
    static void check(boolean c, String m) { if (c) passed++; else throw new AssertionError(m); }

    static void u1(DataOutputStream d, int v) throws Exception { d.writeByte(v); }
    static void u2(DataOutputStream d, int v) throws Exception { d.writeShort(v); }
    static void utf8(DataOutputStream d, String s) throws Exception {
        u1(d, 1); byte[] b = s.getBytes("UTF-8"); u2(d, b.length); d.write(b);
    }

    static byte[] readAll(InputStream in) throws Exception {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        byte[] buf = new byte[4096]; int n;
        while ((n = in.read(buf)) >= 0) o.write(buf, 0, n);
        return o.toByteArray();
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

    // Skips a constant pool and returns the access_flags of the class.
    static int accessFlags(byte[] b) throws Exception {
        DataInputStream d = new DataInputStream(new java.io.ByteArrayInputStream(b));
        d.readInt(); d.readUnsignedShort(); d.readUnsignedShort();
        int n = d.readUnsignedShort();
        for (int i = 1; i < n; i++) {
            int tag = d.readUnsignedByte();
            if (tag == 1) { int len = d.readUnsignedShort(); d.skipBytes(len); }
            else if (tag == 7) d.readUnsignedShort();
            else if (tag == 3 || tag == 4) d.readInt();
            else if (tag == 5 || tag == 6) { d.readLong(); i++; }
            else if (tag == 8 || tag == 16 || tag == 19 || tag == 20) d.readUnsignedShort();
            else if (tag == 9 || tag == 10 || tag == 11 || tag == 12 || tag == 17 || tag == 18) { d.readUnsignedShort(); d.readUnsignedShort(); }
            else if (tag == 15) { d.readUnsignedByte(); d.readUnsignedShort(); }
        }
        return d.readUnsignedShort();
    }

    // Builds a minimal class file with the given access_flags.
    static byte[] build(int access) throws Exception {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        DataOutputStream d = new DataOutputStream(o);
        d.writeInt(0xCAFEBABE); u2(d, 0); u2(d, 52);
        u2(d, 5);
        u1(d, 7); u2(d, 2); utf8(d, "X");
        u1(d, 7); u2(d, 4); utf8(d, "java/lang/Object");
        u2(d, access); u2(d, 1); u2(d, 3); u2(d, 0); u2(d, 0); u2(d, 0); u2(d, 0);
        return o.toByteArray();
    }

    public static void main(String[] args) throws Exception {
        // Positive: our own class file has ACC_PUBLIC and ACC_SUPER set (JVMS 4.1).
        int m = accessFlags(readAll(ClassAccessFlagsTest.class.getResourceAsStream("ClassAccessFlagsTest.class")));
        check((m & 0x0001) != 0, "own class ACC_PUBLIC");
        check((m & 0x0020) != 0, "own class ACC_SUPER");

        // Negative: ACC_INTERFACE must not be combined with ACC_FINAL (JVMS 4.1).
        Throwable t1 = loadError("X", build(0x0200 | 0x0400 | 0x0010));
        check(t1 instanceof ClassFormatError, "interface+final rejected: " + t1);

        // Negative: an interface must set ACC_ABSTRACT (JVMS 4.1).
        Throwable t2 = loadError("X", build(0x0200));
        check(t2 instanceof ClassFormatError, "interface without ACC_ABSTRACT rejected: " + t2);

        System.out.println("PASS (" + passed + " assertions)");
    }
}
