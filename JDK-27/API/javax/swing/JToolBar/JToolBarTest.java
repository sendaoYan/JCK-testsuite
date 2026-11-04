/*
 * @test
 * @summary javax.swing.JToolBar conformance: white-box structure + black-box behavior.
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
        System.out.println("PASS (" + passed + " assertions)");
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
