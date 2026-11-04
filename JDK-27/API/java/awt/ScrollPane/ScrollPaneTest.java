/*
 * @test
 * @summary java.awt.ScrollPane conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main ScrollPaneTest
 */

import java.awt.*;
import java.lang.reflect.*;

public class ScrollPaneTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.ScrollPane");
        check(c != null, "type loads: java.awt.ScrollPane");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public");
        check(!Modifier.isAbstract(m), "not abstract");
        check(c.getSuperclass() == Container.class, "extends Container");
        for (String name : new String[]{"getScrollbarDisplayPolicy", "getViewportSize",
                "getVAdjustable", "getHAdjustable", "setScrollPosition", "getScrollPosition",
                "setWheelScrollingEnabled", "isWheelScrollingEnabled"}) {
            check(hasMethod(c, name), "has " + name);
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        check(ScrollPane.SCROLLBARS_AS_NEEDED == 0, "SCROLLBARS_AS_NEEDED 0");
        check(ScrollPane.SCROLLBARS_ALWAYS == 1, "SCROLLBARS_ALWAYS 1");
        check(ScrollPane.SCROLLBARS_NEVER == 2, "SCROLLBARS_NEVER 2");

        ScrollPane sp = new ScrollPane();
        check(sp.getScrollbarDisplayPolicy() == ScrollPane.SCROLLBARS_AS_NEEDED, "default policy AS_NEEDED");
        check(sp.getSize().equals(new Dimension(100, 100)), "default size 100x100");

        ScrollPane spAlways = new ScrollPane(ScrollPane.SCROLLBARS_ALWAYS);
        check(spAlways.getScrollbarDisplayPolicy() == ScrollPane.SCROLLBARS_ALWAYS, "ALWAYS policy");
        check(new ScrollPane(ScrollPane.SCROLLBARS_NEVER).getScrollbarDisplayPolicy()
                == ScrollPane.SCROLLBARS_NEVER, "NEVER policy");

        boolean threw = false;
        try { new ScrollPane(99); } catch (IllegalArgumentException e) { threw = true; }
        check(threw, "illegal policy throws IllegalArgumentException");

        threw = false;
        try { sp.setLayout(new FlowLayout()); } catch (AWTError e) { threw = true; }
        check(threw, "setLayout throws AWTError");

        check(sp.getVAdjustable() != null && sp.getHAdjustable() != null, "adjustables non-null");
        check(sp.getVAdjustable() instanceof Adjustable && sp.getHAdjustable() instanceof Adjustable,
              "adjustables implement Adjustable");

        sp.add(new Label("child"));
        check(sp.getScrollPosition().equals(new Point(0, 0)), "default scroll position 0,0");
        sp.setScrollPosition(3, 4);
        check(sp.getScrollPosition().equals(new Point(0, 0)), "scroll position clamped to 0 pre-layout");

        check(sp.isWheelScrollingEnabled(), "wheel scrolling enabled default");
        sp.setWheelScrollingEnabled(false);
        check(!sp.isWheelScrollingEnabled(), "setWheelScrollingEnabled(false)");

        check(sp.getViewportSize() != null, "viewport size non-null");
        check(sp.getVScrollbarWidth() >= 0 && sp.getHScrollbarHeight() >= 0, "scrollbar extents non-negative");

        Frame f = new Frame("ScrollPaneTest");
        f.setSize(200, 200);
        f.setVisible(true);
        try {
            f.add(sp);
            check(sp.isDisplayable(), "displayable after add");
        } finally {
            f.dispose();
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
