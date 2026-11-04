/*
 * @test
 * @summary jdk.jshell.Wrap.Range conformance: wrap range reflection.
 * @run main RangeTest
 */

import java.lang.reflect.*;

public class RangeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("jdk.jshell.Wrap$Range");
        check(!c.isInterface() && !java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete class");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        check(c.getMethod("toString") != null, "toString method");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
