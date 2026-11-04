/*
 * @test
 * @summary javax.swing.BoxLayout conformance: type shape via reflection + axis validation,
 *          exact tiling geometry, glue/strut distribution, shared-container guard (headless-safe).
 * @run main BoxLayoutTest
 */

import java.awt.Component;
import java.awt.Dimension;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.UIManager;

public class BoxLayoutTest {
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
        Class<?> c = Class.forName("javax.swing.BoxLayout", false, BoxLayoutTest.class.getClassLoader());
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
        check(names.contains("getAxis"), "method getAxis");
        check(names.contains("invalidateLayout"), "method invalidateLayout");
        check(names.contains("addLayoutComponent"), "method addLayoutComponent");
        check(names.contains("removeLayoutComponent"), "method removeLayoutComponent");
        check(names.contains("preferredLayoutSize"), "method preferredLayoutSize");
    }

    static void testAxisAndValidation() {
        check(BoxLayout.X_AXIS == 0, "X_AXIS value");
        check(BoxLayout.Y_AXIS == 1, "Y_AXIS value");
        check(BoxLayout.LINE_AXIS == 2, "LINE_AXIS value");
        check(BoxLayout.PAGE_AXIS == 3, "PAGE_AXIS value");

        JPanel p = new JPanel();
        BoxLayout x = new BoxLayout(p, BoxLayout.X_AXIS);
        check(x.getTarget() == p, "getTarget");
        check(x.getAxis() == BoxLayout.X_AXIS, "getAxis X");
        check(new BoxLayout(p, BoxLayout.Y_AXIS).getAxis() == BoxLayout.Y_AXIS, "getAxis Y");
        check(new BoxLayout(p, BoxLayout.LINE_AXIS).getAxis() == BoxLayout.LINE_AXIS, "getAxis LINE");
        check(new BoxLayout(p, BoxLayout.PAGE_AXIS).getAxis() == BoxLayout.PAGE_AXIS, "getAxis PAGE");

        // invalid axis -> AWTError with stable message
        checkThrows(java.awt.AWTError.class, "Invalid axis",
                new Runnable() { public void run() { new BoxLayout(new JPanel(), 99); } },
                "ctor axis 99");
        checkThrows(java.awt.AWTError.class, "Invalid axis",
                new Runnable() { public void run() { new BoxLayout(new JPanel(), -1); } },
                "ctor axis -1");
        // null target is accepted by the constructor itself
        new BoxLayout(null, BoxLayout.X_AXIS);
        check(true, "ctor(null, X_AXIS) no-throw");
    }

    static void testTilingGeometry() {
        // horizontal: rigid 10/20/30 in 60x10 -> exact stack, full-height children
        JPanel p = new JPanel();
        BoxLayout g = new BoxLayout(p, BoxLayout.X_AXIS);
        p.setLayout(g);
        Component a = Box.createRigidArea(new Dimension(10, 10));
        Component b = Box.createRigidArea(new Dimension(20, 10));
        Component d = Box.createRigidArea(new Dimension(30, 10));
        p.add(a); p.add(b); p.add(d);
        p.setBounds(0, 0, 60, 10);
        p.doLayout();
        check(a.getX() == 0 && b.getX() == 10 && d.getX() == 30, "X_AXIS stack x=0,10,30");
        check(a.getWidth() == 10 && b.getWidth() == 20 && d.getWidth() == 30, "X_AXIS widths 10,20,30");
        check(a.getY() == 0 && b.getY() == 0 && d.getY() == 0, "X_AXIS children fill height");

        // vertical: rigid heights 5,7 in 12-high -> y=0,5
        JPanel q = new JPanel();
        BoxLayout v = new BoxLayout(q, BoxLayout.Y_AXIS);
        q.setLayout(v);
        Component e = Box.createRigidArea(new Dimension(10, 5));
        Component f = Box.createRigidArea(new Dimension(10, 7));
        q.add(e); q.add(f);
        q.setBounds(0, 0, 10, 12);
        q.doLayout();
        check(e.getY() == 0 && f.getY() == 5, "Y_AXIS stack y=0,5");
        check(e.getHeight() == 5 && f.getHeight() == 7, "Y_AXIS heights 5,7");
        check(e.getX() == 0 && f.getX() == 0, "Y_AXIS children fill width");

        // preferred/min/max sizes with rigid (min=pref=max) children
        Dimension pref = g.preferredLayoutSize(p);
        check(pref.width == 60 && pref.height == 10, "X_AXIS preferred 60x10 (10+20+30)");
        Dimension min = g.minimumLayoutSize(p);
        check(min.width == 60 && min.height == 10, "X_AXIS minimum 60x10");
        Dimension max = g.maximumLayoutSize(p);
        check(max.width == 60 && max.height == 10, "X_AXIS maximum 60x10");
        Dimension vpref = v.preferredLayoutSize(q);
        check(vpref.width == 10 && vpref.height == 12, "Y_AXIS preferred 10x12");

        // alignment: 0.5 on both axes once requests are cached
        check(g.getLayoutAlignmentX(p) == 0.5f, "X_AXIS alignmentX 0.5");
        check(g.getLayoutAlignmentY(p) == 0.5f, "X_AXIS alignmentY 0.5");
        check(v.getLayoutAlignmentX(q) == 0.5f, "Y_AXIS alignmentX 0.5");
        check(v.getLayoutAlignmentY(q) == 0.5f, "Y_AXIS alignmentY 0.5");

        // LINE_AXIS with left-to-right orientation must tile exactly like X_AXIS
        JPanel l = new JPanel();
        l.setLayout(new BoxLayout(l, BoxLayout.LINE_AXIS));
        Component la = Box.createRigidArea(new Dimension(10, 5));
        Component lb = Box.createRigidArea(new Dimension(20, 7));
        l.add(la); l.add(lb);
        l.setBounds(0, 0, 100, 10);
        l.doLayout();
        check(la.getX() == 0 && lb.getX() == 10 && lb.getWidth() == 20, "LINE_AXIS(LTR) == X_AXIS tiling");
        check(la.getY() == 2 && lb.getY() == 1, "LINE_AXIS(LTR) vertical centering y=2,1");

        // glue absorbs the slack between rigid children
        Box hb = Box.createHorizontalBox();
        Component ga = Box.createRigidArea(new Dimension(10, 10));
        Component gb = Box.createRigidArea(new Dimension(10, 10));
        hb.add(ga); hb.add(Box.createHorizontalGlue()); hb.add(gb);
        hb.setBounds(0, 0, 100, 10);
        hb.doLayout();
        check(ga.getX() == 0 && gb.getX() == 90, "horizontal glue pins second child at x=90");

        // fixed strut contributes exactly its width
        Box sb = Box.createHorizontalBox();
        Component s1 = Box.createRigidArea(new Dimension(5, 10));
        Component s2 = Box.createRigidArea(new Dimension(5, 10));
        sb.add(s1); sb.add(Box.createHorizontalStrut(7)); sb.add(s2);
        sb.setBounds(0, 0, 20, 10);
        sb.doLayout();
        check(s1.getX() == 0 && s2.getX() == 12, "horizontal strut advances x by 7");

        // vertical glue pins bottom child at container bottom
        Box vb = Box.createVerticalBox();
        Component d1 = Box.createRigidArea(new Dimension(10, 5));
        Component d2 = Box.createRigidArea(new Dimension(10, 5));
        vb.add(d1); vb.add(Box.createVerticalGlue()); vb.add(d2);
        vb.setBounds(0, 0, 10, 50);
        vb.doLayout();
        check(d1.getY() == 0 && d2.getY() == 45, "vertical glue pins second child at y=45");

        // a lone horizontal strut: preferred keeps the height 0 but max is unbounded vertically
        Box lone = Box.createHorizontalBox();
        lone.add(Box.createHorizontalStrut(7));
        Dimension lp = lone.getPreferredSize();
        check(lp.width == 7 && lp.height == 0, "hstrut-only preferred 7x0");
        Dimension lm = lone.getMaximumSize();
        check(lm.width == 7 && lm.height == 32767, "hstrut-only maximum 7x32767");
    }

    static void testProtocolAndSharing() {
        final JPanel p = new JPanel();
        final BoxLayout g = new BoxLayout(p, BoxLayout.X_AXIS);
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

        // an orphan component has a null parent -> invalidateLayout(null) -> shared guard
        checkThrows(java.awt.AWTError.class, "BoxLayout can't be shared",
                new Runnable() { public void run() { g.addLayoutComponent("x", new javax.swing.JLabel()); } },
                "addLayoutComponent(orphan)");
        // every LayoutManager2 entry point guards against foreign containers
        final JPanel other = new JPanel();
        checkThrows(java.awt.AWTError.class, "BoxLayout can't be shared",
                new Runnable() { public void run() { g.layoutContainer(other); } },
                "layoutContainer(other)");
        checkThrows(java.awt.AWTError.class, "BoxLayout can't be shared",
                new Runnable() { public void run() { g.preferredLayoutSize(other); } },
                "preferredLayoutSize(other)");
        checkThrows(java.awt.AWTError.class, "BoxLayout can't be shared",
                new Runnable() { public void run() { g.minimumLayoutSize(other); } },
                "minimumLayoutSize(other)");
        checkThrows(java.awt.AWTError.class, "BoxLayout can't be shared",
                new Runnable() { public void run() { g.getLayoutAlignmentY(other); } },
                "getLayoutAlignmentY(other)");
    }

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(new javax.swing.plaf.metal.MetalLookAndFeel());
        testStructure();
        testAxisAndValidation();
        testTilingGeometry();
        testProtocolAndSharing();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
