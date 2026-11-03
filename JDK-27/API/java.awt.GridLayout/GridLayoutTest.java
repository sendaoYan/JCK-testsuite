/*
 * @test
 * @summary java.awt.GridLayout conformance: white-box structure + black-box behavior.
 * @run main GridLayoutTest
 */

import java.awt.*;
import java.lang.reflect.*;

public class GridLayoutTest {
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
        Class<?> c = Class.forName("java.awt.GridLayout");
        check(c != null, "type loads: java.awt.GridLayout");
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
                "minimumLayoutSize", "setRows", "setColumns", "setHgap", "setVgap"}) {
            check(hasMethod(c, name), "has " + name);
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // constructors / getters
        GridLayout g = new GridLayout();
        check(g.getRows() == 1 && g.getColumns() == 0, "default rows=1 cols=0");
        check(g.getHgap() == 0 && g.getVgap() == 0, "default gaps 0");
        GridLayout g2 = new GridLayout(2, 3);
        check(g2.getRows() == 2 && g2.getColumns() == 3, "two-arg rows/cols");
        GridLayout g3 = new GridLayout(2, 3, 4, 5);
        check(g3.getRows() == 2 && g3.getColumns() == 3 && g3.getHgap() == 4 && g3.getVgap() == 5,
              "four-arg all fields");
        g3.setRows(3); g3.setColumns(4); g3.setHgap(6); g3.setVgap(7);
        check(g3.getRows() == 3 && g3.getColumns() == 4 && g3.getHgap() == 6 && g3.getVgap() == 7,
              "setters roundtrip");

        // preferred/minimum layout size
        Panel p = new Panel();
        GridLayout l = new GridLayout(2, 3, 4, 5);
        p.setLayout(l);
        for (int i = 0; i < 6; i++) p.add(new Box(50, 30, 40, 20));
        check(l.preferredLayoutSize(p).equals(new Dimension(158, 65)), "preferredLayoutSize 158x65");
        check(l.minimumLayoutSize(p).equals(new Dimension(128, 45)), "minimumLayoutSize 128x45");

        // layoutContainer exact cell assignment
        p.setSize(200, 100);
        p.doLayout();
        Rectangle[] expected = {
            new Rectangle(0, 0, 64, 47), new Rectangle(68, 0, 64, 47), new Rectangle(136, 0, 64, 47),
            new Rectangle(0, 52, 64, 47), new Rectangle(68, 52, 64, 47), new Rectangle(136, 52, 64, 47),
        };
        for (int i = 0; i < 6; i++) {
            check(p.getComponent(i).getBounds().equals(expected[i]), "cell " + i + " bounds");
        }

        // auto-compute rows when columns specified
        Panel p2 = new Panel();
        GridLayout l2 = new GridLayout(0, 1);
        p2.setLayout(l2);
        p2.add(new Box(10, 10));
        check(l2.preferredLayoutSize(p2).equals(new Dimension(10, 10)), "single comp 1x1");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
