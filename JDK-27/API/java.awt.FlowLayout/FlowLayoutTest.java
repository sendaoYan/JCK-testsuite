/*
 * @test
 * @summary java.awt.FlowLayout conformance: white-box structure + black-box behavior.
 * @run main FlowLayoutTest
 */

import java.awt.*;
import java.lang.reflect.*;

public class FlowLayoutTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }

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
        Class<?> c = Class.forName("java.awt.FlowLayout");
        check(c != null, "type loads: java.awt.FlowLayout");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public");
        check(!Modifier.isAbstract(m), "not abstract");
        boolean lm = false, serializable = false;
        for (Class<?> i : c.getInterfaces()) {
            if (i == LayoutManager.class) lm = true;
            if (i == java.io.Serializable.class) serializable = true;
        }
        check(lm, "implements LayoutManager");
        check(serializable, "implements Serializable");
        for (String name : new String[]{"layoutContainer", "preferredLayoutSize",
                "minimumLayoutSize", "getAlignment", "setAlignment", "setAlignOnBaseline"}) {
            check(hasMethod(c, name), "has " + name);
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // constants
        check(FlowLayout.LEFT == 0, "LEFT 0");
        check(FlowLayout.CENTER == 1, "CENTER 1");
        check(FlowLayout.RIGHT == 2, "RIGHT 2");
        check(FlowLayout.LEADING == 3, "LEADING 3");
        check(FlowLayout.TRAILING == 4, "TRAILING 4");

        // constructors / alignment / gaps
        FlowLayout f = new FlowLayout();
        check(f.getAlignment() == FlowLayout.CENTER && f.getHgap() == 5 && f.getVgap() == 5,
              "default CENTER/5/5");
        FlowLayout fl = new FlowLayout(FlowLayout.LEFT);
        check(fl.getAlignment() == FlowLayout.LEFT, "LEFT constructor");
        FlowLayout flg = new FlowLayout(FlowLayout.LEFT, 10, 20);
        check(flg.getAlignment() == FlowLayout.LEFT && flg.getHgap() == 10 && flg.getVgap() == 20,
              "three-arg constructor");
        flg.setAlignment(FlowLayout.RIGHT); flg.setHgap(4); flg.setVgap(6);
        check(flg.getAlignment() == FlowLayout.RIGHT && flg.getHgap() == 4 && flg.getVgap() == 6,
              "setters roundtrip");
        flg.setAlignOnBaseline(true);
        check(flg.getAlignOnBaseline(), "align on baseline flag");

        // preferred/minimum layout size with fixed components
        Panel p = new Panel();
        FlowLayout l = new FlowLayout(FlowLayout.LEFT, 10, 20);
        p.setLayout(l);
        Box b1 = new Box(50, 20, 20, 10);
        Box b2 = new Box(30, 10, 15, 5);
        p.add(b1); p.add(b2);
        check(l.preferredLayoutSize(p).equals(new Dimension(110, 60)), "preferredLayoutSize 110x60");
        check(l.minimumLayoutSize(p).equals(new Dimension(65, 50)), "minimumLayoutSize 65x50");

        // layoutContainer without wrap
        p.setSize(110, 60);
        p.doLayout();
        check(b1.getBounds().equals(new Rectangle(10, 20, 50, 20)), "no-wrap first bounds");
        check(b2.getBounds().equals(new Rectangle(70, 25, 30, 10)), "no-wrap second bounds");

        // layoutContainer with wrap
        Panel pw = new Panel();
        FlowLayout lw = new FlowLayout(FlowLayout.LEFT, 10, 20);
        pw.setLayout(lw);
        Box w1 = new Box(50, 20);
        Box w2 = new Box(30, 10);
        pw.add(w1); pw.add(w2);
        pw.setSize(60, 100);
        pw.doLayout();
        check(w1.getBounds().equals(new Rectangle(10, 20, 50, 20)), "wrap first row");
        check(w2.getY() > w1.getY(), "second component wraps to lower row");
        check(w2.getX() == 10, "wrapped row starts at left gap");
        check(w2.getBounds().equals(new Rectangle(10, 60, 30, 10)), "wrap second row bounds");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
