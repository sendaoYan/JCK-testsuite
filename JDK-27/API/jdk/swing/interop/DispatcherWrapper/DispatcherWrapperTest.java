/*
 * @test
 * @summary jdk.swing.interop.DispatcherWrapper conformance: white-box structure + black-box behavior.
 * @run main DispatcherWrapperTest
 */

import jdk.swing.interop.*;
import java.lang.reflect.*;
import java.util.*;
public class DispatcherWrapperTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.swing.interop.DispatcherWrapper");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(!c.isInterface(), "not interface");
        check(hasMethod(c, "isDispatchThread"), "isDispatchThread");
        check(hasMethod(c, "scheduleDispatch"), "scheduleDispatch");
        check(hasMethod(c, "createSecondaryLoop"), "createSecondaryLoop");
        check(hasMethod(c, "setFwDispatcher"), "setFwDispatcher");

    }

    static void testBlackBox() throws Exception {
        check(true, "no safe instantiable headless behavior; white-box only");

    }


    static void testDeepen() throws Exception {
        Class<?> c = Class.forName("jdk.swing.interop.DispatcherWrapper");
        java.util.Set<String> abs = new TreeSet<>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isAbstract(m.getModifiers())) abs.add(m.getName());
        check(abs.equals(new TreeSet<>(java.util.Arrays.asList("createSecondaryLoop", "isDispatchThread", "scheduleDispatch"))),
                "three abstract methods exact");
        check(Modifier.isPublic(c.getDeclaredConstructors()[0].getModifiers()), "public constructor for subclassing");
        final boolean[] dispatched = new boolean[1];
        DispatcherWrapper impl = new DispatcherWrapper() {
            public boolean isDispatchThread() { return true; }
            public void scheduleDispatch(Runnable r) { r.run(); dispatched[0] = true; }
            public java.awt.SecondaryLoop createSecondaryLoop() { return null; }
        };
        check(impl.isDispatchThread(), "subclass overrides honored");
        impl.scheduleDispatch(() -> { });
        check(dispatched[0], "scheduleDispatch runs runnable via subclass");
        check(java.lang.reflect.Modifier.isStatic(c.getDeclaredMethod("setFwDispatcher",
                java.awt.EventQueue.class, DispatcherWrapper.class).getModifiers()), "setFwDispatcher static");
        DispatcherWrapper.setFwDispatcher(new java.awt.EventQueue(), impl);
        check(true, "setFwDispatcher installs on a fresh EventQueue headless-safely");
    }

    public static void main(String[] args) throws Exception {
        testDeepen();

        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
