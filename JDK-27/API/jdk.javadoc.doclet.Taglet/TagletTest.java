/*
 * @test
 * @summary jdk.javadoc.doclet.Taglet conformance: interface.
 * @run main TagletTest
 */

import java.lang.reflect.*;


public class TagletTest {
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
        Class<?> c = Class.forName("jdk.javadoc.doclet.Taglet");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 0, "no superinterfaces");
        check(hasMethod(c, "getAllowedLocations"), "declares getAllowedLocations");
        check(hasMethod(c, "isInlineTag"), "declares isInlineTag");
        check(hasMethod(c, "isBlockTag"), "declares isBlockTag");
        check(hasMethod(c, "getName"), "declares getName");
        check(hasMethod(c, "init"), "declares init");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
