/*
 * @test
 * @summary javax.swing.JCheckBoxMenuItem conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main JCheckBoxMenuItemTest
 */

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.KeyStroke;

public class JCheckBoxMenuItemTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static final class FixedIcon implements Icon {
        final int w, h;
        FixedIcon(int w, int h) { this.w = w; this.h = h; }
        public int getIconWidth() { return w; }
        public int getIconHeight() { return h; }
        public void paintIcon(Component c, Graphics g, int x, int y) {}
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.JCheckBoxMenuItem");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JMenuItem"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 2, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.SwingConstants"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("javax.accessibility.Accessible"), "interface 1");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // constructors + state/text/UIClassID
        JCheckBoxMenuItem dflt = new JCheckBoxMenuItem();
        check("".equals(dflt.getText()), "default text is empty string");
        check(!dflt.isSelected(), "default not selected");
        check(!dflt.getState(), "default getState false");
        JCheckBoxMenuItem checked = new JCheckBoxMenuItem("Checked", true);
        check("Checked".equals(checked.getText()), "JCheckBoxMenuItem(String,boolean) text");
        check(checked.isSelected(), "JCheckBoxMenuItem(String,true) selected");
        check(checked.getState(), "getState true when selected");
        JCheckBoxMenuItem unchecked = new JCheckBoxMenuItem("Unchecked", false);
        check(!unchecked.getState(), "JCheckBoxMenuItem(String,false) not selected");
        check("CheckBoxMenuItemUI".equals(checked.getUIClassID()), "UIClassID is CheckBoxMenuItemUI");

        // state round-trip via setState/setSelected + getSelectedObjects
        JCheckBoxMenuItem item = new JCheckBoxMenuItem("Feature");
        item.setState(true);
        check(item.isSelected(), "setState(true) selects");
        check(item.getState(), "getState reflects setState(true)");
        check(item.getSelectedObjects() != null && item.getSelectedObjects().length == 1, "getSelectedObjects non-null when selected");
        check("Feature".equals(item.getSelectedObjects()[0]), "getSelectedObjects contains text");
        item.setState(false);
        check(!item.isSelected(), "setState(false) deselects");
        check(item.getSelectedObjects() == null, "getSelectedObjects null when deselected");
        item.setSelected(true);
        check(item.getState(), "setSelected(true) reflected in getState");

        // ItemEvent round-trip: state change fires SELECTED then DESELECTED
        JCheckBoxMenuItem ev = new JCheckBoxMenuItem("Toggle");
        final List<ItemEvent> itemEvents = new ArrayList<ItemEvent>();
        ItemListener il = e -> itemEvents.add(e);
        ev.addItemListener(il);
        ev.setSelected(true);
        check(itemEvents.size() == 1, "setSelected(true) fires one item event");
        check(itemEvents.get(0).getStateChange() == ItemEvent.SELECTED, "first item event is SELECTED");
        check(itemEvents.get(0).getItem() == ev, "item event item is the checkbox item");
        ev.setSelected(false);
        check(itemEvents.size() == 2, "setSelected(false) fires second item event");
        check(itemEvents.get(1).getStateChange() == ItemEvent.DESELECTED, "second item event is DESELECTED");
        ev.setSelected(false);
        check(itemEvents.size() == 2, "setSelected(false) on already-deselected is silent");

        // doClick toggles selection + fires action + item event
        JCheckBoxMenuItem click = new JCheckBoxMenuItem("Click");
        final List<ActionEvent> actions = new ArrayList<ActionEvent>();
        final List<ItemEvent> clickItems = new ArrayList<ItemEvent>();
        click.addActionListener(actions::add);
        click.addItemListener(clickItems::add);
        click.doClick(0);
        check(click.isSelected(), "doClick toggles from false to true");
        check(actions.size() == 1, "doClick fires one action event");
        check(actions.get(0).getSource() == click, "action event source is the item");
        check(clickItems.size() == 1 && clickItems.get(0).getStateChange() == ItemEvent.SELECTED, "doClick fires SELECTED item event");
        click.doClick(0);
        check(!click.isSelected(), "second doClick toggles back to false");
        check(actions.size() == 2, "second doClick fires another action");
        check(clickItems.size() == 2 && clickItems.get(1).getStateChange() == ItemEvent.DESELECTED, "second doClick fires DESELECTED item event");

        // disabled item does not toggle on doClick
        click.setEnabled(false);
        click.doClick(0);
        check(!click.isSelected(), "disabled doClick does not toggle");
        check(actions.size() == 2, "disabled doClick fires no action");

        // inherited JMenuItem plumbing
        check(click.getSubElements().length == 0, "no sub-elements");
        check(click.getComponent() == click, "MenuElement.getComponent returns item");
        check(click.getAccelerator() == null, "accelerator defaults null");
        KeyStroke ks = KeyStroke.getKeyStroke('X');
        click.setAccelerator(ks);
        check(ks.equals(click.getAccelerator()), "setAccelerator reflected");
        check(click.getMnemonic() == 0, "mnemonic defaults 0");
        click.setMnemonic('K');
        check(click.getMnemonic() == 'K', "setMnemonic reflected");
    }

    interface ThrowingRunnable { void run() throws Exception; }

    static void runOnEDT(ThrowingRunnable r) throws Exception {
        try {
            EventQueue.invokeAndWait(() -> {
                try { r.run(); }
                catch (Exception e) { throw new RuntimeException(e); }
            });
        } catch (java.lang.reflect.InvocationTargetException e) {
            Throwable t = e.getCause();
            if (t instanceof RuntimeException) throw (RuntimeException) t;
            if (t instanceof Error) throw (Error) t;
            throw new RuntimeException(t);
        }
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(JCheckBoxMenuItemTest::testWhiteBox);
        runOnEDT(JCheckBoxMenuItemTest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }
}
