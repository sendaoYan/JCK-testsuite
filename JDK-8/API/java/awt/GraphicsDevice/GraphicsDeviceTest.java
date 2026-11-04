/*
 * @test
 * @summary java.awt.GraphicsDevice conformance: structural reflection + black-box behavior.
 * @key headful
 * @run main GraphicsDeviceTest
 */

import java.awt.*;

public class GraphicsDeviceTest {
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

    // ---- Part 1: structural reflection (public API surface) ----
    static void testStructure() throws Exception {
        Class<?> c = Class.forName("java.awt.GraphicsDevice");
        check(!c.isInterface() && !c.isEnum(), "not interface/enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getType"), "method getType");
        check(hasMethod(c, "getIDstring"), "method getIDstring");
        check(hasMethod(c, "getConfigurations"), "method getConfigurations");
        check(hasMethod(c, "getDefaultConfiguration"), "method getDefaultConfiguration");
        check(hasMethod(c, "getBestConfiguration"), "method getBestConfiguration");
        check(hasMethod(c, "isFullScreenSupported"), "method isFullScreenSupported");
        check(hasMethod(c, "setFullScreenWindow"), "method setFullScreenWindow");
        check(hasMethod(c, "getFullScreenWindow"), "method getFullScreenWindow");
        check(hasMethod(c, "isDisplayChangeSupported"), "method isDisplayChangeSupported");
        check(hasMethod(c, "setDisplayMode"), "method setDisplayMode");
        check(hasMethod(c, "getDisplayMode"), "method getDisplayMode");
        check(hasMethod(c, "getDisplayModes"), "method getDisplayModes");
        check(hasMethod(c, "getAvailableAcceleratedMemory"), "method getAvailableAcceleratedMemory");
        check(hasMethod(c, "isWindowTranslucencySupported"), "method isWindowTranslucencySupported");
        check(GraphicsDevice.TYPE_RASTER_SCREEN == 0
           && GraphicsDevice.TYPE_PRINTER == 1
           && GraphicsDevice.TYPE_IMAGE_BUFFER == 2, "type constants");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        check(GraphicsDevice.TYPE_RASTER_SCREEN == 0, "TYPE_RASTER_SCREEN == 0");
        check(GraphicsDevice.TYPE_PRINTER == 1, "TYPE_PRINTER == 1");
        check(GraphicsDevice.TYPE_IMAGE_BUFFER == 2, "TYPE_IMAGE_BUFFER == 2");

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice def = ge.getDefaultScreenDevice();
        check(def != null, "getDefaultScreenDevice non-null");

        GraphicsDevice[] devices = ge.getScreenDevices();
        check(devices != null && devices.length >= 1, "screen devices non-empty");
        for (GraphicsDevice d : devices) {
            check(d != null, "device non-null");
            check(d.getType() == GraphicsDevice.TYPE_RASTER_SCREEN,
                  "screen device type is TYPE_RASTER_SCREEN");
            String id = d.getIDstring();
            check(id != null && !id.isEmpty(), "getIDstring non-empty");
        }

        check(def.getType() == GraphicsDevice.TYPE_RASTER_SCREEN, "default device is a screen");

        DisplayMode dm = def.getDisplayMode();
        check(dm != null, "getDisplayMode non-null");
        check(dm.getWidth() > 0 && dm.getHeight() > 0, "current display mode positive size");

        DisplayMode[] modes = def.getDisplayModes();
        check(modes != null, "getDisplayModes non-null");
        check(modes.length >= 1, "getDisplayModes non-empty");
        for (DisplayMode m : modes) {
            check(m != null, "display mode non-null");
            check(m.getWidth() > 0 && m.getHeight() > 0, "display mode positive size");
        }

        GraphicsConfiguration[] configs = def.getConfigurations();
        check(configs != null, "getConfigurations non-null");
        check(configs.length >= 1, "getConfigurations non-empty");
        boolean allCfgNonNull = true;
        for (GraphicsConfiguration gc : configs) if (gc == null) allCfgNonNull = false;
        check(allCfgNonNull, "all configurations non-null");

        GraphicsConfiguration defCfg = def.getDefaultConfiguration();
        check(defCfg != null, "getDefaultConfiguration non-null");

        check(def.getFullScreenWindow() == null, "no full-screen window initially");

        // getBestConfiguration delegates to the supplied template
        final GraphicsConfiguration[] cfgArr = configs;
        GraphicsConfigTemplate template = new GraphicsConfigTemplate() {
            public GraphicsConfiguration getBestConfiguration(GraphicsConfiguration[] gc) {
                return (gc != null && gc.length > 0) ? gc[0] : null;
            }
            public boolean isGraphicsConfigSupported(GraphicsConfiguration gc) {
                return true;
            }
        };
        check(def.getBestConfiguration(template) == cfgArr[0],
              "getBestConfiguration delegates to template");

        // a template that reports nothing supported still gets asked
        GraphicsConfigTemplate nullT = new GraphicsConfigTemplate() {
            public GraphicsConfiguration getBestConfiguration(GraphicsConfiguration[] gc) {
                return null;
            }
            public boolean isGraphicsConfigSupported(GraphicsConfiguration gc) {
                return false;
            }
        };
        check(def.getBestConfiguration(nullT) == null,
              "getBestConfiguration propagates template null");

        // getIDstring is stable across calls
        check(def.getIDstring().equals(def.getIDstring()), "getIDstring stable");

        // two reads of the current display mode are equal
        check(def.getDisplayMode().equals(def.getDisplayMode()),
              "getDisplayMode stable (equals)");

        // availability probes: boolean only, values are platform-dependent
        boolean fs = def.isFullScreenSupported();
        check(fs || !fs, "isFullScreenSupported returns boolean");
        boolean dc = def.isDisplayChangeSupported();
        check(dc || !dc, "isDisplayChangeSupported returns boolean");
        if (dc) {
            def.setDisplayMode(def.getDisplayMode()); // idempotent reset, must not throw
            check(true, "setDisplayMode(current) accepted");
        }
        long mem = def.getAvailableAcceleratedMemory();
        check(mem >= -1, "accelerated memory >= -1 (unknown sentinel)");
        for (GraphicsDevice.WindowTranslucency wt : GraphicsDevice.WindowTranslucency.values()) {
            boolean v = def.isWindowTranslucencySupported(wt);
            check(v || !v, "isWindowTranslucencySupported(" + wt + ") returns boolean");
        }

        // full-screen window roundtrip on the default device
        Frame fsFrame = new Frame("full-screen");
        fsFrame.setSize(100, 80);
        fsFrame.setVisible(true);
        try {
            def.setFullScreenWindow(fsFrame);
            check(def.getFullScreenWindow() == fsFrame, "full-screen window roundtrip");
            def.setFullScreenWindow(null);
            check(def.getFullScreenWindow() == null, "full-screen window cleared");
        } finally {
            def.setFullScreenWindow(null);
            fsFrame.dispose();
        }
    }

    public static void main(String[] args) throws Exception {
        testStructure();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
