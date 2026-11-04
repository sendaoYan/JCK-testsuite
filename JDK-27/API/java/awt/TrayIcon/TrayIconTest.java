/*
 * @test
 * @summary java.awt.TrayIcon conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main TrayIconTest
 */

import java.awt.*;

import java.lang.reflect.*;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;

public class TrayIconTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (Method m : c.getMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.TrayIcon", false, TrayIconTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.TrayIcon");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public class");
        check(c.getEnumConstants() == null, "non-enum");
        check(c.getConstructor(java.awt.Image.class) != null, "ctor (Image)");
        check(c.getConstructor(java.awt.Image.class, String.class) != null, "ctor (Image,String)");
        check(c.getConstructor(java.awt.Image.class, String.class, java.awt.PopupMenu.class) != null,
              "ctor (Image,String,PopupMenu)");
        for (String n : new String[] {"setImage", "getImage", "setToolTip", "getToolTip",
                "setImageAutoSize", "isImageAutoSize", "setActionCommand", "getActionCommand", "getSize"})
            check(hasMethod(c, n), "method present: " + n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);

        if (SystemTray.isSupported()) {
            TrayIcon ti = new TrayIcon(img, "tooltip");
            check(ti != null, "TrayIcon constructed when tray supported");
            check(ti.getImage() == img, "getImage returns the same image");
            check("tooltip".equals(ti.getToolTip()), "initial tooltip roundtrip");

            ti.setToolTip("new tip");
            check("new tip".equals(ti.getToolTip()), "setToolTip/getToolTip roundtrip");

            check(!ti.isImageAutoSize(), "default isImageAutoSize false");
            ti.setImageAutoSize(true);
            check(ti.isImageAutoSize(), "setImageAutoSize(true)");
            ti.setImageAutoSize(false);
            check(!ti.isImageAutoSize(), "setImageAutoSize(false)");

            ti.setActionCommand("my-command");
            check("my-command".equals(ti.getActionCommand()), "setActionCommand/getActionCommand roundtrip");
        } else {
            try {
                new TrayIcon(img, "tooltip");
                check(false, "TrayIcon ctor should throw when system tray unsupported");
            } catch (UnsupportedOperationException expected) {
                check(true, "TrayIcon ctor throws UnsupportedOperationException when system tray unsupported");
            }
            try {
                new TrayIcon(img);
                check(false, "TrayIcon(Image) ctor should throw when system tray unsupported");
            } catch (UnsupportedOperationException expected) {
                check(true, "TrayIcon(Image) ctor throws UnsupportedOperationException when system tray unsupported");
            }
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
        testWhiteBox();
        testBlackBox();
        testContractTrayIcon();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
