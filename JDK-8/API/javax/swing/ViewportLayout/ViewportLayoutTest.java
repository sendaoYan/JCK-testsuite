/*
 * @test
 * @summary javax.swing.ViewportLayout conformance: type shape via reflection + layout
 *          contract (default installation, size delegation, stretch-to-viewport layout;
 *          headless-safe, Metal LaF, EDT).
 * @run main ViewportLayoutTest
 */

import java.awt.Dimension;
import java.awt.Rectangle;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.ViewportLayout;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;

public class ViewportLayoutTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    // ---- Part 1: structural shape (kept from the original skeleton) ----
    static void testShape() throws Exception {
        Class<?> c = Class.forName("javax.swing.ViewportLayout", false, ViewportLayoutTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(hasInterface(c, "java.awt.LayoutManager"), "implements java.awt.LayoutManager");
        check(hasInterface(c, "java.io.Serializable"), "implements java.io.Serializable");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("addLayoutComponent"), "method addLayoutComponent");
        check(names.contains("removeLayoutComponent"), "method removeLayoutComponent");
        check(names.contains("preferredLayoutSize"), "method preferredLayoutSize");
        check(names.contains("minimumLayoutSize"), "method minimumLayoutSize");
        check(names.contains("layoutContainer"), "method layoutContainer");
    }

    // ---- Part 2: black-box contract ----
    static void testBlackBox() {
        // Installed by default on every JViewport.
        JViewport vp = new JViewport();
        check(vp.getLayout() instanceof ViewportLayout, "default viewport layout is ViewportLayout");
        ViewportLayout layout = new ViewportLayout();

        // Size delegation: preferred/minimum come from the view (viewport insets are 0).
        JPanel view = new JPanel();
        view.setPreferredSize(new Dimension(37, 23));
        view.setMinimumSize(new Dimension(5, 6));
        vp.setView(view);
        check(new Dimension(37, 23).equals(layout.preferredLayoutSize(vp)),
                "preferredLayoutSize delegates to view preferred size");
        check(new Dimension(4, 4).equals(layout.minimumLayoutSize(vp)),
                "minimumLayoutSize constant 4x4 (ignores view minimum)");
        check(new Dimension(4, 4).equals(layout.minimumLayoutSize(new JViewport())),
                "minimumLayoutSize constant 4x4 without view");
        check(new Dimension(0, 0).equals(layout.preferredLayoutSize(new JViewport())),
                "preferredLayoutSize 0x0 without view");

        // Stretch-to-viewport: after layoutContainer the view fills the viewport.
        vp.setSize(50, 40);
        layout.layoutContainer(vp);
        check(new Rectangle(0, 0, 50, 40).equals(view.getBounds()),
                "layoutContainer stretches view to viewport size");

        // add/removeLayoutComponent are tolerated no-ops (String constraint form).
        layout.addLayoutComponent("x", new JPanel());
        check(true, "addLayoutComponent no-throw");
        layout.removeLayoutComponent(new JPanel());
        check(true, "removeLayoutComponent no-throw");

        // layoutContainer requires a JViewport: any other container fails the cast.
        final JPanel plain = new JPanel();
        try {
            layout.layoutContainer(plain);
            check(false, "layoutContainer on non-viewport should throw CCE");
        } catch (ClassCastException expected) {
            check(true, "layoutContainer on non-viewport CCE");
        }
    }

    public static void main(String[] args) throws Exception {
        try {
            UIManager.setLookAndFeel(new MetalLookAndFeel());
        } catch (Exception e) {
            throw new AssertionError("cannot install Metal LaF", e);
        }
        testShape();
        SwingUtilities.invokeAndWait(new Runnable() { public void run() { testBlackBox(); } });
        System.out.println("PASS (" + passed + " assertions)");
    }
}
