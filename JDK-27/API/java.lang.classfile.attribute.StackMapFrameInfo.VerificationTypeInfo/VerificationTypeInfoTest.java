/*
 * @test
 * @summary java.lang.classfile.attribute.StackMapFrameInfo.VerificationTypeInfo conformance: reflection shape checks.
 * @run main VerificationTypeInfoTest
 */

import java.lang.reflect.*;

public class VerificationTypeInfoTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static java.util.Set<String> ifaceNames(Class<?> c) {
        java.util.Set<String> s = new java.util.HashSet<>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static boolean declares(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }


    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.lang.classfile.attribute.StackMapFrameInfo$VerificationTypeInfo");
        check(c != null, "java.lang.classfile.attribute.StackMapFrameInfo.VerificationTypeInfo loads");
        check(c.getName().equals("java.lang.classfile.attribute.StackMapFrameInfo$VerificationTypeInfo"), "binary name is java.lang.classfile.attribute.StackMapFrameInfo$VerificationTypeInfo");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.attribute.StackMapFrameInfo.VerificationTypeInfo is public");
        check(c.isInterface(), "java.lang.classfile.attribute.StackMapFrameInfo.VerificationTypeInfo is an interface");
        check(declares(c, "tag"), "java.lang.classfile.attribute.StackMapFrameInfo.VerificationTypeInfo declares tag()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
