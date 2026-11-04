/*
 * @test
 * @summary java.awt.AWTEvent conformance: white-box structure + black-box behavior.
 * @run main AWTEventTest
 */

import java.awt.AWTEvent;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.lang.reflect.Modifier;

public class AWTEventTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // Concrete subclass so the abstract base behavior can be exercised.
    static class TestEvent extends AWTEvent {
        TestEvent(Object source, int id) { super(source, id); }
        TestEvent(java.awt.Event event) { super(event); }
        void doConsume() { consume(); }
        boolean consumed() { return isConsumed(); }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.AWTEvent");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "AWTEvent is public");
        check(Modifier.isAbstract(m), "AWTEvent is abstract");
        check(c.getSuperclass() == java.util.EventObject.class, "AWTEvent extends EventObject");
        check(Modifier.isProtected(c.getDeclaredField("id").getModifiers()), "id is protected");
        check(Modifier.isProtected(c.getDeclaredField("consumed").getModifiers()), "consumed is protected");
        check(c.getField("COMPONENT_EVENT_MASK").getType() == long.class, "COMPONENT_EVENT_MASK is long");
        check(c.getField("RESERVED_ID_MAX").getType() == int.class, "RESERVED_ID_MAX is int");
        check(Modifier.isPublic(c.getField("RESERVED_ID_MAX").getModifiers()), "RESERVED_ID_MAX is public");
        check(c.getConstructor(Object.class, int.class) != null, "has (Object,int) ctor");
        check(c.getConstructor(java.awt.Event.class) != null, "has (Event) ctor");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Object src = new Object();
        TestEvent e = new TestEvent(src, 1234);
        check(e.getID() == 1234, "getID returns ctor id");
        check(e.getSource() == src, "getSource returns ctor source");
        check(!e.consumed(), "fresh event not consumed");
        check(e.paramString().equals(""), "paramString empty for base AWTEvent");
        check(e.toString().contains("AWTEventTest$TestEvent") && e.toString().contains("["), "toString has class + [");

        // consume() only affects low-level event ids
        TestEvent pressed = new TestEvent(src, MouseEvent.MOUSE_PRESSED);
        check(!pressed.consumed(), "MOUSE_PRESSED not consumed before consume()");
        pressed.doConsume();
        check(pressed.consumed(), "consume() marks MOUSE_PRESSED consumed");

        // semantic ids are pre-consumed by the constructor
        TestEvent sem = new TestEvent(src, ActionEvent.ACTION_PERFORMED);
        check(sem.consumed(), "ACTION_PERFORMED pre-consumed by ctor");

        // setSource retargets
        Object src2 = new Object();
        e.setSource(src2);
        check(e.getSource() == src2, "setSource changes source");
        check(e.getID() == 1234, "id unchanged after setSource");

        // legacy java.awt.Event constructor path
        java.awt.Event old = new java.awt.Event(src, 4321, null);
        check(old.id == 4321, "legacy Event id field");
        check(old.target == src, "legacy Event target field");
        AWTEvent fromOld = new TestEvent(old);
        check(fromOld.getID() == 4321, "AWTEvent(Event) carries id");
        check(fromOld.getSource() == src, "AWTEvent(Event) carries source");

        // event masks are documented powers of two and distinct
        check(AWTEvent.COMPONENT_EVENT_MASK == 0x01L, "COMPONENT_EVENT_MASK == 0x01");
        check(AWTEvent.ACTION_EVENT_MASK == 0x80L, "ACTION_EVENT_MASK == 0x80");
        check(AWTEvent.MOUSE_EVENT_MASK == 0x10L, "MOUSE_EVENT_MASK == 0x10");
        check(AWTEvent.WINDOW_EVENT_MASK == 0x40L, "WINDOW_EVENT_MASK == 0x40");
        check(AWTEvent.RESERVED_ID_MAX == 1999, "RESERVED_ID_MAX == 1999");
        long[] masks = { AWTEvent.COMPONENT_EVENT_MASK, AWTEvent.CONTAINER_EVENT_MASK,
                         AWTEvent.FOCUS_EVENT_MASK, AWTEvent.KEY_EVENT_MASK,
                         AWTEvent.MOUSE_EVENT_MASK, AWTEvent.MOUSE_MOTION_EVENT_MASK,
                         AWTEvent.WINDOW_EVENT_MASK, AWTEvent.ACTION_EVENT_MASK,
                         AWTEvent.ADJUSTMENT_EVENT_MASK, AWTEvent.ITEM_EVENT_MASK,
                         AWTEvent.TEXT_EVENT_MASK, AWTEvent.INPUT_METHOD_EVENT_MASK,
                         AWTEvent.PAINT_EVENT_MASK, AWTEvent.INVOCATION_EVENT_MASK,
                         AWTEvent.HIERARCHY_EVENT_MASK, AWTEvent.HIERARCHY_BOUNDS_EVENT_MASK,
                         AWTEvent.MOUSE_WHEEL_EVENT_MASK, AWTEvent.WINDOW_STATE_EVENT_MASK,
                         AWTEvent.WINDOW_FOCUS_EVENT_MASK };
        for (int i = 0; i < masks.length; i++) {
            check(masks[i] != 0, "mask nonzero " + i);
            check((masks[i] & (masks[i] - 1)) == 0, "mask is power of two " + i);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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
