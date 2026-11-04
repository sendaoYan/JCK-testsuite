/*
 * @test
 * @summary javax.swing.BoxLayout conformance: white-box structure + black-box behavior.
 * @run main BoxLayoutTest
 */

import java.awt.Dimension;
import java.lang.reflect.Modifier;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

public class BoxLayoutTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.BoxLayout");
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
        check(c.getInterfaces()[0].getName().equals("java.awt.LayoutManager2"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("java.io.Serializable"), "interface 1");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // axis constants are distinct
        check(BoxLayout.X_AXIS == 0, "X_AXIS");
        check(BoxLayout.Y_AXIS == 1, "Y_AXIS");
        check(BoxLayout.LINE_AXIS == 2, "LINE_AXIS");
        check(BoxLayout.PAGE_AXIS == 3, "PAGE_AXIS");

        JPanel p = new JPanel();
        BoxLayout layout = new BoxLayout(p, BoxLayout.Y_AXIS);
        check(layout.getTarget() == p, "getTarget");
        check(layout.getAxis() == BoxLayout.Y_AXIS, "getAxis");

        // layout manager protocol
        JButton b1 = new JButton("a");
        JButton b2 = new JButton("b");
        p.setLayout(layout);
        p.add(b1);
        p.add(b2);

        p.setSize(200, 200);
        layout.layoutContainer(p);
        check(b1.getX() == 0 && b2.getX() == 0, "vertical children share x");
        check(b2.getY() >= b1.getY() + b1.getHeight(), "vertical stacking order");

        Dimension pref = layout.preferredLayoutSize(p);
        check(pref.width >= 0 && pref.height >= 0, "preferred size valid");

        layout.invalidateLayout(p);
        layout.removeLayoutComponent(b1);
        check(true, "removeLayoutComponent no-op smoke");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
