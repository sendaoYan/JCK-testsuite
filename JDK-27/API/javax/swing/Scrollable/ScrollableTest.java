/*
 * @test
 * @summary javax.swing.Scrollable conformance: white-box structure + black-box behavior.
 * @run main ScrollableTest
 */

import java.awt.Dimension;
import java.awt.Rectangle;
import java.lang.reflect.Modifier;

import javax.swing.Scrollable;

public class ScrollableTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.Scrollable");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        Scrollable s = new Scrollable() {
            public Dimension getPreferredScrollableViewportSize() { return new Dimension(100, 200); }
            public int getScrollableUnitIncrement(Rectangle r, int o, int dir) { return 10; }
            public int getScrollableBlockIncrement(Rectangle r, int o, int dir) { return 20; }
            public boolean getScrollableTracksViewportWidth() { return true; }
            public boolean getScrollableTracksViewportHeight() { return false; }
        };
        check(s.getPreferredScrollableViewportSize().equals(new Dimension(100, 200)), "preferred size");
        check(s.getScrollableUnitIncrement(null, 0, 0) == 10, "unit increment");
        check(s.getScrollableBlockIncrement(null, 0, 0) == 20, "block increment");
        check(s.getScrollableTracksViewportWidth(), "tracks width");
        check(!s.getScrollableTracksViewportHeight(), "tracks height");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
