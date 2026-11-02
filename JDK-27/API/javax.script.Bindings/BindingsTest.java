/*
 * @test
 * @summary javax.script.Bindings conformance: interface.
 * @run main BindingsTest
 */

import java.lang.reflect.*;


public class BindingsTest {
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
        Class<?> c = Class.forName("javax.script.Bindings");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 1, "1 superinterface(s)");
        check(hasInterface(c, "java.util.Map"), "extends java.util.Map");
        check(hasMethod(c, "put"), "declares put");
        check(hasMethod(c, "putAll"), "declares putAll");
        check(hasMethod(c, "containsKey"), "declares containsKey");
        check(hasMethod(c, "get"), "declares get");
        check(hasMethod(c, "remove"), "declares remove");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
