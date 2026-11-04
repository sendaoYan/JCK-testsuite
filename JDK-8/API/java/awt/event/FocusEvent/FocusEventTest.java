/*
 * @test
 * @summary java.awt.event.FocusEvent conformance: white-box structure + black-box behavior.
 * @run main FocusEventTest
 */

import java.awt.Component;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.lang.reflect.Modifier;

public class FocusEventTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.event.FocusEvent");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "FocusEvent is public");
        check(!Modifier.isAbstract(m), "FocusEvent is concrete");
        check(c.getSuperclass() == ComponentEvent.class, "FocusEvent extends ComponentEvent");
        check(c.getField("FOCUS_GAINED").getType() == int.class, "FOCUS_GAINED int");
        check(c.getField("FOCUS_LOST").getType() == int.class, "FOCUS_LOST int");
        check(c.getConstructor(Component.class, int.class) != null, "has (Component,int) ctor");
        check(c.getConstructor(Component.class, int.class, boolean.class) != null, "has 3-arg ctor");
        check(c.getConstructor(Component.class, int.class, boolean.class, Component.class) != null, "has 4-arg ctor");
        // JDK-8 has no FocusEvent.Cause nested type (added in JDK 9)
        check(!hasNested(c, "Cause"), "no FocusEvent.Cause in JDK-8");
    }

    static boolean hasNested(Class<?> c, String simple) {
        for (Class<?> n : c.getDeclaredClasses())
            if (n.getSimpleName().equals(simple)) return true;
        return false;
    }

    static void testBlackBox() throws Exception {
        Component src = new Component() { };
        Component opp = new Component() { };
        FocusEvent e = new FocusEvent(src, FocusEvent.FOCUS_GAINED, true, opp);

        check(e.getID() == FocusEvent.FOCUS_GAINED, "getID round-trips");
        check(e.getSource() == src, "getSource round-trips");
        check(e.getComponent() == src, "getComponent returns source");
        check(e.isTemporary(), "isTemporary true");
        check(e.getOppositeComponent() == opp, "getOppositeComponent round-trips");

        FocusEvent e2 = new FocusEvent(src, FocusEvent.FOCUS_LOST, false, opp);
        check(!e2.isTemporary(), "isTemporary false");
        check(e2.getOppositeComponent() == opp, "e2 opposite");

        // documented constants
        check(FocusEvent.FOCUS_FIRST == 1004, "FOCUS_FIRST == 1004");
        check(FocusEvent.FOCUS_LAST == 1005, "FOCUS_LAST == 1005");
        check(FocusEvent.FOCUS_GAINED == 1004, "FOCUS_GAINED == 1004");
        check(FocusEvent.FOCUS_LOST == 1005, "FOCUS_LOST == 1005");

        // opposite may be null (focus change with no other component)
        FocusEvent e4 = new FocusEvent(src, FocusEvent.FOCUS_LOST, false);
        check(e4.getOppositeComponent() == null, "null opposite from 3-arg ctor");
        FocusEvent e5 = new FocusEvent(src, FocusEvent.FOCUS_GAINED);
        check(e5.getOppositeComponent() == null, "null opposite from 2-arg ctor");
        check(!e5.isTemporary(), "2-arg ctor isTemporary false (permanent)");

        // paramString
        check(e.paramString().contains("FOCUS_GAINED"), "paramString has id");
        check(e.paramString().contains(",temporary"), "paramString temporary");
        check(e2.paramString().contains(",permanent"), "paramString permanent");
        check(e.paramString().contains("opposite="), "paramString opposite");

        check(e instanceof ComponentEvent, "FocusEvent instanceof ComponentEvent");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
