/*
 * @test
 * @summary java.lang.classfile.attribute.StackMapFrameInfo.UninitializedVerificationTypeInfo conformance: reflection shape checks.
 * @run main UninitializedVerificationTypeInfoTest
 */

import java.lang.reflect.*;

public class UninitializedVerificationTypeInfoTest {
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
        Class<?> c = Class.forName("java.lang.classfile.attribute.StackMapFrameInfo$UninitializedVerificationTypeInfo");
        check(c != null, "java.lang.classfile.attribute.StackMapFrameInfo.UninitializedVerificationTypeInfo loads");
        check(c.getName().equals("java.lang.classfile.attribute.StackMapFrameInfo$UninitializedVerificationTypeInfo"), "binary name is java.lang.classfile.attribute.StackMapFrameInfo$UninitializedVerificationTypeInfo");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.attribute.StackMapFrameInfo.UninitializedVerificationTypeInfo is public");
        check(c.isInterface(), "java.lang.classfile.attribute.StackMapFrameInfo.UninitializedVerificationTypeInfo is an interface");
        check(declares(c, "newTarget"), "java.lang.classfile.attribute.StackMapFrameInfo.UninitializedVerificationTypeInfo declares newTarget()");
        check(declares(c, "of"), "java.lang.classfile.attribute.StackMapFrameInfo.UninitializedVerificationTypeInfo declares of()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
