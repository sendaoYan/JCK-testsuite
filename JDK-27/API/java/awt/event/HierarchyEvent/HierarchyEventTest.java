/*
 * @test
 * @summary java.awt.event.HierarchyEvent conformance: white-box structure + black-box behavior.
 * @run main HierarchyEventTest
 */

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.HierarchyEvent;
import java.lang.reflect.Modifier;

public class HierarchyEventTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.event.HierarchyEvent");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "HierarchyEvent is public");
        check(!Modifier.isAbstract(m), "HierarchyEvent is concrete");
        check(c.getSuperclass() == AWTEvent.class, "HierarchyEvent extends AWTEvent");
        check(c.getField("HIERARCHY_CHANGED").getType() == int.class, "HIERARCHY_CHANGED int");
        check(c.getField("PARENT_CHANGED").getType() == int.class, "PARENT_CHANGED int");
        check(c.getConstructor(Component.class, int.class, Component.class, Container.class) != null, "has 4-arg ctor");
        check(c.getConstructor(Component.class, int.class, Component.class, Container.class, long.class) != null, "has 5-arg ctor");
    }

    static void testBlackBox() throws Exception {
        Component src = new Component() { };
        Component changed = new Component() { };
        Container parent = new Container();
        HierarchyEvent e = new HierarchyEvent(src, HierarchyEvent.HIERARCHY_CHANGED,
                                              changed, parent, HierarchyEvent.PARENT_CHANGED);

        check(e.getID() == HierarchyEvent.HIERARCHY_CHANGED, "getID round-trips");
        check(e.getSource() == src, "getSource round-trips");
        check(e.getComponent() == src, "getComponent returns source");
        check(e.getChanged() == changed, "getChanged round-trips");
        check(e.getChangedParent() == parent, "getChangedParent round-trips");
        check(e.getChangeFlags() == HierarchyEvent.PARENT_CHANGED, "getChangeFlags round-trips");

        // combined flags
        long flags = HierarchyEvent.PARENT_CHANGED | HierarchyEvent.SHOWING_CHANGED;
        HierarchyEvent combo = new HierarchyEvent(src, HierarchyEvent.HIERARCHY_CHANGED, changed, parent, flags);
        check(combo.getChangeFlags() == flags, "combined flags round-trip");

        // documented constants
        check(HierarchyEvent.HIERARCHY_FIRST == 1400, "HIERARCHY_FIRST == 1400");
        check(HierarchyEvent.HIERARCHY_CHANGED == 1400, "HIERARCHY_CHANGED == 1400");
        check(HierarchyEvent.ANCESTOR_MOVED == 1401, "ANCESTOR_MOVED == 1401");
        check(HierarchyEvent.ANCESTOR_RESIZED == 1402, "ANCESTOR_RESIZED == 1402");
        check(HierarchyEvent.HIERARCHY_LAST == 1402, "HIERARCHY_LAST == 1402");
        check(HierarchyEvent.PARENT_CHANGED == 0x1, "PARENT_CHANGED == 0x1");
        check(HierarchyEvent.DISPLAYABILITY_CHANGED == 0x2, "DISPLAYABILITY_CHANGED == 0x2");
        check(HierarchyEvent.SHOWING_CHANGED == 0x4, "SHOWING_CHANGED == 0x4");

        // paramString
        check(e.paramString().contains("HIERARCHY_CHANGED"), "paramString HIERARCHY_CHANGED");
        check(e.paramString().contains("PARENT_CHANGED"), "paramString PARENT_CHANGED");
        HierarchyEvent moved = new HierarchyEvent(src, HierarchyEvent.ANCESTOR_MOVED, changed, parent);
        check(moved.paramString().contains("ANCESTOR_MOVED"), "paramString ANCESTOR_MOVED");
        check(moved.getChangeFlags() == 0, "4-arg ctor changeFlags 0");

        check(e instanceof AWTEvent, "HierarchyEvent instanceof AWTEvent");
        // all three flag bits combined
        long all = HierarchyEvent.PARENT_CHANGED | HierarchyEvent.DISPLAYABILITY_CHANGED | HierarchyEvent.SHOWING_CHANGED;
        HierarchyEvent allEv = new HierarchyEvent(src, HierarchyEvent.HIERARCHY_CHANGED, changed, parent, all);
        check(allEv.getChangeFlags() == all, "all flags combined round-trip");
        // ANCESTOR events ignore change flags in getChangeFlags (4-arg ctor keeps 0)
        check(moved.getChangeFlags() == 0, "ancestor event change flags 0");
        // consume semantics: consume()/isConsumed() are protected in AWTEvent, exercised via a local subclass
        // consume semantics: HIERARCHY_CHANGED is not consumable; consume() is a no-op
        Consumable ev = new Consumable();
        check(!ev.consumedFlag(), "fresh not consumed");
        ev.consumeIt();
        check(!ev.consumedFlag(), "consume() is a no-op for hierarchy events");
        // toString
        check(e.toString().startsWith("java.awt.event.HierarchyEvent["), "toString starts with class name");
    }

    static class Consumable extends HierarchyEvent {
        Consumable() { super(new java.awt.Component() { }, HierarchyEvent.HIERARCHY_CHANGED, null, null); }
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
        java.awt.event.HierarchyEvent e = new java.awt.event.HierarchyEvent(src, java.awt.event.HierarchyEvent.HIERARCHY_CHANGED, src, null, java.awt.event.HierarchyEvent.SHOWING_CHANGED);
        java.awt.event.HierarchyEvent back = roundTrip(e);
        check(back.getID() == java.awt.event.HierarchyEvent.HIERARCHY_CHANGED, "serialization preserves id");
        check(back.getChangeFlags() == java.awt.event.HierarchyEvent.SHOWING_CHANGED, "serialization preserves change flags");
        check(back.getComponent() == null, "EventObject source is transient: component null after round-trip");
        check(back.getChanged() != null, "serialization preserves changed");
        check(back.getChangedParent() == null, "serialization preserves null parent");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
