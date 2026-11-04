/*
 * @test
 * @summary javax.swing.JCheckBox conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main JCheckBoxTest
 */

import java.awt.EventQueue;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JToggleButton;

public class JCheckBoxTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.JCheckBox");
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
        check(c.getDeclaredField("BORDER_PAINTED_FLAT_CHANGED_PROPERTY").get(null).equals("borderPaintedFlat"),
                "BORDER_PAINTED_FLAT_CHANGED_PROPERTY value");
        check(c.getConstructor(String.class, boolean.class) != null, "ctor String,boolean");
        check(Class.forName("javax.swing.JCheckBox$AccessibleJCheckBox").getSuperclass().getName()
                .equals("javax.swing.JToggleButton$AccessibleJToggleButton"), "AccessibleJCheckBox nesting");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        JCheckBox box = new JCheckBox("Enable");
        check(box.getText().equals("Enable"), "ctor text");
        check(!box.isSelected(), "default unselected");

        // setSelected / isSelected round trip with ItemEvent recording
        final List<Integer> states = new ArrayList<>();
        box.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) { states.add(e.getStateChange()); }
        });
        box.setSelected(true);
        check(box.isSelected(), "setSelected(true) round trip");
        check(box.getModel().isSelected(), "model mirrors selected");
        check(states.size() == 1, "one ItemEvent on select");
        check(states.get(0) == ItemEvent.SELECTED, "SELECTED on select");
        box.setSelected(false);
        check(!box.isSelected(), "setSelected(false) round trip");
        check(states.size() == 2, "one ItemEvent on deselect");
        check(states.get(1) == ItemEvent.DESELECTED, "DESELECTED on deselect");
        box.setSelected(true);
        box.setSelected(true);
        check(states.size() == 3, "redundant setSelected(true) is silent");

        // selected constructor variant
        JCheckBox preset = new JCheckBox("Preset", true);
        check(preset.isSelected(), "ctor(text, true) selected");
        final List<Integer> presetStates = new ArrayList<>();
        preset.addItemListener(e -> presetStates.add(e.getStateChange()));
        preset.doClick();
        check(!preset.isSelected(), "doClick toggles checkbox off");
        check(presetStates.size() == 1 && presetStates.get(0) == ItemEvent.DESELECTED,
                "doClick fires DESELECTED");

        // mnemonic round trip
        box.setMnemonic('E');
        check(box.getMnemonic() == (int) 'E', "setMnemonic(char) keycode round trip");
        box.setMnemonic(KeyEvent.VK_C);
        check(box.getMnemonic() == KeyEvent.VK_C, "setMnemonic(int) round trip");

        // borderPainted: only round-trip (default is L&F driven for checkboxes)
        box.setBorderPainted(true);
        check(box.isBorderPainted(), "borderPainted true round trip");
        box.setBorderPainted(false);
        check(!box.isBorderPainted(), "borderPainted false round trip");

        // borderPaintedFlat round trip
        box.setBorderPaintedFlat(true);
        check(box.isBorderPaintedFlat(), "borderPaintedFlat true round trip");
        box.setBorderPaintedFlat(false);
        check(!box.isBorderPaintedFlat(), "borderPaintedFlat false round trip");

        // icon/text mutation chain
        box.setText("Disable");
        check(box.getText().equals("Disable"), "setText round trip");
        box.setToolTipText("tip");
        check(box.getToolTipText().equals("tip"), "toolTipText round trip");
        box.setActionCommand("chk");
        check(box.getActionCommand().equals("chk"), "actionCommand round trip");

        // enabled chain
        box.setEnabled(false);
        check(!box.isEnabled(), "setEnabled(false) round trip");
        int before = states.size();
        box.doClick();
        check(states.size() == before, "disabled doClick does not change selection");
        check(box.isSelected(), "selection retained while disabled");
        box.setEnabled(true);
        check(box.isEnabled(), "setEnabled(true) round trip");

        // realized-state chain
        JFrame frame = new JFrame("JCheckBoxTest");
        try {
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.getContentPane().add(box);
            frame.pack();
            frame.setVisible(true);
            check(frame.isVisible(), "frame visible");
            check(box.isDisplayable(), "checkbox displayable in realized frame");
            check(box.isVisible(), "checkbox visible in realized frame");
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
        check(!new JCheckBox().isSelected(), "no-arg unselected");
        check(new JCheckBox(ic).getIcon() == ic, "icon ctor");
        check(new JCheckBox("ci", ic).getIcon() == ic, "text+icon ctor");
        check(new JCheckBox("cs", true).isSelected(), "text+selected ctor");
        check(!new JCheckBox("cs", false).isSelected(), "text+selected ctor false");
        check(new JCheckBox(ic, true).isSelected(), "icon+selected ctor");
        check(new JCheckBox("csc", ic, true).isSelected(), "text+icon+selected ctor");
        check(new JCheckBox((String) null, (javax.swing.Icon) null, true).isSelected(),
                "null text/icon with selected ctor");

        // checkbox-specific defaults under Metal
        JCheckBox b0 = new JCheckBox();
        check(b0.getModel() instanceof JToggleButton.ToggleButtonModel, "ToggleButtonModel inherited");
        check("CheckBoxUI".equals(b0.getUIClassID()), "getUIClassID CheckBoxUI");
        check("javax.swing.plaf.metal.MetalCheckBoxUI".equals(b0.getUI().getClass().getName()),
                "Metal installs MetalCheckBoxUI");
        check(!b0.isBorderPainted(), "borderPainted default false (unlike plain buttons)");
        check(b0.isRolloverEnabled(), "rolloverEnabled default true");
        check(b0.isFocusPainted(), "focusPainted default true");

        // doClick toggles and fires ItemEvents
        JCheckBox b1 = new JCheckBox("c");
        List<ItemEvent> items = new ArrayList<>();
        b1.addItemListener((ItemListener) e -> items.add(e));
        b1.doClick();
        check(b1.isSelected(), "doClick selects checkbox");
        check(items.size() == 1 && items.get(0).getStateChange() == ItemEvent.SELECTED, "ItemEvent SELECTED");
        b1.doClick();
        check(!b1.isSelected(), "doClick again unselects");
        check(items.get(1).getStateChange() == ItemEvent.DESELECTED, "ItemEvent DESELECTED");

        // grouping keeps checkbox semantics exclusive while inside the group
        JCheckBox x = new JCheckBox("x"), y = new JCheckBox("y");
        javax.swing.ButtonGroup g = new javax.swing.ButtonGroup();
        g.add(b1); g.add(x); g.add(y);
        b1.doClick();
        y.doClick();
        check(y.isSelected() && !b1.isSelected(), "group exclusivity for checkboxes");
        check(g.getSelection() == y.getModel(), "group selection tracks checkbox model");
        g.remove(y);
        check(y.isSelected(), "removed checkbox keeps state");

        // UI auto-derives displayedMnemonicIndex from a matching mnemonic
        b1.setText("check");
        check(b1.getDisplayedMnemonicIndex() == -1, "displayedMnemonicIndex -1 before mnemonic");
        b1.setMnemonic('C');
        check(b1.getMnemonic() == 'C', "setMnemonic");
        check(b1.getDisplayedMnemonicIndex() == 0,
                "UI auto-derives displayedMnemonicIndex when mnemonic matches text");
        b1.setDisplayedMnemonicIndex(-1);
        check(b1.getDisplayedMnemonicIndex() == -1, "setDisplayedMnemonicIndex(-1) resets");
    }

    public static void main(String[] args) throws Exception {
        javax.swing.UIManager.setLookAndFeel(new javax.swing.plaf.metal.MetalLookAndFeel());
        runOnEDT(JCheckBoxTest::testWhiteBox);
        runOnEDT(JCheckBoxTest::testBlackBox);
        runOnEDT(JCheckBoxTest::testContract);
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
