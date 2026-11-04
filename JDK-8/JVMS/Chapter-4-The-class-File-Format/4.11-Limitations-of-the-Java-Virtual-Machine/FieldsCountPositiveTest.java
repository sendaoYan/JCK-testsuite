/*
 * @test
 * @summary JVMS 4.11: fields_count == 65535 is the legal upper bound and the class loads; 65535 distinct (name,descriptor) pairs are formed from 256 names x 256 descriptors
 * @run main FieldsCountPositiveTest
 */

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;

public class FieldsCountPositiveTest {
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
        for (int i = 0; i < NAMES; i++) names[i] = "f" + i;
        for (int i = 0; i < 8; i++) descs[i] = "BCDFIJSZ".substring(i, i+1);
        int di = 8;
        for (int dim = 1; dim <= 31 && di < DESCS; dim++)
            for (int p = 0; p < 8 && di < DESCS; p++) {
                StringBuilder sb = new StringBuilder();
                for (int k = 0; k < dim; k++) sb.append('[');
                sb.append("BCDFIJSZ".charAt(p));
                descs[di++] = sb.toString();
            }

        ByteArrayOutputStream o = new ByteArrayOutputStream();
        DataOutputStream d = new DataOutputStream(o);
        d.writeInt(0xCAFEBABE); u2(d, 0); u2(d, 52);
        // constant pool: #1,#2 class+name, #3,#4 super Object, then 256 names, 256 descs
        int cpCount = 1 + 4 + NAMES + DESCS;
        u2(d, cpCount);
        u1(d, 7); u2(d, 2);        // #1 Class -> #2
        utf8(d, "ManyFields");     // #2
        u1(d, 7); u2(d, 4);        // #3 Class -> #4
        utf8(d, "java/lang/Object"); // #4
        int nameBase = 5, descBase = 5 + NAMES;
        for (int i = 0; i < NAMES; i++) utf8(d, names[i]);
        for (int i = 0; i < DESCS; i++) utf8(d, descs[i]);
        u2(d, 0x0021); u2(d, 1); u2(d, 3); u2(d, 0);
        u2(d, TOTAL);              // fields_count = 65535
        for (int i = 0; i < TOTAL; i++) {
            u2(d, 0x0009);         // public static
            u2(d, nameBase + (i / DESCS));
            u2(d, descBase + (i % DESCS));
            u2(d, 0);              // attributes_count = 0
        }
        u2(d, 0); u2(d, 0);        // methods_count, attributes_count
        return o.toByteArray();
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = load("ManyFields", build());
        check(c != null, "class with 65535 fields loads");
        check(c.getDeclaredFields().length == 65535, "field count == 65535");
        System.out.println("PASS (" + passed + " assertions)");
    }

}
