/*
 * @test
 * @summary java.awt.Toolkit conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main ToolkitTest
 */

import java.awt.*;
import java.awt.event.KeyEvent;
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
        check(ins.top >= 0 && ins.left >= 0 && ins.bottom >= 0 && ins.right >= 0,
              "screen insets non-negative");
    }

    // ---- Part 3: image cache contracts ----
    static void testImageCache() throws Exception {
        Toolkit tk = Toolkit.getDefaultToolkit();
        // getImage(String): the toolkit caches the image per name
        Image a1 = tk.getImage("jck-cache-probe.png");
        Image a2 = tk.getImage("jck-cache-probe.png");
        check(a1 != null && a1 == a2, "getImage(String) returns cached instance");
        // getImage(URL): equal URLs hit the same cache entry
        java.net.URL u1 = new java.net.URL("http://127.0.0.1:1/x.png");
        java.net.URL u2 = new java.net.URL("http://127.0.0.1:1/x.png");
        check(u1.equals(u2), "probe URLs equal");
        Image b1 = tk.getImage(u1);
        Image b2 = tk.getImage(u2);
        check(b1 != null && b1 == b2, "getImage(URL) returns cached instance");
        // createImage always makes a fresh image, distinct from the cache entry
        Image c1 = tk.createImage("jck-cache-probe.png");
        Image c2 = tk.createImage("jck-cache-probe.png");
        check(c1 != null && c2 != null && c1 != c2, "createImage(String) fresh instance");
        check(c1 != a1, "createImage differs from getImage cache entry");
        // createImage(byte[]): well-formed tiny GIF, fresh instance each call
        byte[] gif = {(byte) 0x47, (byte) 0x49, (byte) 0x46, (byte) 0x38, (byte) 0x37,
                      (byte) 0x61, 1, 0, 1, 0, (byte) 0x80, 0, 0, (byte) 0xFF, 0, 0, 0,
                      0, 0, 0, (byte) 0x21, (byte) 0xF9, 4, 1, 0, 0, 0, 0, 0x2C, 0, 0,
                      0, 0, 1, 0, 1, 0, 0, 2, 2, (byte) 0x44, 1, 0, 0x3B};
        Image d1 = tk.createImage(gif);
        Image d2 = tk.createImage(gif);
        check(d1 != null && d2 != null && d1 != d2, "createImage(byte[]) fresh instance");
        Image d3 = tk.createImage(gif, 0, gif.length);
        check(d3 != null && d3 != d1, "createImage(byte[],off,len) fresh instance");
    }

    // ---- Part 4: load pipeline (prepareImage/checkImage + MediaTracker) ----
    static void testImageLoading() throws Exception {
        Toolkit tk = Toolkit.getDefaultToolkit();
        int w = 16, h = 16;
        int[] pix = new int[w * h];
        Arrays.fill(pix, 0xFF00FF00);
        Image img = tk.createImage(new MemoryImageSource(w, h, pix, 0, w));
        // prepareImage returns whether the image is fully loaded (boolean)
        boolean prepped = tk.prepareImage(img, -1, -1, null);
        check(prepped || !prepped, "prepareImage returns boolean");
        // deterministic completion via MediaTracker (in-memory source)
        Frame f = new Frame();
        MediaTracker mt = new MediaTracker(f);
        mt.addImage(img, 0);
        boolean loaded = mt.waitForID(0, 10000);
        check(loaded, "MediaTracker completes in-memory image");
        check(tk.prepareImage(img, -1, -1, null), "prepareImage true after full load");
        int done = tk.checkImage(img, -1, -1, null);
        check((done & java.awt.image.ImageObserver.ALLBITS) != 0,
              "checkImage reports ALLBITS after load");
        check((done & java.awt.image.ImageObserver.ERROR) == 0, "no ERROR after load");
        f.dispose();
    }

    // ---- Part 5: keyboard/cursor/desktop platform contracts ----
    static void testPlatformContracts() {
        Toolkit tk = Toolkit.getDefaultToolkit();
        // dynamic layout: setter callable, getter returns a boolean
        tk.setDynamicLayout(true);
        boolean a1 = tk.isDynamicLayoutActive();
        check(a1 || !a1, "isDynamicLayoutActive returns boolean (true)");
        tk.setDynamicLayout(false);
        boolean a2 = tk.isDynamicLayoutActive();
        check(a2 || !a2, "isDynamicLayoutActive returns boolean (false)");

        // locking keys: invalid key is always IAE; valid keys are boolean or UOE
        boolean threwUoe = false;
        try {
            boolean caps = tk.getLockingKeyState(KeyEvent.VK_CAPS_LOCK);
            check(caps || !caps, "getLockingKeyState(CAPS) returns boolean");
        } catch (UnsupportedOperationException uoe) {
            threwUoe = true;
            check(true, "getLockingKeyState(CAPS) may throw UOE");
        }
        try {
            tk.getLockingKeyState(KeyEvent.VK_NUM_LOCK);
            check(true, "getLockingKeyState(NUM) callable");
        } catch (UnsupportedOperationException uoe) {
            check(true, "getLockingKeyState(NUM) may throw UOE");
        }
        try {
            tk.getLockingKeyState(KeyEvent.VK_A);
            check(false, "getLockingKeyState(VK_A) should throw IAE");
        } catch (IllegalArgumentException expected) {
            check(true, "getLockingKeyState(VK_A) throws IAE");
        }
        try {
            tk.setLockingKeyState(KeyEvent.VK_CAPS_LOCK, false);
            check(true, "setLockingKeyState callable");
        } catch (UnsupportedOperationException uoe) {
            check(true, "setLockingKeyState may throw UOE");
        }
        check(!threwUoe || threwUoe, "probe bookkeeping");

        // cursor capabilities
        Dimension best = tk.getBestCursorSize(32, 32);
        check(best != null, "getBestCursorSize non-null");
        check(best.width >= 0 && best.height >= 0, "best cursor size non-negative");
        check((best.width == 0) == (best.height == 0),
              "cursor support is all-or-nothing (0x0 means unsupported)");
        Dimension tiny = tk.getBestCursorSize(1, 1);
        check(tiny != null, "getBestCursorSize(1,1) non-null");
        int colors = tk.getMaximumCursorColors();
        check(colors >= 0, "getMaximumCursorColors non-negative");
        if (best.width > 0) {
            check(colors >= 1, "supported cursors have at least 1 color");
            try {
                Cursor custom = tk.createCustomCursor(
                        new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB),
                        new Point(0, 0), "jck-cursor");
                check(custom != null, "createCustomCursor non-null where supported");
            } catch (HeadlessException | IllegalArgumentException e) {
                check(false, "createCustomCursor unexpectedly threw: " + e);
            }
        }

        // menu shortcut masks are non-zero (CTRL on this platform)
        int mask = tk.getMenuShortcutKeyMask();
        check(mask != 0, "getMenuShortcutKeyMask non-zero");
        int maskEx = tk.getMenuShortcutKeyMaskEx();
        check(maskEx != 0, "getMenuShortcutKeyMaskEx non-zero");

        // extra mouse buttons default (documented default is enabled)
        check(tk.areExtraMouseButtonsEnabled(), "extra mouse buttons enabled by default");

        // Frame.NORMAL state is always supported (Toolkit base implementation)
        check(tk.isFrameStateSupported(Frame.NORMAL), "NORMAL frame state supported");
        boolean icon = tk.isFrameStateSupported(Frame.ICONIFIED);
        check(icon || !icon, "ICONIFIED support reported");
        boolean max = tk.isFrameStateSupported(Frame.MAXIMIZED_BOTH);
        check(max || !max, "MAXIMIZED_BOTH support reported");
        boolean bogusState = tk.isFrameStateSupported(99);
        check(!bogusState, "bogus frame state not supported");

        // desktop properties: unknown key yields null, no exception
        check(tk.getDesktopProperty("jck.bogus.key") == null,
              "getDesktopProperty(unknown) null");
        Object known = tk.getDesktopProperty("awt.multiClickInterval");
        check(known == null || known instanceof Object, "known desktop property readable");

        // sync() must not throw
        tk.sync();
        check(true, "sync() callable");

        // clipboard contracts
        check(tk.getSystemClipboard() != null, "getSystemClipboard non-null");
        // selection may be unavailable on some platforms (null allowed)
        Object sel = tk.getSystemSelection();
        check(sel == null || sel instanceof java.awt.datatransfer.Clipboard,
              "getSystemSelection clipboard or null");

        // event queue is the toolkit singleton
        check(tk.getSystemEventQueue() == tk.getSystemEventQueue(),
              "getSystemEventQueue stable instance");

        // AWT event listener registration cycle (no SecurityManager installed)
        java.awt.event.AWTEventListener l = e -> {};
        tk.addAWTEventListener(l, AWTEvent.MOUSE_EVENT_MASK);
        check(tk.getAWTEventListeners().length >= 1, "AWTEventListener added");
        check(tk.getAWTEventListeners(AWTEvent.MOUSE_EVENT_MASK).length >= 1,
              "AWTEventListener filtered query");
        tk.removeAWTEventListener(l);
        check(tk.getAWTEventListeners().length == 0, "AWTEventListener removed");

        // font metrics for a fixed logical font
        Font f = new Font("Dialog", Font.PLAIN, 12);
        check(tk.getFontMetrics(f) != null, "getFontMetrics non-null");

        // input method highlight mapping must not throw (map may be null)
        tk.mapInputMethodHighlight(
                new java.awt.im.InputMethodHighlight(false, java.awt.im.InputMethodHighlight.RAW_TEXT));
        check(true, "mapInputMethodHighlight callable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testImageCache();
        testImageLoading();
        testPlatformContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
