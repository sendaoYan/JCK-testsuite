/*
 * @test
 * @summary javax.accessibility.AccessibleStreamable conformance: interface.
 * @run main AccessibleStreamableTest
 */

import java.lang.reflect.*;


public class AccessibleStreamableTest {
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
        Class<?> c = Class.forName("javax.accessibility.AccessibleStreamable");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 0, "no superinterfaces");
        check(hasMethod(c, "getMimeTypes"), "declares getMimeTypes");
        check(hasMethod(c, "getStream"), "declares getStream");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
