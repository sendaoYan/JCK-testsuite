/*
 * @test
 * @summary JVMS 4.7.23: a lambda compiles to an invokedynamic instruction and a BootstrapMethods attribute
 * @run main BootstrapMethodsTest
 */

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;

public class BootstrapMethodsTest {
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
        // A lambda body compiles to an invokedynamic instruction and a
        // BootstrapMethods attribute (JVMS 4.7.23); both are visible in the
        // constant pool / attributes of the compiled class.
        Runnable r = () -> { };
        byte[] b = readAll(BootstrapMethodsTest.class.getResourceAsStream("BootstrapMethodsTest.class"));
        DataInputStream d = new DataInputStream(new java.io.ByteArrayInputStream(b));
        check(d.readInt() == 0xCAFEBABE, "magic");
        d.readUnsignedShort(); d.readUnsignedShort();
        int n = d.readUnsignedShort();
        boolean hasInvokeDynamic = false, hasBootstrapMethods = false;
        for (int i = 1; i < n; i++) {
            int tag = d.readUnsignedByte();
            if (tag == 1) { int len = d.readUnsignedShort(); byte[] s = new byte[len]; d.readFully(s);
                if (new String(s, "UTF-8").equals("BootstrapMethods")) hasBootstrapMethods = true; }
            else if (tag == 18) { hasInvokeDynamic = true; d.readUnsignedShort(); d.readUnsignedShort(); }
            else if (tag == 7 || tag == 8 || tag == 16 || tag == 19 || tag == 20) d.readUnsignedShort();
            else if (tag == 3 || tag == 4) d.readInt();
            else if (tag == 5 || tag == 6) { d.readLong(); i++; }
            else if (tag == 9 || tag == 10 || tag == 11 || tag == 12) { d.readUnsignedShort(); d.readUnsignedShort(); }
            else if (tag == 15) { d.readUnsignedByte(); d.readUnsignedShort(); }
        }
        check(hasInvokeDynamic, "constant pool contains CONSTANT_InvokeDynamic");
        check(hasBootstrapMethods, "constant pool contains BootstrapMethods attribute name");
        System.out.println("PASS (" + passed + " assertions)");
    }

}
