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

    public static void main(String[] args) throws Exception {
        runOnEDT(JCheckBoxTest::testWhiteBox);
        runOnEDT(JCheckBoxTest::testBlackBox);
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
