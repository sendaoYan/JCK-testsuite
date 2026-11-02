/*
 * @test
 * @summary javax.tools.DocumentationTool.Location conformance: enum.
 * @run main LocationTest
 */

import java.lang.reflect.*;


public class LocationTest {
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
        Class<?> c = Class.forName("javax.tools.DocumentationTool$Location");
        check(c.isEnum(), "isEnum");
        check(c.getSuperclass() == java.lang.Enum.class, "extends Enum");
        check(c.getEnumConstants() != null && c.getEnumConstants().length > 0, "enum constants");
        check(c.getInterfaces().length == 1, "1 interface(s)");
        check(hasInterface(c, "javax.tools.JavaFileManager$Location"), "implements javax.tools.JavaFileManager$Location");
        check(hasMethod(c, "getName"), "declares getName");
        check(hasMethod(c, "isOutputLocation"), "declares isOutputLocation");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
