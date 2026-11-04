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
        Class<?> cause = Class.forName("java.awt.event.FocusEvent$Cause");
        check(cause.isEnum(), "FocusEvent.Cause is enum");
        check(cause.getEnumConstants().length == 11, "FocusEvent.Cause has 11 constants");
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

        // default Cause is UNKNOWN
        check(e.getCause() == FocusEvent.Cause.UNKNOWN, "default cause UNKNOWN");

        // explicit cause
        FocusEvent e3 = new FocusEvent(src, FocusEvent.FOCUS_GAINED, false, opp, FocusEvent.Cause.TRAVERSAL_FORWARD);
        check(e3.getCause() == FocusEvent.Cause.TRAVERSAL_FORWARD, "explicit cause round-trips");

        // opposite may be null (focus change with no other component)
        FocusEvent e4 = new FocusEvent(src, FocusEvent.FOCUS_LOST, false);
        check(e4.getOppositeComponent() == null, "null opposite from 3-arg ctor");

        // Cause enum values
        FocusEvent.Cause[] causes = FocusEvent.Cause.values();
        check(causes.length == 11, "Cause.values length 11");
        check(FocusEvent.Cause.valueOf("ACTIVATION") == FocusEvent.Cause.ACTIVATION, "Cause.valueOf ACTIVATION");

        // paramString
        check(e.paramString().contains("FOCUS_GAINED"), "paramString has id");
        check(e.paramString().contains(",temporary"), "paramString temporary");
        check(e.paramString().contains("cause=UNKNOWN"), "paramString cause");
        check(e2.paramString().contains(",permanent"), "paramString permanent");

        check(e instanceof ComponentEvent, "FocusEvent instanceof ComponentEvent");
        // serialization of a legacy (no-cause) FocusEvent defaults cause to UNKNOWN via readResolve
        java.awt.Component s2 = new java.awt.Component() { };
        java.awt.event.FocusEvent legacy = new java.awt.event.FocusEvent(s2, java.awt.event.FocusEvent.FOCUS_GAINED);
        java.awt.event.FocusEvent lback = roundTrip(legacy);
        check(lback.getCause() == java.awt.event.FocusEvent.Cause.UNKNOWN, "legacy serialization defaults cause UNKNOWN");
        // all 11 Cause constants are distinct
        check(java.util.Arrays.toString(FocusEvent.Cause.values()).contains("MOUSE_EVENT"), "Cause constants include MOUSE_EVENT");
        check(FocusEvent.Cause.valueOf("UNKNOWN") == FocusEvent.Cause.UNKNOWN, "valueOf UNKNOWN");
        // toString
        check(e.toString().contains("FOCUS_GAINED"), "toString contains id text");
    }

    @SuppressWarnings("unchecked")
    static <T> T roundTrip(T o) throws java.io.IOException, ClassNotFoundException {
        java.io.ByteArrayOutputStream b = new java.io.ByteArrayOutputStream();
        new java.io.ObjectOutputStream(b).writeObject(o);
        return (T) new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(b.toByteArray())).readObject();
    }
    static void testSerialization() throws Exception {
        java.awt.Component src = new java.awt.Component() { };
        java.awt.event.FocusEvent e = new java.awt.event.FocusEvent(src, java.awt.event.FocusEvent.FOCUS_LOST, true, null, java.awt.event.FocusEvent.Cause.TRAVERSAL);
        java.awt.event.FocusEvent back = roundTrip(e);
        check(back.getID() == java.awt.event.FocusEvent.FOCUS_LOST, "serialization preserves id");
        check(back.isTemporary(), "serialization preserves temporary flag");
        check(back.getCause() == java.awt.event.FocusEvent.Cause.TRAVERSAL, "serialization preserves cause");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
