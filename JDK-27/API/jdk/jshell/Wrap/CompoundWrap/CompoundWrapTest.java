/*
 * @test
 * @summary jdk.jshell.Wrap.CompoundWrap conformance: compound wrap reflection.
 * @run main CompoundWrapTest
 */

import java.lang.reflect.*;

public class CompoundWrapTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("jdk.jshell.Wrap$CompoundWrap");
        check(!c.isInterface() && !java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete class");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        check(c.getSuperclass().getName().equals("jdk.jshell.Wrap"), "extends Wrap");
        check(c.getMethod("wrapped") != null, "wrapped method");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
