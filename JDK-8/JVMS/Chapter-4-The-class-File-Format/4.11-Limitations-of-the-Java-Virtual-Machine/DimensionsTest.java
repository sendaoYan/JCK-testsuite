/*
 * @test
 * @summary JVMS 4.11 (with 4.3.2): an array descriptor may name at most 255
 *          dimensions.  A field/method whose descriptor uses exactly 255 '['
 *          prefixes loads, reflects a 255-deep component chain ending in int,
 *          and instances can be built; 256 dimensions can be written into the
 *          class file but the class is rejected with ClassFormatError.
 * @run main DimensionsTest
 */

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.lang.reflect.Array;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;

public class DimensionsTest {
    static int passed;
    static void check(boolean c, String m) { if (c) passed++; else throw new AssertionError(m); }

    static void u1(java.io.DataOutputStream d, int v) throws Exception { d.writeByte(v); }
    static void u2(java.io.DataOutputStream d, int v) throws Exception { d.writeShort(v); }
    static void u4(java.io.DataOutputStream d, long v) throws Exception { d.writeInt((int) v); }
    static void utf8(java.io.DataOutputStream d, String s) throws Exception {
        u1(d, 1); byte[] b = s.getBytes("UTF-8"); u2(d, b.length); d.write(b);
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

    static String dimsDesc(int dims) {
        StringBuilder sb = new StringBuilder(dims + 1);
        for (int i = 0; i < dims; i++) sb.append('[');
        return sb.append('I').toString();
    }

    // CP: 1,2=this, 3,4=Object, 5=f, 6=fieldDesc, 7=m, 8=methodDesc
    static byte[] build(String cls, int dims) throws Exception {
        String desc = dimsDesc(dims);
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        DataOutputStream d = new DataOutputStream(o);
        d.writeInt(0xCAFEBABE); u2(d, 0); u2(d, 52);
        u2(d, 9);
        u1(d, 7); u2(d, 2); utf8(d, cls); u1(d, 7); u2(d, 4); utf8(d, "java/lang/Object");
        utf8(d, "f"); utf8(d, desc); utf8(d, "m"); utf8(d, "()" + desc);
        u2(d, 0x0021); u2(d, 1); u2(d, 3); u2(d, 0);
        u2(d, 1);
        u2(d, 0x0002); u2(d, 5); u2(d, 6); u2(d, 0);           // private f <desc>
        u2(d, 1);
        u2(d, 0x0401); u2(d, 7); u2(d, 8); u2(d, 0);           // public abstract m()<desc>
        u2(d, 0);
        return o.toByteArray();
    }

    public static void main(String[] args) throws Exception {
        // positive: exactly 255 dimensions
        Class<?> c = load("Dims255", build("Dims255", 255));
        Class<?> ft = c.getDeclaredField("f").getType();
        check(ft.isArray(), "field type is an array class");
        check(ft.getName().equals(dimsDesc(255)), "field type name == 255 '[' + I");
        Class<?> comp = ft;
        int depth = 0;
        while (comp.isArray()) { comp = comp.getComponentType(); depth++; }
        check(depth == 255, "component chain depth == 255, got " + depth);
        check(comp == int.class, "leaf component type is int");
        check(c.getDeclaredMethods()[0].getReturnType() == ft,
              "method return type is the same 255-dim class");
        Object a = Array.newInstance(int.class, 1);
        for (int i = 1; i < 255; i++) a = Array.newInstance(a.getClass(), 1);
        check(ft.isInstance(a), "255-deep instance created via Array.newInstance chain");

        // negative: 256 dimensions, encodable but rejected at load
        Throwable t = loadError("Dims256", build("Dims256", 256));
        check(t instanceof ClassFormatError, "256 dims -> ClassFormatError, got " + t);
        check(t.getMessage().startsWith("Array type descriptor has more than 255 dimensions"),
              "message: " + t.getMessage());

        System.out.println("PASS (" + passed + " assertions)");
    }
}
