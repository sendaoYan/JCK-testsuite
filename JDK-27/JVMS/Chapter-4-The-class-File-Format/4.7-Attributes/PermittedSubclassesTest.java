/*
 * @test
 * @summary JVMS 4.7.31: a sealed interface carries a PermittedSubclasses attribute, observable via getPermittedSubclasses()
 * @run main PermittedSubclassesTest
 */

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;

public class PermittedSubclassesTest {
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


    public sealed interface Shape permits Circle, Square { }
    public static final class Circle implements Shape { }
    public static final class Square implements Shape { }
    public static void main(String[] args) throws Exception {
        // A sealed class/interface carries a PermittedSubclasses attribute (JVMS 4.7.31).
        Class<?>[] permitted = Shape.class.getPermittedSubclasses();
        check(permitted != null && permitted.length == 2, "two permitted subclasses");
        java.util.List<String> names = new java.util.ArrayList<String>();
        for (Class<?> c : permitted) names.add(c.getSimpleName());
        check(names.contains("Circle") && names.contains("Square"), "permitted names");
        System.out.println("PASS (" + passed + " assertions)");
    }

}
