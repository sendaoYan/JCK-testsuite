/*
 * @test
 * @summary java.awt.event.ItemEvent conformance: white-box structure + black-box behavior.
 * @run main ItemEventTest
 */

import java.awt.AWTEvent;
import java.awt.ItemSelectable;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.reflect.Modifier;

public class ItemEventTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class DummyItemSelectable implements ItemSelectable {
        public Object[] getSelectedObjects() { return null; }
        public void addItemListener(ItemListener l) { }
        public void removeItemListener(ItemListener l) { }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.event.ItemEvent");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "ItemEvent is public");
        check(!Modifier.isAbstract(m), "ItemEvent is concrete");
        check(c.getSuperclass() == AWTEvent.class, "ItemEvent extends AWTEvent");
        check(c.getField("ITEM_STATE_CHANGED").getType() == int.class, "ITEM_STATE_CHANGED int");
        check(c.getField("SELECTED").getType() == int.class, "SELECTED int");
        check(c.getField("DESELECTED").getType() == int.class, "DESELECTED int");
        check(c.getConstructor(ItemSelectable.class, int.class, Object.class, int.class) != null, "has 4-arg ctor");
    }

    static void testBlackBox() throws Exception {
        DummyItemSelectable src = new DummyItemSelectable();
        Object item = new Object();
        ItemEvent e = new ItemEvent(src, ItemEvent.ITEM_STATE_CHANGED, item, ItemEvent.SELECTED);

        check(e.getID() == ItemEvent.ITEM_STATE_CHANGED, "getID round-trips");
        check(e.getSource() == src, "getSource round-trips");
        check(e.getItemSelectable() == src, "getItemSelectable returns source");
        check(e.getItem() == item, "getItem round-trips");
        check(e.getStateChange() == ItemEvent.SELECTED, "getStateChange round-trips");

        ItemEvent d = new ItemEvent(src, ItemEvent.ITEM_STATE_CHANGED, item, ItemEvent.DESELECTED);
        check(d.getStateChange() == ItemEvent.DESELECTED, "DESELECTED round-trips");

        // documented constants
        check(ItemEvent.ITEM_FIRST == 701, "ITEM_FIRST == 701");
        check(ItemEvent.ITEM_LAST == 701, "ITEM_LAST == 701");
        check(ItemEvent.ITEM_STATE_CHANGED == 701, "ITEM_STATE_CHANGED == 701");
        check(ItemEvent.SELECTED == 1, "SELECTED == 1");
        check(ItemEvent.DESELECTED == 2, "DESELECTED == 2");

        // paramString
        check(e.paramString().contains("ITEM_STATE_CHANGED"), "paramString id");
        check(e.paramString().contains("stateChange=SELECTED"), "paramString SELECTED");
        check(d.paramString().contains("stateChange=DESELECTED"), "paramString DESELECTED");

        // null item allowed
        ItemEvent nil = new ItemEvent(src, ItemEvent.ITEM_STATE_CHANGED, null, ItemEvent.SELECTED);
        check(nil.getItem() == null, "null item round-trips");

        check(e instanceof AWTEvent, "ItemEvent instanceof AWTEvent");
        // arbitrary state-change values round-trip (no validation)
        ItemEvent weird = new ItemEvent(src, ItemEvent.ITEM_STATE_CHANGED, item, 42);
        check(weird.getStateChange() == 42, "arbitrary state change round-trips");
        // consume semantics: consume()/isConsumed() are protected in AWTEvent, exercised via a local subclass
        // consume semantics: semantic event ids are pre-consumed by AWTEvent's constructor
        Consumable ev = new Consumable();
        check(ev.consumedFlag(), "ItemEvent" + " pre-consumed at construction");
        ev.consumeIt();
        check(ev.consumedFlag(), "consume keeps consumed");
        // toString
        check(e.toString().startsWith("java.awt.event.ItemEvent["), "toString starts with class name");
    }

    static class Consumable extends ItemEvent {
        Consumable() { super(new DummyItemSelectable(), ItemEvent.ITEM_STATE_CHANGED, "i", ItemEvent.SELECTED); }
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
        DummyItemSelectable src = new DummyItemSelectable();
        java.awt.event.ItemEvent e = new java.awt.event.ItemEvent(src, java.awt.event.ItemEvent.ITEM_STATE_CHANGED, "item", java.awt.event.ItemEvent.DESELECTED);
        java.awt.event.ItemEvent back = roundTrip(e);
        check(back.getID() == java.awt.event.ItemEvent.ITEM_STATE_CHANGED, "serialization preserves id");
        check("item".equals(back.getItem()), "serialization preserves item");
        check(back.getStateChange() == java.awt.event.ItemEvent.DESELECTED, "serialization preserves state change");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
