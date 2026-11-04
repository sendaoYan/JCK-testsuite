/*
 * @test
 * @summary javax.swing.JRadioButton conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main JRadioButtonTest
 */

import java.awt.EventQueue;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JFrame;
import javax.swing.JRadioButton;
import javax.swing.JToggleButton;

public class JRadioButtonTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.JRadioButton");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JToggleButton"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.accessibility.Accessible"), "interface 0");
        // supplemented structural checks
        check(c.getConstructor(String.class) != null, "ctor String");
        check(c.getConstructor(String.class, boolean.class) != null, "ctor String,boolean");
        check(Class.forName("javax.swing.JRadioButton$AccessibleJRadioButton").getSuperclass().getName()
                .equals("javax.swing.JToggleButton$AccessibleJToggleButton"), "AccessibleJRadioButton nesting");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        JRadioButton one = new JRadioButton("First");
        JRadioButton two = new JRadioButton("Second");
        check(one.getText().equals("First"), "ctor text one");
        check(two.getText().equals("Second"), "ctor text two");
        check(!one.isSelected() && !two.isSelected(), "both default unselected");
        check(one.getModel().getGroup() == null, "model group null before grouping");

        // standalone selection round trip with ItemEvent recording
        final List<Integer> oneStates = new ArrayList<>();
        one.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) { oneStates.add(e.getStateChange()); }
        });
        one.setSelected(true);
        check(one.isSelected(), "standalone setSelected(true) round trip");
        check(oneStates.size() == 1 && oneStates.get(0) == ItemEvent.SELECTED, "SELECTED ItemEvent");
        one.setSelected(false);
        check(!one.isSelected(), "standalone setSelected(false) round trip");
        check(oneStates.size() == 2 && oneStates.get(1) == ItemEvent.DESELECTED, "DESELECTED ItemEvent");

        // ButtonGroup mutual exclusion chain
        ButtonGroup group = new ButtonGroup();
        group.add(one);
        group.add(two);
        check(group.getButtonCount() == 2, "group holds both buttons");
        check(one.getModel().getGroup() == group, "one model bound to group");
        check(two.getModel().getGroup() == group, "two model bound to group");
        check(group.getSelection() == null, "no selection in group yet");

        one.setSelected(true);
        check(one.isSelected(), "one selected");
        check(!two.isSelected(), "two unselected");
        check(group.getSelection() == one.getModel(), "group selection is one's model");

        final List<Integer> twoStates = new ArrayList<>();
        two.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) { twoStates.add(e.getStateChange()); }
        });
        two.setSelected(true);
        check(two.isSelected(), "two selected");
        check(!one.isSelected(), "one automatically deselected by group");
        check(group.getSelection() == two.getModel(), "group selection switched to two's model");
        check(twoStates.size() == 1 && twoStates.get(0) == ItemEvent.SELECTED,
                "two fired SELECTED");
        check(oneStates.size() == 4 && oneStates.get(3) == ItemEvent.DESELECTED,
                "one fired DESELECTED when two took over");

        // deselecting the selected radio is refused by the group
        two.setSelected(false);
        check(two.isSelected(), "group refuses deselecting the active radio");

        // direct group API: setSelected(null selection keep) + getSelection identity
        ButtonModel modelTwo = two.getModel();
        group.clearSelection();
        check(group.getSelection() == null, "clearSelection empties group");
        check(!two.isSelected() && !one.isSelected(), "both radios cleared");
        two.setSelected(true);
        check(group.getSelection() == modelTwo, "getSelection identity after reselect");

        // doClick on a grouped radio also honors mutual exclusion
        one.doClick();
        check(one.isSelected() && !two.isSelected(), "doClick honors group exclusion");
        check(group.getSelection() == one.getModel(), "group selection after doClick");

        // mnemonic round trip
        one.setMnemonic('F');
        check(one.getMnemonic() == (int) 'F', "setMnemonic(char) round trip");
        one.setMnemonic(KeyEvent.VK_T);
        check(one.getMnemonic() == KeyEvent.VK_T, "setMnemonic(int) round trip");

        // action command chain
        one.setActionCommand("radio-one");
        check(one.getActionCommand().equals("radio-one"), "actionCommand round trip");

        // realized-state chain
        JFrame frame = new JFrame("JRadioButtonTest");
        try {
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.getContentPane().add(one);
            frame.getContentPane().add(two);
            frame.pack();
            frame.setVisible(true);
            check(frame.isVisible(), "frame visible");
            check(one.isDisplayable() && two.isDisplayable(), "radios displayable in realized frame");
            check(one.isSelected() && !two.isSelected(), "selection preserved when realized");
        } finally {
            frame.dispose();
        }
        check(!frame.isVisible(), "frame not visible after dispose");
    }

    // ---- Part 3: deep contract additions under Metal ----
    static void testContract() throws Exception {
        javax.swing.Icon ic = new javax.swing.ImageIcon(
                new java.awt.image.BufferedImage(2, 2, java.awt.image.BufferedImage.TYPE_INT_RGB));
        // full constructor matrix
        JRadioButton b0 = new JRadioButton();
        check(!b0.isSelected(), "no-arg unselected");
        check(b0.getIcon() == null, "no-arg default icon null");
        check(new JRadioButton(ic).getIcon() == ic, "icon ctor");
        check(new JRadioButton("ri", ic).getIcon() == ic, "text+icon ctor");
        check(new JRadioButton("rs", true).isSelected(), "text+selected ctor");
        check(!new JRadioButton("rs", false).isSelected(), "text+selected ctor false");
        check(new JRadioButton(ic, true).isSelected(), "icon+selected ctor");
        check(new JRadioButton("rsc", ic, true).isSelected(), "text+icon+selected ctor");

        // radio-specific defaults under Metal
        check(b0.getModel() instanceof JToggleButton.ToggleButtonModel, "ToggleButtonModel inherited");
        check("RadioButtonUI".equals(b0.getUIClassID()), "getUIClassID RadioButtonUI");
        check("javax.swing.plaf.metal.MetalRadioButtonUI".equals(b0.getUI().getClass().getName()),
                "Metal installs MetalRadioButtonUI");
        check(!b0.isBorderPainted(), "borderPainted default false");
        check(b0.isRolloverEnabled(), "rolloverEnabled default true");
        check(b0.isFocusPainted(), "focusPainted default true");

        // re-clicking the selected radio keeps it selected and stays quiet
        JRadioButton r1 = new JRadioButton("r1"), r3 = new JRadioButton("r3");
        List<ItemEvent> items3 = new ArrayList<>();
        r3.addItemListener((ItemListener) e -> items3.add(e));
        ButtonGroup g = new ButtonGroup();
        g.add(r1); g.add(r3);
        check(g.getButtonCount() == 2, "group count");
        r3.doClick();
        check(r3.isSelected() && !r1.isSelected(), "r3 selected exclusively");
        check(items3.size() == 1 && items3.get(0).getStateChange() == ItemEvent.SELECTED, "one SELECTED ItemEvent");
        r1.doClick();
        check(r1.isSelected() && !r3.isSelected(), "selection transfers to r1");
        check(items3.size() == 2 && items3.get(1).getStateChange() == ItemEvent.DESELECTED, "r3 DESELECTED");
        items3.clear();
        r1.doClick();
        check(r1.isSelected(), "re-clicking selected radio keeps selection");
        check(items3.isEmpty(), "no ItemEvent from clicking unselected sibling");
        check(g.getSelection() == r1.getModel(), "group selection is r1 model");

        // removal restores independent toggle behaviour for the removed radio only
        g.clearSelection();
        check(!r1.isSelected(), "clearSelection empties radio group");
        g.remove(r1);
        r1.doClick();
        check(r1.isSelected(), "removed radio selects");
        r1.doClick();
        check(!r1.isSelected(), "ungrouped radio toggles off on second click");
        r1.doClick();
        r3.setSelected(true);
        check(r3.isSelected() && r1.isSelected(), "grouped setSelected leaves ungrouped selection alone");

        // icon plumbing
        b0.setSelectedIcon(ic);
        check(b0.getSelectedIcon() == ic, "selectedIcon identity");
    }

    public static void main(String[] args) throws Exception {
        javax.swing.UIManager.setLookAndFeel(new javax.swing.plaf.metal.MetalLookAndFeel());
        runOnEDT(JRadioButtonTest::testWhiteBox);
        runOnEDT(JRadioButtonTest::testBlackBox);
        runOnEDT(JRadioButtonTest::testContract);
        System.out.println("PASS (" + passed + " assertions)");
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
}
