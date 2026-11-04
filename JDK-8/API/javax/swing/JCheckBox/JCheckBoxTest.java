/*
 * @test
 * @summary javax.swing.JCheckBox conformance: type shape + checkbox-specific contract (headless-safe, Metal LaF).
 * @run main JCheckBoxTest
 */

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JToggleButton.ToggleButtonModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;

public class JCheckBoxTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    static void testShape() throws Exception {
        Class<?> c = Class.forName("javax.swing.JCheckBox", false, JCheckBoxTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JToggleButton"), "superclass");
        check(hasInterface(c, "javax.accessibility.Accessible"), "implements javax.accessibility.Accessible");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("updateUI"), "method updateUI");
        check(names.contains("getUIClassID"), "method getUIClassID");
    }

    static void testContract() throws Exception {
        // ---- constructor matrix
        JCheckBox b0 = new JCheckBox();
        check(!b0.isSelected(), "no-arg unselected");
        JCheckBox b1 = new JCheckBox("c");
        check("c".equals(b1.getText()), "text ctor");
        ImageIcon ic = new ImageIcon(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB));
        check(new JCheckBox(ic).getIcon() == ic, "icon ctor");
        check(new JCheckBox("ci", ic).getIcon() == ic, "text+icon ctor");
        check(new JCheckBox("cs", true).isSelected(), "text+selected ctor");
        check(!new JCheckBox("cs", false).isSelected(), "text+selected ctor false");
        check(new JCheckBox(ic, true).isSelected(), "icon+selected ctor");
        check(new JCheckBox("csc", ic, true).isSelected(), "text+icon+selected ctor");
        check(new JCheckBox((String) null, (javax.swing.Icon) null, true).isSelected(),
                "null text/icon with selected ctor");

        // ---- checkbox-specific defaults under Metal
        check(b0.getModel() instanceof ToggleButtonModel, "ToggleButtonModel inherited");
        check("CheckBoxUI".equals(b0.getUIClassID()), "getUIClassID CheckBoxUI");
        check("javax.swing.plaf.metal.MetalCheckBoxUI".equals(b0.getUI().getClass().getName()),
                "Metal installs MetalCheckBoxUI");
        check(!b0.isBorderPainted(), "borderPainted default false (unlike plain buttons)");
        check(b0.isRolloverEnabled(), "rolloverEnabled default true");
        check(b0.isFocusPainted(), "focusPainted default true");

        // ---- doClick toggles and fires ItemEvent
        final List<ItemEvent> items = new ArrayList<ItemEvent>();
        b1.addItemListener(new ItemListener() { public void itemStateChanged(ItemEvent e) { items.add(e); } });
        b1.doClick();
        check(b1.isSelected(), "doClick selects checkbox");
        check(items.size() == 1 && items.get(0).getStateChange() == ItemEvent.SELECTED, "ItemEvent SELECTED");
        b1.doClick();
        check(!b1.isSelected(), "doClick again unselects");
        check(items.get(1).getStateChange() == ItemEvent.DESELECTED, "ItemEvent DESELECTED");

        // ---- grouping keeps checkbox semantics exclusive while inside the group
        JCheckBox x = new JCheckBox("x"), y = new JCheckBox("y");
        ButtonGroup g = new ButtonGroup();
        g.add(b1); g.add(x); g.add(y);
        b1.doClick();
        y.doClick();
        check(y.isSelected() && !b1.isSelected(), "group exclusivity for checkboxes");
        check(g.getSelection() == y.getModel(), "group selection tracks checkbox model");
        g.remove(y);
        check(y.isSelected(), "removed checkbox keeps state");

        // ---- mnemonic + displayed index plumbing inherited
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
        UIManager.setLookAndFeel(new MetalLookAndFeel());
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                try {
                    testShape();
                    testContract();
                } catch (Exception e) { throw new RuntimeException(e); }
            }
        });
        System.out.println("PASS (" + passed + " assertions)");
    }
}
