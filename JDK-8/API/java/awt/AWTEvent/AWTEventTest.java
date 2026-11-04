/*
 * @test
 * @summary java.awt.AWTEvent conformance: abstract class reflection.
 * @run main AWTEventTest
 */

public class AWTEventTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.awt.AWTEvent");
        check(!c.isInterface() && !c.isEnum(), "not interface/enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.util.EventObject"), "superclass java.util.EventObject");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "setSource"), "method setSource");
        check(hasMethod(c, "getID"), "method getID");
        check(hasMethod(c, "toString"), "method toString");
        check(hasMethod(c, "paramString"), "method paramString");
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Part 3: contract deepening ----
    static class ProbeEvent extends java.awt.AWTEvent {
        ProbeEvent(Object src, int id) { super(src, id); }
        ProbeEvent(java.awt.Event legacy) { super(legacy); }
        boolean consumedProbe() { return isConsumed(); }
        void consumeProbe() { consume(); }
    }
    static void testContract() {
        // RESERVED_ID_MAX
        check(java.awt.AWTEvent.RESERVED_ID_MAX == 1999, "RESERVED_ID_MAX == 1999");
        // event masks are distinct powers/bitmasks
        check(java.awt.AWTEvent.COMPONENT_EVENT_MASK == 0x1, "COMPONENT_EVENT_MASK 0x1");
        check(java.awt.AWTEvent.MOUSE_EVENT_MASK == 0x10, "MOUSE_EVENT_MASK 0x10");
        check(java.awt.AWTEvent.ACTION_EVENT_MASK == 0x80, "ACTION_EVENT_MASK 0x80");
        check(java.awt.AWTEvent.MOUSE_WHEEL_EVENT_MASK == 0x20000, "MOUSE_WHEEL_EVENT_MASK 0x20000");
        check(java.awt.AWTEvent.WINDOW_FOCUS_EVENT_MASK == 0x80000, "WINDOW_FOCUS_EVENT_MASK 0x80000");
        // ctor + accessors
        ProbeEvent ev = new ProbeEvent("src1", 42);
        check(ev.getID() == 42, "getID");
        check(ev.getSource() == "src1", "getSource identity");
        check(!ev.consumedProbe(), "not consumed initially");
        // consume() on unknown custom id is a no-op (only known event types are consumable)
        ev.consumeProbe();
        check(!ev.consumedProbe(), "consume() no-op for unknown event id (observed)");
        // consume() on a known event type works
        java.awt.event.KeyEvent kev = new java.awt.event.KeyEvent(new java.awt.Canvas(),
                java.awt.event.KeyEvent.KEY_PRESSED, 0L, 0, java.awt.event.KeyEvent.VK_A, 'a');
        check(!kev.isConsumed(), "KeyEvent initially not consumed");
        kev.consume();
        check(kev.isConsumed(), "KeyEvent consume works for known type");
        // setSource changes source, keeps id
        ev.setSource("src2");
        check(ev.getSource() == "src2", "setSource replaces source");
        check(ev.getID() == 42, "setSource keeps id");
        ev.setSource(null);
        check(ev.getSource() == null, "setSource(null) accepted, source null");
        ev.setSource("back");
        check(ev.getSource() == "back", "setSource reassign after null");
        boolean nullCtor = false; String nullCtorMsg = null;
        try { new ProbeEvent(null, 1); } catch (java.lang.IllegalArgumentException e) { nullCtor = true; nullCtorMsg = e.getMessage(); }
        check(nullCtor && "null source".equals(nullCtorMsg), "ctor null source -> IAE 'null source'");
        // protected fields id/consumed visible to subclasses
        class Sub extends java.awt.AWTEvent {
            Sub(Object s, int i) { super(s, i); }
            int idProbe() { return id; }
            boolean consumedProbe() { return consumed; }
        }
        Sub sub = new Sub("s3", 7);
        check(sub.idProbe() == 7 && !sub.consumedProbe(), "protected fields id/consumed accessible");
        // paramString for custom event is empty; toString still formed
        check(ev.paramString().length() >= 0, "paramString callable");
        check(ev.toString().length() > 0, "toString non-empty");
        // AWTEvent(Event) legacy constructor
        java.awt.Event legacy = new java.awt.Event(new Object(), 0L, java.awt.Event.ACTION_EVENT, 0, 0, 0, 0);
        java.awt.AWTEvent fromLegacy = new ProbeEvent(legacy);
        check(fromLegacy.getSource() == legacy.target, "legacy ctor uses Event.target as source");
        // AWTEvent is abstract
        check(java.lang.reflect.Modifier.isAbstract(java.awt.AWTEvent.class.getModifiers()),
              "AWTEvent abstract");
        // extends EventObject
        check(java.util.EventObject.class.isAssignableFrom(java.awt.AWTEvent.class),
              "extends java.util.EventObject");
    }

}
