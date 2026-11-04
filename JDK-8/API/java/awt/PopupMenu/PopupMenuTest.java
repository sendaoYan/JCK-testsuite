/*
 * @test
 * @key headful
 * @summary java.awt.PopupMenu conformance: class reflection.
 * @run main PopupMenuTest
 */

import java.awt.*;

public class PopupMenuTest {
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
    static void testContractPopupMenu() throws Exception {
        PopupMenu p = new PopupMenu("p");
        check("p".equals(p.getLabel()), "ctor label");
        check(p.getParent() == null, "no parent before attach");
        p.add("one"); p.addSeparator();
        check(p.getItemCount() == 2, "inherited Menu item plumbing");
        Frame f = new Frame("PopupMenuContract");
        f.setSize(160, 160);
        Canvas host = new Canvas();
        f.add(host);
        host.add(p);
        check(p.getParent() == host, "add wires popup parent");
        // show() before the origin is mapped fails deterministically (X11 contract)
        expectExact(RuntimeException.class, "show() with non-showing origin", () -> p.show(host, 5, 5));
        f.setVisible(true);
        try {
            p.show(host, 5, 5); // real show on a showing origin must not throw
            check(p.getItemCount() == 2, "items intact after show");
            java.awt.EventQueue.invokeAndWait(() -> { });
        } finally {
            f.dispose();
        }
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.awt.PopupMenu");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.Menu"), "superclass java.awt.Menu");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getParent"), "method getParent");
        check(hasMethod(c, "addNotify"), "method addNotify");
        check(hasMethod(c, "show"), "method show");
        check(hasMethod(c, "getAccessibleContext"), "method getAccessibleContext");
        testContractPopupMenu();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
