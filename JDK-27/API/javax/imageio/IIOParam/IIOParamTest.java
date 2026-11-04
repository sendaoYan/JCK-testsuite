/*
 * @test
 * @summary javax.imageio.IIOParam conformance: white-box structure + black-box behavior.
 * @run main IIOParamTest
 */

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.lang.reflect.Modifier;
import javax.imageio.IIOParam;
import javax.imageio.IIOParamController;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageTypeSpecifier;

public class IIOParamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface ThrowingRunnable { void run() throws Exception; }
    static void expectThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        boolean thrown = false;
        try { r.run(); } catch (Throwable t) {
            thrown = true;
            check(expected.isAssignableFrom(t.getClass()),
                  msg + " expected " + expected.getSimpleName() + " but got " + t.getClass().getSimpleName());
        }
        check(thrown, msg + " should throw " + expected.getSimpleName());
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.imageio.IIOParam");
        int mods = c.getModifiers();
        check(!c.isInterface(), "not interface");
        check(Modifier.isPublic(mods), "public");
        check(Modifier.isAbstract(mods), "abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        check(Modifier.isProtected(c.getDeclaredConstructor().getModifiers()), "protected no-arg ctor");
        java.util.Set<String> dm = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("setSourceRegion"), "method setSourceRegion");
        check(dm.contains("getSourceRegion"), "method getSourceRegion");
        check(dm.contains("setSourceSubsampling"), "method setSourceSubsampling");
        check(dm.contains("getSourceXSubsampling"), "method getSourceXSubsampling");
        check(dm.contains("getSourceYSubsampling"), "method getSourceYSubsampling");
        check(dm.contains("getSubsamplingXOffset"), "method getSubsamplingXOffset");
        check(dm.contains("getSubsamplingYOffset"), "method getSubsamplingYOffset");
        check(dm.contains("setSourceBands"), "method setSourceBands");
        check(dm.contains("getSourceBands"), "method getSourceBands");
        check(dm.contains("setDestinationType"), "method setDestinationType");
        check(dm.contains("getDestinationType"), "method getDestinationType");
        check(dm.contains("setDestinationOffset"), "method setDestinationOffset");
        check(dm.contains("getDestinationOffset"), "method getDestinationOffset");
        check(dm.contains("setController"), "method setController");
        check(dm.contains("getController"), "method getController");
        check(dm.contains("getDefaultController"), "method getDefaultController");
        check(dm.contains("hasController"), "method hasController");
        check(dm.contains("activateController"), "method activateController");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ImageReadParam p = new ImageReadParam(); // concrete IIOParam

        // defaults
        check(p.getSourceRegion() == null, "default sourceRegion null");
        check(p.getSourceXSubsampling() == 1, "default xSubsampling 1");
        check(p.getSourceYSubsampling() == 1, "default ySubsampling 1");
        check(p.getSubsamplingXOffset() == 0, "default xOffset 0");
        check(p.getSubsamplingYOffset() == 0, "default yOffset 0");
        check(p.getSourceBands() == null, "default sourceBands null");
        check(p.getDestinationType() == null, "default destinationType null");
        check(p.getDestinationOffset().equals(new Point(0, 0)), "default destinationOffset (0,0)");
        check(p.getController() == null, "default controller null");
        check(p.getDefaultController() == null, "default defaultController null");
        check(!p.hasController(), "default hasController false");

        // source region: stored as a copy
        Rectangle r = new Rectangle(1, 2, 8, 9);
        p.setSourceRegion(r);
        check(p.getSourceRegion().equals(r), "getSourceRegion equals set value");
        check(p.getSourceRegion() != r, "getSourceRegion returns a copy");
        r.x = 99;
        check(p.getSourceRegion().x == 1, "setSourceRegion copies input");
        p.setSourceRegion(null);
        check(p.getSourceRegion() == null, "setSourceRegion null clears");

        // invalid regions
        expectThrows(IllegalArgumentException.class, () -> p.setSourceRegion(new Rectangle(-1, 0, 4, 4)), "negative x");
        expectThrows(IllegalArgumentException.class, () -> p.setSourceRegion(new Rectangle(0, 0, 0, 4)), "zero width");

        // subsampling
        p.setSourceSubsampling(2, 3, 1, 2);
        check(p.getSourceXSubsampling() == 2, "xSubsampling");
        check(p.getSourceYSubsampling() == 3, "ySubsampling");
        check(p.getSubsamplingXOffset() == 1, "xOffset");
        check(p.getSubsamplingYOffset() == 2, "yOffset");
        p.setSourceSubsampling(1, 1, 0, 0);
        check(p.getSourceXSubsampling() == 1, "reset xSubsampling");
        expectThrows(IllegalArgumentException.class, () -> p.setSourceSubsampling(0, 1, 0, 0), "zero period");
        expectThrows(IllegalArgumentException.class, () -> p.setSourceSubsampling(2, 1, 2, 0), "offset out of range");

        // source bands: stored as a copy, duplicates/negative rejected
        int[] bands = new int[]{2, 0, 1};
        p.setSourceBands(bands);
        int[] got = p.getSourceBands();
        check(got.length == 3 && got[0] == 2 && got[1] == 0 && got[2] == 1, "sourceBands round-trip");
        check(got != bands, "getSourceBands returns a copy");
        bands[0] = 42;
        check(p.getSourceBands()[0] == 2, "setSourceBands copies input");
        p.setSourceBands(null);
        check(p.getSourceBands() == null, "setSourceBands null");
        expectThrows(IllegalArgumentException.class, () -> p.setSourceBands(new int[]{0, 0}), "duplicate band");
        expectThrows(IllegalArgumentException.class, () -> p.setSourceBands(new int[]{-1}), "negative band");

        // destination type stored by reference
        ImageTypeSpecifier its = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB);
        p.setDestinationType(its);
        check(p.getDestinationType() == its, "set/get destinationType identity");

        // destination offset: stored as a copy, null rejected
        Point off = new Point(3, 4);
        p.setDestinationOffset(off);
        check(p.getDestinationOffset().equals(new Point(3, 4)), "destinationOffset round-trip");
        check(p.getDestinationOffset() != off, "getDestinationOffset returns a copy");
        off.x = 100;
        check(p.getDestinationOffset().x == 3, "setDestinationOffset copies input");
        expectThrows(IllegalArgumentException.class, () -> p.setDestinationOffset(null), "null destinationOffset");

        // controller: trivial controller returning false, then one returning true
        IIOParamController no = new IIOParamController() { public boolean activate(IIOParam param) { return false; } };
        p.setController(no);
        check(p.getController() == no, "set/get controller identity");
        check(p.hasController(), "hasController true after set");
        check(p.getDefaultController() == null, "defaultController still null");
        check(!p.activateController(), "activateController delegates to false controller");
        final IIOParam[] seen = new IIOParam[1];
        p.setController(new IIOParamController() { public boolean activate(IIOParam param) { seen[0] = param; return true; } });
        check(p.activateController(), "activateController delegates to true controller");
        check(seen[0] == p, "controller receives the param being activated");
        p.setController(null);
        check(!p.hasController(), "setController null clears");
        expectThrows(IllegalStateException.class, () -> p.activateController(), "activateController without controller");
    }

    // ---- additions: exact-exception probes and cross-state contracts ----
    static void expectExact(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        boolean thrown = false;
        try { r.run(); } catch (Throwable t) {
            thrown = true;
            check(expected == t.getClass(),
                  msg + " expected exactly " + expected.getSimpleName() + " but got " + t.getClass().getSimpleName());
        }
        check(thrown, msg + " should throw " + expected.getSimpleName());
    }

    // IIOParam subclass exposing the protected defaultController field
    static class ParamWithDefaultController extends ImageReadParam {
        ParamWithDefaultController(IIOParamController def) {
            defaultController = def;
        }
    }

    static void testCoreBAdditions() {
        ImageReadParam p = new ImageReadParam();

        // full invalid-region matrix (each probed: IllegalArgumentException)
        expectExact(IllegalArgumentException.class, () -> p.setSourceRegion(new Rectangle(0, -1, 4, 4)), "region y<0");
        expectExact(IllegalArgumentException.class, () -> p.setSourceRegion(new Rectangle(0, 0, -2, 4)), "region w<0");
        expectExact(IllegalArgumentException.class, () -> p.setSourceRegion(new Rectangle(0, 0, 4, 0)), "region h=0");
        expectExact(IllegalArgumentException.class, () -> p.setSourceRegion(new Rectangle(0, 0, 4, -1)), "region h<0");
        p.setSourceRegion(new Rectangle(0, 0, 10000, 20000));
        check(p.getSourceRegion().width == 10000, "oversized region accepted at param level");
        Rectangle got = p.getSourceRegion();
        got.y = 77;
        check(p.getSourceRegion().y == 0, "getSourceRegion returns defensive copy");

        // baseline subsampling and offset==period / >period probes
        p.setSourceSubsampling(1, 1, 0, 0);
        check(p.getSourceXSubsampling() == 1 && p.getSubsamplingYOffset() == 0, "baseline (1,1,0,0)");
        expectExact(IllegalArgumentException.class, () -> p.setSourceSubsampling(-1, 1, 0, 0), "x period negative");
        expectExact(IllegalArgumentException.class, () -> p.setSourceSubsampling(1, 0, 0, 0), "y period 0");
        expectExact(IllegalArgumentException.class, () -> p.setSourceSubsampling(1, -3, 0, 0), "y period negative");
        expectExact(IllegalArgumentException.class, () -> p.setSourceSubsampling(2, 1, 3, 0), "x offset > x period");
        expectExact(IllegalArgumentException.class, () -> p.setSourceSubsampling(1, 2, 0, 2), "y offset == y period");
        expectExact(IllegalArgumentException.class, () -> p.setSourceSubsampling(1, 2, 0, 5), "y offset > y period");

        // cross-state: region vs subsampling throws IllegalStateException, not IAE
        ImageReadParam x = new ImageReadParam();
        x.setSourceSubsampling(3, 4, 2, 3);
        expectExact(IllegalStateException.class, () -> x.setSourceRegion(new Rectangle(0, 0, 2, 5)),
                    "region width <= subsamplingXOffset -> ISE");
        expectExact(IllegalStateException.class, () -> x.setSourceRegion(new Rectangle(0, 0, 5, 3)),
                    "region height <= subsamplingYOffset -> ISE");
        x.setSourceRegion(new Rectangle(0, 0, 3, 4));
        check(x.getSourceRegion().width == 3, "region w == xOff+1 accepted");
        ImageReadParam y = new ImageReadParam();
        y.setSourceRegion(new Rectangle(0, 0, 5, 5));
        expectExact(IllegalStateException.class, () -> y.setSourceSubsampling(6, 1, 5, 0),
                    "subsampling xOffset >= region width -> ISE");
        expectExact(IllegalStateException.class, () -> y.setSourceSubsampling(1, 7, 0, 5),
                    "subsampling yOffset >= region height -> ISE");
        y.setSourceSubsampling(2, 2, 1, 1);
        check(y.getSourceRegion().width == 5 && y.getSourceXSubsampling() == 2, "legal subsampling keeps region");

        // destination type null clears; negative destination offset is legal
        p.setDestinationType(null);
        check(p.getDestinationType() == null, "setDestinationType null clears");
        p.setDestinationOffset(new Point(-3, -4));
        check(p.getDestinationOffset().equals(new Point(-3, -4)), "negative destinationOffset legal");

        // zero-length band arrays accepted
        p.setSourceBands(new int[0]);
        check(p.getSourceBands() != null && p.getSourceBands().length == 0, "empty band array accepted");

        // controller exception relayed as the same object
        final RuntimeException marker = new RuntimeException("marker");
        p.setController(new IIOParamController() {
            public boolean activate(IIOParam param) { throw marker; }
        });
        RuntimeException relayed = null;
        try { p.activateController(); } catch (RuntimeException e) { relayed = e; }
        check(relayed == marker, "controller exception relayed unchanged");

        // defaultController alone never satisfies hasController/activateController
        IIOParamController def = param -> true;
        ParamWithDefaultController pd = new ParamWithDefaultController(def);
        check(pd.getDefaultController() == def, "getDefaultController returns protected default");
        check(pd.getController() == null, "controller null when only default installed");
        check(!pd.hasController(), "hasController false with only defaultController");
        expectExact(IllegalStateException.class, () -> pd.activateController(),
                    "activateController ignores defaultController");
        pd.setController(def);
        check(pd.activateController(), "explicit controller activated");

        // failed operations leave unrelated state intact
        ImageReadParam s = new ImageReadParam();
        s.setSourceRegion(new Rectangle(1, 1, 10, 10));
        s.setSourceBands(new int[]{0, 1});
        try { s.setSourceRegion(new Rectangle(0, 0, 0, 0)); } catch (IllegalArgumentException expected) { }
        check(s.getSourceRegion().equals(new Rectangle(1, 1, 10, 10)), "bad region leaves region intact");
        try { s.setSourceBands(new int[]{5, 5}); } catch (IllegalArgumentException expected) { }
        check(s.getSourceBands().length == 2 && s.getSourceBands()[1] == 1, "bad bands leave bands intact");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testCoreBAdditions();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
