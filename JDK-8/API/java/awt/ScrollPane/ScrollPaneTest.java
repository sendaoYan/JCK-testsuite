/*
 * @test
 * @key headful
 * @summary java.awt.ScrollPane conformance: class reflection.
 * @run main ScrollPaneTest
 */

import java.awt.*;

public class ScrollPaneTest {
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
        Class<?> c = Class.forName("java.awt.ScrollPane");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.Container"), "superclass java.awt.Container");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"javax.accessibility.Accessible"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "getScrollbarDisplayPolicy"), "method getScrollbarDisplayPolicy");
        check(hasMethod(c, "getViewportSize"), "method getViewportSize");
        check(hasMethod(c, "getHScrollbarHeight"), "method getHScrollbarHeight");
        check(hasMethod(c, "getVScrollbarWidth"), "method getVScrollbarWidth");
        check(hasMethod(c, "getVAdjustable"), "method getVAdjustable");
        check(hasMethod(c, "getHAdjustable"), "method getHAdjustable");
        check(hasMethod(c, "setScrollPosition"), "method setScrollPosition");
        check(hasMethod(c, "getScrollPosition"), "method getScrollPosition");
        check(hasMethod(c, "setLayout"), "method setLayout");
        check(hasMethod(c, "doLayout"), "method doLayout");
        check(hasMethod(c, "layout"), "method layout");
        check(hasMethod(c, "printComponents"), "method printComponents");
        check(hasMethod(c, "addNotify"), "method addNotify");
        check(hasMethod(c, "paramString"), "method paramString");
        check(hasMethod(c, "setWheelScrollingEnabled"), "method setWheelScrollingEnabled");
        check(hasMethod(c, "isWheelScrollingEnabled"), "method isWheelScrollingEnabled");
        check(hasMethod(c, "getAccessibleContext"), "method getAccessibleContext");
        testContractScrollPane();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
