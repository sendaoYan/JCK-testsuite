/*
 * @test
 * @summary JVMS 4.11 (with 4.7.3 and 4.9.1): max_stack and max_locals are u2
 *          fields, so 65535 is the legal upper bound for each; declaring far
 *          more than the code uses is accepted, while declaring less than the
 *          code needs (max_stack=0 with a push, max_locals=0 with astore_1)
 *          violates the 4.9 static constraints and yields VerifyError.
 * @run main StackAndLocalsTest
 */

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;

public class StackAndLocalsTest {
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

    // CP: 1,2=this, 3,4=Object, 5=m, 6=descriptor, 7=Code
    static byte[] build(String cls, int maxStack, int maxLocals, boolean intReturn, int[] code) throws Exception {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        DataOutputStream d = new DataOutputStream(o);
        d.writeInt(0xCAFEBABE); u2(d, 0); u2(d, 52);
        u2(d, 8);
        u1(d, 7); u2(d, 2); utf8(d, cls); u1(d, 7); u2(d, 4); utf8(d, "java/lang/Object");
        utf8(d, "m"); utf8(d, intReturn ? "()I" : "()V"); utf8(d, "Code");
        u2(d, 0x0021); u2(d, 1); u2(d, 3); u2(d, 0);
        u2(d, 0);
        u2(d, 1);
        u2(d, 0x0009); u2(d, 5); u2(d, 6); u2(d, 1);          // public static m
        u2(d, 7); u4(d, 12 + code.length); u2(d, maxStack); u2(d, maxLocals);
        u4(d, code.length);
        for (int b : code) u1(d, b);
        u2(d, 0); u2(d, 0); u2(d, 0);
        return o.toByteArray();
    }

    public static void main(String[] args) throws Exception {
        // positive: max_stack = 65535 while the code only ever holds 1 entry
        Class<?> c1 = load("StackMax", build("StackMax", 65535, 1, true, new int[]{0x04, 0xAC}));
        Object r = c1.getMethod("m").invoke(null);             // iconst_1; ireturn
        check(((Integer) r).intValue() == 1, "max_stack 65535 method executes");

        // negative: max_stack = 0 but iconst_1 pushes one entry
        Throwable t1 = loadError("StackBad", build("StackBad", 0, 1, true, new int[]{0x04, 0xAC}));
        check(t1 instanceof VerifyError, "max_stack 0 with push -> VerifyError, got " + t1);
        check(t1.getMessage().startsWith("Operand stack overflow"), "message: " + t1.getMessage());

        // positive: max_locals = 65535 while the code uses none
        Class<?> c2 = load("LocalsMax", build("LocalsMax", 0, 65535, false, new int[]{0xB1}));
        c2.getMethod("m").invoke(null);                        // return
        check(true, "max_locals 65535 method loads and executes");

        // negative: max_locals = 0 but astore_1 stores into local slot 1
        Throwable t2 = loadError("LocalsBad",
                build("LocalsBad", 1, 0, false, new int[]{0x01, 0x4C, 0xB1}));
        check(t2 instanceof VerifyError, "max_locals 0 with astore_1 -> VerifyError, got " + t2);
        check(t2.getMessage().startsWith("Local variable table overflow"), "message: " + t2.getMessage());

        System.out.println("PASS (" + passed + " assertions)");
    }
}
