/*
 * @test
 * @summary javax.swing.JToolBar conformance: white-box structure + black-box behavior
 *          + deep component-layer contracts (Action wiring, separators, orientation negatives).
 *          + deep component-layer contracts (Action wiring, separators, orientation negatives).
 * @key headful
 * @run main JToolBarTest
 */

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.lang.reflect.Modifier;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

public class JToolBarTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
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
        runOnEDT(JToolBarTest::testWhiteBox);
        runOnEDT(JToolBarTest::testBlackBox);
        runOnEDT(JToolBarTest::testDeepContracts);
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Part 3: deep component-layer contracts (Action wiring, separators, orientation negatives) ----
    static void testDeepContracts() throws Exception {
        // add(Action): button text synced from action name, enabled state propagates
        JToolBar tb = new JToolBar();
        final boolean[] fired = new boolean[1];
        Action act = new AbstractAction("TB") {
            public void actionPerformed(ActionEvent e) { fired[0] = true; }
        };
        JButton added = tb.add(act);
        check(added instanceof JButton, "add(Action) returns JButton");
        check("TB".equals(added.getText()), "button text synced from action name");
        check(added.getAction() == act, "button action is the given action");
        check(added.isEnabled(), "button enabled while action enabled");
        act.setEnabled(false);
        check(!added.isEnabled(), "action disable propagates to button");
        act.setEnabled(true);
        check(added.isEnabled(), "action re-enable propagates to button");
        added.doClick();
        check(fired[0], "button click dispatches through the action");

        // indexed child access edges
        JButton b1 = new JButton("1");
        tb.add(b1);
        check(tb.getComponentIndex(b1) == 1, "index after action button is 1");
        check(tb.getComponentIndex(new JButton("alien")) == -1, "foreign button index -1");
        check(tb.getComponentAtIndex(tb.getComponentCount()) == null, "getComponentAtIndex(count) null");
        check(tb.getComponentAtIndex(99) == null && tb.getComponentAtIndex(-1) == null,
                "getComponentAtIndex(99)/(-1) null");

        // separators: exact class and counting
        int before = tb.getComponentCount();
        tb.addSeparator();
        check(tb.getComponentCount() == before + 1, "addSeparator() adds one child");
        check("javax.swing.JToolBar$Separator".equals(
                tb.getComponent(tb.getComponentCount() - 1).getClass().getName()),
                "separator component class JToolBar$Separator");
        tb.addSeparator(new Dimension(11, 22));
        check(tb.getComponentCount() == before + 2, "addSeparator(dim) adds one child");

        // margin roundtrip
        Insets m = new Insets(1, 2, 3, 4);
        tb.setMargin(m);
        check(tb.getMargin().equals(m), "setMargin roundtrip");

        // orientation negatives with state kept
        tb.setOrientation(SwingConstants.VERTICAL);
        check(tb.getOrientation() == SwingConstants.VERTICAL, "setOrientation(VERTICAL)");
        try {
            tb.setOrientation(99);
            check(false, "setOrientation(99) must throw");
        } catch (IllegalArgumentException e) { check(true, "setOrientation(99) IAE"); }
        check(tb.getOrientation() == SwingConstants.VERTICAL, "orientation kept after rejection");
        try {
            tb.setOrientation(-5);
            check(false, "setOrientation(-5) must throw");
        } catch (IllegalArgumentException e) { check(true, "setOrientation(-5) IAE"); }
        check(tb.getOrientation() == SwingConstants.VERTICAL, "orientation kept after second rejection");

        check(tb.getUI() != null && tb.getUI().getClass().getName().endsWith("MetalToolBarUI"),
                "Metal binding installs MetalToolBarUI");
        check("ToolBarUI".equals(tb.getUIClassID()), "getUIClassID ToolBarUI");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.JToolBar");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JComponent"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 2, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.SwingConstants"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("javax.accessibility.Accessible"), "interface 1");
    }

    static void testBlackBox() throws Exception {
        JToolBar tb = new JToolBar();
        check(tb.getOrientation() == SwingConstants.HORIZONTAL, "default orientation HORIZONTAL");
        check(tb.isFloatable() == true, "default floatable");
        check(tb.isRollover() == false, "default rollover false");
        check(tb.isBorderPainted() == true, "default border painted");
        check(tb.getComponentCount() == 0, "default empty");
        check(tb.getMargin().equals(new Insets(0, 0, 0, 0)), "default margin zeros");

        JToolBar vert = new JToolBar(SwingConstants.VERTICAL);
        check(vert.getOrientation() == SwingConstants.VERTICAL, "vertical ctor");
        JToolBar named = new JToolBar("main");
        check("main".equals(named.getName()), "named ctor name");
        JToolBar namedVert = new JToolBar("v", SwingConstants.VERTICAL);
        check("v".equals(namedVert.getName()), "named+orient name");
        check(namedVert.getOrientation() == SwingConstants.VERTICAL, "named+orient orientation");

        JButton b1 = new JButton("one");
        JButton b2 = new JButton("two");
        check(tb.add(b1) == b1, "add returns component");
        tb.add(b2);
        check(tb.getComponentCount() == 2, "two components");
        check(tb.getComponentIndex(b1) == 0, "index b1 0");
        check(tb.getComponentIndex(b2) == 1, "index b2 1");
        check(tb.getComponentAtIndex(0) == b1, "component at 0");
        check(tb.getComponentAtIndex(1) == b2, "component at 1");
        check(tb.getComponentAtIndex(-1) == null, "component at -1 null");
        check(tb.getComponentAtIndex(2) == null, "component at 2 null");
        check(tb.getComponentIndex(new JButton()) == -1, "unknown index -1");

        tb.setFloatable(false);
        check(tb.isFloatable() == false, "floatable off");
        tb.setFloatable(true);
        check(tb.isFloatable() == true, "floatable on");

        tb.setRollover(true);
        check(tb.isRollover() == true, "rollover on");
        tb.setRollover(false);
        check(tb.isRollover() == false, "rollover off");

        tb.setOrientation(SwingConstants.VERTICAL);
        check(tb.getOrientation() == SwingConstants.VERTICAL, "setOrientation vertical");
        tb.setOrientation(SwingConstants.HORIZONTAL);
        check(tb.getOrientation() == SwingConstants.HORIZONTAL, "setOrientation horizontal");
        boolean iae = false;
        try { tb.setOrientation(12345); } catch (IllegalArgumentException e) { iae = true; }
        check(iae, "invalid orientation IAE");

        tb.setBorderPainted(false);
        check(tb.isBorderPainted() == false, "border unpainted");
        tb.setBorderPainted(true);
        check(tb.isBorderPainted() == true, "border painted");

        tb.setMargin(new Insets(1, 2, 3, 4));
        check(tb.getMargin().equals(new Insets(1, 2, 3, 4)), "margin set");
        tb.setMargin(null);
        check(tb.getMargin().equals(new Insets(0, 0, 0, 0)), "margin null resets");

        int before = tb.getComponentCount();
        tb.addSeparator();
        check(tb.getComponentCount() == before + 1, "addSeparator increments");
        check(tb.getComponentAtIndex(before) instanceof JSeparator, "separator is JSeparator");
        check(((JSeparator) tb.getComponentAtIndex(before)).getOrientation() == SwingConstants.VERTICAL,
              "separator vertical for horizontal toolbar");

        before = tb.getComponentCount();
        tb.addSeparator(new Dimension(10, 20));
        check(tb.getComponentCount() == before + 1, "addSeparator(dim) increments");

        final boolean[] fired = {false};
        Action act = new AbstractAction("Go") {
            public void actionPerformed(ActionEvent e) { fired[0] = true; }
        };
        JButton ab = tb.add(act);
        check(ab != null, "add Action returns JButton");
        check("Go".equals(ab.getText()), "action button text");
        ab.doClick();
        check(fired[0], "action fired via click");
    }
}
