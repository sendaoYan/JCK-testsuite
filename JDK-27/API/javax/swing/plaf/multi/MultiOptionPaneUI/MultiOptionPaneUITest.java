/*
 * @test
 * @summary javax.swing.plaf.multi.MultiOptionPaneUI conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main MultiOptionPaneUITest
 */

import java.awt.EventQueue;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.lang.reflect.Modifier;
import java.util.Vector;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.multi.MultiLookAndFeel;
import javax.swing.plaf.multi.MultiOptionPaneUI;
import javax.swing.JOptionPane;

public class MultiOptionPaneUITest {
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

    // Expose the protected uis vector so we can exercise real multiplexing without global state.
    static final class Exposed extends MultiOptionPaneUI {
        void seedFrom(JComponent c) { uis.addElement(c.getUI()); }
        int uiCount() { return uis.size(); }
    }

    static boolean sameDim(Dimension a, Dimension b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.width == b.width && a.height == b.height;
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(MultiOptionPaneUITest::testWhiteBox);
        runOnEDT(MultiOptionPaneUITest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.multi.MultiOptionPaneUI");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.plaf.OptionPaneUI"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
        check(c.getConstructor() != null, "public no-arg ctor");
        check(c.getDeclaredField("uis") != null, "protected uis field");
        check(c.getMethod("getUIs") != null, "getUIs");
        check(c.getMethod("createUI", JComponent.class) != null, "createUI");
        check(c.getMethod("contains", JComponent.class, int.class, int.class) != null, "contains");
        check(c.getMethod("update", Graphics.class, JComponent.class) != null, "update");
        check(c.getMethod("installUI", JComponent.class) != null, "installUI");
        check(c.getMethod("uninstallUI", JComponent.class) != null, "uninstallUI");
        check(c.getMethod("paint", Graphics.class, JComponent.class) != null, "paint");
        check(c.getMethod("getPreferredSize", JComponent.class) != null, "getPreferredSize");
        check(c.getMethod("getMinimumSize", JComponent.class) != null, "getMinimumSize");
        check(c.getMethod("getMaximumSize", JComponent.class) != null, "getMaximumSize");
        check(c.getMethod("getAccessibleChildrenCount", JComponent.class) != null, "getAccessibleChildrenCount");
        check(c.getMethod("getAccessibleChild", JComponent.class, int.class) != null, "getAccessibleChild");
    }

    static void testBlackBox() throws Exception {
        JOptionPane comp = new JOptionPane("message");

        ComponentUI created = MultiOptionPaneUI.createUI(comp);
        check(created != null, "createUI non-null");
        check(created instanceof ComponentUI, "createUI returns ComponentUI");

        Exposed factory = new Exposed();
        check(factory.uiCount() == 0, "fresh uis empty");
        Vector<ComponentUI> probe = new Vector<>();
        ComponentUI viaFactory = MultiLookAndFeel.createUIs(factory, probe, comp);
        check(viaFactory != null, "createUIs non-null");
        check(probe.size() >= 1, "createUIs populated >= 1 UI");
        check(probe.get(0) != null, "createUIs first UI non-null");

        ComponentUI own = comp.getUI();
        check(own != null, "component default UI non-null");
        Exposed mui = new Exposed();
        mui.seedFrom(comp);
        check(mui.uiCount() == 1, "seeded one UI");
        ComponentUI[] all = mui.getUIs();
        check(all != null && all.length == 1 && all[0] == own, "getUIs returns seeded UI");

        BufferedImage img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics g = img.getGraphics();
        g.setClip(0, 0, 64, 64);
        mui.paint(g, comp);
        mui.update(g, comp);
        mui.selectInitialValue(comp);
        check(true, "selectInitialValue no error");
        check(mui.containsCustomComponents(comp) == false, "containsCustomComponents false for plain message");
        g.dispose();
        check(true, "paint/update no error");

        check(mui.contains(comp, 0, 0) == own.contains(comp, 0, 0), "contains dispatches to first UI");
        check(mui.getAccessibleChildrenCount(comp) == own.getAccessibleChildrenCount(comp), "getAccessibleChildrenCount dispatches");
        mui.getAccessibleChild(comp, 0);
        check(true, "getAccessibleChild no error");

        check(sameDim(mui.getPreferredSize(comp), own.getPreferredSize(comp)), "getPreferredSize dispatches");
        check(sameDim(mui.getMinimumSize(comp), own.getMinimumSize(comp)), "getMinimumSize dispatches");
        check(sameDim(mui.getMaximumSize(comp), own.getMaximumSize(comp)), "getMaximumSize dispatches");

        mui.installUI(comp);
        mui.uninstallUI(comp);
        check(true, "installUI/uninstallUI no error");
    }
}
