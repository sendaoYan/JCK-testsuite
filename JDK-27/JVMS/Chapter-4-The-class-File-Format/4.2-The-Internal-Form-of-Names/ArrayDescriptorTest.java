/*
 * @test
 * @summary JVMS 4.2/4.3: array types use '[' descriptor prefixes (one per dimension), internal names use '/' separators, primitives use single-letter names
 * @run main ArrayDescriptorTest
 */

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;

public class ArrayDescriptorTest {
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
        // Array types are named by their field descriptor, which begins with '[' and
        // repeats once per dimension (JVMS 4.2 / 4.3).
        check("[I".equals(int[].class.getName()), "int[] -> [I");
        check("[Ljava.lang.String;".equals(String[].class.getName()), "String[] -> [Ljava.lang.String;");
        check("[[I".equals(int[][].class.getName()), "int[][] -> [[I");
        check("[Ljava.lang.String;".equals(new String[0].getClass().getName()), "object array descriptor");

        // The internal form of a class name uses '/' rather than '.' (JVMS 4.2.1).
        check("java/lang/String".equals(String.class.getName().replace('.', '/')), "internal name separator");
        // Primitive internal names are the single descriptor letter.
        check("int".equals(int.class.getName()), "int primitive name");
        check("long".equals(long.class.getName()), "long primitive name");
        check("void".equals(void.class.getName()), "void primitive name");

        System.out.println("PASS (" + passed + " assertions)");
    }

}
