/*
 * @test
 * @summary javax.swing.OverlayLayout conformance: type shape via reflection + overlay geometry,
 *          alignment math, shared-container guard (headless-safe).
 * @run main OverlayLayoutTest
 */

import java.awt.Component;
import java.awt.Dimension;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.OverlayLayout;
import javax.swing.UIManager;

public class OverlayLayoutTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void checkThrows(Class<?> ex, String msg, Runnable r, String what) {
        try {
            r.run();
            throw new AssertionError(what + ": expected " + ex.getSimpleName() + " not thrown");
        } catch (AssertionError e) {
            throw e;
        } catch (Throwable t) {
            check(ex.isInstance(t), what + " type " + ex.getSimpleName());
            check(msg == null || msg.equals(t.getMessage()), what + " message");
            passed += (msg == null) ? 1 : 2;
        }
    }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    static void testStructure() throws Exception {
        Class<?> c = Class.forName("javax.swing.OverlayLayout", false, OverlayLayoutTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(hasInterface(c, "java.awt.LayoutManager2"), "implements java.awt.LayoutManager2");
        check(hasInterface(c, "java.io.Serializable"), "implements java.io.Serializable");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getTarget"), "method getTarget");
        check(names.contains("invalidateLayout"), "method invalidateLayout");
        check(names.contains("addLayoutComponent"), "method addLayoutComponent");
        check(names.contains("removeLayoutComponent"), "method removeLayoutComponent");
        check(names.contains("preferredLayoutSize"), "method preferredLayoutSize");
        check(names.contains("minimumLayoutSize"), "method minimumLayoutSize");
    }

    static void testOverlayGeometry() {
        // two rigid children 10x10 and 20x20: sizes all collapse to the largest child
        JPanel p = new JPanel();
        OverlayLayout g = new OverlayLayout(p);
        p.setLayout(g);
        Component small = Box.createRigidArea(new Dimension(10, 10));
        Component large = Box.createRigidArea(new Dimension(20, 20));
        p.add(small); p.add(large);
        check(g.getTarget() == p, "getTarget");
        Dimension pref = g.preferredLayoutSize(p);
        check(pref.width == 20 && pref.height == 20, "preferred = largest child 20x20");
        Dimension min = g.minimumLayoutSize(p);
        check(min.width == 20 && min.height == 20, "minimum = largest child 20x20");
        Dimension max = g.maximumLayoutSize(p);
        check(max.width == 20 && max.height == 20, "maximum = largest child 20x20");
        // both children centered at 0.5 alignment in a 20x20 container
        p.setBounds(0, 0, 20, 20);
        p.doLayout();
        check(small.getX() == 5 && small.getY() == 5 && small.getWidth() == 10 && small.getHeight() == 10,
                "0.5-aligned 10x10 child centered at (5,5)");
        check(large.getX() == 0 && large.getY() == 0 && large.getWidth() == 20 && large.getHeight() == 20,
                "0.5-aligned 20x20 child fills container");
        check(g.getLayoutAlignmentX(p) == 0.5f, "alignmentX from 0.5 children");
        check(g.getLayoutAlignmentY(p) == 0.5f, "alignmentY from 0.5 children");
    }

    static void testAlignmentQuirk() {
        // OverlayLayout positions each child at x = containerAlign*W - childAlign*childW,
        // so a left-aligned (0.0) child lands on the RIGHT and vice versa
        JPanel tgt = new JPanel();
        tgt.setLayout(new OverlayLayout(tgt));
        JPanel chA = new JPanel() { @Override public float getAlignmentX() { return 0f; } };
        JPanel chB = new JPanel() { @Override public float getAlignmentX() { return 1f; } };
        chA.setPreferredSize(new Dimension(10, 10));
        chB.setPreferredSize(new Dimension(10, 10));
        tgt.add(chA); tgt.add(chB);
        tgt.setBounds(0, 0, 20, 20);
        tgt.doLayout();
        check(chA.getX() == 10, "alignmentX=0 child lands at x=10 (container 0.5 - 0.0*10)");
        check(chA.getWidth() == 10, "alignmentX=0 child keeps preferred width");
        check(chA.getHeight() == 20, "child stretched to container height when max allows");
        check(chB.getX() == 0, "alignmentX=1 child lands at x=0 (0.5*20 - 1.0*10)");
        // container alignment derives from children: a lone JLabel child (align 0) -> 0.0
        JPanel jp = new JPanel();
        OverlayLayout jg = new OverlayLayout(jp);
        jp.setLayout(jg);
        jp.add(new JLabel());
        check(jg.getLayoutAlignmentX(jp) == 0.0f, "alignmentX follows left-aligned child");
        check(jg.getLayoutAlignmentY(jp) == 0.0f, "alignmentY follows top-aligned child");
    }

    static void testProtocolAndSharing() {
        final JPanel p = new JPanel();
        final OverlayLayout g = new OverlayLayout(p);
        p.setLayout(g);
        final Component child = Box.createRigidArea(new Dimension(5, 5));
        p.add(child);
        // addLayoutComponent(String,...) with a child of the bound container is a no-op
        g.addLayoutComponent("name", child);
        check(true, "addLayoutComponent(own child) no-throw");
        g.removeLayoutComponent(child);
        check(true, "removeLayoutComponent no-throw");
        g.invalidateLayout(p);
        check(true, "invalidateLayout(own) no-throw");

        // orphan component -> invalidateLayout(null) -> shared guard
        checkThrows(java.awt.AWTError.class, "OverlayLayout can't be shared",
                new Runnable() { public void run() { g.addLayoutComponent("x", new JLabel()); } },
                "addLayoutComponent(orphan)");
        // foreign container guard on the sizing/layout entry points
        final JPanel other = new JPanel();
        checkThrows(java.awt.AWTError.class, "OverlayLayout can't be shared",
                new Runnable() { public void run() { g.layoutContainer(other); } },
                "layoutContainer(other)");
        checkThrows(java.awt.AWTError.class, "OverlayLayout can't be shared",
                new Runnable() { public void run() { g.preferredLayoutSize(other); } },
                "preferredLayoutSize(other)");
        checkThrows(java.awt.AWTError.class, "OverlayLayout can't be shared",
                new Runnable() { public void run() { g.minimumLayoutSize(other); } },
                "minimumLayoutSize(other)");
    }

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(new javax.swing.plaf.metal.MetalLookAndFeel());
        testStructure();
        testOverlayGeometry();
        testAlignmentQuirk();
        testProtocolAndSharing();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
