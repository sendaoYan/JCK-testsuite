/*
 * @test
 * @summary java.awt.BorderLayout conformance: class reflection.
 * @run main BorderLayoutTest
 */

public class BorderLayoutTest {
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
        Class<?> c = Class.forName("java.awt.BorderLayout");
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
        check(hasMethod(c, "getLayoutComponent"), "method getLayoutComponent");
        check(hasMethod(c, "getConstraints"), "method getConstraints");
        check(hasMethod(c, "minimumLayoutSize"), "method minimumLayoutSize");
        check(hasMethod(c, "preferredLayoutSize"), "method preferredLayoutSize");
        check(hasMethod(c, "maximumLayoutSize"), "method maximumLayoutSize");
        check(hasMethod(c, "getLayoutAlignmentX"), "method getLayoutAlignmentX");
        check(hasMethod(c, "getLayoutAlignmentY"), "method getLayoutAlignmentY");
        check(hasMethod(c, "invalidateLayout"), "method invalidateLayout");
        check(hasMethod(c, "layoutContainer"), "method layoutContainer");
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
        // constants are convenience object keys, distinct
        check(java.awt.BorderLayout.NORTH != java.awt.BorderLayout.SOUTH, "NORTH/SOUTH distinct");
        check(java.awt.BorderLayout.EAST != java.awt.BorderLayout.WEST, "EAST/WEST distinct");
        check(java.awt.BorderLayout.CENTER != java.awt.BorderLayout.LINE_START, "CENTER/LINE_START distinct");
        check(java.awt.BorderLayout.BEFORE_FIRST_LINE != null, "BEFORE_FIRST_LINE exists");
        check(java.awt.BorderLayout.AFTER_LAST_LINE != null, "AFTER_LAST_LINE exists");
        // hgap/vgap defaults + setters
        java.awt.BorderLayout bl = new java.awt.BorderLayout();
        check(bl.getHgap() == 0 && bl.getVgap() == 0, "default hgap/vgap 0");
        bl.setHgap(7); bl.setVgap(9);
        check(bl.getHgap() == 7 && bl.getVgap() == 9, "setHgap/setVgap readback");
        check(bl.toString().contains("hgap=7") && bl.toString().contains("vgap=9"),
              "toString exposes gaps: " + bl);
        // alignment (LayoutManager2) is 0.5 both axes
        check(bl.getLayoutAlignmentX(new java.awt.Container()) == 0.5f, "layoutAlignmentX 0.5");
        check(bl.getLayoutAlignmentY(new java.awt.Container()) == 0.5f, "layoutAlignmentY 0.5");
        bl.invalidateLayout(new java.awt.Container());
        check(true, "invalidateLayout callable");
        // exact 5-region geometry at 100x100, all preferred sizes honored
        java.awt.Container p = container(new java.awt.BorderLayout());
        java.awt.Component n = comp(50, 20), s = comp(50, 30), e = comp(5, 50), w = comp(15, 50), c = comp(40, 40);
        p.add(n, java.awt.BorderLayout.NORTH); p.add(s, java.awt.BorderLayout.SOUTH);
        p.add(e, java.awt.BorderLayout.EAST); p.add(w, java.awt.BorderLayout.WEST);
        p.add(c, java.awt.BorderLayout.CENTER);
        p.setSize(100, 100); p.doLayout();
        check(n.getBounds().equals(new java.awt.Rectangle(0, 0, 100, 20)), "NORTH spans width, pref height: " + n.getBounds());
        check(s.getBounds().equals(new java.awt.Rectangle(0, 70, 100, 30)), "SOUTH pinned bottom: " + s.getBounds());
        check(e.getBounds().equals(new java.awt.Rectangle(95, 20, 5, 50)), "EAST pinned right, pref width: " + e.getBounds());
        check(w.getBounds().equals(new java.awt.Rectangle(0, 20, 15, 50)), "WEST pinned left, pref width: " + w.getBounds());
        check(c.getBounds().equals(new java.awt.Rectangle(15, 20, 80, 50)), "CENTER takes remainder: " + c.getBounds());
        // NORTH/SOUTH span full width even when wider than container
        java.awt.Container pw = container(new java.awt.BorderLayout());
        java.awt.Component nw = comp(150, 10);
        pw.add(nw, java.awt.BorderLayout.NORTH); pw.add(comp(10, 10), java.awt.BorderLayout.CENTER);
        pw.setSize(100, 100); pw.doLayout();
        check(nw.getBounds().width == 100, "NORTH width clamps to container: " + nw.getBounds());
        // hgap/vgap shrink the center rectangle on all sides
        java.awt.Container p2 = container(new java.awt.BorderLayout(2, 3));
        check(p2.getLayout() instanceof java.awt.BorderLayout, "layout attached");
        java.awt.Component n2 = comp(50, 20), s2 = comp(50, 30), e2 = comp(5, 50), w2 = comp(15, 50), c2 = comp(40, 40);
        p2.add(n2, java.awt.BorderLayout.NORTH); p2.add(s2, java.awt.BorderLayout.SOUTH);
        p2.add(e2, java.awt.BorderLayout.EAST); p2.add(w2, java.awt.BorderLayout.WEST);
        p2.add(c2, java.awt.BorderLayout.CENTER);
        p2.setSize(100, 100); p2.doLayout();
        check(c2.getBounds().equals(new java.awt.Rectangle(17, 23, 76, 44)),
              "gaps carve center: " + c2.getBounds());
        check(e2.getBounds().equals(new java.awt.Rectangle(95, 23, 5, 44)), "EAST respects vgap bands: " + e2.getBounds());
        check(w2.getBounds().equals(new java.awt.Rectangle(0, 23, 15, 44)), "WEST respects vgap bands: " + w2.getBounds());
        // preferred/minimum sizes aggregate per region
        java.awt.Container p3 = container(new java.awt.BorderLayout());
        java.awt.Component a3 = comp(30, 40);
        p3.add(a3, java.awt.BorderLayout.CENTER);
        java.awt.BorderLayout bl3 = (java.awt.BorderLayout) p3.getLayout();
        check(bl3.preferredLayoutSize(p3).equals(new java.awt.Dimension(30, 40)),
              "prefdim of lone CENTER child: " + bl3.preferredLayoutSize(p3));
        check(bl3.minimumLayoutSize(p3).equals(new java.awt.Dimension(30, 40)),
              "mindim of lone CENTER child: " + bl3.minimumLayoutSize(p3));
        java.awt.Container p5 = container(new java.awt.BorderLayout());
        p5.add(comp(50, 20), java.awt.BorderLayout.NORTH);
        p5.add(comp(50, 30), java.awt.BorderLayout.SOUTH);
        p5.add(comp(40, 40), java.awt.BorderLayout.CENTER);
        check(p5.getLayout().preferredLayoutSize(p5).equals(new java.awt.Dimension(50, 90)),
              "N/C/S prefdim max width, sum heights: " + p5.getLayout().preferredLayoutSize(p5));
        // a layout instance only knows components registered through the container it serves
        java.awt.BorderLayout foreign = new java.awt.BorderLayout();
        check(foreign.preferredLayoutSize(p3).equals(new java.awt.Dimension(0, 0)),
              "unattached layout instance reports zero (observed)");
        // null/relative constraint add lands in CENTER
        java.awt.Container p4 = container(new java.awt.BorderLayout());
        p4.add(comp(30, 40));
        check(p4.getLayout().preferredLayoutSize(p4).equals(new java.awt.Dimension(30, 40)),
              "null constraint defaults to CENTER");
        // unknown constraint string -> IAE with the exact message
        boolean iae = false; String msg = null;
        java.awt.Container pb = container(new java.awt.BorderLayout());
        try { pb.add(comp(1, 1), "bogus"); }
        catch (java.lang.IllegalArgumentException ex) { iae = true; msg = ex.getMessage(); }
        check(iae && "cannot add to layout: unknown constraint: bogus".equals(msg),
              "bogus constraint -> IAE 'cannot add to layout: unknown constraint: bogus': " + msg);
        // addLayoutComponent/removeLayoutComponent callable directly
        java.awt.BorderLayout blr = new java.awt.BorderLayout();
        java.awt.Component rc = comp(1, 1);
        blr.addLayoutComponent("North", rc);
        blr.removeLayoutComponent(rc);
        check(true, "add/removeLayoutComponent callable");
        boolean npe = false;
        try { blr.removeLayoutComponent(null); } catch (java.lang.NullPointerException nplex) { npe = true; }
        check(npe, "removeLayoutComponent(null) -> NPE (observed)");
    }

}
