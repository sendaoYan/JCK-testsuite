/*
 * @test
 * @summary JVMS 4.11 (via 4.4.7): a field or method name may be exactly 65535
 *          UTF-8 bytes long (the u2 length bound of CONSTANT_Utf8_info) and the
 *          class still loads and reflects the name; a zero-length unqualified
 *          name violates JVMS 4.2 (an unqualified name contains at least one
 *          Unicode code point) and is rejected with ClassFormatError.
 * @run main NameLengthTest
 */

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;

public class NameLengthTest {
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

    // CP: 1,2=this, 3,4=Object, 5=methName, 6=()V, 7=fieldName, 8=I
    static byte[] build(String cls, String methName, String fieldName) throws Exception {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        DataOutputStream d = new DataOutputStream(o);
        d.writeInt(0xCAFEBABE); u2(d, 0); u2(d, 52);
        u2(d, 9);
        u1(d, 7); u2(d, 2); utf8(d, cls); u1(d, 7); u2(d, 4); utf8(d, "java/lang/Object");
        utf8(d, methName != null ? methName : "unused"); utf8(d, "()V");
        utf8(d, fieldName != null ? fieldName : "unused"); utf8(d, "I");
        u2(d, 0x0021); u2(d, 1); u2(d, 3); u2(d, 0);
        if (fieldName != null) {
            u2(d, 1);
            u2(d, 0x0002); u2(d, 7); u2(d, 8); u2(d, 0);   // private I field
        } else {
            u2(d, 0);
        }
        if (methName != null) {
            u2(d, 1);
            u2(d, 0x0401); u2(d, 5); u2(d, 6); u2(d, 0);   // public abstract ()V
        } else {
            u2(d, 0);
        }
        u2(d, 0);
        return o.toByteArray();
    }

    static String bigName() {
        StringBuilder sb = new StringBuilder(65535);
        for (int i = 0; i < 65535; i++) sb.append((char) ('a' + (i % 16)));
        return sb.toString();
    }

    public static void main(String[] args) throws Exception {
        String big = bigName();
        check(big.getBytes("UTF-8").length == 65535, "name must encode to exactly 65535 UTF-8 bytes");

        // positive: field name of exactly 65535 bytes loads
        Class<?> cf = load("BigFieldName", build("BigFieldName", null, big));
        check(cf.getDeclaredFields().length == 1, "one field declared");
        String fn = cf.getDeclaredFields()[0].getName();
        check(fn.length() == 65535, "field name length == 65535, got " + fn.length());
        check(fn.charAt(0) == 'a' && fn.charAt(65534) == 'o', "name round-trips byte-exact");

        // positive: method name of exactly 65535 bytes loads
        Class<?> cm = load("BigMethodName", build("BigMethodName", big, null));
        check(cm.getDeclaredMethods().length == 1, "one method declared");
        check(cm.getDeclaredMethods()[0].getName().length() == 65535, "method name length == 65535");

        // negative: zero-length method name
        Throwable t1 = loadError("EmptyMethodName", build("EmptyMethodName", "", null));
        check(t1 instanceof ClassFormatError, "empty method name -> ClassFormatError, got " + t1);
        check(t1.getMessage().startsWith("Illegal method name \"\""), "message: " + t1.getMessage());

        // negative: zero-length field name
        Throwable t2 = loadError("EmptyFieldName", build("EmptyFieldName", null, ""));
        check(t2 instanceof ClassFormatError, "empty field name -> ClassFormatError, got " + t2);
        check(t2.getMessage().startsWith("Illegal field name \"\""), "message: " + t2.getMessage());

        System.out.println("PASS (" + passed + " assertions)");
    }
}
