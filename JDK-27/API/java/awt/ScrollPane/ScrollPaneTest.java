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

    // ---- shared helper for exact-exception assertions (deepening batch) ----
    interface ThrowingAction { void run() throws Exception; }

    static void expectExact(Class<? extends Throwable> expected, String tag, ThrowingAction action) {
        try {
            action.run();
            check(false, tag + ": expected " + expected.getSimpleName() + " but nothing was thrown");
        } catch (Throwable thrown) {
            check(thrown.getClass() == expected,
                  tag + ": expected " + expected.getName() + " but got " + thrown.getClass().getName());
        }
    }

    // ---- Part 3: contract deepening ----
    static void testContractScrollPane() throws Exception {
        expectExact(IllegalArgumentException.class, "ctor(-1)", () -> new ScrollPane(-1));
        expectExact(IllegalArgumentException.class, "ctor(99)", () -> new ScrollPane(99));
        check(new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED).getScrollbarDisplayPolicy() == 0,
              "SCROLLBARS_AS_NEEDED policy");
        check(new ScrollPane(ScrollPane.SCROLLBARS_ALWAYS).getScrollbarDisplayPolicy() == 1,
              "SCROLLBARS_ALWAYS policy");
        check(new ScrollPane(ScrollPane.SCROLLBARS_NEVER).getScrollbarDisplayPolicy() == 2,
              "SCROLLBARS_NEVER policy");
        ScrollPane sp = new ScrollPane();
        // layout manager is fixed
        expectExact(java.awt.AWTError.class, "setLayout(FlowLayout)", () -> sp.setLayout(new FlowLayout()));
        // scroll position requires a child
        expectExact(NullPointerException.class, "setScrollPosition without child", () -> sp.setScrollPosition(1, 1));
        expectExact(NullPointerException.class, "getScrollPosition without child", () -> sp.getScrollPosition());
        Button child = new Button("c");
        check(sp.add(child) == child, "add returns the child");
        check(sp.getComponentCount() == 1, "child count");
        check(sp.getScrollPosition().equals(new Point(0, 0)), "initial scroll position (0,0)");
        sp.setScrollPosition(50, 50);
        check(sp.getScrollPosition().equals(new Point(0, 0)),
              "position clamps to 0 while child fits the viewport");
        Dimension vp0 = sp.getViewportSize();
        check(vp0 != null && vp0.width >= 0 && vp0.height >= 0, "viewport size non-negative");
        check(sp.getHScrollbarHeight() >= 0 && sp.getVScrollbarWidth() >= 0,
              "scrollbar extents non-negative before peer");
        sp.remove(0);
        check(sp.getComponentCount() == 0, "child removed");
        expectExact(NullPointerException.class, "getScrollPosition after removing child",
              () -> sp.getScrollPosition());

        // headful geometry: real extents and scroll arithmetic on a shown pane
        Frame f = new Frame("ScrollPaneContract");
        f.setSize(300, 300);
        ScrollPane shown = new ScrollPane(ScrollPane.SCROLLBARS_ALWAYS);
        Panel big = new Panel();
        big.setPreferredSize(new Dimension(600, 600));
        shown.add(big);
        f.add(shown);
        f.setVisible(true);
        try {
            check(shown.getHScrollbarHeight() >= 0 && shown.getVScrollbarWidth() >= 0,
                  "scrollbar extents non-negative when shown");
            Dimension vp = shown.getViewportSize();
            check(vp.width > 0 && vp.height > 0, "viewport positive when shown");
            java.awt.EventQueue.invokeAndWait(() -> { });
            shown.setScrollPosition(100, 200);
            java.awt.EventQueue.invokeAndWait(() -> { });
            check(shown.getScrollPosition().equals(new Point(100, 200)),
                  "setScrollPosition/getScrollPosition roundtrip");
            shown.setScrollPosition(100000, 100000);
            java.awt.EventQueue.invokeAndWait(() -> { });
            Point max = shown.getScrollPosition();
            check(max.x == 600 - vp.width && max.y == 600 - vp.height,
                  "position clamps to child size minus viewport");
            shown.setScrollPosition(-5, -5);
            java.awt.EventQueue.invokeAndWait(() -> { });
            check(shown.getScrollPosition().equals(new Point(0, 0)), "negative position clamps to 0");
        } finally {
            f.dispose();
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractScrollPane();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
