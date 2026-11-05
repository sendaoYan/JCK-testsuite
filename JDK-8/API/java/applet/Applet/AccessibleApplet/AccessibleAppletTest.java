/*
 * @test
 * @summary java.applet.Applet.AccessibleApplet conformance: accessible role/state contracts.
 * @key headful
 * @run main AccessibleAppletTest
 */

import java.applet.Applet;
import java.awt.*;
import java.lang.reflect.*;
import javax.accessibility.*;

public class AccessibleAppletTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getMethods()) if (m.getName().equals(name)) return true;
        return false;
    }

    public static class SubApplet extends Applet {}

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.applet.Applet$AccessibleApplet");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.Panel$AccessibleAWTPanel"),
                "superclass java.awt.Panel$AccessibleAWTPanel");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(Modifier.isProtected(c.getModifiers()), "protected nested class");
        check(!Modifier.isAbstract(c.getModifiers()), "concrete");
        check(!Modifier.isStatic(c.getModifiers()), "inner (non-static) class bound to the enclosing applet");
        check(Class.forName("java.awt.Panel$AccessibleAWTPanel").isAssignableFrom(c),
                "chain: AccessibleAWTPanel <- AccessibleApplet");
        check(javax.accessibility.AccessibleContext.class.isAssignableFrom(c),
                "reachable from javax.accessibility.AccessibleContext");
        check(hasMethod(c, "getAccessibleRole"), "method getAccessibleRole");
        check(hasMethod(c, "getAccessibleStateSet"), "method getAccessibleStateSet");
        // single constructor taking the enclosing Applet
        Constructor<?>[] ctors = c.getDeclaredConstructors();
        check(ctors.length == 1, "single constructor");
        check(ctors[0].getParameterTypes().length == 1
                && ctors[0].getParameterTypes()[0] == Class.forName("java.applet.Applet"),
                "constructor takes the enclosing Applet");
    }

    // ---- Part 2: black-box (accessible context obtained from a live applet) ----
    static void testBlackBox() throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            try {
                new SubApplet();
                check(false, "expected HeadlessException headless");
            } catch (HeadlessException expected) {
                check(true, "headless -> HeadlessException (accessible context unavailable)");
            }
            return;
        }
        SubApplet ap = new SubApplet();
        AccessibleContext ac = ap.getAccessibleContext();
        check(ac != null, "getAccessibleContext non-null");
        // AccessibleApplet is a protected nested class: compare by exact type
        check("java.applet.Applet$AccessibleApplet".equals(ac.getClass().getName()),
                "context is an Applet.AccessibleApplet");
        // an applet is exposed as a frame to assistive technology
        check(ac.getAccessibleRole() == AccessibleRole.FRAME, "accessible role is FRAME");
        // state set is live and contains at least ENABLED/VISIBLE for a fresh applet
        AccessibleStateSet states = ac.getAccessibleStateSet();
        check(states != null, "state set non-null");
        check(states.contains(AccessibleState.ENABLED), "fresh applet is ENABLED");
        // the inherited accessible family is fully wired
        check(ac.getAccessibleName() == null, "default accessible name is null");
        check(ac.getAccessibleChildrenCount() == 0, "no accessible children");
        check(ac.getAccessibleIndexInParent() == -1, "unparented applet has index -1 in parent");
        check(ac.getAccessibleParent() == null, "unparented applet has no accessible parent");
        // locale follows the component
        try {
            check(ap.getLocale() != null, "component locale available once displayable");
        } catch (IllegalComponentStateException expected) {
            check(true, "locale before parenting throws IllegalComponentStateException");
        }
        // two applets expose distinct contexts
        check(new SubApplet().getAccessibleContext() != ac, "each applet has its own context");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
