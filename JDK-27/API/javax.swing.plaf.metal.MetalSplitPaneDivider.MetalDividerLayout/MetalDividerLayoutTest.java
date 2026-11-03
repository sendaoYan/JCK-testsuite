/*
 * @test
 * @summary javax.swing.plaf.metal.MetalSplitPaneDivider.MetalDividerLayout conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main MetalDividerLayoutTest
 */

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.LayoutManager;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import javax.swing.JButton;
import javax.swing.JSplitPane;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import javax.swing.plaf.metal.MetalSplitPaneUI;

public class MetalDividerLayoutTest {
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

    static void assertNoThrow(ThrowingRunnable r, String msg) {
        try { r.run(); }
        catch (Throwable t) { throw new AssertionError(msg + ": " + t, t); }
        passed++;
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(MetalDividerLayoutTest::testWhiteBox);
        runOnEDT(MetalDividerLayoutTest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.metal.MetalSplitPaneDivider$MetalDividerLayout");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.awt.LayoutManager"), "interface 0");
    }

    static void testBlackBox() throws Exception {
        // The enclosing MetalSplitPaneDivider class is package-private, so the
        // layout (whose synthetic constructor takes that type) is obtained via
        // reflection from a divider created through the public MetalSplitPaneUI
        // API, after installing the UI on a real JSplitPane.
        JSplitPane sp = new JSplitPane();
        BasicSplitPaneUI ui = new MetalSplitPaneUI();
        ui.installUI(sp);
        BasicSplitPaneDivider divider = ui.createDefaultDivider();
        check(divider != null, "createDefaultDivider non-null");

        Class<?> layoutClass = Class.forName("javax.swing.plaf.metal.MetalSplitPaneDivider$MetalDividerLayout");
        Constructor<?> ctor = layoutClass.getDeclaredConstructors()[0];
        ctor.setAccessible(true);
        LayoutManager lm = (LayoutManager) ctor.newInstance(divider);
        check(lm instanceof LayoutManager, "instanceof LayoutManager");

        Dimension min = lm.minimumLayoutSize(divider);
        Dimension pref = lm.preferredLayoutSize(divider);
        check(min != null, "minimumLayoutSize non-null");
        check(pref != null, "preferredLayoutSize non-null");
        check(min.width == 0 && min.height == 0, "minimum size is (0,0)");
        check(pref.width == 0 && pref.height == 0, "preferred size is (0,0)");

        assertNoThrow(() -> lm.layoutContainer(divider), "layoutContainer");
        assertNoThrow(() -> lm.addLayoutComponent("x", new JButton()), "addLayoutComponent");
        assertNoThrow(() -> lm.removeLayoutComponent(new JButton()), "removeLayoutComponent");
    }
}
