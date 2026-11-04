/*
 * @test
 * @summary java.awt.GraphicsDevice conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main GraphicsDeviceTest
 */

import java.awt.*;
import java.lang.reflect.*;
import java.util.*;

public class GraphicsDeviceTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.GraphicsDevice", false,
                                   GraphicsDeviceTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.GraphicsDevice");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public type");
        check(Modifier.isAbstract(m), "abstract class");
        check(!c.isInterface(), "not interface");
        check(c.getEnumConstants() == null, "non-enum");
        check(c.getSuperclass() == Object.class, "extends Object");

        for (String name : new String[] {"getType", "getIDstring", "getConfigurations",
                "getDefaultConfiguration", "getBestConfiguration", "isFullScreenSupported",
                "setFullScreenWindow", "getFullScreenWindow", "isDisplayChangeSupported",
                "setDisplayMode", "getDisplayMode", "getDisplayModes",
                "getAvailableAcceleratedMemory", "isWindowTranslucencySupported"})
            check(Arrays.stream(c.getMethods()).anyMatch(mm -> mm.getName().equals(name)),
                  "method present: " + name);

        for (String name : new String[] {"TYPE_RASTER_SCREEN", "TYPE_PRINTER", "TYPE_IMAGE_BUFFER"}) {
            Field f = c.getField(name);
            check(Modifier.isStatic(f.getModifiers()) && Modifier.isFinal(f.getModifiers())
                  && f.getType() == int.class, "public static final int " + name);
        }
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
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
