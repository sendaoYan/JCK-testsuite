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

    public static void main(String[] args) throws Exception {
        runOnEDT(JRadioButtonTest::testWhiteBox);
        runOnEDT(JRadioButtonTest::testBlackBox);
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
