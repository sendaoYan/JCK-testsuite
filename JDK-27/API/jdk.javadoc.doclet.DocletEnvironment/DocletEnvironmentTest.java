/*
 * @test
 * @summary jdk.javadoc.doclet.DocletEnvironment conformance: interface.
 * @run main DocletEnvironmentTest
 */

import java.lang.reflect.*;


public class DocletEnvironmentTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasInterface(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.javadoc.doclet.DocletEnvironment");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 0, "no superinterfaces");
        check(hasMethod(c, "getSpecifiedElements"), "declares getSpecifiedElements");
        check(hasMethod(c, "getIncludedElements"), "declares getIncludedElements");
        check(hasMethod(c, "getDocTrees"), "declares getDocTrees");
        check(hasMethod(c, "getElementUtils"), "declares getElementUtils");
        check(hasMethod(c, "getTypeUtils"), "declares getTypeUtils");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
