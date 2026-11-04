/*
 * @test
 * @summary java.awt.GridBagLayout conformance: class reflection.
 * @run main GridBagLayoutTest
 */

public class GridBagLayoutTest {
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
        Class<?> c = Class.forName("java.awt.GridBagLayout");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.awt.LayoutManager2", "java.io.Serializable"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "setConstraints"), "method setConstraints");
        check(hasMethod(c, "getConstraints"), "method getConstraints");
        check(hasMethod(c, "getLayoutOrigin"), "method getLayoutOrigin");
        check(hasMethod(c, "getLayoutDimensions"), "method getLayoutDimensions");
        check(hasMethod(c, "getLayoutWeights"), "method getLayoutWeights");
        check(hasMethod(c, "location"), "method location");
        check(hasMethod(c, "addLayoutComponent"), "method addLayoutComponent");
        check(hasMethod(c, "removeLayoutComponent"), "method removeLayoutComponent");
        check(hasMethod(c, "preferredLayoutSize"), "method preferredLayoutSize");
        check(hasMethod(c, "minimumLayoutSize"), "method minimumLayoutSize");
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
        java.awt.GridBagLayout gbl = new java.awt.GridBagLayout();
        // columnWidths/rowHeights/columnWeights/rowWeights are public mutable fields
        gbl.columnWidths = new int[]{30, 40};
        gbl.rowHeights = new int[]{10, 20};
        gbl.columnWeights = new double[]{1.0, 2.0};
        gbl.rowWeights = new double[]{0.5};
        check(gbl.columnWidths.length == 2 && gbl.columnWidths[1] == 40, "columnWidths field mutable");
        check(gbl.rowHeights[1] == 20, "rowHeights field mutable");
        check(gbl.columnWeights[1] == 2.0, "columnWeights field mutable");
        check(gbl.rowWeights[0] == 0.5, "rowWeights field mutable");
        // two cells, weightx 1:3, fill BOTH -> extra space split 60/140 (observed)
        java.awt.Container p = container(new java.awt.GridBagLayout());
        java.awt.GridBagLayout lay = (java.awt.GridBagLayout) p.getLayout();
        java.awt.Component g1 = comp(60, 10), g2 = comp(140, 10);
        java.awt.GridBagConstraints gc1 = new java.awt.GridBagConstraints();
        gc1.gridx = 0; gc1.gridy = 0; gc1.weightx = 1.0; gc1.fill = java.awt.GridBagConstraints.BOTH;
        java.awt.GridBagConstraints gc2 = new java.awt.GridBagConstraints();
        gc2.gridx = 1; gc2.gridy = 0; gc2.weightx = 3.0; gc2.fill = java.awt.GridBagConstraints.BOTH;
        p.add(g1, gc1); p.add(g2, gc2);
        p.setSize(200, 100); p.doLayout();
        check(g1.getBounds().equals(new java.awt.Rectangle(0, 45, 60, 10)),
              "cell1 bounds: " + g1.getBounds());
        check(g2.getBounds().equals(new java.awt.Rectangle(60, 45, 140, 10)),
              "cell2 bounds: " + g2.getBounds());
        // getConstraints returns an independent copy that reflects placement
        java.awt.GridBagConstraints back = lay.getConstraints(g1);
        check(back != gc1, "getConstraints returns copy, not the passed object");
        check(back.gridx == 0 && back.gridy == 0 && back.weightx == 1.0
              && back.fill == java.awt.GridBagConstraints.BOTH,
              "getConstraints copy preserves placement fields");
        // layout info reflects the grid
        check(java.util.Arrays.deepToString(lay.getLayoutDimensions()).contains("140"),
              "getLayoutDimensions includes stretched column: " + java.util.Arrays.deepToString(lay.getLayoutDimensions()));
        check(java.util.Arrays.deepToString(lay.getLayoutWeights()).contains("1.0"),
              "getLayoutWeights includes cell weights: " + java.util.Arrays.deepToString(lay.getLayoutWeights()));
        // single weighted child centered vertically when weighty is 0
        java.awt.Container p2 = container(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints sw = new java.awt.GridBagConstraints();
        sw.gridx = 0; sw.gridy = 0; sw.weightx = 1.0; sw.fill = java.awt.GridBagConstraints.BOTH;
        java.awt.Component q = comp(60, 10);
        p2.add(q, sw);
        p2.setSize(200, 100); p2.doLayout();
        check(q.getBounds().equals(new java.awt.Rectangle(0, 45, 200, 10)),
              "weighted-x child fills width, centers vertically: " + q.getBounds());
        check(((java.awt.GridBagLayout) p2.getLayout()).getLayoutOrigin().equals(new java.awt.Point(0, 45)),
              "getLayoutOrigin reports grid origin");
        // relative add (no constraints object) still lays out
        java.awt.Container p3 = container(new java.awt.GridBagLayout());
        java.awt.Component rel = comp(10, 10);
        p3.add(rel);
        p3.setSize(50, 50); p3.doLayout();
        check(rel.getBounds().equals(new java.awt.Rectangle(20, 20, 10, 10)),
              "default-constraint child centered: " + rel.getBounds());
        // string constraint -> IAE with the exact message
        boolean iae = false; String msg = null;
        java.awt.Container p4 = container(new java.awt.GridBagLayout());
        try { p4.add(comp(1, 1), "notagbc"); }
        catch (java.lang.IllegalArgumentException ex) { iae = true; msg = ex.getMessage(); }
        check(iae && msg != null && msg.contains("cannot add to layout"),
              "string constraint -> IAE mentioning 'cannot add to layout': " + msg);
        // alignment 0.5 both axes; invalidateLayout callable
        java.awt.Container any = new java.awt.Container();
        check(lay.getLayoutAlignmentX(any) == 0.5f, "layoutAlignmentX 0.5");
        check(lay.getLayoutAlignmentY(any) == 0.5f, "layoutAlignmentY 0.5");
        lay.invalidateLayout(any);
        check(true, "invalidateLayout callable");
        // maximumLayoutSize unbounded
        check(lay.maximumLayoutSize(any).width == Integer.MAX_VALUE, "maximumLayoutSize unbounded");
        // addLayoutComponent(String)/removeLayoutComponent callable (legacy paths)
        lay.removeLayoutComponent(g2);
        check(true, "removeLayoutComponent callable");
    }

}
