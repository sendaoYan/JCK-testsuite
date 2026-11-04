/*
 * @test
 * @summary java.awt.GridBagLayout conformance: white-box structure + black-box behavior.
 * @run main GridBagLayoutTest
 */

import java.awt.*;
import java.lang.reflect.*;

public class GridBagLayoutTest {
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
        Class<?> c = Class.forName("java.awt.GridBagLayout");
        check(c != null, "type loads: java.awt.GridBagLayout");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public");
        check(!Modifier.isAbstract(m), "not abstract");
        boolean lm2 = false, serializable = false;
        for (Class<?> i : c.getInterfaces()) {
            if (i == LayoutManager2.class) lm2 = true;
            if (i == java.io.Serializable.class) serializable = true;
        }
        check(lm2, "implements LayoutManager2");
        check(serializable, "implements Serializable");
        for (String name : new String[]{"layoutContainer", "preferredLayoutSize",
                "minimumLayoutSize", "setConstraints", "getConstraints",
                "getLayoutOrigin", "getLayoutDimensions", "getLayoutWeights", "location"}) {
            check(hasMethod(c, name), "has " + name);
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // setConstraints clones; getConstraints returns a clone
        GridBagLayout g = new GridBagLayout();
        Box b = new Box(50, 30);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 1; c.gridy = 2; c.weightx = 0.5;
        g.setConstraints(b, c);
        c.gridx = 99; // mutate original after set
        GridBagConstraints got = g.getConstraints(b);
        check(got.gridx == 1 && got.gridy == 2 && got.weightx == 0.5, "setConstraints stored a clone");
        check(got != c, "getConstraints returns distinct object");
        got.gridx = 77; // mutate returned clone
        check(g.getConstraints(b).gridx == 1, "getConstraints returns a clone");

        // getConstraints on unknown component returns default constraints clone
        Box unknown = new Box(1, 1);
        GridBagConstraints def = g.getConstraints(unknown);
        check(def.gridx == GridBagConstraints.RELATIVE && def.anchor == GridBagConstraints.CENTER
                && def.fill == GridBagConstraints.NONE, "default constraints for unknown component");

        // single component, weight 1,1, fill BOTH -> fills container
        Panel p = new Panel();
        GridBagLayout l = new GridBagLayout();
        p.setLayout(l);
        Box s = new Box(50, 30);
        GridBagConstraints sc = new GridBagConstraints();
        sc.gridx = 0; sc.gridy = 0; sc.weightx = 1; sc.weighty = 1; sc.fill = GridBagConstraints.BOTH;
        p.add(s, sc);
        p.setSize(200, 100);
        p.doLayout();
        check(s.getBounds().equals(new Rectangle(0, 0, 200, 100)), "fill BOTH stretches to full cell");
        check(l.getLayoutOrigin().equals(new Point(0, 0)), "layout origin 0,0");
        check(java.util.Arrays.deepEquals(l.getLayoutDimensions(), new int[][]{{200}, {100}}),
              "layout dimensions single cell");
        check(java.util.Arrays.deepEquals(l.getLayoutWeights(), new double[][]{{1.0}, {1.0}}),
              "layout weights single cell");

        // two rows: horizontal fill + both fill
        Panel p2 = new Panel();
        GridBagLayout l2 = new GridBagLayout();
        p2.setLayout(l2);
        Box b0 = new Box(40, 20);
        Box b1 = new Box(60, 30);
        GridBagConstraints c0 = new GridBagConstraints();
        c0.gridx = 0; c0.gridy = 0; c0.weightx = 1; c0.weighty = 0; c0.fill = GridBagConstraints.HORIZONTAL;
        GridBagConstraints c1 = new GridBagConstraints();
        c1.gridx = 0; c1.gridy = 1; c1.weightx = 1; c1.weighty = 1; c1.fill = GridBagConstraints.BOTH;
        p2.add(b0, c0); p2.add(b1, c1);
        p2.setSize(200, 100);
        p2.doLayout();
        check(b0.getBounds().equals(new Rectangle(0, 0, 200, 20)), "row0 horizontal fill");
        check(b1.getBounds().equals(new Rectangle(0, 20, 200, 80)), "row1 both fill");
        check(java.util.Arrays.deepEquals(l2.getLayoutDimensions(), new int[][]{{200}, {20, 80}}),
              "two-row dimensions");
        check(java.util.Arrays.deepEquals(l2.getLayoutWeights(), new double[][]{{1.0}, {0.0, 1.0}}),
              "two-row weights");
        check(l2.location(5, 5).equals(new Point(0, 0)), "location top-left cell");
        check(l2.location(150, 50).equals(new Point(0, 1)), "location bottom cell");

        // anchor NORTHWEST keeps preferred size and centers it
        Panel p3 = new Panel();
        GridBagLayout l3 = new GridBagLayout();
        p3.setLayout(l3);
        Box a = new Box(30, 20);
        GridBagConstraints ac = new GridBagConstraints();
        ac.gridx = 0; ac.gridy = 0; ac.anchor = GridBagConstraints.NORTHWEST;
        p3.add(a, ac);
        p3.setSize(100, 100);
        p3.doLayout();
        check(a.getBounds().equals(new Rectangle(35, 40, 30, 20)), "anchor NORTHWEST centers preferred size");
        check(l3.getLayoutOrigin().equals(new Point(35, 40)), "anchor origin");

        // preferredLayoutSize with fixed components
        check(l3.preferredLayoutSize(p3).equals(new Dimension(30, 20)), "preferredLayoutSize single component");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
