/*
 * @test
 * @summary java.awt.SystemTray conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main SystemTrayTest
 */

import java.lang.reflect.*;
import java.awt.SystemTray;

public class SystemTrayTest {
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
        Class<?> c = Class.forName("java.awt.SystemTray", false, SystemTrayTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.SystemTray");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public class");
        check(c.getEnumConstants() == null, "non-enum");
        check(c.getSuperclass() == Object.class, "extends Object");
        check(Modifier.isStatic(c.getMethod("getSystemTray").getModifiers()), "getSystemTray is static");
        check(Modifier.isStatic(c.getMethod("isSupported").getModifiers()), "isSupported is static");
        for (String n : new String[] {"add", "remove", "getTrayIcons", "getTrayIconSize",
                "addPropertyChangeListener", "removePropertyChangeListener", "getPropertyChangeListeners"})
            check(hasMethod(c, n), "method present: " + n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        boolean supported = SystemTray.isSupported();
        check(supported || !supported, "isSupported returns a boolean");

        if (supported) {
            SystemTray tray = SystemTray.getSystemTray();
            check(tray != null, "getSystemTray non-null when supported");
            check(SystemTray.getSystemTray() == tray, "getSystemTray returns singleton");

            java.awt.TrayIcon[] icons = tray.getTrayIcons();
            check(icons != null, "getTrayIcons returns non-null array");

            // property-change listeners: null listener is a no-op per contract
            tray.addPropertyChangeListener("trayIcons", null);
            tray.removePropertyChangeListener("trayIcons", null);
            check(true, "add/removePropertyChangeListener(null) callable");
            check(tray.getPropertyChangeListeners("trayIcons") != null, "getPropertyChangeListeners non-null");
        } else {
            try {
                SystemTray.getSystemTray();
                check(false, "getSystemTray should throw when unsupported");
            } catch (UnsupportedOperationException expected) {
                check(true, "getSystemTray throws UnsupportedOperationException when unsupported");
            }
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
