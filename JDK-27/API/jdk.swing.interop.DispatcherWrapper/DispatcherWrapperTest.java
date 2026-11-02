/*
 * @test
 * @summary jdk.swing.interop.DispatcherWrapper conformance: abstract class.
 * @run main DispatcherWrapperTest
 */

import java.lang.reflect.*;


public class DispatcherWrapperTest {
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
        Class<?> c = Class.forName("jdk.swing.interop.DispatcherWrapper");
        check(Modifier.isAbstract(c.getModifiers()), "isAbstract");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == java.lang.Object.class, "extends Object");
        check(hasMethod(c, "isDispatchThread"), "declares isDispatchThread");
        check(hasMethod(c, "scheduleDispatch"), "declares scheduleDispatch");
        check(hasMethod(c, "createSecondaryLoop"), "declares createSecondaryLoop");
        check(hasMethod(c, "setFwDispatcher"), "declares setFwDispatcher");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
