/*
 * @test
 * @summary javax.swing.plaf.metal.MetalTabbedPaneUI.TabbedPaneLayout conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main TabbedPaneLayoutTest
 */

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.LayoutManager;
import java.lang.reflect.Modifier;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.plaf.metal.MetalTabbedPaneUI;

public class TabbedPaneLayoutTest {
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
        runOnEDT(TabbedPaneLayoutTest::testWhiteBox);
        runOnEDT(TabbedPaneLayoutTest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.metal.MetalTabbedPaneUI$TabbedPaneLayout");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.plaf.basic.BasicTabbedPaneUI$TabbedPaneLayout"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static void testBlackBox() throws Exception {
        JTabbedPane tp = new JTabbedPane();
        tp.addTab("a", new JLabel());
        // Install the UI first so the layout's tabPane back-reference is set;
        // the layout reads that field, not the passed-in container.
        MetalTabbedPaneUI ui = new MetalTabbedPaneUI();
        ui.installUI(tp);
        MetalTabbedPaneUI.TabbedPaneLayout l = ui.new TabbedPaneLayout();
        check(l instanceof LayoutManager, "instanceof LayoutManager");

        Dimension pref = l.preferredLayoutSize(tp);
        Dimension min = l.minimumLayoutSize(tp);
        check(pref != null, "preferredLayoutSize non-null");
        check(min != null, "minimumLayoutSize non-null");

        assertNoThrow(() -> l.layoutContainer(tp), "layoutContainer");
        assertNoThrow(() -> l.addLayoutComponent("x", new JButton()), "addLayoutComponent");
        assertNoThrow(() -> l.removeLayoutComponent(new JButton()), "removeLayoutComponent");
    }
}
