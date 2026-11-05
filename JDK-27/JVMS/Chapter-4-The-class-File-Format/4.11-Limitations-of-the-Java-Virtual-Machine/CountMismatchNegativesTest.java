/*
 * @test
 * @summary JVMS 4.1/4.11 malformed-count negatives: a methods_count larger
 *          than the number of method_info entries actually present, a
 *          constant_pool_count of 0, a method name_index of 0, and a method
 *          name_index equal to constant_pool_count (the first invalid index)
 *          all make the class malformed and are rejected with
 *          ClassFormatError; a plain class with fields_count = 0 is well-formed
 *          and loads (positive control).  All counts are single-slot entries
 *          (Utf8), so no long/double slot-skipping is involved.
 * @run main CountMismatchNegativesTest
 */

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;

public class CountMismatchNegativesTest {
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

    static void expectCfe(Throwable t, String prefix, String what) {
        check(t instanceof ClassFormatError, what + " -> ClassFormatError, got " + t);
        check(t.getMessage().startsWith(prefix), what + " message prefix, got: " + t.getMessage());
    }

    // CP: 1,2=this, 3,4=Object, 5="m", 6="()V"  (count = 7, valid indices 1..6)
    static byte[] wellFormed(String cls) throws Exception {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        DataOutputStream d = new DataOutputStream(o);
        d.writeInt(0xCAFEBABE); u2(d, 0); u2(d, 52);
        u2(d, 7);
        u1(d, 7); u2(d, 2); utf8(d, cls); u1(d, 7); u2(d, 4); utf8(d, "java/lang/Object");
        utf8(d, "m"); utf8(d, "()V");
        u2(d, 0x0021); u2(d, 1); u2(d, 3); u2(d, 0);
        u2(d, 0);                       // fields_count = 0 (legal)
        u2(d, 1);                       // methods_count = 1
        u2(d, 0x0401); u2(d, 5); u2(d, 6); u2(d, 0);
        u2(d, 0);
        return o.toByteArray();
    }

    // methods_count declared 10, only 3 method_info entries present
    static byte[] methodsCountTooBig(String cls) throws Exception {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        DataOutputStream d = new DataOutputStream(o);
        d.writeInt(0xCAFEBABE); u2(d, 0); u2(d, 52);
        u2(d, 9);
        u1(d, 7); u2(d, 2); utf8(d, cls); u1(d, 7); u2(d, 4); utf8(d, "java/lang/Object");
        utf8(d, "m"); utf8(d, "()V"); utf8(d, "n"); utf8(d, "()V");
        u2(d, 0x0021); u2(d, 1); u2(d, 3); u2(d, 0);
        u2(d, 0);
        u2(d, 10);                      // declared 10 methods
        for (int i = 0; i < 3; i++) { u2(d, 0x0401); u2(d, 5); u2(d, 6); u2(d, 0); }
        u2(d, 0);                       // class attributes_count misread as a 4th entry
        return o.toByteArray();
    }

    // constant_pool_count = 0 (index 0 is reserved, count must be >= 1)
    static byte[] cpCountZero() throws Exception {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        DataOutputStream d = new DataOutputStream(o);
        d.writeInt(0xCAFEBABE); u2(d, 0); u2(d, 52);
        u2(d, 0);
        u2(d, 0x0021); u2(d, 1); u2(d, 3); u2(d, 0);
        u2(d, 0); u2(d, 0); u2(d, 0);
        return o.toByteArray();
    }

    // CP count = 6 (valid indices 1..5): method name_index given explicitly
    static byte[] badNameIndex(String cls, int nameIndex) throws Exception {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        DataOutputStream d = new DataOutputStream(o);
        d.writeInt(0xCAFEBABE); u2(d, 0); u2(d, 52);
        u2(d, 6);
        u1(d, 7); u2(d, 2); utf8(d, cls); u1(d, 7); u2(d, 4); utf8(d, "java/lang/Object");
        utf8(d, "()V");
        u2(d, 0x0021); u2(d, 1); u2(d, 3); u2(d, 0);
        u2(d, 0);
        u2(d, 1);
        u2(d, 0x0401); u2(d, nameIndex); u2(d, 5); u2(d, 0);
        u2(d, 0);
        return o.toByteArray();
    }

    public static void main(String[] args) throws Exception {
        // positive control: fields_count = 0 is well-formed
        Class<?> ok = load("CountsOk", wellFormed("CountsOk"));
        check(ok.getDeclaredFields().length == 0, "fields_count 0 class loads with no fields");
        check(ok.getDeclaredMethods().length == 1, "its one method is visible");

        // methods_count = 10 but only 3 entries present
        expectCfe(loadError("MethodsShort", methodsCountTooBig("MethodsShort")),
                  "Truncated class file", "methods_count 10 with 3 entries");

        // constant_pool_count = 0
        expectCfe(loadError("CpZero", cpCountZero()),
                  "Illegal constant pool size 0", "constant_pool_count 0");

        // method name_index = 0 (reserved, always invalid)
        expectCfe(loadError("NameZero", badNameIndex("NameZero", 0)),
                  "Illegal constant pool index 0 for method name", "name_index 0");

        // method name_index == constant_pool_count (first invalid index, 6 with count 6)
        expectCfe(loadError("NameEqCount", badNameIndex("NameEqCount", 6)),
                  "Illegal constant pool index 6 for method name", "name_index == count");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
