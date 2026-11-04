/*
 * @test
 * @summary java.awt.event.ComponentEvent conformance: white-box structure + black-box behavior.
 * @run main ComponentEventTest
 */

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.event.ComponentEvent;
import java.lang.reflect.Modifier;

public class ComponentEventTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.event.ComponentEvent");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "ComponentEvent is public");
        check(!Modifier.isAbstract(m), "ComponentEvent is concrete");
        check(c.getSuperclass() == AWTEvent.class, "ComponentEvent extends AWTEvent");
        check(c.getField("COMPONENT_MOVED").getType() == int.class, "COMPONENT_MOVED int");
        check(c.getField("COMPONENT_RESIZED").getType() == int.class, "COMPONENT_RESIZED int");
        check(c.getConstructor(Component.class, int.class) != null, "has (Component,int) ctor");
    }

    static void testBlackBox() throws Exception {
        Component src = new Component() { };
        ComponentEvent e = new ComponentEvent(src, ComponentEvent.COMPONENT_MOVED);

        check(e.getID() == ComponentEvent.COMPONENT_MOVED, "getID round-trips");
        check(e.getSource() == src, "getSource round-trips");
        check(e.getComponent() == src, "getComponent returns source");

        // documented constants
        check(ComponentEvent.COMPONENT_FIRST == 100, "COMPONENT_FIRST == 100");
        check(ComponentEvent.COMPONENT_LAST == 103, "COMPONENT_LAST == 103");
        check(ComponentEvent.COMPONENT_MOVED == 100, "COMPONENT_MOVED == 100");
        check(ComponentEvent.COMPONENT_RESIZED == 101, "COMPONENT_RESIZED == 101");
        check(ComponentEvent.COMPONENT_SHOWN == 102, "COMPONENT_SHOWN == 102");
        check(ComponentEvent.COMPONENT_HIDDEN == 103, "COMPONENT_HIDDEN == 103");

        // paramString (default bounds of a bare Component are 0,0 0x0)
        check(e.paramString().equals("COMPONENT_MOVED (0,0 0x0)"), "paramString COMPONENT_MOVED");
        ComponentEvent r = new ComponentEvent(src, ComponentEvent.COMPONENT_RESIZED);
        check(r.paramString().equals("COMPONENT_RESIZED (0,0 0x0)"), "paramString COMPONENT_RESIZED");
        ComponentEvent h = new ComponentEvent(src, ComponentEvent.COMPONENT_HIDDEN);
        check(h.paramString().equals("COMPONENT_HIDDEN"), "paramString COMPONENT_HIDDEN");
        ComponentEvent unk = new ComponentEvent(src, 9999);
        check(unk.paramString().equals("unknown type"), "paramString unknown type");

        // getComponent with a non-Component source would CCE; here it is a Component, so fine
        check(e.getComponent() == src, "getComponent same identity");
        // subclassing: a ComponentEvent IS-A AWTEvent
        check(e instanceof AWTEvent, "ComponentEvent instanceof AWTEvent");
        // toString contains class name and paramString
        check(e.toString().startsWith("java.awt.event.ComponentEvent["), "toString starts with class name");
        check(e.toString().contains("COMPONENT_MOVED"), "toString contains id text");
        // consume semantics: consume()/isConsumed() are protected in AWTEvent, exercised via a local subclass
        // consume semantics: COMPONENT_* ids are not consumable; consume() is a no-op
        Consumable ev = new Consumable();
        check(!ev.consumedFlag(), "fresh not consumed");
        ev.consumeIt();
        check(!ev.consumedFlag(), "consume() is a no-op for component events");
    }

    static class Consumable extends ComponentEvent {
        Consumable() { super(new java.awt.Component() { }, ComponentEvent.COMPONENT_SHOWN); }
        void consumeIt() { consume(); }
        boolean consumedFlag() { return isConsumed(); }
    }

    @SuppressWarnings("unchecked")
    static <T> T roundTrip(T o) throws java.io.IOException, ClassNotFoundException {
        java.io.ByteArrayOutputStream b = new java.io.ByteArrayOutputStream();
        new java.io.ObjectOutputStream(b).writeObject(o);
        return (T) new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(b.toByteArray())).readObject();
    }
    static void testSerialization() throws Exception {
        java.awt.Component src = new java.awt.Component() { };
        java.awt.event.ComponentEvent e = new java.awt.event.ComponentEvent(src, java.awt.event.ComponentEvent.COMPONENT_SHOWN);
        java.awt.event.ComponentEvent back = roundTrip(e);
        check(back.getID() == java.awt.event.ComponentEvent.COMPONENT_SHOWN, "serialization preserves id");
        check(back.getComponent() == null, "EventObject source is transient: component null after round-trip");
        check(back.paramString() != null, "serialization keeps paramString");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
