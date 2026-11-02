/*
 * @test
 * @summary jdk.swing.interop.SwingInterOpUtils conformance: class.
 * @run main SwingInterOpUtilsTest
 */

import java.lang.reflect.*;


public class SwingInterOpUtilsTest {
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
        Class<?> c = Class.forName("jdk.swing.interop.SwingInterOpUtils");
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(!Modifier.isAbstract(c.getModifiers()), "is concrete");
        check(hasMethod(c, "postEvent"), "declares postEvent");
        check(hasMethod(c, "grab"), "declares grab");
        check(hasMethod(c, "ungrab"), "declares ungrab");
        check(hasMethod(c, "isUngrabEvent"), "declares isUngrabEvent");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
