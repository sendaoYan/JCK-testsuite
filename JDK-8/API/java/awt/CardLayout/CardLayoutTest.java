/*
 * @test
 * @summary java.awt.CardLayout conformance: class reflection.
 * @run main CardLayoutTest
 */

public class CardLayoutTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.awt.CardLayout");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.awt.LayoutManager2", "java.io.Serializable"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "getHgap"), "method getHgap");
        check(hasMethod(c, "setHgap"), "method setHgap");
        check(hasMethod(c, "getVgap"), "method getVgap");
        check(hasMethod(c, "setVgap"), "method setVgap");
        check(hasMethod(c, "addLayoutComponent"), "method addLayoutComponent");
        check(hasMethod(c, "removeLayoutComponent"), "method removeLayoutComponent");
        check(hasMethod(c, "preferredLayoutSize"), "method preferredLayoutSize");
        check(hasMethod(c, "minimumLayoutSize"), "method minimumLayoutSize");
        check(hasMethod(c, "maximumLayoutSize"), "method maximumLayoutSize");
        check(hasMethod(c, "getLayoutAlignmentX"), "method getLayoutAlignmentX");
        check(hasMethod(c, "getLayoutAlignmentY"), "method getLayoutAlignmentY");
        check(hasMethod(c, "invalidateLayout"), "method invalidateLayout");
        check(hasMethod(c, "layoutContainer"), "method layoutContainer");
        check(hasMethod(c, "first"), "method first");
        check(hasMethod(c, "next"), "method next");
        check(hasMethod(c, "previous"), "method previous");
        check(hasMethod(c, "last"), "method last");
        check(hasMethod(c, "show"), "method show");
        check(hasMethod(c, "toString"), "method toString");
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static java.awt.Component comp(final int w, final int h) {
        return new java.awt.Component() {
            public java.awt.Dimension getPreferredSize() { return new java.awt.Dimension(w, h); }
            public java.awt.Dimension getMinimumSize() { return new java.awt.Dimension(w, h); }
        };
    }
    static java.awt.Container container(java.awt.LayoutManager lm) {
        java.awt.Container c = new java.awt.Container();
        c.setLayout(lm);
        return c;
    }

    static void testContract() {
        java.awt.CardLayout cl = new java.awt.CardLayout(4, 6);
        check(cl.getHgap() == 4 && cl.getVgap() == 6, "hgap/vgap ctor readback");
        cl.setHgap(1); cl.setVgap(2);
        check(cl.getHgap() == 1 && cl.getVgap() == 2, "setHgap/setVgap readback");
        cl.setHgap(4); cl.setVgap(6);
        // two cards in a 200x200 container
        java.awt.Container p = container(cl);
        java.awt.Component c1 = comp(100, 100), c2 = comp(120, 80);
        p.add(c1, "first"); p.add(c2, "second");
        p.setSize(200, 200); cl.layoutContainer(p);
        // every card is laid out at the same inset rectangle
        java.awt.Rectangle expect = new java.awt.Rectangle(4, 6, 192, 188);
        check(c1.getBounds().equals(expect), "card1 laid out with hgap/vgap insets: " + c1.getBounds());
        check(c2.getBounds().equals(expect), "card2 laid out identically: " + c2.getBounds());
        // exactly one card visible after layout (the first, before any flip)
        check(c1.isVisible() && !c2.isVisible(), "first card visible after initial layout");
        // next/previous flip both directions, wrapping
        cl.next(p);
        check(!c1.isVisible() && c2.isVisible(), "next flips to second");
        cl.next(p);
        check(c1.isVisible() && !c2.isVisible(), "next wraps to first");
        cl.previous(p);
        check(!c1.isVisible() && c2.isVisible(), "previous flips back");
        cl.last(p);
        check(c2.isVisible() && !c1.isVisible(), "last shows final card");
        cl.first(p);
        check(c1.isVisible() && !c2.isVisible(), "first shows initial card");
        // show() by name
        cl.show(p, "second");
        check(c2.isVisible() && !c1.isVisible(), "show(name) flips");
        // show() with an unknown name is a silent no-op (observed; no exception)
        boolean threw = false;
        try { cl.show(p, "nope"); } catch (Exception ex) { threw = true; }
        check(!threw, "show(unknown) is a silent no-op");
        check(c2.isVisible(), "show(unknown) keeps current card");
        // flipping keeps hidden card bounds laid out
        check(c1.getBounds().equals(expect), "hidden card keeps its bounds");
        // preferred size is the max child size plus double gaps
        java.awt.Container p2 = container(new java.awt.CardLayout());
        p2.add(comp(70, 80), "a"); p2.add(comp(90, 60), "b");
        java.awt.CardLayout cl2 = (java.awt.CardLayout) p2.getLayout();
        check(cl2.preferredLayoutSize(p2).equals(new java.awt.Dimension(90, 80)),
              "prefdim max of children: " + cl2.preferredLayoutSize(p2));
        check(cl2.minimumLayoutSize(p2).equals(new java.awt.Dimension(90, 80)),
              "mindim max of children: " + cl2.minimumLayoutSize(p2));
        check(cl2.maximumLayoutSize(p2).width == Integer.MAX_VALUE
              && cl2.maximumLayoutSize(p2).height == Integer.MAX_VALUE,
              "maximumLayoutSize unbounded");
        // gapped variant adds 2*hgap/2*vgap
        java.awt.Container p3 = container(new java.awt.CardLayout(4, 6));
        p3.add(comp(90, 80), "a");
        java.awt.CardLayout cl3 = (java.awt.CardLayout) p3.getLayout();
        check(cl3.preferredLayoutSize(p3).equals(new java.awt.Dimension(98, 92)),
              "gapped prefdim adds 2*gaps: " + cl3.preferredLayoutSize(p3));
        // alignment 0.5 both axes; invalidateLayout callable
        check(cl2.getLayoutAlignmentX(p2) == 0.5f, "layoutAlignmentX 0.5");
        check(cl2.getLayoutAlignmentY(p2) == 0.5f, "layoutAlignmentY 0.5");
        cl2.invalidateLayout(p2);
        check(true, "invalidateLayout callable");
        // single card: next/previous wrap to itself, stays visible
        java.awt.Container p4 = container(new java.awt.CardLayout());
        java.awt.Component only = comp(10, 10);
        p4.add(only, "only");
        p4.setSize(20, 20);
        ((java.awt.CardLayout) p4.getLayout()).next(p4);
        check(only.isVisible(), "single card next keeps it visible");
        ((java.awt.CardLayout) p4.getLayout()).previous(p4);
        check(only.isVisible(), "single card previous keeps it visible");
        // flipping a container whose layout is a different object -> IAE (observed)
        java.awt.Container other = container(new java.awt.BorderLayout());
        boolean wp = false; String wpMsg = null;
        try { cl.next(other); } catch (java.lang.IllegalArgumentException ex) { wp = true; wpMsg = ex.getMessage(); }
        check(wp && "wrong parent for CardLayout".equals(wpMsg),
              "next on foreign container -> IAE 'wrong parent for CardLayout': " + wpMsg);
        // removeLayoutComponent is a no-op for CardLayout size math: it queries the
        // container's component list directly (observed identical prefdim before/after)
        java.awt.Dimension before = cl2.preferredLayoutSize(p2);
        cl2.removeLayoutComponent(p2.getComponent(0));
        check(p2.getComponentCount() == 2, "removeLayoutComponent keeps container list intact");
        check(cl2.preferredLayoutSize(p2).equals(before),
              "removeLayoutComponent does not change prefdim (observed)");
    }

}
