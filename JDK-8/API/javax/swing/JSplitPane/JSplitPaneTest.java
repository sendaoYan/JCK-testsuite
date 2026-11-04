/*
 * @test
 * @summary javax.swing.JSplitPane conformance: type shape via reflection + split pane
 *          contract (constants, ctor matrix, left/top aliasing, divider/weight rules,
 *          lastDividerLocation sequence; headless-safe, Metal LaF, EDT).
 * @run main JSplitPaneTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;

public class JSplitPaneTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void checkThrows(Class<? extends Throwable> ex, Runnable r, String m) {
        try { r.run(); }
        catch (Throwable t) {
            if (!ex.isInstance(t)) throw new AssertionError(m + ": expected " + ex.getSimpleName() + " got " + t);
            passed++; return; }
        throw new AssertionError(m + ": expected " + ex.getSimpleName() + " not thrown");
    }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    // ---- Part 1: structural shape (kept from the original skeleton) ----
    static void testShape() throws Exception {
        Class<?> c = Class.forName("javax.swing.JSplitPane", false, JSplitPaneTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JComponent"), "superclass");
        check(hasInterface(c, "javax.accessibility.Accessible"), "implements javax.accessibility.Accessible");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("setUI"), "method setUI");
        check(names.contains("getUI"), "method getUI");
        check(names.contains("updateUI"), "method updateUI");
    }

    // ---- Part 2: constants, constructors, defaults ----
    static void testConstantsAndCtors() {
        check(JSplitPane.HORIZONTAL_SPLIT == 1, "HORIZONTAL_SPLIT 1");
        check(JSplitPane.VERTICAL_SPLIT == 0, "VERTICAL_SPLIT 0");

        JSplitPane sp = new JSplitPane();
        check(sp.getOrientation() == JSplitPane.HORIZONTAL_SPLIT, "default orientation horizontal");
        check(sp.getLeftComponent() != null && sp.getLeftComponent() instanceof JButton,
                "default ctor installs left button");
        check(sp.getRightComponent() != null && sp.getRightComponent() instanceof JButton,
                "default ctor installs right button");
        check(sp.isContinuousLayout() == false, "default continuousLayout false");
        check(sp.isOneTouchExpandable() == false, "default oneTouchExpandable false");
        check(sp.getResizeWeight() == 0.0, "default resizeWeight 0.0");
        check(sp.getDividerSize() == 10, "default dividerSize 10");
        check("SplitPaneUI".equals(sp.getUIClassID()), "getUIClassID SplitPaneUI");
        check("MetalSplitPaneUI".equals(sp.getUI().getClass().getSimpleName()), "Metal installs MetalSplitPaneUI");
        check(sp.getMinimumDividerLocation() >= 0, "minimum divider location non-negative");

        JSplitPane v = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        check(v.getOrientation() == JSplitPane.VERTICAL_SPLIT, "VERTICAL ctor orientation");

        JLabel left = new JLabel("L");
        JLabel right = new JLabel("R");
        JSplitPane sp2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        check(sp2.getLeftComponent() == left, "two-comp ctor left");
        check(sp2.getRightComponent() == right, "two-comp ctor right");
        JSplitPane sp3 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, false, left, right);
        check(sp3.getOrientation() == JSplitPane.VERTICAL_SPLIT, "four-arg ctor orientation");
        check(sp3.isContinuousLayout() == false, "four-arg ctor continuous false");
        check(sp3.getLeftComponent() == left && sp3.getRightComponent() == right, "four-arg ctor components");
        JSplitPane sp4 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
        check(sp4.isContinuousLayout(), "two-arg ctor continuous true");

        checkThrows(IllegalArgumentException.class,
                new Runnable() { public void run() { new JSplitPane(999); } },
                "JSplitPane(999) ctor IAE");
    }

    // ---- Part 3: component slots and aliasing ----
    static void testComponents() {
        JSplitPane s = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false);
        JLabel c1 = new JLabel("c1");
        JLabel c2 = new JLabel("c2");
        s.setLeftComponent(c1);
        check(s.getLeftComponent() == c1, "setLeftComponent round-trip");
        s.setRightComponent(c2);
        check(s.getRightComponent() == c2, "setRightComponent round-trip");
        // Top/bottom are aliases of left/right.
        JLabel top = new JLabel("top");
        s.setTopComponent(top);
        check(s.getTopComponent() == top, "setTopComponent round-trip");
        check(s.getLeftComponent() == top, "top aliases left");
        JLabel bottom = new JLabel("bottom");
        s.setBottomComponent(bottom);
        check(s.getBottomComponent() == bottom, "setBottomComponent round-trip");
        check(s.getRightComponent() == bottom, "bottom aliases right");
        s.setLeftComponent(null);
        check(s.getLeftComponent() == null, "setLeftComponent(null)");
        s.setRightComponent(null);
        check(s.getRightComponent() == null, "setRightComponent(null)");
    }

    // ---- Part 4: divider/weight/flags rules ----
    static void testDividerRules() {
        final JSplitPane s = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false);
        s.setDividerSize(11);
        check(s.getDividerSize() == 11, "setDividerSize round-trip");
        // JDK-8 stores negative sizes verbatim (no validation);
        // JDK-27 rejects the negative and keeps the previous value.
        s.setDividerSize(-1);
        check(s.getDividerSize() == -1, "setDividerSize(-1) stored on JDK-8");

        // lastDividerLocation: 0 by default, -1 right after the first move,
        // then the previous location after subsequent moves.
        check(s.getLastDividerLocation() == 0, "default lastDividerLocation 0");
        s.setDividerLocation(40);
        check(s.getDividerLocation() == 40, "setDividerLocation(int) round-trip");
        check(s.getLastDividerLocation() == -1, "lastDividerLocation -1 after first move");
        s.setDividerLocation(60);
        check(s.getLastDividerLocation() == 40, "lastDividerLocation tracks previous");
        s.setDividerLocation(0.5);
        check(true, "setDividerLocation(0.5) legal");
        s.setDividerLocation(-5);
        check(true, "setDividerLocation(-5) no-throw");

        checkThrows(IllegalArgumentException.class,
                new Runnable() { public void run() { s.setDividerLocation(1.5); } },
                "setDividerLocation(1.5) IAE");
        checkThrows(IllegalArgumentException.class,
                new Runnable() { public void run() { s.setDividerLocation(-0.1); } },
                "setDividerLocation(-0.1) IAE");

        checkThrows(IllegalArgumentException.class,
                new Runnable() { public void run() { s.setResizeWeight(1.5); } },
                "setResizeWeight(1.5) IAE");
        checkThrows(IllegalArgumentException.class,
                new Runnable() { public void run() { s.setResizeWeight(-0.1); } },
                "setResizeWeight(-0.1) IAE");
        s.setResizeWeight(0.5);
        check(s.getResizeWeight() == 0.5, "setResizeWeight round-trip");
        s.setResizeWeight(1.0);
        check(s.getResizeWeight() == 1.0, "setResizeWeight boundary 1.0");
        s.setResizeWeight(0.0);
        check(s.getResizeWeight() == 0.0, "setResizeWeight boundary 0.0");

        s.setOneTouchExpandable(true);
        check(s.isOneTouchExpandable(), "oneTouchExpandable on");
        s.setOneTouchExpandable(false);
        check(!s.isOneTouchExpandable(), "oneTouchExpandable off");
        s.setContinuousLayout(true);
        check(s.isContinuousLayout(), "continuousLayout on");
        s.setLastDividerLocation(77);
        check(s.getLastDividerLocation() == 77, "setLastDividerLocation round-trip");
        s.resetToPreferredSizes();
        check(true, "resetToPreferredSizes no-throw");
    }

    public static void main(String[] args) throws Exception {
        try {
            UIManager.setLookAndFeel(new MetalLookAndFeel());
        } catch (Exception e) {
            throw new AssertionError("cannot install Metal LaF", e);
        }
        testShape();
        SwingUtilities.invokeAndWait(new Runnable() { public void run() { testConstantsAndCtors(); } });
        SwingUtilities.invokeAndWait(new Runnable() { public void run() { testComponents(); } });
        SwingUtilities.invokeAndWait(new Runnable() { public void run() { testDividerRules(); } });
        System.out.println("PASS (" + passed + " assertions)");
    }
}
