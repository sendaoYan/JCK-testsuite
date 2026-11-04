/*
 * @test
 * @summary JVMS 4.7.28/4.7.29: a nest of a class and its nested class is represented by NestHost/NestMembers attributes, observable via getNestHost/getNestMembers
 * @run main NestHostTest
 */

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;

public class NestHostTest {
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


    public static class Inner { }
    public static void main(String[] args) throws Exception {
        // A nest is represented by NestHost/NestMembers attributes (JVMS 4.7.28/4.7.29).
        Class<?> outer = NestHostTest.class;
        Class<?> inner = NestHostTest.Inner.class;
        check(inner.getNestHost() == outer, "inner nest host is outer");
        java.util.List<Class<?>> members = java.util.Arrays.asList(outer.getNestMembers());
        check(members.contains(outer) && members.contains(inner), "nest members include outer and inner");
        System.out.println("PASS (" + passed + " assertions)");
    }

}
