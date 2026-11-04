/*
 * @test
 * @summary javax.swing.JButton conformance: type shape + constructor/UI-delegate contract (headless-safe, Metal LaF).
 * @run main JButtonTest
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
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultButtonModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;

public class JButtonTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    static void testShape() throws Exception {
        Class<?> c = Class.forName("javax.swing.JButton", false, JButtonTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.AbstractButton"), "superclass");
        check(hasInterface(c, "javax.accessibility.Accessible"), "implements javax.accessibility.Accessible");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("updateUI"), "method updateUI");
        check(names.contains("getUIClassID"), "method getUIClassID");
        check(names.contains("isDefaultButton"), "method isDefaultButton");
    }

    static void testContract() throws Exception {
        // ---- constructor matrix
        JButton b0 = new JButton();
        check("".equals(b0.getText()), "no-arg text is the LaF default (empty string)");
        check("".equals(b0.getActionCommand()), "no-arg actionCommand falls back to text");
        check(b0.getIcon() == null, "no-arg icon null");
        JButton bi = new JButton((Icon) new ImageIcon(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB)));
        check(bi.getIcon() != null, "icon ctor");
        JButton bt = new JButton("text");
        check("text".equals(bt.getText()), "text ctor");
        check("text".equals(bt.getActionCommand()), "text ctor seeds actionCommand");
        ImageIcon ic = new ImageIcon(new BufferedImage(3, 2, BufferedImage.TYPE_INT_RGB));
        JButton bti = new JButton("t+i", ic);
        check("t+i".equals(bti.getText()) && bti.getIcon() == ic, "text+icon ctor");
        AbstractAction act = new AbstractAction("fromAction") {
            public void actionPerformed(ActionEvent e) { }
        };
        act.putValue(Action.MNEMONIC_KEY, Integer.valueOf('F'));
        act.putValue(Action.SHORT_DESCRIPTION, "tipA");
        JButton ba = new JButton(act);
        check("fromAction".equals(ba.getText()), "Action ctor syncs text");
        check(ba.getMnemonic() == 'F', "Action ctor syncs mnemonic");
        check("tipA".equals(ba.getToolTipText()), "Action ctor syncs tooltip");
        check(ba.getAction() == act, "Action ctor stores action");

        // ---- UI delegate identity under Metal
        check("ButtonUI".equals(b0.getUIClassID()), "getUIClassID ButtonUI");
        check("javax.swing.plaf.metal.MetalButtonUI".equals(b0.getUI().getClass().getName()),
                "Metal installs MetalButtonUI");
        JButton pre = new JButton("before");
        UIManager.setLookAndFeel(new MetalLookAndFeel()); // reinstall same LaF
        check("javax.swing.plaf.metal.MetalButtonUI".equals(pre.getUI().getClass().getName()),
                "setLookAndFeel updates existing button UI");

        // ---- default capability / default-button state
        check(b0.isDefaultCapable(), "default capable");
        check(!b0.isDefaultButton(), "not a default button without a root pane");

        // ---- model defaults and swap
        check(b0.getModel() instanceof DefaultButtonModel, "DefaultButtonModel by default");
        DefaultButtonModel m2 = new DefaultButtonModel();
        b0.setModel(m2);
        check(b0.getModel() == m2, "setModel identity");
        b0.updateUI(); // no-throw with swapped model
        passed++; // updateUI survived

        // ---- doClick plumbing through the model
        final List<ActionEvent> acts = new ArrayList<ActionEvent>();
        b0.setText("go");
        b0.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { acts.add(e); }
        });
        b0.doClick();
        check(acts.size() == 1 && "go".equals(acts.get(0).getActionCommand()), "doClick via model");

        // ---- selection state is observable but not a toggle for plain buttons
        final List<ItemEvent> items = new ArrayList<ItemEvent>();
        b0.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) { items.add(e); }
        });
        b0.setSelected(true);
        check(b0.isSelected(), "setSelected(true) reflected");
        check(items.size() == 1, "one ItemEvent on setSelected");
        check(b0.isRolloverEnabled(), "rolloverEnabled default true under Metal");

        // ---- alignment inheritance from AbstractButton remains valid here
        b0.setHorizontalAlignment(SwingConstants.LEFT);
        check(b0.getHorizontalAlignment() == SwingConstants.LEFT, "horizontalAlignment setter");
        b0.setVerticalAlignment(SwingConstants.TOP);
        check(b0.getVerticalAlignment() == SwingConstants.TOP, "verticalAlignment setter");

        // ---- change listeners present out of the box (UI + handler wiring)
        check(b0.getChangeListeners().length >= 1, "change listeners non-empty");
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
