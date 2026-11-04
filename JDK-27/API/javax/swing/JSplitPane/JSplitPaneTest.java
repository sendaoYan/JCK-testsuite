/*
 * @test
 * @summary javax.swing.JSplitPane conformance: white-box structure + black-box behavior
 *          + constants/defaults, lastDividerLocation sequence, weight matrix
 *          (headless-safe, Metal LaF).
 * @run main JSplitPaneTest
 */

import java.awt.Component;
import java.awt.EventQueue;
import java.lang.reflect.Modifier;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSplitPane;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;

public class JSplitPaneTest {
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
        Class<?> c = Class.forName("javax.swing.JSplitPane");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JComponent"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.accessibility.Accessible"), "interface 0");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // Default constructor creates a horizontal split with two buttons.
        JSplitPane sp = new JSplitPane();
        check(sp.getOrientation() == JSplitPane.HORIZONTAL_SPLIT, "default orientation horizontal");
        check(sp.getLeftComponent() != null && sp.getLeftComponent() instanceof JButton, "default left button");
        check(sp.getRightComponent() != null && sp.getRightComponent() instanceof JButton, "default right button");
        check(sp.getResizeWeight() == 0.0, "default resizeWeight 0.0");
        check("SplitPaneUI".equals(sp.getUIClassID()), "getUIClassID SplitPaneUI");

        // Orientation constructor.
        JSplitPane v = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        check(v.getOrientation() == JSplitPane.VERTICAL_SPLIT, "JSplitPane(VERTICAL_SPLIT)");

        // Component constructor roundtrip.
        JLabel left = new JLabel("L");
        JLabel right = new JLabel("R");
        JSplitPane sp2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        check(sp2.getLeftComponent() == left, "ctor left component");
        check(sp2.getRightComponent() == right, "ctor right component");

        // Continuous layout + orientation constructor.
        JSplitPane sp3 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, false, left, right);
        check(sp3.getOrientation() == JSplitPane.VERTICAL_SPLIT, "ctor vertical orientation");
        check(!sp3.isContinuousLayout(), "ctor continuousLayout false");
        JSplitPane sp4 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
        check(sp4.isContinuousLayout(), "ctor continuousLayout true");

        // Invalid orientation throws IAE.
        try {
            new JSplitPane(999);
            check(false, "JSplitPane(999) should throw IAE");
        } catch (IllegalArgumentException expected) {
            check(true, "JSplitPane(999) IAE");
        }

        // Left/right component roundtrip.
        JSplitPane s = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false);
        JLabel c1 = new JLabel("c1");
        JLabel c2 = new JLabel("c2");
        s.setLeftComponent(c1);
        check(s.getLeftComponent() == c1, "setLeftComponent/getLeftComponent");
        s.setRightComponent(c2);
        check(s.getRightComponent() == c2, "setRightComponent/getRightComponent");

        // Top/bottom map onto left/right.
        JLabel top = new JLabel("top");
        s.setTopComponent(top);
        check(s.getTopComponent() == top, "setTopComponent/getTopComponent");
        check(s.getLeftComponent() == top, "setTopComponent updates left");
        JLabel bottom = new JLabel("bottom");
        s.setBottomComponent(bottom);
        check(s.getBottomComponent() == bottom, "setBottomComponent/getBottomComponent");
        check(s.getRightComponent() == bottom, "setBottomComponent updates right");

        // Divider size roundtrip + negative ignored.
        s.setDividerSize(10);
        check(s.getDividerSize() == 10, "setDividerSize/getDividerSize");
        s.setDividerSize(-1);
        check(s.getDividerSize() == 10, "setDividerSize(-1) ignored");

        // One-touch expandable roundtrip.
        s.setOneTouchExpandable(true);
        check(s.isOneTouchExpandable(), "setOneTouchExpandable(true)");
        s.setOneTouchExpandable(false);
        check(!s.isOneTouchExpandable(), "setOneTouchExpandable(false)");

        // Last divider location roundtrip.
        s.setLastDividerLocation(77);
        check(s.getLastDividerLocation() == 77, "setLastDividerLocation/getLastDividerLocation");

        // Resize weight roundtrip.
        s.setResizeWeight(0.5);
        check(s.getResizeWeight() == 0.5, "setResizeWeight/getResizeWeight");

        // Continuous layout roundtrip.
        s.setContinuousLayout(true);
        check(s.isContinuousLayout(), "setContinuousLayout(true)");
        s.setContinuousLayout(false);
        check(!s.isContinuousLayout(), "setContinuousLayout(false)");

        // Divider location roundtrip (field-backed).
        s.setDividerLocation(100);
        check(s.getDividerLocation() == 100, "setDividerLocation(int)/getDividerLocation");

        // Proportional divider location bounds.
        try {
            s.setDividerLocation(1.5);
            check(false, "setDividerLocation(1.5) should throw IAE");
        } catch (IllegalArgumentException expected) {
            check(true, "setDividerLocation(1.5) IAE");
        }
        try {
            s.setDividerLocation(-0.1);
            check(false, "setDividerLocation(-0.1) should throw IAE");
        } catch (IllegalArgumentException expected) {
            check(true, "setDividerLocation(-0.1) IAE");
        }

        // resetToPreferredSizes is callable.
        s.resetToPreferredSizes();
        check(true, "resetToPreferredSizes no-throw");

        // Removing components clears the slots.
        s.setLeftComponent(null);
        check(s.getLeftComponent() == null, "setLeftComponent(null)");
        s.setRightComponent(null);
        check(s.getRightComponent() == null, "setRightComponent(null)");
    }

    // ---- Part 3: constants, defaults, lastDividerLocation sequence, weight matrix ----
    static void testContractExtras() throws Exception {
        check(JSplitPane.HORIZONTAL_SPLIT == 1, "HORIZONTAL_SPLIT 1");
        check(JSplitPane.VERTICAL_SPLIT == 0, "VERTICAL_SPLIT 0");

        JSplitPane fresh = new JSplitPane();
        check(fresh.isContinuousLayout() == false, "default continuousLayout false");
        check(fresh.isOneTouchExpandable() == false, "default oneTouchExpandable false");
        check(fresh.getDividerSize() == 10, "default dividerSize 10");
        check("MetalSplitPaneUI".equals(fresh.getUI().getClass().getSimpleName()), "Metal installs MetalSplitPaneUI");
        check(fresh.getMinimumDividerLocation() >= 0, "minimum divider location non-negative");

        // lastDividerLocation: 0 by default, -1 right after the first move,
        // then the previous location after subsequent moves.
        JSplitPane s = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false);
        check(s.getLastDividerLocation() == 0, "default lastDividerLocation 0");
        s.setDividerLocation(40);
        check(s.getLastDividerLocation() == -1, "lastDividerLocation -1 after first move");
        s.setDividerLocation(60);
        check(s.getLastDividerLocation() == 40, "lastDividerLocation tracks previous");
        s.setDividerLocation(0.5);
        check(true, "setDividerLocation(0.5) legal");
        s.setDividerLocation(-5);
        check(true, "setDividerLocation(-5) no-throw");

        try {
            s.setResizeWeight(1.5);
            check(false, "setResizeWeight(1.5) should throw IAE");
        } catch (IllegalArgumentException expected) {
            check(true, "setResizeWeight(1.5) IAE");
        }
        try {
            s.setResizeWeight(-0.1);
            check(false, "setResizeWeight(-0.1) should throw IAE");
        } catch (IllegalArgumentException expected) {
            check(true, "setResizeWeight(-0.1) IAE");
        }
        s.setResizeWeight(0.0);
        check(s.getResizeWeight() == 0.0, "setResizeWeight boundary 0.0");
        s.setResizeWeight(1.0);
        check(s.getResizeWeight() == 1.0, "setResizeWeight boundary 1.0");
    }

    public static void main(String[] args) throws Exception {
        try {
            UIManager.setLookAndFeel(new MetalLookAndFeel());
        } catch (Exception e) {
            throw new AssertionError("cannot install Metal LaF", e);
        }
        runOnEDT(JSplitPaneTest::testWhiteBox);
        runOnEDT(JSplitPaneTest::testBlackBox);
        runOnEDT(JSplitPaneTest::testContractExtras);
        System.out.println("PASS (" + passed + " assertions)");
    }
}
