/*
 * @test
 * @summary java.lang.classfile.attribute.StackMapFrameInfo conformance: reflection shape checks.
 * @run main StackMapFrameInfoTest
 */

import java.lang.reflect.*;

public class StackMapFrameInfoTest {
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
        Class<?> c = Class.forName("java.lang.classfile.attribute.StackMapFrameInfo");
        check(c != null, "java.lang.classfile.attribute.StackMapFrameInfo loads");
        check(c.getName().equals("java.lang.classfile.attribute.StackMapFrameInfo"), "binary name is java.lang.classfile.attribute.StackMapFrameInfo");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.attribute.StackMapFrameInfo is public");
        check(c.isInterface(), "java.lang.classfile.attribute.StackMapFrameInfo is an interface");
        check(declares(c, "frameType"), "java.lang.classfile.attribute.StackMapFrameInfo declares frameType()");
        check(declares(c, "locals"), "java.lang.classfile.attribute.StackMapFrameInfo declares locals()");
        check(declares(c, "of"), "java.lang.classfile.attribute.StackMapFrameInfo declares of()");
        check(declares(c, "stack"), "java.lang.classfile.attribute.StackMapFrameInfo declares stack()");
        check(declares(c, "target"), "java.lang.classfile.attribute.StackMapFrameInfo declares target()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
