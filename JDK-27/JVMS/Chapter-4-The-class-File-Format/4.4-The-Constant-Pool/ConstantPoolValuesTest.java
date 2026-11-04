/*
 * @test
 * @summary JVMS 4.4: CONSTANT_Integer/Float/Long/Double/String and Utf8 values load and are exposed through reflection, including long/double two-slot entries and a supplementary character encoded as a MUTF-8 surrogate pair
 * @run main ConstantPoolValuesTest
 */

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConstantPoolValuesTest {
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


    public static final boolean Z = true;
    public static final byte B = 7;
    public static final char C = 0x1234;
    public static final short S = -2;
    public static final int I = 42;
    public static final long J = 1L << 40;
    public static final float F = 1.5f;
    public static final double D = 2.5d;
    public static final String STR = "hello";
    public static final String SUPP = "\ud83d\ude00";

    public static void main(String[] args) throws Exception {
        // CONSTANT_Integer/Float/Long/Double/String and Utf8 values are represented
        // in the constant pool and exposed through reflection (JVMS 4.4).
        check((Boolean) ConstantPoolValuesTest.class.getField("Z").get(null), "boolean constant");
        check(((Byte) ConstantPoolValuesTest.class.getField("B").get(null)) == 7, "byte constant");
        check(((Character) ConstantPoolValuesTest.class.getField("C").get(null)) == 0x1234, "char constant");
        check(((Short) ConstantPoolValuesTest.class.getField("S").get(null)) == -2, "short constant");
        check(((Integer) ConstantPoolValuesTest.class.getField("I").get(null)) == 42, "int constant");
        check(((Long) ConstantPoolValuesTest.class.getField("J").get(null)) == (1L << 40), "long constant (two slots)");
        check(((Float) ConstantPoolValuesTest.class.getField("F").get(null)) == 1.5f, "float constant");
        check(((Double) ConstantPoolValuesTest.class.getField("D").get(null)) == 2.5d, "double constant (two slots)");
        check("hello".equals(ConstantPoolValuesTest.class.getField("STR").get(null)), "String constant");
        check("\ud83d\ude00".equals(ConstantPoolValuesTest.class.getField("SUPP").get(null)), "supplementary char in String (MUTF-8 surrogate pair)");

        System.out.println("PASS (" + passed + " assertions)");
    }

}
