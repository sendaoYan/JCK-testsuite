/*
 * @test
 * @summary java.awt.TrayIcon conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main TrayIconTest
 */

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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
