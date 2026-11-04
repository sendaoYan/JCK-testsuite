/*
 * @test
 * @summary java.awt.BorderLayout conformance: white-box structure + black-box behavior.
 * @run main BorderLayoutTest
 */

import java.awt.*;
import java.lang.reflect.*;

public class BorderLayoutTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }

    // fixed-size lightweight component: never consults the toolkit
    static class Box extends Component {
        final Dimension pref;
        final Dimension min;
        Box(int w, int h) { this(w, h, w, h); }
        Box(int w, int h, int mw, int mh) { pref = new Dimension(w, h); min = new Dimension(mw, mh); }
        public Dimension getPreferredSize() { return pref; }
        public Dimension getMinimumSize() { return min; }
        public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE); }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.BorderLayout");
        check(c != null, "type loads: java.awt.BorderLayout");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public");
        check(!Modifier.isAbstract(m), "not abstract");
        check(c.getSuperclass() == Object.class, "extends Object");
        boolean lm2 = false, serializable = false;
        for (Class<?> i : c.getInterfaces()) {
            if (i == LayoutManager2.class) lm2 = true;
            if (i == java.io.Serializable.class) serializable = true;
        }
        check(lm2, "implements LayoutManager2");
        check(serializable, "implements Serializable");
        for (String name : new String[]{"layoutContainer", "preferredLayoutSize",
                "minimumLayoutSize", "maximumLayoutSize", "addLayoutComponent",
                "removeLayoutComponent", "getLayoutComponent", "getConstraints"}) {
            check(hasMethod(c, name), "has " + name);
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // constructors / gaps
        BorderLayout bl = new BorderLayout();
        check(bl.getHgap() == 0 && bl.getVgap() == 0, "default gaps 0");
        BorderLayout bl2 = new BorderLayout(3, 5);
        check(bl2.getHgap() == 3 && bl2.getVgap() == 5, "constructor gaps");
        bl.setHgap(7); bl.setVgap(9);
        check(bl.getHgap() == 7 && bl.getVgap() == 9, "setHgap/setVgap");

        // constraint string constants
        check(BorderLayout.NORTH.equals("North"), "NORTH string");
        check(BorderLayout.SOUTH.equals("South"), "SOUTH string");
        check(BorderLayout.EAST.equals("East"), "EAST string");
        check(BorderLayout.WEST.equals("West"), "WEST string");
        check(BorderLayout.CENTER.equals("Center"), "CENTER string");

        // layoutContainer positioning (zero gaps, insets 0)
        Panel p = new Panel();
        BorderLayout l = new BorderLayout();
        p.setLayout(l);
        Box north = new Box(10, 20);
        Box south = new Box(10, 30);
        Box east = new Box(40, 10);
        Box west = new Box(30, 10);
        Box center = new Box(50, 50, 20, 20);
        p.add(north, BorderLayout.NORTH);
        p.add(south, BorderLayout.SOUTH);
        p.add(east, BorderLayout.EAST);
        p.add(west, BorderLayout.WEST);
        p.add(center, BorderLayout.CENTER);
        p.setSize(200, 100);
        p.doLayout();
        check(p.getComponentCount() == 5, "five children added");
        check(north.getBounds().equals(new Rectangle(0, 0, 200, 20)), "north bounds");
        check(south.getBounds().equals(new Rectangle(0, 70, 200, 30)), "south bounds");
        check(east.getBounds().equals(new Rectangle(160, 20, 40, 50)), "east bounds");
        check(west.getBounds().equals(new Rectangle(0, 20, 30, 50)), "west bounds");
        check(center.getBounds().equals(new Rectangle(30, 20, 130, 50)), "center bounds");

        // constraints lookup
        check(l.getLayoutComponent(BorderLayout.NORTH) == north, "getLayoutComponent NORTH");
        check(l.getLayoutComponent(p, BorderLayout.CENTER) == center, "getLayoutComponent(container, constraint)");
        check(BorderLayout.CENTER.equals(l.getConstraints(center)), "getConstraints center");

        // preferredLayoutSize uses preferred sizes
        Dimension pref = l.preferredLayoutSize(p);
        check(pref.equals(new Dimension(120, 100)), "preferredLayoutSize 120x100");

        // minimumLayoutSize uses minimum sizes (center min 20x20)
        Dimension min = l.minimumLayoutSize(p);
        check(min.equals(new Dimension(90, 70)), "minimumLayoutSize 90x70");

        // maximumLayoutSize
        check(l.maximumLayoutSize(p).equals(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE)),
              "maximumLayoutSize unbounded");

        // removeLayoutComponent removes the component from layout
        l.removeLayoutComponent(center);
        p.remove(center);
        p.doLayout();
        check(l.getLayoutComponent(BorderLayout.CENTER) == null, "center removed from layout");
        check(l.getConstraints(center) == null, "constraints of removed component null");
        check(BorderLayout.NORTH.equals(l.getConstraints(north)), "remaining north constraint intact");

        // gap-aware preferred size
        BorderLayout lg = new BorderLayout(10, 20);
        Panel pg = new Panel();
        pg.setLayout(lg);
        Box c1 = new Box(50, 40);
        pg.add(c1, BorderLayout.CENTER);
        Dimension d = lg.preferredLayoutSize(pg);
        check(d.equals(new Dimension(50, 40)), "gap-aware preferred with single center");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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
