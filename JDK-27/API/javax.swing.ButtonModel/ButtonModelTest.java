/*
 * @test
 * @summary javax.swing.ButtonModel conformance: white-box structure + black-box behavior.
 * @run main ButtonModelTest
 */

import java.lang.reflect.Modifier;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.DefaultButtonModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class ButtonModelTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    /** Minimal AbstractButton so ButtonGroup can drive plain DefaultButtonModels (no L&F). */
    static class TestButton extends AbstractButton {
        TestButton() { setModel(new DefaultButtonModel()); }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.ButtonModel");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == true, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.awt.ItemSelectable"), "interface 0");
        // supplemented structural assertions
        String[] mnames = {"isArmed", "isSelected", "isEnabled", "isPressed", "isRollover",
                           "setArmed", "setSelected", "setEnabled", "setPressed", "setRollover",
                           "setMnemonic", "getMnemonic", "setActionCommand", "getActionCommand",
                           "setGroup", "getGroup", "addActionListener", "removeActionListener",
                           "addItemListener", "removeItemListener", "addChangeListener", "removeChangeListener"};
        for (String m : mnames) {
            boolean found = false;
            for (java.lang.reflect.Method mm : c.getMethods())
                if (mm.getName().equals(m)) { found = true; break; }
            check(found, "interface declares " + m);
        }
        Class<?> def = Class.forName("javax.swing.DefaultButtonModel");
        check(java.util.Arrays.asList(def.getInterfaces()).toString().contains("ButtonModel"),
              "DefaultButtonModel implements ButtonModel");
        check(!Modifier.isAbstract(def.getModifiers()), "DefaultButtonModel concrete");
    }

    // ---- recording listeners ----
    static class StateRec implements ChangeListener {
        int count;
        public void stateChanged(ChangeEvent e) { count++; }
        void reset() { count = 0; }
    }
    static class ItemRec implements ItemListener {
        int count; ItemEvent last;
        public void itemStateChanged(ItemEvent e) { count++; last = e; }
        void reset() { count = 0; last = null; }
    }
    static class ActionRec implements ActionListener {
        int count; ActionEvent last;
        public void actionPerformed(ActionEvent e) { count++; last = e; }
        void reset() { count = 0; last = null; }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        javax.swing.ButtonModel m = new DefaultButtonModel();
        // defaults of the reference implementation
        check(m.isEnabled(), "default enabled true");
        check(!m.isArmed(), "default armed false");
        check(!m.isPressed(), "default pressed false");
        check(!m.isRollover(), "default rollover false");
        check(!m.isSelected(), "default selected false");
        check(m.getActionCommand() == null, "default actionCommand null");
        check(m.getMnemonic() == 0, "default mnemonic 0");
        check(m.getGroup() == null, "default group null");

        // state flips + ChangeEvent recording
        StateRec sr = new StateRec();
        m.addChangeListener(sr);
        m.setArmed(true);
        check(m.isArmed() && sr.count == 1, "setArmed(true) flips and fires ChangeEvent");
        m.setPressed(true);
        check(m.isPressed() && sr.count == 2, "setPressed(true) flips and fires ChangeEvent");
        m.setRollover(true);
        check(m.isRollover() && sr.count == 3, "setRollover(true) flips and fires ChangeEvent");
        m.setMnemonic('A');
        check(m.getMnemonic() == 'A' && sr.count == 4, "setMnemonic fires ChangeEvent");
        m.setMnemonic('A');
        check(sr.count == 4, "same mnemonic fires nothing");
        m.setActionCommand("save-now");
        check("save-now".equals(m.getActionCommand()), "actionCommand round trip");

        // setSelected: ItemEvent + ChangeEvent ordering/content
        ItemRec ir = new ItemRec();
        m.addItemListener(ir);
        m.setSelected(true);
        check(m.isSelected() && ir.count == 1, "setSelected(true) fires ItemEvent");
        check(ir.last.getStateChange() == ItemEvent.SELECTED, "ItemEvent state SELECTED");
        check(ir.last.getItemSelectable() == m, "ItemEvent source selectable is the model");
        check(sr.count == 5, "setSelected also fires ChangeEvent");
        m.setSelected(true);
        check(ir.count == 1 && sr.count == 5, "same selection state fires nothing");
        m.setSelected(false);
        check(ir.count == 2 && ir.last.getStateChange() == ItemEvent.DESELECTED, "deselect fires DESELECTED");

        // ActionEvent on press-release while armed
        ActionRec ar = new ActionRec();
        m.addActionListener(ar);
        m.setPressed(true);   // re-press (armed still true)
        m.setPressed(false);  // release while armed -> ActionEvent
        check(ar.count == 1, "release-while-armed fires one ActionEvent");
        check("save-now".equals(ar.last.getActionCommand()), "ActionEvent carries actionCommand");
        check(ar.last.getSource() == m, "ActionEvent source is the model");

        // reset rollover to false while still enabled so the disabled-block test is meaningful
        m.setRollover(false);
        check(!m.isRollover(), "rollover cleared while enabled");

        // disabling clears armed+pressed and blocks armed/pressed/rollover mutation
        m.setArmed(true);
        m.setPressed(true);
        m.setEnabled(false);
        check(!m.isEnabled(), "setEnabled(false)");
        check(!m.isArmed() && !m.isPressed(), "disable clears armed and pressed");
        m.setArmed(true);
        check(!m.isArmed(), "disabled model ignores setArmed");
        m.setPressed(true);
        check(!m.isPressed(), "disabled model ignores setPressed");
        m.setRollover(true);
        check(!m.isRollover(), "disabled model ignores setRollover");
        m.setEnabled(true);
        check(m.isEnabled(), "setEnabled(true) restores");
        m.setArmed(true);
        check(m.isArmed(), "armed works again after enable");

        // setGroup/getGroup + group-driven mutual exclusion with a second model
        ButtonGroup group = new ButtonGroup();
        TestButton b1 = new TestButton(), b2 = new TestButton();
        group.add(b1);
        group.add(b2);
        javax.swing.ButtonModel m1 = b1.getModel(), m2 = b2.getModel();
        check(m1.getGroup() == group && m2.getGroup() == group, "setGroup via ButtonGroup.add, getGroup same instance");
        group.setSelected(m1, true);
        check(m1.isSelected() && !m2.isSelected(), "group selects m1 only");
        group.setSelected(m2, true);
        check(m2.isSelected() && !m1.isSelected(), "group mutual exclusion: selecting m2 deselects m1");
        group.clearSelection();
        check(!m1.isSelected() && !m2.isSelected(), "clearSelection deselects the group model");

        // getSelectedObjects (ItemSelectable contract): DefaultButtonModel returns null
        check(m.getSelectedObjects() == null, "DefaultButtonModel.getSelectedObjects is null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
