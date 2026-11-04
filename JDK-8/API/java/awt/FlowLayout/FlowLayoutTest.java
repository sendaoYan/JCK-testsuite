/*
 * @test
 * @summary java.awt.FlowLayout conformance: class reflection.
 * @run main FlowLayoutTest
 */

public class FlowLayoutTest {
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
        Class<?> c = Class.forName("java.awt.FlowLayout");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.awt.LayoutManager", "java.io.Serializable"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "getAlignment"), "method getAlignment");
        check(hasMethod(c, "setAlignment"), "method setAlignment");
        check(hasMethod(c, "getHgap"), "method getHgap");
        check(hasMethod(c, "setHgap"), "method setHgap");
        check(hasMethod(c, "getVgap"), "method getVgap");
        check(hasMethod(c, "setVgap"), "method setVgap");
        check(hasMethod(c, "setAlignOnBaseline"), "method setAlignOnBaseline");
        check(hasMethod(c, "getAlignOnBaseline"), "method getAlignOnBaseline");
        check(hasMethod(c, "addLayoutComponent"), "method addLayoutComponent");
        check(hasMethod(c, "removeLayoutComponent"), "method removeLayoutComponent");
        check(hasMethod(c, "preferredLayoutSize"), "method preferredLayoutSize");
        check(hasMethod(c, "minimumLayoutSize"), "method minimumLayoutSize");
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
        // alignment constants
        check(java.awt.FlowLayout.LEFT == 0, "LEFT == 0");
        check(java.awt.FlowLayout.CENTER == 1, "CENTER == 1");
        check(java.awt.FlowLayout.RIGHT == 2, "RIGHT == 2");
        check(java.awt.FlowLayout.LEADING == 3, "LEADING == 3");
        check(java.awt.FlowLayout.TRAILING == 4, "TRAILING == 4");
        java.awt.FlowLayout fl = new java.awt.FlowLayout();
        check(fl.getAlignment() == java.awt.FlowLayout.CENTER, "default alignment CENTER");
        check(fl.getHgap() == 5 && fl.getVgap() == 5, "default gaps 5/5");
        check(!fl.getAlignOnBaseline(), "default alignOnBaseline false");
        fl.setAlignment(java.awt.FlowLayout.RIGHT);
        check(fl.getAlignment() == java.awt.FlowLayout.RIGHT, "setAlignment readback");
        fl.setHgap(7); fl.setVgap(8);
        check(fl.getHgap() == 7 && fl.getVgap() == 8, "setHgap/setVgap readback");
        fl.setAlignOnBaseline(true);
        check(fl.getAlignOnBaseline(), "setAlignOnBaseline readback");
        // full ctor
        java.awt.FlowLayout flf = new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 3, 4);
        check(flf.getAlignment() == 0 && flf.getHgap() == 3 && flf.getVgap() == 4, "full ctor readback");
        // CENTER: two fit on row 1 (centered), third wraps to centered row 2
        java.awt.Container p = container(new java.awt.FlowLayout());
        java.awt.Component f1 = comp(30, 10), f2 = comp(30, 10), f3 = comp(30, 10);
        p.add(f1); p.add(f2); p.add(f3);
        p.setSize(100, 50); p.doLayout();
        check(f1.getBounds().equals(new java.awt.Rectangle(17, 5, 30, 10)),
              "row1 first centered: " + f1.getBounds());
        check(f2.getBounds().equals(new java.awt.Rectangle(52, 5, 30, 10)),
              "row1 second follows with hgap: " + f2.getBounds());
        check(f3.getBounds().equals(new java.awt.Rectangle(35, 20, 30, 10)),
              "row2 wraps and centers: " + f3.getBounds());
        // LEFT alignment pins rows to hgap
        java.awt.Container p2 = container(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 5));
        java.awt.Component l1 = comp(30, 10), l2 = comp(30, 10), l3 = comp(30, 10);
        p2.add(l1); p2.add(l2); p2.add(l3);
        p2.setSize(100, 50); p2.doLayout();
        check(l1.getBounds().equals(new java.awt.Rectangle(5, 5, 30, 10)), "left row1 first at hgap");
        check(l2.getBounds().equals(new java.awt.Rectangle(40, 5, 30, 10)), "left row1 second");
        check(l3.getBounds().equals(new java.awt.Rectangle(5, 20, 30, 10)), "left row2 restarts at hgap");
        // RIGHT alignment pins trailing edge
        java.awt.Container p3 = container(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 5, 2));
        java.awt.Component r1 = comp(30, 10);
        p3.add(r1);
        p3.setSize(100, 50); p3.doLayout();
        check(r1.getBounds().equals(new java.awt.Rectangle(65, 2, 30, 10)),
              "right-aligned x = width - hgap - w: " + r1.getBounds());
        // preferred size packs one row: sum widths + (n+1)*hgap, max height + 2*vgap
        java.awt.Container p4 = container(new java.awt.FlowLayout());
        p4.add(comp(30, 10)); p4.add(comp(30, 10)); p4.add(comp(30, 10));
        java.awt.FlowLayout fl4 = (java.awt.FlowLayout) p4.getLayout();
        check(fl4.preferredLayoutSize(p4).equals(new java.awt.Dimension(110, 20)),
              "prefdim single packed row 3*30+4*5 x 10+2*5: " + fl4.preferredLayoutSize(p4));
        check(fl4.minimumLayoutSize(p4).equals(new java.awt.Dimension(110, 20)),
              "mindim same for fixed-size children: " + fl4.minimumLayoutSize(p4));
        // FlowLayout is only a LayoutManager: no alignment/maximumSize methods (LayoutManager2-only)
        check(!(fl4 instanceof java.awt.LayoutManager2), "FlowLayout not a LayoutManager2");
        // layoutContainer works standalone on the passed container
        fl4.layoutContainer(p4);
        check(true, "layoutContainer standalone callable");
        // removeLayoutComponent is a no-op for FlowLayout size math: it queries the
        // container's component list directly (observed identical prefdim before/after)
        java.awt.FlowLayout fl5 = new java.awt.FlowLayout();
        java.awt.Container p5 = container(fl5);
        p5.add(comp(10, 10));
        check(fl5.preferredLayoutSize(p5).equals(new java.awt.Dimension(20, 20)),
              "single 10x10 child packs to 10+2*hgap x 10+2*vgap: " + fl5.preferredLayoutSize(p5));
        fl5.removeLayoutComponent(p5.getComponent(0));
        check(p5.getComponentCount() == 1, "removeLayoutComponent keeps container list intact");
        check(fl5.preferredLayoutSize(p5).equals(new java.awt.Dimension(20, 20)),
              "removeLayoutComponent does not change prefdim (observed)");
        // moving components right-to-left keeps alignment behavior stable (LTR assumption)
        check(java.awt.FlowLayout.LEFT != java.awt.FlowLayout.TRAILING, "LEFT distinct from TRAILING");
    }

}
