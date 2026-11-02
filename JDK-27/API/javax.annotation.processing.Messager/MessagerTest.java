/*
 * @test
 * @summary javax.annotation.processing.Messager conformance: interface.
 * @run main MessagerTest
 */

import java.lang.reflect.*;


public class MessagerTest {
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
        Class<?> c = Class.forName("javax.annotation.processing.Messager");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 0, "no superinterfaces");
        check(hasMethod(c, "printMessage"), "declares printMessage");
        check(hasMethod(c, "printError"), "declares printError");
        check(hasMethod(c, "printWarning"), "declares printWarning");
        check(hasMethod(c, "printNote"), "declares printNote");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
