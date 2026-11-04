/*
 * @test
 * @summary javax.swing.JToggleButton conformance: type shape + toggle state contract (headless-safe, Metal LaF).
 * @run main JToggleButtonTest
 */

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.JToggleButton;
import javax.swing.JToggleButton.ToggleButtonModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;

public class JToggleButtonTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    static void testShape() throws Exception {
        Class<?> c = Class.forName("javax.swing.JToggleButton", false, JToggleButtonTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.AbstractButton"), "superclass");
        check(hasInterface(c, "javax.accessibility.Accessible"), "implements javax.accessibility.Accessible");
        check(Class.forName("javax.swing.JToggleButton$ToggleButtonModel") != null,
                "nested ToggleButtonModel class");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("updateUI"), "method updateUI");
        check(names.contains("getUIClassID"), "method getUIClassID");
    }

    static void testContract() throws Exception {
        // ---- constructor matrix (8 public variants)
        JToggleButton b0 = new JToggleButton();
        check(!b0.isSelected(), "no-arg unselected");
        check("".equals(b0.getText()) || b0.getText() == null, "no-arg text blank");
        JToggleButton b1 = new JToggleButton("t");
        check("t".equals(b1.getText()), "text ctor");
        ImageIcon ic = new ImageIcon(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB));
        check(new JToggleButton(ic).getIcon() == ic, "icon ctor");
        check(new JToggleButton("ti", ic).getIcon() == ic, "text+icon ctor");
        check(new JToggleButton("s", true).isSelected(), "text+selected ctor true");
        check(!new JToggleButton("s", false).isSelected(), "text+selected ctor false");
        check(new JToggleButton(ic, true).isSelected(), "icon+selected ctor");
        check(new JToggleButton("tsc", ic, true).isSelected(), "text+icon+selected ctor");

        // ---- model and UI delegate
        check(b0.getModel() instanceof ToggleButtonModel, "ToggleButtonModel installed");
        check("ToggleButtonUI".equals(b0.getUIClassID()), "getUIClassID ToggleButtonUI");
        check("javax.swing.plaf.metal.MetalToggleButtonUI".equals(b0.getUI().getClass().getName()),
                "Metal installs MetalToggleButtonUI");
        check(!b0.isRolloverEnabled(), "rolloverEnabled default false for toggle buttons");

        // ---- doClick toggles selection and fires both Action and Item events
        final List<ActionEvent> acts = new ArrayList<ActionEvent>();
        final List<ItemEvent> items = new ArrayList<ItemEvent>();
        b1.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { acts.add(e); } });
        b1.addItemListener(new ItemListener() { public void itemStateChanged(ItemEvent e) { items.add(e); } });
        b1.doClick();
        check(b1.isSelected(), "doClick selects");
        check(acts.size() == 1, "doClick fired ActionEvent");
        check(items.size() == 1 && items.get(0).getStateChange() == ItemEvent.SELECTED, "doClick fired ItemEvent SELECTED");
        b1.doClick();
        check(!b1.isSelected(), "second doClick unselects");
        check(items.size() == 2 && items.get(1).getStateChange() == ItemEvent.DESELECTED,
                "second doClick fired ItemEvent DESELECTED");
        check(items.get(0).getItem() == b1, "ItemEvent item is the button (re-fired)");

        // ---- press/release state machine on the toggle model
        ToggleButtonModel mod = (ToggleButtonModel) b0.getModel();
        mod.setArmed(true);
        mod.setPressed(true);
        check(mod.isArmed() && mod.isPressed() && !mod.isSelected(), "armed+pressed keeps selection pending");
        mod.setPressed(false);
        check(mod.isSelected(), "release latches selection");
        mod.setPressed(true);
        mod.setPressed(false);
        check(!mod.isSelected(), "second release cycle unselects");
        // pressing again does not toggle anything — the flip only happens on release while armed
        mod.setPressed(true);
        check(!mod.isSelected(), "selection unchanged while pressed (flip waits for release)");
        mod.setPressed(false);
        check(mod.isSelected(), "release flips again");

        // ---- ButtonGroup exclusivity across toggles
        JToggleButton x = new JToggleButton("x"), y = new JToggleButton("y");
        ButtonGroup g = new ButtonGroup();
        g.add(b1); g.add(x); g.add(y);
        b1.doClick();
        check(b1.isSelected() && !x.isSelected() && !y.isSelected(), "one selected after first click");
        y.doClick();
        check(!b1.isSelected() && y.isSelected(), "clicking another group member transfers selection");
        check(g.getSelection() == y.getModel(), "group tracks the winner");

        // ---- icon plumbing
        b1.setSelectedIcon(ic);
        check(b1.getSelectedIcon() == ic, "selectedIcon identity");
        b1.setPressedIcon(ic);
        check(b1.getPressedIcon() == ic, "pressedIcon identity");
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
