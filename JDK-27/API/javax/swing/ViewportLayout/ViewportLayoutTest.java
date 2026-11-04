/*
 * @test
 * @summary javax.swing.ViewportLayout conformance: white-box structure + black-box
 *          behavior (default installation, size delegation, stretch layout, CCE
 *          reverse; headless-safe, Metal LaF, EDT).
 * @run main ViewportLayoutTest
 */

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Rectangle;
import java.lang.reflect.Modifier;

import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.UIManager;
import javax.swing.ViewportLayout;
import javax.swing.plaf.metal.MetalLookAndFeel;

public class ViewportLayoutTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.ViewportLayout");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(!Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 2, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.awt.LayoutManager"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("java.io.Serializable"), "interface 1");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // Installed by default on every JViewport.
        JViewport vp = new JViewport();
        check(vp.getLayout() instanceof ViewportLayout, "default viewport layout is ViewportLayout");
        ViewportLayout layout = new ViewportLayout();

        // Size delegation: preferred comes from the view (constant minimum 4x4).
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
        JPanel plain = new JPanel();
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
        EventQueue.invokeAndWait(() -> {
            try {
                testWhiteBox();
                testBlackBox();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        System.out.println("PASS (" + passed + " assertions)");
    }
}
