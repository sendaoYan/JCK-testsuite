/*
 * @test
 * @summary javax.swing.event.InternalFrameListener conformance: type shape via reflection (headless-safe).
 * @run main InternalFrameListenerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class InternalFrameListenerTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.event.InternalFrameListener", false, InternalFrameListenerTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface(), "is interface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(hasInterface(c, "java.util.EventListener"), "implements java.util.EventListener");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("internalFrameOpened"), "method internalFrameOpened");
        check(names.contains("internalFrameClosing"), "method internalFrameClosing");
        check(names.contains("internalFrameClosed"), "method internalFrameClosed");
        check(names.contains("internalFrameIconified"), "method internalFrameIconified");
        check(names.contains("internalFrameDeiconified"), "method internalFrameDeiconified");
        check(names.contains("internalFrameActivated"), "method internalFrameActivated");
        check(names.contains("internalFrameDeactivated"), "method internalFrameDeactivated");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
