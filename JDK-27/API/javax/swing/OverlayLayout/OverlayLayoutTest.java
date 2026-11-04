/*
 * @test
 * @summary javax.swing.OverlayLayout conformance: white-box structure + black-box behavior.
 * @run main OverlayLayoutTest
 */

import java.awt.Dimension;
import java.lang.reflect.Modifier;

import javax.swing.JPanel;
import javax.swing.OverlayLayout;

public class OverlayLayoutTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.OverlayLayout");
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
        JPanel target = new JPanel();
        OverlayLayout layout = new OverlayLayout(target);
        check(layout.getTarget() == target, "getTarget");
        target.setLayout(layout);
        JPanel child1 = new JPanel();
        JPanel child2 = new JPanel();
        target.add(child1);
        target.add(child2);

        layout.layoutContainer(target);
        Dimension pref = layout.preferredLayoutSize(target);
        Dimension min = layout.minimumLayoutSize(target);
        Dimension max = layout.maximumLayoutSize(target);
        check(pref != null, "preferredLayoutSize");
        check(min != null, "minimumLayoutSize");
        check(max != null, "maximumLayoutSize");
        check(layout.getLayoutAlignmentX(target) == 0.5f, "layoutAlignmentX center");
        check(layout.getLayoutAlignmentY(target) == 0.5f, "layoutAlignmentY center");
        layout.removeLayoutComponent(child1);
        layout.invalidateLayout(target);
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
