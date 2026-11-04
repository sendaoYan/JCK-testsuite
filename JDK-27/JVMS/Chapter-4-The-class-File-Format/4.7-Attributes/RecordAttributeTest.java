/*
 * @test
 * @summary JVMS 4.7.30: a record class carries a Record attribute, observable via isRecord() and getRecordComponents()
 * @run main RecordAttributeTest
 */

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;

public class RecordAttributeTest {
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


    public record Point(int x, int y) { }
    public static void main(String[] args) throws Exception {
        // A record class carries a Record attribute naming its record components
        // (JVMS 4.7.30).
        Class<?> c = Point.class;
        check(c.isRecord(), "record class");
        java.lang.reflect.RecordComponent[] comps = c.getRecordComponents();
        check(comps.length == 2, "two record components");
        check(comps[0].getName().equals("x") && comps[1].getName().equals("y"), "component names");
        check(comps[0].getType() == int.class && comps[1].getType() == int.class, "component types");
        System.out.println("PASS (" + passed + " assertions)");
    }

}
