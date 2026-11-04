/*
 * @test
 * @key headful
 * @summary java.awt.TrayIcon conformance: class reflection.
 * @run main TrayIconTest
 */

import java.awt.*;

public class TrayIconTest {
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
    static void testContractTrayIcon() throws Exception {
        // a zero-size image is rejected before any tray-support consideration
        expectExact(IllegalArgumentException.class, "TrayIcon(zero-size image)",
              () -> new java.awt.TrayIcon(new java.awt.image.BufferedImage(0, 0, java.awt.image.BufferedImage.TYPE_INT_RGB)));
        boolean tray = java.awt.SystemTray.isSupported();
        check(tray || !tray, "SystemTray.isSupported answers a boolean");
        if (!tray) {
            expectExact(UnsupportedOperationException.class, "getSystemTray without tray support",
                  () -> java.awt.SystemTray.getSystemTray());
            java.awt.image.BufferedImage img =
                  new java.awt.image.BufferedImage(16, 16, java.awt.image.BufferedImage.TYPE_INT_RGB);
            expectExact(UnsupportedOperationException.class, "TrayIcon(Image) without tray support",
                  () -> new java.awt.TrayIcon(img));
            expectExact(UnsupportedOperationException.class, "TrayIcon(Image,String) without tray support",
                  () -> new java.awt.TrayIcon(img, "tip"));
            expectExact(UnsupportedOperationException.class, "TrayIcon(null)",
                  () -> new java.awt.TrayIcon(null));
        }
        java.util.Set<String> names = new java.util.HashSet<>();
        for (java.awt.TrayIcon.MessageType mt : java.awt.TrayIcon.MessageType.values()) names.add(mt.name());
        check(names.equals(new java.util.HashSet<>(java.util.Arrays.asList(
                "ERROR", "WARNING", "INFO", "NONE"))), "MessageType constant set");
        check(java.awt.TrayIcon.MessageType.values().length == 4, "4 MessageType constants");
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.awt.TrayIcon");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "setImage"), "method setImage");
        check(hasMethod(c, "getImage"), "method getImage");
        check(hasMethod(c, "setPopupMenu"), "method setPopupMenu");
        check(hasMethod(c, "getPopupMenu"), "method getPopupMenu");
        check(hasMethod(c, "setToolTip"), "method setToolTip");
        check(hasMethod(c, "getToolTip"), "method getToolTip");
        check(hasMethod(c, "setImageAutoSize"), "method setImageAutoSize");
        check(hasMethod(c, "isImageAutoSize"), "method isImageAutoSize");
        check(hasMethod(c, "addMouseListener"), "method addMouseListener");
        check(hasMethod(c, "removeMouseListener"), "method removeMouseListener");
        check(hasMethod(c, "getMouseListeners"), "method getMouseListeners");
        check(hasMethod(c, "addMouseMotionListener"), "method addMouseMotionListener");
        check(hasMethod(c, "removeMouseMotionListener"), "method removeMouseMotionListener");
        check(hasMethod(c, "getMouseMotionListeners"), "method getMouseMotionListeners");
        check(hasMethod(c, "getActionCommand"), "method getActionCommand");
        check(hasMethod(c, "setActionCommand"), "method setActionCommand");
        check(hasMethod(c, "addActionListener"), "method addActionListener");
        check(hasMethod(c, "removeActionListener"), "method removeActionListener");
        check(hasMethod(c, "getActionListeners"), "method getActionListeners");
        check(hasMethod(c, "displayMessage"), "method displayMessage");
        check(hasMethod(c, "getSize"), "method getSize");
        testContractTrayIcon();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
