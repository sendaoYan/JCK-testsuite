/*
 * @test
 * @summary javax.imageio.IIOParam conformance: abstract hierarchy and methods.
 * @run main IIOParamTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
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
            check(expected == t.getClass(),
                  msg + " expected " + expected.getSimpleName() + " but got " + t.getClass().getSimpleName());
        }
        check(thrown, msg + " should throw " + expected.getSimpleName());
    }

    // An IIOParam subclass that installs a default controller via the
    // protected defaultController field.
    static class ParamWithDefaultController extends ImageReadParam {
        ParamWithDefaultController(IIOParamController def) {
            defaultController = def;
        }
    }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.imageio.IIOParam");
        check(!c.isInterface(), "not interface");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        Set<String> dm = new HashSet<>(); for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
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
        check(Modifier.isProtected(c.getDeclaredConstructor().getModifiers()), "protected no-arg ctor");
        testDefaults();
        testRegionValidation();
        testRegionCloneSemantics();
        testSubsamplingValidation();
        testRegionSubsamplingCrossState();
        testBands();
        testDestinationTypeAndOffset();
        testControllerDispatch();
        testStatePollution();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- defaults on a fresh concrete IIOParam ----
    static void testDefaults() {
        ImageReadParam p = new ImageReadParam();
        check(p.getSourceRegion() == null, "default sourceRegion null");
        check(p.getSourceXSubsampling() == 1, "default xSubsampling 1");
        check(p.getSourceYSubsampling() == 1, "default ySubsampling 1");
        check(p.getSubsamplingXOffset() == 0, "default subsamplingXOffset 0");
        check(p.getSubsamplingYOffset() == 0, "default subsamplingYOffset 0");
        check(p.getSourceBands() == null, "default sourceBands null");
        check(p.getDestinationType() == null, "default destinationType null");
        check(p.getDestinationOffset().equals(new Point(0, 0)), "default destinationOffset (0,0)");
        check(p.getController() == null, "default controller null");
        check(p.getDefaultController() == null, "default defaultController null");
        check(!p.hasController(), "default hasController false");
    }

    // ---- invalid source regions (each IAE probed) ----
    static void testRegionValidation() {
        ImageReadParam p = new ImageReadParam();
        expectThrows(IllegalArgumentException.class, () -> p.setSourceRegion(new Rectangle(-1, 0, 4, 4)), "region x<0");
        expectThrows(IllegalArgumentException.class, () -> p.setSourceRegion(new Rectangle(0, -1, 4, 4)), "region y<0");
        expectThrows(IllegalArgumentException.class, () -> p.setSourceRegion(new Rectangle(0, 0, 0, 4)), "region w=0");
        expectThrows(IllegalArgumentException.class, () -> p.setSourceRegion(new Rectangle(0, 0, -2, 4)), "region w<0");
        expectThrows(IllegalArgumentException.class, () -> p.setSourceRegion(new Rectangle(0, 0, 4, 0)), "region h=0");
        expectThrows(IllegalArgumentException.class, () -> p.setSourceRegion(new Rectangle(0, 0, 4, -1)), "region h<0");
        // large but non-negative geometry is accepted at the IIOParam level;
        // clipping to actual source bounds happens at I/O time
        p.setSourceRegion(new Rectangle(0, 0, 10000, 20000));
        check(p.getSourceRegion().width == 10000 && p.getSourceRegion().height == 20000,
              "oversized region accepted at param level");
        p.setSourceRegion(null);
        check(p.getSourceRegion() == null, "setSourceRegion null clears");
    }

    // ---- region stored as a copy ----
    static void testRegionCloneSemantics() {
        ImageReadParam p = new ImageReadParam();
        Rectangle r = new Rectangle(1, 2, 8, 9);
        p.setSourceRegion(r);
        check(p.getSourceRegion().equals(r), "region round-trip");
        check(p.getSourceRegion() != r, "getSourceRegion returns a copy");
        r.x = 99;
        check(p.getSourceRegion().x == 1, "setSourceRegion clones input");
        Rectangle got = p.getSourceRegion();
        got.y = 77;
        check(p.getSourceRegion().y == 2, "getSourceRegion returns defensive copy");
    }

    // ---- invalid subsampling (each IAE probed) ----
    static void testSubsamplingValidation() {
        ImageReadParam p = new ImageReadParam();
        p.setSourceSubsampling(1, 1, 0, 0);
        check(p.getSourceXSubsampling() == 1 && p.getSourceYSubsampling() == 1
              && p.getSubsamplingXOffset() == 0 && p.getSubsamplingYOffset() == 0,
              "baseline (1,1,0,0) subsampling");
        p.setSourceSubsampling(2, 3, 1, 2);
        check(p.getSourceXSubsampling() == 2, "x period round-trip");
        check(p.getSourceYSubsampling() == 3, "y period round-trip");
        check(p.getSubsamplingXOffset() == 1, "x offset round-trip");
        check(p.getSubsamplingYOffset() == 2, "y offset round-trip");
        expectThrows(IllegalArgumentException.class, () -> p.setSourceSubsampling(0, 1, 0, 0), "x period 0");
        expectThrows(IllegalArgumentException.class, () -> p.setSourceSubsampling(-1, 1, 0, 0), "x period negative");
        expectThrows(IllegalArgumentException.class, () -> p.setSourceSubsampling(1, 0, 0, 0), "y period 0");
        expectThrows(IllegalArgumentException.class, () -> p.setSourceSubsampling(1, -3, 0, 0), "y period negative");
        expectThrows(IllegalArgumentException.class, () -> p.setSourceSubsampling(2, 1, -1, 0), "x offset negative");
        expectThrows(IllegalArgumentException.class, () -> p.setSourceSubsampling(2, 1, 0, -1), "y offset negative");
        expectThrows(IllegalArgumentException.class, () -> p.setSourceSubsampling(2, 1, 2, 0), "x offset == x period");
        expectThrows(IllegalArgumentException.class, () -> p.setSourceSubsampling(2, 1, 3, 0), "x offset > x period");
        expectThrows(IllegalArgumentException.class, () -> p.setSourceSubsampling(1, 2, 0, 2), "y offset == y period");
        expectThrows(IllegalArgumentException.class, () -> p.setSourceSubsampling(1, 2, 0, 5), "y offset > y period");
        check(p.getSourceXSubsampling() == 2 && p.getSourceYSubsampling() == 3,
              "failed subsampling leaves state intact");
    }

    // ---- cross-state invariants between region and subsampling (ISE, not IAE) ----
    static void testRegionSubsamplingCrossState() {
        // subsampling offsets installed first, region too small afterwards
        ImageReadParam p = new ImageReadParam();
        p.setSourceSubsampling(3, 4, 2, 3);
        expectThrows(IllegalStateException.class,
                     () -> p.setSourceRegion(new Rectangle(0, 0, 2, 5)),
                     "region width <= subsamplingXOffset");
        expectThrows(IllegalStateException.class,
                     () -> p.setSourceRegion(new Rectangle(0, 0, 5, 3)),
                     "region height <= subsamplingYOffset");
        p.setSourceRegion(new Rectangle(0, 0, 3, 4));
        check(p.getSourceRegion().width == 3, "region w == xOff+1 accepted");
        // region installed first, subsampling offset starving the region
        ImageReadParam q = new ImageReadParam();
        q.setSourceRegion(new Rectangle(0, 0, 5, 5));
        expectThrows(IllegalStateException.class, () -> q.setSourceSubsampling(6, 1, 5, 0),
                     "subsampling xOffset >= region width");
        expectThrows(IllegalStateException.class, () -> q.setSourceSubsampling(1, 7, 0, 5),
                     "subsampling yOffset >= region height");
        q.setSourceSubsampling(2, 2, 1, 1);
        check(q.getSourceRegion().width == 5, "legal subsampling keeps region");
        check(q.getSourceXSubsampling() == 2, "legal subsampling applied");
    }

    // ---- source bands: copy semantics + validation ----
    static void testBands() {
        ImageReadParam p = new ImageReadParam();
        int[] bands = new int[]{2, 0, 1};
        p.setSourceBands(bands);
        int[] got = p.getSourceBands();
        check(got.length == 3 && got[0] == 2 && got[1] == 0 && got[2] == 1, "bands round-trip");
        check(got != bands, "getSourceBands returns a copy");
        bands[0] = 42;
        check(p.getSourceBands()[0] == 2, "setSourceBands clones input");
        expectThrows(IllegalArgumentException.class, () -> p.setSourceBands(new int[]{0, 0}), "duplicate band");
        expectThrows(IllegalArgumentException.class, () -> p.setSourceBands(new int[]{-1}), "negative band");
        check(p.getSourceBands()[0] == 2, "failed band update leaves state intact");
        p.setSourceBands(null);
        check(p.getSourceBands() == null, "setSourceBands null clears");
        // empty (zero-length) band arrays are accepted
        p.setSourceBands(new int[0]);
        check(p.getSourceBands() != null && p.getSourceBands().length == 0, "empty band array accepted");
    }

    // ---- destination type (reference semantics) and destination offset ----
    static void testDestinationTypeAndOffset() {
        ImageReadParam p = new ImageReadParam();
        ImageTypeSpecifier its = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB);
        p.setDestinationType(its);
        check(p.getDestinationType() == its, "destinationType stored by reference");
        p.setDestinationType(null);
        check(p.getDestinationType() == null, "setDestinationType null clears");
        Point off = new Point(3, 4);
        p.setDestinationOffset(off);
        check(p.getDestinationOffset().equals(new Point(3, 4)), "destinationOffset round-trip");
        check(p.getDestinationOffset() != off, "getDestinationOffset returns a copy");
        off.x = 100;
        check(p.getDestinationOffset().x == 3, "setDestinationOffset clones input");
        // negative offsets are explicitly legal (only null is rejected)
        p.setDestinationOffset(new Point(-3, -4));
        check(p.getDestinationOffset().equals(new Point(-3, -4)), "negative destinationOffset legal");
        expectThrows(IllegalArgumentException.class, () -> p.setDestinationOffset(null), "null destinationOffset");
        check(p.getDestinationOffset().x == -3, "failed offset update leaves state intact");
    }

    // ---- controller dispatch incl. defaultController quirk ----
    static void testControllerDispatch() {
        ImageReadParam p = new ImageReadParam();
        expectThrows(IllegalStateException.class, () -> p.activateController(), "activateController without controller");
        final IIOParam[] seen = new IIOParam[1];
        IIOParamController recorder = new IIOParamController() {
            public boolean activate(IIOParam param) { seen[0] = param; return true; }
        };
        p.setController(recorder);
        check(p.getController() == recorder, "controller stored by reference");
        check(p.hasController(), "hasController true after set");
        check(p.activateController(), "activateController delegates");
        check(seen[0] == p, "controller receives this param");
        // controller exception is relayed unchanged
        final RuntimeException marker = new RuntimeException("marker");
        p.setController(new IIOParamController() {
            public boolean activate(IIOParam param) { throw marker; }
        });
        RuntimeException relayed = null;
        try { p.activateController(); } catch (RuntimeException e) { relayed = e; }
        check(relayed == marker, "controller exception relayed as same object");
        p.setController(null);
        check(!p.hasController(), "setController null clears");
        check(p.getController() == null, "controller null after clear");
        // defaultController alone does not satisfy hasController/activateController
        IIOParamController def = new IIOParamController() {
            public boolean activate(IIOParam param) { return true; }
        };
        ParamWithDefaultController pd = new ParamWithDefaultController(def);
        check(pd.getDefaultController() == def, "getDefaultController returns protected default");
        check(pd.getController() == null, "controller still null when only default set");
        check(!pd.hasController(), "hasController false with only defaultController");
        expectThrows(IllegalStateException.class, () -> pd.activateController(),
                     "activateController requires explicit controller even with default present");
        pd.setController(def);
        check(pd.hasController(), "explicit controller wins");
        check(pd.activateController(), "explicit controller activated");
    }

    // ---- failed operations never corrupt unrelated state ----
    static void testStatePollution() {
        ImageReadParam p = new ImageReadParam();
        p.setSourceRegion(new Rectangle(1, 1, 10, 10));
        p.setSourceSubsampling(2, 2, 1, 1);
        p.setSourceBands(new int[]{0, 1});
        p.setDestinationOffset(new Point(5, 6));
        try { p.setSourceRegion(new Rectangle(0, 0, 0, 0)); } catch (IllegalArgumentException expected) { }
        check(p.getSourceRegion().equals(new Rectangle(1, 1, 10, 10)), "bad region leaves region intact");
        try { p.setSourceSubsampling(0, 0, 0, 0); } catch (IllegalArgumentException expected) { }
        check(p.getSourceXSubsampling() == 2 && p.getSubsamplingXOffset() == 1, "bad subsampling leaves subsampling intact");
        try { p.setSourceBands(new int[]{5, 5}); } catch (IllegalArgumentException expected) { }
        check(p.getSourceBands().length == 2 && p.getSourceBands()[1] == 1, "bad bands leave bands intact");
        try { p.setDestinationOffset(null); } catch (IllegalArgumentException expected) { }
        check(p.getDestinationOffset().equals(new Point(5, 6)), "bad offset leaves offset intact");
    }
}
