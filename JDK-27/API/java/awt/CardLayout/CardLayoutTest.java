/*
 * @test
 * @summary java.awt.CardLayout conformance: white-box structure + black-box behavior.
 * @run main CardLayoutTest
 */

import java.awt.*;
import java.lang.reflect.*;

public class CardLayoutTest {
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
        Class<?> c = Class.forName("java.awt.CardLayout");
        check(c != null, "type loads: java.awt.CardLayout");
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
                "minimumLayoutSize", "first", "last", "next", "previous", "show"}) {
            check(hasMethod(c, name), "has " + name);
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // constructors / gaps
        CardLayout cl = new CardLayout();
        check(cl.getHgap() == 0 && cl.getVgap() == 0, "default gaps 0");
        CardLayout clg = new CardLayout(5, 10);
        check(clg.getHgap() == 5 && clg.getVgap() == 10, "constructor gaps");
        clg.setHgap(6); clg.setVgap(7);
        check(clg.getHgap() == 6 && clg.getVgap() == 7, "setHgap/setVgap");

        // three cards
        Panel p = new Panel();
        CardLayout l = new CardLayout();
        p.setLayout(l);
        Box a = new Box(30, 10);
        Box b = new Box(40, 20);
        Box c = new Box(50, 30);
        p.add("a", a); p.add("b", b); p.add("c", c);
        p.setSize(100, 100);
        p.doLayout();
        check(a.isVisible() && !b.isVisible() && !c.isVisible(), "doLayout shows first card");
        check(a.getBounds().equals(new Rectangle(0, 0, 100, 100)), "first card bounds");

        l.show(p, "b");
        check(!a.isVisible() && b.isVisible() && !c.isVisible(), "show b");
        check(b.getBounds().equals(new Rectangle(0, 0, 100, 100)), "shown card bounds");

        l.next(p);
        check(!a.isVisible() && !b.isVisible() && c.isVisible(), "next shows c");
        l.previous(p);
        check(!a.isVisible() && b.isVisible() && !c.isVisible(), "previous back to b");
        l.last(p);
        check(!a.isVisible() && !b.isVisible() && c.isVisible(), "last shows c");
        l.first(p);
        check(a.isVisible() && !b.isVisible() && !c.isVisible(), "first shows a");

        // preferred/minimum layout size uses max preferred/minimum
        check(l.preferredLayoutSize(p).equals(new Dimension(50, 30)), "preferredLayoutSize 50x30");
        check(l.minimumLayoutSize(p).equals(new Dimension(50, 30)), "minimumLayoutSize 50x30");

        // gap-aware layout positioning
        Panel pg = new Panel();
        CardLayout lg = new CardLayout(5, 10);
        pg.setLayout(lg);
        Box x = new Box(20, 15);
        pg.add("x", x);
        pg.setSize(100, 100);
        pg.doLayout();
        check(x.getBounds().equals(new Rectangle(5, 10, 90, 80)), "gap-aware card bounds");
        check(lg.preferredLayoutSize(pg).equals(new Dimension(30, 35)), "gap-aware preferred");

        // removeLayoutComponent drops the card
        lg.removeLayoutComponent(x);
        pg.remove(x);
        check(pg.getComponentCount() == 0, "card removed");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
