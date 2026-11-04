/*
 * @test
 * @summary javax.swing.SpringLayout conformance: white-box structure + black-box behavior.
 * @run main SpringLayoutTest
 */

import java.lang.reflect.Modifier;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.Spring;
import javax.swing.SpringLayout;

public class SpringLayoutTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.SpringLayout");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(!Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.awt.LayoutManager2"), "interface 0");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        SpringLayout layout = new SpringLayout();
        JPanel panel = new JPanel(layout);
        JButton b1 = new JButton();
        JButton b2 = new JButton();
        panel.add(b1);
        panel.add(b2);

        layout.putConstraint(SpringLayout.WEST, b1, 5, SpringLayout.WEST, panel);
        Spring s = layout.getConstraint(SpringLayout.WEST, b1);
        check(s != null, "getConstraint after putConstraint");
        check(layout.getConstraints(b1) != null, "getConstraints");

        check(layout.minimumLayoutSize(panel) != null, "minimumLayoutSize");
        check(layout.preferredLayoutSize(panel) != null, "preferredLayoutSize");
        check(layout.maximumLayoutSize(panel) != null, "maximumLayoutSize");
        check(layout.getLayoutAlignmentX(panel) == 0.5f, "alignmentX");
        check(layout.getLayoutAlignmentY(panel) == 0.5f, "alignmentY");

        layout.addLayoutComponent("x", b2);
        layout.addLayoutComponent(b1, "constraint");
        layout.removeLayoutComponent(b2);
        layout.invalidateLayout(panel);
        layout.layoutContainer(panel);
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
