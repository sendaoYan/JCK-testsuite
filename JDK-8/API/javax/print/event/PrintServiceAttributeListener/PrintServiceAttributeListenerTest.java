/*
 * @test
 * @summary javax.print.event.PrintServiceAttributeListener conformance: listener interface.
 * @run main PrintServiceAttributeListenerTest
 */

import java.lang.reflect.*;


public class PrintServiceAttributeListenerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasIf(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.print.event.PrintServiceAttributeListener");
        check(c.isInterface(), "isInterface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasMethod(c, "attributeUpdate"), "declares attributeUpdate");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
