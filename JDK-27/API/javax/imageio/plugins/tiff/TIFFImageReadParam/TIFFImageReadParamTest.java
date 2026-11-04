/*
 * @test
 * @summary javax.imageio.plugins.tiff.TIFFImageReadParam conformance: white-box structure + black-box behavior.
 * @run main TIFFImageReadParamTest
 */

import java.lang.reflect.Modifier;
import java.util.List;
import javax.imageio.plugins.tiff.BaselineTIFFTagSet;
import javax.imageio.plugins.tiff.ExifParentTIFFTagSet;
import javax.imageio.plugins.tiff.FaxTIFFTagSet;
import javax.imageio.plugins.tiff.GeoTIFFTagSet;
import javax.imageio.plugins.tiff.TIFFImageReadParam;
import javax.imageio.plugins.tiff.TIFFTag;
import javax.imageio.plugins.tiff.TIFFTagSet;

public class TIFFImageReadParamTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
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
        Class<?> c = Class.forName("javax.imageio.plugins.tiff.TIFFImageReadParam");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(Modifier.isFinal(mods), "final modifier");
        check(c.getSuperclass().getName().equals("javax.imageio.ImageReadParam"), "extends ImageReadParam");
        check(c.getConstructors().length == 1, "one public constructor");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        TIFFImageReadParam p = new TIFFImageReadParam();

        // default allowed tag sets: Baseline, Fax, ExifParent, GeoTIFF
        List<TIFFTagSet> allowed = p.getAllowedTagSets();
        check(allowed != null && allowed.size() == 4, "default 4 allowed tag sets");
        check(allowed.contains(BaselineTIFFTagSet.getInstance()), "contains Baseline");
        check(allowed.contains(FaxTIFFTagSet.getInstance()), "contains Fax");
        check(allowed.contains(ExifParentTIFFTagSet.getInstance()), "contains ExifParent");
        check(allowed.contains(GeoTIFFTagSet.getInstance()), "contains GeoTIFF");

        // default unknown tags false
        check(!p.getReadUnknownTags(), "default readUnknownTags false");

        // readUnknownTags round-trip
        p.setReadUnknownTags(true);
        check(p.getReadUnknownTags(), "setReadUnknownTags true");
        p.setReadUnknownTags(false);
        check(!p.getReadUnknownTags(), "setReadUnknownTags false");

        // add a tag set
        TIFFTagSet custom = ExifParentTIFFTagSet.getInstance(); // already present -> no-op
        int before = p.getAllowedTagSets().size();
        p.addAllowedTagSet(custom);
        check(p.getAllowedTagSets().size() == before, "duplicate addAllowedTagSet is no-op");

        TIFFTagSet fresh = new javax.imageio.plugins.tiff.TIFFTagSet(
            new java.util.ArrayList<javax.imageio.plugins.tiff.TIFFTag>());
        p.addAllowedTagSet(fresh);
        check(p.getAllowedTagSets().size() == before + 1, "addAllowedTagSet increases size");
        check(p.getAllowedTagSets().contains(fresh), "added tag set present");

        // remove a tag set
        p.removeAllowedTagSet(fresh);
        check(p.getAllowedTagSets().size() == before, "removeAllowedTagSet decreases size");
        check(!p.getAllowedTagSets().contains(fresh), "removed tag set absent");

        // remove a default tag set
        p.removeAllowedTagSet(BaselineTIFFTagSet.getInstance());
        check(p.getAllowedTagSets().size() == before - 1, "remove default tag set");

        // invalid arguments
        expectThrows(IllegalArgumentException.class, () -> p.addAllowedTagSet(null), "addAllowedTagSet null");
        expectThrows(IllegalArgumentException.class, () -> p.removeAllowedTagSet(null), "removeAllowedTagSet null");
    }

    // ---- Part 3 (deepened): exact-exception helper ----
    static void expectExact(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
            check(false, msg + " should throw " + expected.getSimpleName() + " but nothing was thrown");
        } catch (Throwable t) {
            check(t.getClass() == expected,
                  msg + " expected exactly " + expected.getName() + " but got " + t.getClass().getName());
        }
    }

    // ---- Part 3 (deepened): live list semantics, no-ops, inherited param state ----
    static void testDeepened() throws Exception {
        TIFFImageReadParam p = new TIFFImageReadParam();

        // exact exception classes for null arguments (probed on JDK-27)
        expectExact(IllegalArgumentException.class, () -> p.addAllowedTagSet(null), "addAllowedTagSet null IAE");
        expectExact(IllegalArgumentException.class,
            () -> p.removeAllowedTagSet(null), "removeAllowedTagSet null IAE");

        // removing a tag set that was never added is a no-op (probed)
        TIFFTagSet stranger = new TIFFTagSet(new java.util.ArrayList<TIFFTag>());
        int before = p.getAllowedTagSets().size();
        p.removeAllowedTagSet(stranger);
        check(p.getAllowedTagSets().size() == before, "removeAllowedTagSet absent no-op");

        // getAllowedTagSets returns the live internal list: direct add/remove take effect (probed)
        List<TIFFTagSet> view = p.getAllowedTagSets();
        view.add(stranger);
        check(p.getAllowedTagSets().size() == before + 1, "direct list add visible");
        check(p.getAllowedTagSets().contains(stranger), "direct add present");
        p.getAllowedTagSets().remove(stranger);
        check(p.getAllowedTagSets().size() == before, "direct list remove visible");
        // successive getAllowedTagSets calls expose the same live list
        check(p.getAllowedTagSets() == view, "getAllowedTagSets same live list");

        // removing every default tag set is permitted; they can be added back
        TIFFTagSet[] defaults = { BaselineTIFFTagSet.getInstance(), FaxTIFFTagSet.getInstance(),
                                  ExifParentTIFFTagSet.getInstance(), GeoTIFFTagSet.getInstance() };
        for (TIFFTagSet t : defaults) p.removeAllowedTagSet(t);
        check(p.getAllowedTagSets().isEmpty(), "all defaults removable");
        for (TIFFTagSet t : defaults) p.addAllowedTagSet(t);
        check(p.getAllowedTagSets().size() == 4, "defaults restorable");

        // duplicate addAllowedTagSet stays at one entry (probed)
        int sizeOnce = p.getAllowedTagSets().size();
        p.addAllowedTagSet(BaselineTIFFTagSet.getInstance());
        p.addAllowedTagSet(BaselineTIFFTagSet.getInstance());
        check(p.getAllowedTagSets().size() == sizeOnce, "duplicate add no-op");
        int baselineSeen = 0;
        for (TIFFTagSet t : p.getAllowedTagSets())
            if (t == BaselineTIFFTagSet.getInstance()) baselineSeen++;
        check(baselineSeen == 1, "baseline appears exactly once");

        // readUnknownTags initial default false for fresh instances
        check(!new TIFFImageReadParam().getReadUnknownTags(), "fresh instance readUnknownTags false");

        // inherited ImageReadParam contracts round-trip through the subclass
        p.setDestinationOffset(new java.awt.Point(3, 4));
        check(p.getDestinationOffset().x == 3 && p.getDestinationOffset().y == 4,
              "destination offset round-trip");
        p.setSourceSubsampling(2, 3, 1, 1);
        check(p.getSourceXSubsampling() == 2 && p.getSourceYSubsampling() == 3
              && p.getSubsamplingXOffset() == 1 && p.getSubsamplingYOffset() == 1,
              "source subsampling round-trip");
        p.setSourceSubsampling(1, 1, 0, 0);
        check(p.getSourceXSubsampling() == 1 && p.getSourceYSubsampling() == 1,
              "subsampling reset to baseline");
        p.setSourceBands(new int[]{0, 1});
        check(java.util.Arrays.equals(p.getSourceBands(), new int[]{0, 1}), "source bands round-trip");
        p.setSourceBands(null);
        check(p.getSourceBands() == null, "null clears source bands");
        check(p.canSetSourceRenderSize() == false, "canSetSourceRenderSize false");
        check(p.getDestination() == null && p.getDestinationType() == null, "default destination state");
        p.setSourceRegion(new java.awt.Rectangle(1, 2, 3, 4));
        check(p.getSourceRegion().equals(new java.awt.Rectangle(1, 2, 3, 4)), "source region round-trip");
        p.setSourceRegion(null);
        check(p.getSourceRegion() == null, "null clears source region");

        // invalid inherited settings behave per ImageReadParam contract (probed)
        expectExact(IllegalArgumentException.class,
            () -> p.setSourceSubsampling(-1, 1, 0, 0), "negative x subsampling IAE");
        expectExact(IllegalArgumentException.class,
            () -> p.setSourceSubsampling(1, 1, -1, 0), "negative x offset IAE");
        expectExact(IllegalArgumentException.class,
            () -> p.setSourceBands(new int[]{-1}), "negative source band IAE");
    }

    // ---- Part 3 (deepened): the param actually drives the TIFF reader ----
    static void testReaderIntegration() throws Exception {
        // write a small TIFF, then read it back through a TIFFImageReadParam flow
        int w = 4, h = 4;
        java.awt.image.BufferedImage src =
            new java.awt.image.BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_BYTE_GRAY);
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++)
                src.setRGB(x, y, ((x * 50 + y * 25) & 0xFF) << 16);
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        check(javax.imageio.ImageIO.write(src, "tiff", baos), "ImageIO.write tiff true");
        byte[] bytes = baos.toByteArray();

        // ImageIO reader discovery from TIFF magic bytes (probed: non-empty for both endians)
        byte[] littleEndian = {0x49, 0x49, 0x2A, 0x00, 0, 0, 0, 0};
        byte[] bigEndian = {(byte)0x4D, (byte)0x4D, 0x00, 0x2A, 0, 0, 0, 0};
        check(javax.imageio.ImageIO.getImageReaders(
                  javax.imageio.ImageIO.createImageInputStream(
                      new java.io.ByteArrayInputStream(littleEndian))).hasNext(),
              "readers found for II*\\0 magic");
        check(javax.imageio.ImageIO.getImageReaders(
                  javax.imageio.ImageIO.createImageInputStream(
                      new java.io.ByteArrayInputStream(bigEndian))).hasNext(),
              "readers found for MM\\0* magic");

        javax.imageio.stream.ImageInputStream iis =
            javax.imageio.ImageIO.createImageInputStream(new java.io.ByteArrayInputStream(bytes));
        javax.imageio.ImageReader reader =
            javax.imageio.ImageIO.getImageReaders(iis).next();
        try {
            reader.setInput(iis, true, false);
            check(reader.getFormatName().equals("tif"), "TIFF reader format name tif");
            // default param is a TIFFImageReadParam with the 4 standard tag sets
            javax.imageio.ImageReadParam dp = reader.getDefaultReadParam();
            check(dp instanceof TIFFImageReadParam, "default param is TIFFImageReadParam");
            if (dp instanceof TIFFImageReadParam) {
                List<TIFFTagSet> allowed = ((TIFFImageReadParam) dp).getAllowedTagSets();
                check(allowed.size() == 4, "reader default param allows 4 tag sets");
                check(allowed.contains(BaselineTIFFTagSet.getInstance()), "reader default includes baseline");
            }
            // read through a hand-made TIFFImageReadParam: pixels survive
            TIFFImageReadParam trp = new TIFFImageReadParam();
            trp.setReadUnknownTags(true);
            check(trp.getReadUnknownTags(), "readUnknownTags true before read");
            java.awt.image.BufferedImage out = reader.read(0, trp);
            check(out.getWidth() == w && out.getHeight() == h, "param-driven read dimensions");
            boolean eq = true;
            for (int y = 0; y < h && eq; y++)
                for (int x = 0; x < w && eq; x++)
                    if (src.getRGB(x, y) != out.getRGB(x, y)) eq = false;
            check(eq, "param-driven read pixels equal");
            // removing all allowed tag sets does not break basic decoding (probed)
            TIFFImageReadParam stripped = new TIFFImageReadParam();
            for (TIFFTagSet t : stripped.getAllowedTagSets().toArray(new TIFFTagSet[0]))
                stripped.removeAllowedTagSet(t);
            java.awt.image.BufferedImage out2 = reader.read(0, stripped);
            check(out2 != null && out2.getWidth() == w, "read works with no allowed tag sets");
        } finally {
            reader.dispose();
            iis.close();
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepened();
        testReaderIntegration();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
