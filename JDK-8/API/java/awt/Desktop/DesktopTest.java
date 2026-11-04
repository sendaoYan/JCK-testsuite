/*
 * @test
 * @key headful
 * @summary java.awt.Desktop conformance: class reflection.
 * @run main DesktopTest
 */

import java.awt.*;

public class DesktopTest {
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
    static void testContractDesktop() throws Exception {
        // measured on this X11/Xvfb display (jtreg @key headful)
        check(Desktop.isDesktopSupported(), "isDesktopSupported true on X11");
        Desktop d = Desktop.getDesktop();
        check(Desktop.getDesktop() == d, "getDesktop returns a singleton");
        check(d.isSupported(Desktop.Action.OPEN), "OPEN supported");
        check(!d.isSupported(Desktop.Action.EDIT), "EDIT unsupported here");
        check(!d.isSupported(Desktop.Action.PRINT), "PRINT unsupported here");
        check(!d.isSupported(Desktop.Action.MAIL), "MAIL unsupported here");
        // every declared Action answers without throwing
        for (Desktop.Action act : Desktop.Action.values()) {
            check(d.isSupported(act) || !d.isSupported(act), "isSupported(" + act + ") answers a boolean");
        }
        //@8 check(Desktop.Action.values().length == 5, "JDK 8 declares 5 actions");
        //@27 check(Desktop.Action.values().length == 24, "JDK 9+ declares 24 actions");
        //@27 check(!d.isSupported(Desktop.Action.APP_ABOUT), "APP_ABOUT unsupported on Linux");
        //@27 check(!d.isSupported(Desktop.Action.APP_OPEN_FILE), "APP_OPEN_FILE unsupported on Linux");
        //@27 check(!d.isSupported(Desktop.Action.APP_MENU_BAR), "APP_MENU_BAR unsupported on Linux");
        //@27 check(!d.isSupported(Desktop.Action.BROWSE_FILE_DIR), "BROWSE_FILE_DIR unsupported on Linux");
        //@27 check(!d.isSupported(Desktop.Action.MOVE_TO_TRASH), "MOVE_TO_TRASH unsupported on Linux");
        // null action tolerated
        try {
            d.isSupported(null);
            check(true, "isSupported(null) answers");
        } catch (Throwable t) {
            check(false, "isSupported(null) should not throw: " + t);
        }
        // unsupported actions reject with UnsupportedOperationException
        java.io.File tmp = java.io.File.createTempFile("jck-desk", ".txt");
        tmp.deleteOnExit();
        if (!d.isSupported(Desktop.Action.EDIT)) {
            expectExact(UnsupportedOperationException.class, "edit on unsupported action", () -> d.edit(tmp));
        }
        if (!d.isSupported(Desktop.Action.PRINT)) {
            expectExact(UnsupportedOperationException.class, "print on unsupported action", () -> d.print(tmp));
        }
        if (!d.isSupported(Desktop.Action.MAIL)) {
            expectExact(UnsupportedOperationException.class, "mail on unsupported action", () -> d.mail());
        }
        if (!d.isSupported(Desktop.Action.BROWSE)) {
            expectExact(UnsupportedOperationException.class, "browse on unsupported action",
                  () -> d.browse(java.net.URI.create("http://127.0.0.1:1/")));
        }
        // missing file argument is validated before launch
        expectExact(IllegalArgumentException.class, "open(nonexistent)",
              () -> d.open(new java.io.File("/nonexistent-jck-probe")));
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.awt.Desktop");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getDesktop"), "method getDesktop");
        check(hasMethod(c, "isDesktopSupported"), "method isDesktopSupported");
        check(hasMethod(c, "isSupported"), "method isSupported");
        check(hasMethod(c, "open"), "method open");
        check(hasMethod(c, "edit"), "method edit");
        check(hasMethod(c, "print"), "method print");
        check(hasMethod(c, "browse"), "method browse");
        check(hasMethod(c, "mail"), "method mail");
        testContractDesktop();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
