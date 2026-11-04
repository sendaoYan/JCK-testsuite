/*
 * @test
 * @summary javax.swing.JRadioButton conformance: type shape + radio-group contract (headless-safe, Metal LaF).
 * @run main JRadioButtonTest
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
import javax.swing.JRadioButton;
import javax.swing.JToggleButton.ToggleButtonModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;

public class JRadioButtonTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    static void testShape() throws Exception {
        Class<?> c = Class.forName("javax.swing.JRadioButton", false, JRadioButtonTest.class.getClassLoader());
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
        JRadioButton b0 = new JRadioButton();
        check(!b0.isSelected(), "no-arg unselected");
        check(b0.getIcon() == null, "no-arg default icon null");
        check("r".equals(new JRadioButton("r").getText()), "text ctor");
        ImageIcon ic = new ImageIcon(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB));
        check(new JRadioButton(ic).getIcon() == ic, "icon ctor");
        check(new JRadioButton("ri", ic).getIcon() == ic, "text+icon ctor");
        check(new JRadioButton("rs", true).isSelected(), "text+selected ctor");
        check(!new JRadioButton("rs", false).isSelected(), "text+selected ctor false");
        check(new JRadioButton(ic, true).isSelected(), "icon+selected ctor");
        check(new JRadioButton("rsc", ic, true).isSelected(), "text+icon+selected ctor");

        // ---- radio-specific defaults under Metal
        check(b0.getModel() instanceof ToggleButtonModel, "ToggleButtonModel inherited");
        check("RadioButtonUI".equals(b0.getUIClassID()), "getUIClassID RadioButtonUI");
        check("javax.swing.plaf.metal.MetalRadioButtonUI".equals(b0.getUI().getClass().getName()),
                "Metal installs MetalRadioButtonUI");
        check(!b0.isBorderPainted(), "borderPainted default false");
        check(b0.isRolloverEnabled(), "rolloverEnabled default true");
        check(b0.isFocusPainted(), "focusPainted default true");

        // ---- grouped radios: clicking the selected one keeps it selected
        JRadioButton r1 = new JRadioButton("r1"), r2 = new JRadioButton("r2"), r3 = new JRadioButton("r3");
        final List<ItemEvent> items2 = new ArrayList<ItemEvent>();
        r2.addItemListener(new ItemListener() { public void itemStateChanged(ItemEvent e) { items2.add(e); } });
        ButtonGroup g = new ButtonGroup();
        g.add(r1); g.add(r2); g.add(r3);
        check(g.getButtonCount() == 3, "group count");
        r2.doClick();
        check(r2.isSelected() && !r1.isSelected() && !r3.isSelected(), "r2 selected exclusively");
        check(items2.size() == 1 && items2.get(0).getStateChange() == ItemEvent.SELECTED, "one SELECTED ItemEvent");
        r3.doClick();
        check(r3.isSelected() && !r2.isSelected(), "selection transfers to r3");
        check(items2.size() == 2 && items2.get(1).getStateChange() == ItemEvent.DESELECTED, "r2 DESELECTED");
        items2.clear();
        r3.doClick(); // clicking the already-selected radio keeps it selected
        check(r3.isSelected(), "re-clicking selected radio keeps selection");
        check(!r2.isSelected(), "other radios stay unselected");
        check(items2.isEmpty(), "no ItemEvent from clicking unselected sibling");
        check(g.getSelection() == r3.getModel(), "group selection is r3 model");
        g.clearSelection();
        check(!r3.isSelected() && g.getSelection() == null, "clearSelection empties radio group");

        // ---- removal restores independent toggle behaviour for the removed radio only
        g.remove(r3);
        r3.doClick();
        check(r3.isSelected(), "removed radio selects");
        r3.doClick();
        check(!r3.isSelected(), "ungrouped radio toggles off on second click");

        // ---- setSelected inside the group switches exclusively
        r1.doClick();
        r3.setSelected(true); // r3 is no longer in the group: it cannot steal r1's selection
        check(r3.isSelected(), "ungrouped setSelected stands");
        r2.setSelected(true);
        check(r2.isSelected() && !r1.isSelected(), "setSelected switches exclusively in group");

        // ---- icon plumbing
        b0.setSelectedIcon(ic);
        check(b0.getSelectedIcon() == ic, "selectedIcon identity");
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
