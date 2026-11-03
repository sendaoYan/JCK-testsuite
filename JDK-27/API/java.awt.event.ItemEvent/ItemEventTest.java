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
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
