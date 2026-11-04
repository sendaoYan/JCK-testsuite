/*
 * @test
 * @summary JVMS 4.11: methods_count == 65535 is the legal upper bound and the class loads; 65535 distinct abstract methods are formed from 256 names x 256 descriptors
 * @run main MethodsCountPositiveTest
 */

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;

public class MethodsCountPositiveTest {
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
        int NAMES = 256, DESCS = 256, TOTAL = 65535;
        String[] names = new String[NAMES], descs = new String[DESCS];
        for (int i = 0; i < NAMES; i++) names[i] = "m" + i;
        String[] fd = new String[DESCS];
        for (int i = 0; i < 8; i++) fd[i] = "BCDFIJSZ".substring(i, i+1);
        int di = 8;
        for (int dim = 1; dim <= 31 && di < DESCS; dim++)
            for (int p = 0; p < 8 && di < DESCS; p++) {
                StringBuilder sb = new StringBuilder();
                for (int k = 0; k < dim; k++) sb.append('[');
                sb.append("BCDFIJSZ".charAt(p));
                fd[di++] = sb.toString();
            }
        for (int i = 0; i < DESCS; i++) descs[i] = "()" + fd[i];

        ByteArrayOutputStream o = new ByteArrayOutputStream();
        DataOutputStream d = new DataOutputStream(o);
        d.writeInt(0xCAFEBABE); u2(d, 0); u2(d, 52);
        int cpCount = 1 + 4 + NAMES + DESCS;
        u2(d, cpCount);
        u1(d, 7); u2(d, 2); utf8(d, "ManyMethods");   // #1,#2
        u1(d, 7); u2(d, 4); utf8(d, "java/lang/Object"); // #3,#4
        int nameBase = 5, descBase = 5 + NAMES;
        for (int i = 0; i < NAMES; i++) utf8(d, names[i]);
        for (int i = 0; i < DESCS; i++) utf8(d, descs[i]);
        u2(d, 0x0421); u2(d, 1); u2(d, 3); u2(d, 0);  // public abstract
        u2(d, 0);                                       // fields_count
        u2(d, TOTAL);                                   // methods_count = 65535
        for (int i = 0; i < TOTAL; i++) {
            u2(d, 0x0401);      // public abstract
            u2(d, nameBase + (i / DESCS));
            u2(d, descBase + (i % DESCS));
            u2(d, 0);
        }
        u2(d, 0);
        return o.toByteArray();
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = load("ManyMethods", build());
        check(c != null, "class with 65535 methods loads");
        check(c.getDeclaredMethods().length == 65535, "method count == 65535");
        System.out.println("PASS (" + passed + " assertions)");
    }

}
