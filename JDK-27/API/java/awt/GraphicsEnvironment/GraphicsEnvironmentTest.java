/*
 * @test
 * @summary java.awt.GraphicsEnvironment conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main GraphicsEnvironmentTest
 */

import java.awt.*;
import java.awt.image.*;
import java.lang.reflect.*;
import java.util.*;

public class GraphicsEnvironmentTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.GraphicsEnvironment", false,
                                   GraphicsEnvironmentTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.GraphicsEnvironment");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public type");
        check(Modifier.isAbstract(m), "abstract class");
        check(!c.isInterface(), "not interface");
        check(c.getEnumConstants() == null, "non-enum");
        check(c.getSuperclass() == Object.class, "extends Object");

        check(Modifier.isStatic(c.getMethod("getLocalGraphicsEnvironment").getModifiers()),
              "getLocalGraphicsEnvironment static");
        check(Modifier.isStatic(c.getMethod("isHeadless").getModifiers()),
              "isHeadless static");

        for (String name : new String[] {"isHeadlessInstance", "getScreenDevices",
                "getDefaultScreenDevice", "createGraphics", "getAllFonts",
                "getAvailableFontFamilyNames", "registerFont", "preferLocaleFonts",
                "preferProportionalFonts", "getCenterPoint", "getMaximumWindowBounds"})
            check(Arrays.stream(c.getMethods()).anyMatch(mm -> mm.getName().equals(name)),
                  "method present: " + name);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        check(ge != null, "getLocalGraphicsEnvironment non-null");
        check(GraphicsEnvironment.getLocalGraphicsEnvironment() == ge,
              "getLocalGraphicsEnvironment returns singleton");

        check(!GraphicsEnvironment.isHeadless(), "isHeadless() false under headful run");
        check(ge.isHeadlessInstance() == GraphicsEnvironment.isHeadless(),
              "isHeadlessInstance consistent with isHeadless");

        GraphicsDevice[] devices = ge.getScreenDevices();
        check(devices != null, "getScreenDevices non-null");
        check(devices.length >= 1, "getScreenDevices non-empty");
        for (GraphicsDevice d : devices) check(d != null, "screen device non-null");

        GraphicsDevice def = ge.getDefaultScreenDevice();
        check(def != null, "getDefaultScreenDevice non-null");
        boolean contained = false;
        for (GraphicsDevice d : devices) if (d == def) contained = true;
        check(contained, "default screen device is among screen devices");

        String[] fams = ge.getAvailableFontFamilyNames();
        check(fams != null, "getAvailableFontFamilyNames non-null");
        check(fams.length >= 1, "getAvailableFontFamilyNames non-empty");
        check(contains(fams, "Dialog"), "font family names contain Dialog");

        String[] famsUs = ge.getAvailableFontFamilyNames(Locale.US);
        check(famsUs != null, "getAvailableFontFamilyNames(Locale) non-null");
        check(famsUs.length >= 1, "getAvailableFontFamilyNames(Locale) non-empty");
        check(contains(famsUs, "Dialog"), "font family names (Locale.US) contain Dialog");

        Font[] fonts = ge.getAllFonts();
        check(fonts != null, "getAllFonts non-null");
        check(fonts.length >= 1, "getAllFonts non-empty");
        boolean allFontsNonNull = true;
        boolean hasDialogFont = false;
        for (Font f : fonts) {
            if (f == null) allFontsNonNull = false;
            else if ("Dialog".equals(f.getFamily())) hasDialogFont = true;
        }
        check(allFontsNonNull, "all fonts non-null");
        check(hasDialogFont, "getAllFonts contains logical family Dialog");

        BufferedImage img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = ge.createGraphics(img);
        check(g != null, "createGraphics(BufferedImage) non-null");
        g.setColor(Color.RED);
        g.fillRect(0, 0, 32, 32);
        g.dispose();
        check(img.getRGB(10, 10) == 0xFFFF0000, "createGraphics renders into image");

        Point center = ge.getCenterPoint();
        check(center != null, "getCenterPoint non-null");

        Rectangle maxBounds = ge.getMaximumWindowBounds();
        check(maxBounds != null, "getMaximumWindowBounds non-null");
        check(maxBounds.width > 0 && maxBounds.height > 0, "maximum window bounds positive");
    }

    static boolean contains(String[] arr, String v) {
        for (String s : arr) if (v.equals(s)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
