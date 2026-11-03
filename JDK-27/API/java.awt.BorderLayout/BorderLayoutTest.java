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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
