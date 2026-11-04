/*
 * @test
 * @summary java.awt.Toolkit conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main ToolkitTest
 */

import java.awt.*;
import java.awt.image.*;
import java.lang.reflect.*;
import java.util.*;

public class ToolkitTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.Toolkit", false, ToolkitTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.Toolkit");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public type");
        check(Modifier.isAbstract(m), "abstract class");
        check(!c.isInterface(), "not interface");
        check(c.getEnumConstants() == null, "non-enum");
        check(c.getSuperclass() == Object.class, "extends Object");

        check(Modifier.isStatic(c.getMethod("getDefaultToolkit").getModifiers()),
              "getDefaultToolkit is static");

        for (String name : new String[] {"getScreenSize", "getScreenResolution", "getColorModel",
                "getFontList", "getFontMetrics", "sync", "getImage", "createImage", "beep",
                "getSystemClipboard", "getSystemEventQueue", "getSystemSelection",
                "getMenuShortcutKeyMaskEx", "isAlwaysOnTopSupported"})
            check(Arrays.stream(c.getMethods()).anyMatch(mm -> mm.getName().equals(name)),
                  "method present: " + name);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        Toolkit tk = Toolkit.getDefaultToolkit();
        check(tk != null, "getDefaultToolkit non-null");
        check(Toolkit.getDefaultToolkit() == tk, "getDefaultToolkit returns singleton");

        Dimension screen = tk.getScreenSize();
        check(screen != null, "getScreenSize non-null");
        check(screen.width > 0 && screen.height > 0, "screen size positive");

        int dpi = tk.getScreenResolution();
        check(dpi > 0, "getScreenResolution positive");

        String[] fonts = tk.getFontList();
        check(fonts != null, "getFontList non-null");
        check(fonts.length >= 1, "getFontList non-empty");
        boolean hasDialog = false;
        for (String f : fonts) if ("Dialog".equals(f)) hasDialog = true;
        check(hasDialog, "getFontList contains logical font Dialog");

        ColorModel cm = tk.getColorModel();
        check(cm != null, "getColorModel non-null");
        check(cm.getPixelSize() > 0, "color model has positive pixel size");

        tk.beep(); // must not throw
        check(true, "beep() callable");

        // in-memory image path: createImage(ImageProducer)
        int w = 16, h = 16;
        int[] pix = new int[w * h];
        Arrays.fill(pix, 0xFFFF0000);
        Image img = tk.createImage(new MemoryImageSource(w, h, pix, 0, w));
        check(img != null, "createImage(ImageProducer) non-null");
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bi.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();
        check(bi.getRGB(8, 8) == 0xFFFF0000, "createImage producer draws red pixel");

        // system event queue is shared and non-null
        EventQueue eq = tk.getSystemEventQueue();
        check(eq != null, "getSystemEventQueue non-null");

        // static property lookup falls back to default for unknown key
        check("fallback".equals(Toolkit.getProperty("jck.missing.property.xyz", "fallback")),
              "getProperty returns default for unknown key");

        // screen insets against a default configuration (structural, not exact values)
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsConfiguration gc = ge.getDefaultScreenDevice().getDefaultConfiguration();
        Insets ins = tk.getScreenInsets(gc);
        check(ins != null, "getScreenInsets non-null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
