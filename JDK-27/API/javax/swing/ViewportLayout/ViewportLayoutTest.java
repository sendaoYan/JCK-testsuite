/*
 * @test
 * @summary javax.swing.ViewportLayout conformance: white-box structure + black-box behavior.
 * @run main ViewportLayoutTest
 */

import java.lang.reflect.Modifier;

import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.ViewportLayout;

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
        ViewportLayout layout = new ViewportLayout();
        JViewport vp = new JViewport();
        vp.setLayout(layout);
        vp.setView(new JPanel());
        layout.layoutContainer(vp);
        check(layout.preferredLayoutSize(vp) != null, "preferredLayoutSize");
        layout.addLayoutComponent("x", new JPanel());
        layout.removeLayoutComponent(new JPanel());
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
