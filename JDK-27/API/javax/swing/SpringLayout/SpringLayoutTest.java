/*
 * @test
 * @summary javax.swing.SpringLayout conformance: white-box structure + black-box behavior.
 * @run main SpringLayoutTest
 */

import java.awt.Dimension;
import javax.swing.UIManager;
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


    // ---- Part 3: deep contract coverage ----
    static void testEdgeConstants() {
        check(SpringLayout.NORTH.equals("North"), "NORTH edge name");
        check(SpringLayout.SOUTH.equals("South"), "SOUTH edge name");
        check(SpringLayout.EAST.equals("East"), "EAST edge name");
        check(SpringLayout.WEST.equals("West"), "WEST edge name");
        check(SpringLayout.HORIZONTAL_CENTER.equals("HorizontalCenter"), "HORIZONTAL_CENTER edge name");
        check(SpringLayout.VERTICAL_CENTER.equals("VerticalCenter"), "VERTICAL_CENTER edge name");
        check(SpringLayout.BASELINE.equals("Baseline"), "BASELINE edge name");
        check(SpringLayout.WIDTH.equals("Width"), "WIDTH edge name");
        check(SpringLayout.HEIGHT.equals("Height"), "HEIGHT edge name");
    }

    static void testConstraintsObject() {
        // a fresh Constraints object exposes null springs until edges are attached
        SpringLayout.Constraints fresh = new SpringLayout.Constraints();
        check(fresh.getX() == null, "fresh Constraints x is null");
        check(fresh.getY() == null, "fresh Constraints y is null");
        check(fresh.getWidth() == null, "fresh Constraints width is null");
        check(fresh.getHeight() == null, "fresh Constraints height is null");
        // component-backed Constraints seeds width/height from the component's sizes
        JPanel sized = new JPanel();
        sized.setPreferredSize(new Dimension(25, 15));
        SpringLayout.Constraints fromComp = new SpringLayout.Constraints(sized);
        check(fromComp.getWidth().getPreferredValue() == 25, "component Constraints width preferred");
        check(fromComp.getHeight().getPreferredValue() == 15, "component Constraints height preferred");
        fresh.setX(Spring.constant(4));
        check(fresh.getX().getValue() == 4, "setX stores spring");
        fresh.setY(Spring.constant(5));
        fresh.setWidth(Spring.constant(6));
        fresh.setHeight(Spring.constant(7));
        check(fresh.getY().getValue() == 5 && fresh.getWidth().getValue() == 6 && fresh.getHeight().getValue() == 7,
                "setY/setWidth/setHeight store springs");
    }

    static void testPutConstraintAndLayout() {
        JPanel p = new JPanel(new SpringLayout());
        SpringLayout g = (SpringLayout) p.getLayout();
        // fixed-size child keeps the geometry deterministic (no font metrics involved)
        JButton b = new JButton();
        b.setMinimumSize(new Dimension(40, 12));
        b.setPreferredSize(new Dimension(40, 12));
        b.setMaximumSize(new Dimension(40, 12));
        p.add(b);
        g.putConstraint(SpringLayout.WEST, b, 5, SpringLayout.WEST, p);
        g.putConstraint(SpringLayout.NORTH, b, 7, SpringLayout.NORTH, p);
        check(g.getConstraint(SpringLayout.WEST, b).getValue() == 5, "WEST constraint spring value 5");
        check(g.getConstraint(SpringLayout.NORTH, b).getValue() == 7, "NORTH constraint spring value 7");
        // only WEST/NORTH anchored: the container itself still reports a 0x0 minimum
        java.awt.Dimension min = g.minimumLayoutSize(p);
        check(min.width == 0 && min.height == 0, "unanchored container minimum 0x0");
        java.awt.Dimension pref = g.preferredLayoutSize(p);
        check(pref.width == 0 && pref.height == 0, "unanchored container preferred 0x0");
        p.setBounds(0, 0, 200, 100);
        p.doLayout();
        check(b.getX() == 5 && b.getY() == 7 && b.getWidth() == 40 && b.getHeight() == 12,
                "layout honors putConstraint offsets");

        // explicit edge springs override putConstraint-derived values; EAST/SOUTH are derived
        SpringLayout.Constraints cc = g.getConstraints(b);
        check(g.getConstraints(b) == cc, "getConstraints is stable per component");
        cc.setX(Spring.constant(1));
        cc.setY(Spring.constant(2));
        cc.setWidth(Spring.constant(40));
        cc.setHeight(Spring.constant(12));
        check(g.getConstraint(SpringLayout.EAST, b).getValue() == 41, "EAST == x + width == 41");
        check(g.getConstraint(SpringLayout.SOUTH, b).getValue() == 14, "SOUTH == y + height == 14");
        check(g.getConstraint(SpringLayout.WEST, b).getValue() == 1, "WEST reads explicit x");
        check(g.getConstraint(SpringLayout.NORTH, b).getValue() == 2, "NORTH reads explicit y");
        check(g.getConstraint(SpringLayout.WEST, b) != cc.getX(), "getConstraint returns a derived spring, not the stored one");
        p.doLayout();
        check(b.getX() == 1 && b.getY() == 2 && b.getWidth() == 40 && b.getHeight() == 12,
                "layout honors explicit edge springs");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testEdgeConstants();
        testConstraintsObject();
        testPutConstraintAndLayout();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
