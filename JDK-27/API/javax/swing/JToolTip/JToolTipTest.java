/*
 * @test
 * @summary javax.swing.JToolTip conformance: white-box structure + black-box behavior
 *          + deep component-layer contracts (null-safe roundtrips, UI binding, property split).
 * @run main JToolTipTest
 */

import java.awt.EventQueue;
import java.lang.reflect.Modifier;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolTip;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;

public class JToolTipTest {
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

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.JToolTip");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JComponent"), "superclass");
        check(!Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.accessibility.Accessible"), "interface 0");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        UIManager.setLookAndFeel(new MetalLookAndFeel());
        JToolTip tip = new JToolTip();
        check(tip.getComponent() == null, "component initially null");
        check(tip.getTipText() == null, "tip text initially null");
        tip.setTipText("tip text");
        check("tip text".equals(tip.getTipText()), "setTipText/getTipText");
        tip.setTipText("second");
        check("second".equals(tip.getTipText()), "setTipText overwrite");
        tip.setTipText(null);
        check(tip.getTipText() == null, "setTipText(null) clears, no NPE");
        JPanel c = new JPanel();
        tip.setComponent(c);
        check(tip.getComponent() == c, "setComponent/getComponent");
        tip.setComponent(null);
        check(tip.getComponent() == null, "setComponent(null) clears, no NPE");
        JButton host = new JButton("host");
        tip.setComponent(host);
        check(tip.getComponent() == host, "setComponent identity rebind");

        // UI binding under Metal
        check(tip.getUI() != null && tip.getUI().getClass().getName().endsWith("MetalToolTipUI"),
                "Metal binding installs MetalToolTipUI");
        check("ToolTipUI".equals(tip.getUIClassID()), "getUIClassID ToolTipUI");
        tip.updateUI();
        check(tip.getUI() != null, "updateUI rebinds no-throw");

        // accessibility context exists and is stable across construction
        check(tip.getAccessibleContext() != null, "getAccessibleContext non-null");
        check(tip.getAccessibleContext() == tip.getAccessibleContext(), "accessible context stable instance");

        // setToolTipText is the JComponent-level property, distinct from setTipText
        tip.setToolTipText("x");
        check(tip.getTipText() == null, "setToolTipText does not feed getTipText");
        check("x".equals(tip.getToolTipText()), "JComponent tooltip property roundtrip");
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(JToolTipTest::testWhiteBox);
        runOnEDT(JToolTipTest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }
}
