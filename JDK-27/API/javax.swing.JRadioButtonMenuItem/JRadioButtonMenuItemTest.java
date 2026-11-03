/*
 * @test
 * @summary javax.swing.JRadioButtonMenuItem conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main JRadioButtonMenuItemTest
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

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;

public class JRadioButtonMenuItemTest {
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
        Class<?> c = Class.forName("javax.swing.JRadioButtonMenuItem");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JMenuItem"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.accessibility.Accessible"), "interface 0");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // constructors + text/selection/UIClassID
        JRadioButtonMenuItem dflt = new JRadioButtonMenuItem();
        check("".equals(dflt.getText()), "default text is empty string");
        check(!dflt.isSelected(), "default not selected");
        JRadioButtonMenuItem red = new JRadioButtonMenuItem("Red", true);
        check("Red".equals(red.getText()), "JRadioButtonMenuItem(String,boolean) text");
        check(red.isSelected(), "JRadioButtonMenuItem(String,true) selected");
        JRadioButtonMenuItem blue = new JRadioButtonMenuItem("Blue", false);
        check(!blue.isSelected(), "JRadioButtonMenuItem(String,false) not selected");
        check("RadioButtonMenuItemUI".equals(red.getUIClassID()), "UIClassID is RadioButtonMenuItemUI");

        // selection round-trip + getSelectedObjects
        JRadioButtonMenuItem item = new JRadioButtonMenuItem("Choice");
        check(item.getSelectedObjects() == null, "getSelectedObjects null when deselected");
        item.setSelected(true);
        check(item.isSelected(), "setSelected(true) selects");
        check(item.getSelectedObjects() != null && item.getSelectedObjects().length == 1, "getSelectedObjects non-null when selected");
        check("Choice".equals(item.getSelectedObjects()[0]), "getSelectedObjects contains text");
        item.setSelected(false);
        check(!item.isSelected(), "setSelected(false) deselects");

        // ItemEvent round-trip
        JRadioButtonMenuItem ev = new JRadioButtonMenuItem("Toggle");
        final List<ItemEvent> itemEvents = new ArrayList<ItemEvent>();
        ItemListener il = e -> itemEvents.add(e);
        ev.addItemListener(il);
        ev.setSelected(true);
        check(itemEvents.size() == 1, "setSelected(true) fires one item event");
        check(itemEvents.get(0).getStateChange() == ItemEvent.SELECTED, "first item event is SELECTED");
        check(itemEvents.get(0).getItem() == ev, "item event item is the radio item");
        ev.setSelected(false);
        check(itemEvents.size() == 2, "setSelected(false) fires second item event");
        check(itemEvents.get(1).getStateChange() == ItemEvent.DESELECTED, "second item event is DESELECTED");
        ev.setSelected(false);
        check(itemEvents.size() == 2, "setSelected(false) on already-deselected is silent");

        // doClick toggles selection + fires action + item event
        JRadioButtonMenuItem click = new JRadioButtonMenuItem("Click");
        final List<ActionEvent> actions = new ArrayList<ActionEvent>();
        final List<ItemEvent> clickItems = new ArrayList<ItemEvent>();
        click.addActionListener(actions::add);
        click.addItemListener(clickItems::add);
        click.doClick(0);
        check(click.isSelected(), "doClick toggles from false to true");
        check(actions.size() == 1, "doClick fires one action event");
        check(clickItems.size() == 1 && clickItems.get(0).getStateChange() == ItemEvent.SELECTED, "doClick fires SELECTED item event");
        click.doClick(0);
        check(!click.isSelected(), "second doClick toggles back to false");
        check(clickItems.size() == 2 && clickItems.get(1).getStateChange() == ItemEvent.DESELECTED, "second doClick fires DESELECTED item event");

        // ButtonGroup mutual exclusion
        ButtonGroup group = new ButtonGroup();
        JRadioButtonMenuItem r1 = new JRadioButtonMenuItem("One");
        JRadioButtonMenuItem r2 = new JRadioButtonMenuItem("Two");
        JRadioButtonMenuItem r3 = new JRadioButtonMenuItem("Three");
        group.add(r1);
        group.add(r2);
        group.add(r3);
        check(group.getButtonCount() == 3, "ButtonGroup button count 3");
        r1.setSelected(true);
        check(r1.isSelected() && !r2.isSelected() && !r3.isSelected(), "selecting r1 leaves others deselected");
        check(group.getSelection() == r1.getModel(), "ButtonGroup selection is r1's model");
        r2.setSelected(true);
        check(!r1.isSelected() && r2.isSelected() && !r3.isSelected(), "selecting r2 deselects r1 (mutual exclusion)");
        check(group.getSelection() == r2.getModel(), "ButtonGroup selection switches to r2's model");

        // disabled item does not toggle on doClick
        r2.setEnabled(false);
        r2.doClick(0);
        check(r2.isSelected(), "disabled doClick does not toggle selection");

        // inherited JMenuItem plumbing
        check(r2.getSubElements().length == 0, "no sub-elements");
        check(r2.getComponent() == r2, "MenuElement.getComponent returns item");
        check(r2.getAccelerator() == null, "accelerator defaults null");
        KeyStroke ks = KeyStroke.getKeyStroke('R');
        r2.setAccelerator(ks);
        check(ks.equals(r2.getAccelerator()), "setAccelerator reflected");
        check(r2.getMnemonic() == 0, "mnemonic defaults 0");
        r2.setMnemonic('T');
        check(r2.getMnemonic() == 'T', "setMnemonic reflected");
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
        runOnEDT(JRadioButtonMenuItemTest::testWhiteBox);
        runOnEDT(JRadioButtonMenuItemTest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }
}
