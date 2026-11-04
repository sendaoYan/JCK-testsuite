/*
 * @test
 * @summary javax.swing.GroupLayout conformance: type shape via reflection + sequential/parallel
 *          group geometry, linkSize, replace, layout style plumbing (headless-safe).
 * @run main GroupLayoutTest
 */

import java.awt.Component;
import java.awt.Dimension;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import javax.swing.Box;
import javax.swing.GroupLayout;
import javax.swing.JPanel;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

public class GroupLayoutTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void checkThrows(Class<?> ex, Runnable r, String what) {
        try {
            r.run();
            throw new AssertionError(what + ": expected " + ex.getSimpleName() + " not thrown");
        } catch (AssertionError e) {
            throw e;
        } catch (Throwable t) {
            check(ex.isInstance(t), what + " type " + ex.getSimpleName());
        }
    }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    static void testStructure() throws Exception {
        Class<?> c = Class.forName("javax.swing.GroupLayout", false, GroupLayoutTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(hasInterface(c, "java.awt.LayoutManager2"), "implements java.awt.LayoutManager2");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("setHonorsVisibility"), "method setHonorsVisibility");
        check(names.contains("getHonorsVisibility"), "method getHonorsVisibility");
        check(names.contains("setAutoCreateGaps"), "method setAutoCreateGaps");
        check(names.contains("getAutoCreateGaps"), "method getAutoCreateGaps");
        check(names.contains("setAutoCreateContainerGaps"), "method setAutoCreateContainerGaps");
        check(names.contains("getAutoCreateContainerGaps"), "method getAutoCreateContainerGaps");
        check(names.contains("linkSize"), "method linkSize");
        check(names.contains("replace"), "method replace");
        check(names.contains("setLayoutStyle"), "method setLayoutStyle");
        check(names.contains("getLayoutStyle"), "method getLayoutStyle");
    }

    static void testFlagsAndStyle() {
        JPanel p = new JPanel();
        GroupLayout g = new GroupLayout(p);
        p.setLayout(g);
        check(g.getAutoCreateGaps() == false, "autoCreateGaps default false");
        check(g.getAutoCreateContainerGaps() == false, "autoCreateContainerGaps default false");
        g.setAutoCreateGaps(true);
        check(g.getAutoCreateGaps() == true, "autoCreateGaps roundtrip");
        g.setAutoCreateContainerGaps(true);
        check(g.getAutoCreateContainerGaps() == true, "autoCreateContainerGaps roundtrip");
        check(g.getHonorsVisibility() == true, "honorsVisibility default true");
        g.setHonorsVisibility(false);
        check(g.getHonorsVisibility() == false, "honorsVisibility roundtrip false");

        // layout style starts null, accepts a custom instance, and setLayoutStyle(null) clears it
        check(g.getLayoutStyle() == null, "layoutStyle default null");
        LayoutStyle custom = new LayoutStyle() {
            @Override
            public int getPreferredGap(javax.swing.JComponent a, javax.swing.JComponent b,
                    ComponentPlacement placement, int position, java.awt.Container parent) { return 9; }
            @Override
            public int getContainerGap(javax.swing.JComponent c, int position, java.awt.Container parent) { return 4; }
        };
        g.setLayoutStyle(custom);
        check(g.getLayoutStyle() == custom, "layoutStyle roundtrip");
        g.setLayoutStyle(null);
        check(g.getLayoutStyle() == null, "setLayoutStyle(null) clears the override");

        // linkSize rejects any axis constant other than HORIZONTAL/VERTICAL
        final GroupLayout g2 = g;
        final Component c1 = Box.createRigidArea(new Dimension(10, 10));
        final Component c2 = Box.createRigidArea(new Dimension(20, 10));
        p.add(c1); p.add(c2);
        checkThrows(IllegalArgumentException.class,
                new Runnable() { public void run() { g2.linkSize(-1, c1, c2); } },
                "linkSize(DEFAULT_SIZE) axis");
    }

    static void testSequentialGeometry() {
        // sequential horizontal + baseline vertical: exact stacking with an explicit 5px gap
        JPanel p = new JPanel();
        GroupLayout g = new GroupLayout(p);
        p.setLayout(g);
        Component r1 = Box.createRigidArea(new Dimension(40, 10));
        Component r2 = Box.createRigidArea(new Dimension(60, 12));
        g.setHorizontalGroup(g.createSequentialGroup().addComponent(r1).addGap(5).addComponent(r2));
        g.setVerticalGroup(g.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(r1).addComponent(r2));
        p.setBounds(0, 0, 300, 50);
        p.doLayout();
        check(r1.getX() == 0 && r1.getY() == 0 && r1.getWidth() == 40 && r1.getHeight() == 10,
                "sequential first child at (0,0,40,10)");
        check(r2.getX() == 45 && r2.getWidth() == 60, "sequential second child at x=45 after 5px gap");
        check(r2.getY() == 0 && r2.getHeight() == 12, "baseline children share y=0");
        // preferred size adds up: 40 + 5 + 60 wide, tallest baseline child high
        Dimension pref = g.preferredLayoutSize(p);
        check(pref.width == 105 && pref.height == 12, "preferred 105x12 with explicit gap");
        Dimension min = g.minimumLayoutSize(p);
        check(min.width == 105 && min.height == 12, "minimum 105x12 with explicit gap");
    }

    static void testParallelGeometry() {
        // parallel LEADING stacks all children at x=0; sequential vertical group tiles y
        JPanel p = new JPanel();
        GroupLayout g = new GroupLayout(p);
        p.setLayout(g);
        Component q1 = Box.createRigidArea(new Dimension(40, 10));
        Component q2 = Box.createRigidArea(new Dimension(60, 12));
        g.setHorizontalGroup(g.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(q1).addComponent(q2));
        g.setVerticalGroup(g.createSequentialGroup().addComponent(q1).addComponent(q2));
        p.setBounds(0, 0, 300, 50);
        p.doLayout();
        check(q1.getX() == 0 && q2.getX() == 0, "parallel LEADING both children at x=0");
        check(q1.getY() == 0 && q2.getY() == 10, "vertical sequential tiling y=0,10");
        check(q1.getWidth() == 40 && q2.getWidth() == 60, "children keep preferred widths");
        check(q2.getY() + q2.getHeight() == 22, "second child bottom at 22");
    }

    static void testLinkSizeAndReplace() {
        // linkSize equalizes both widths to the larger preferred width (70)
        JPanel p = new JPanel();
        GroupLayout g = new GroupLayout(p);
        p.setLayout(g);
        Component w1 = Box.createRigidArea(new Dimension(30, 10));
        Component w2 = Box.createRigidArea(new Dimension(70, 10));
        g.setHorizontalGroup(g.createSequentialGroup().addComponent(w1).addComponent(w2));
        g.setVerticalGroup(g.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(w1).addComponent(w2));
        g.linkSize(w1, w2);
        p.setBounds(0, 0, 300, 50);
        p.doLayout();
        check(w1.getWidth() == 70, "linkSize raises first width to 70");
        check(w2.getWidth() == 70, "linkSize keeps larger width 70");
        check(w1.getX() == 0 && w2.getX() == 70, "linked children tile at x=0,70");
        // axis-scoped overload accepts HORIZONTAL without throwing
        g.linkSize(SwingConstants.HORIZONTAL, w1, w2);
        check(true, "linkSize(HORIZONTAL,...) no-throw");
        g.linkSize(SwingConstants.VERTICAL, w1, w2);
        check(true, "linkSize(VERTICAL,...) no-throw");

        // replace swaps the component inside the panel and the group keeps working
        JPanel p4 = new JPanel();
        GroupLayout g4 = new GroupLayout(p4);
        p4.setLayout(g4);
        Component e1 = Box.createRigidArea(new Dimension(20, 10));
        Component e2 = Box.createRigidArea(new Dimension(25, 10));
        g4.setHorizontalGroup(g4.createSequentialGroup().addComponent(e1));
        g4.setVerticalGroup(g4.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(e1));
        p4.add(e1);
        g4.replace(e1, e2);
        check(java.util.Arrays.asList(p4.getComponents()).contains(e2), "replace installs new component in container");
        check(!java.util.Arrays.asList(p4.getComponents()).contains(e1), "replace removes old component from container");
        p4.setBounds(0, 0, 300, 50);
        p4.doLayout();
        check(e2.getX() == 0 && e2.getY() == 0 && e2.getWidth() == 25, "replacement child laid out at (0,0,25,10)");
    }

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(new javax.swing.plaf.metal.MetalLookAndFeel());
        testStructure();
        testFlagsAndStyle();
        testSequentialGeometry();
        testParallelGeometry();
        testLinkSizeAndReplace();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
