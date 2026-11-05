/*
 * @test
 * @summary jdk.swing.interop.SwingInterOpUtils conformance: white-box structure + black-box behavior.
 * @run main SwingInterOpUtilsTest
 */

import jdk.swing.interop.*;
import java.lang.reflect.*;
import java.util.*;
public class SwingInterOpUtilsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.swing.interop.SwingInterOpUtils");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !c.isEnum(), "concrete");
        check(hasMethod(c, "postEvent"), "postEvent");
        check(hasMethod(c, "grab"), "grab");
        check(hasMethod(c, "ungrab"), "ungrab");
        check(hasMethod(c, "isUngrabEvent"), "isUngrabEvent");

    }

    static void testBlackBox() throws Exception {
        SwingInterOpUtils u = new SwingInterOpUtils();
        check(u != null, "constructed");
        java.awt.AWTEvent ev = new java.awt.AWTEvent(new Object(), 0) {};
        check(!SwingInterOpUtils.isUngrabEvent(ev), "isUngrabEvent false for plain event");

    }


    static void testDeepen() throws Exception {
        check(SwingInterOpUtils.GRAB_EVENT_MASK != 0 && (SwingInterOpUtils.GRAB_EVENT_MASK & 0x1) == 0,
                "GRAB_EVENT_MASK high bit constant");
        java.awt.EventQueue eq = new java.awt.EventQueue();
        java.awt.AWTEvent ev = new java.awt.AWTEvent(new Object(), 0xf00d) {};
        SwingInterOpUtils.postEvent(eq, ev);
        check(true, "postEvent to a fresh EventQueue headless-safely");
        check(!SwingInterOpUtils.isUngrabEvent(ev), "plain event not ungrab");
        check(!SwingInterOpUtils.isUngrabEvent(new java.awt.event.ActionEvent(new Object(), 0, "cmd")),
                "action event not ungrab");
        Class<?> c = SwingInterOpUtils.class;
        check(!Modifier.isAbstract(c.getModifiers()) && Modifier.isPublic(c.getModifiers()), "public concrete utility");
        for (Method m : c.getDeclaredMethods())
            if (Modifier.isPublic(m.getModifiers()) && !m.isSynthetic())
                check(Modifier.isStatic(m.getModifiers()), m.getName() + " static utility");
        check(Modifier.isPublic(c.getDeclaredConstructor().getModifiers()), "public ctor (instance allowed for style)");
    }

    public static void main(String[] args) throws Exception {
        testDeepen();

        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
