/*
 * @test
 * @summary javax.imageio.ImageWriteParam conformance: white-box structure + black-box behavior.
 * @run main ImageWriteParamTest
 */

import java.awt.Dimension;
import java.lang.reflect.Modifier;
import java.util.Locale;
import javax.imageio.ImageWriteParam;

public class ImageWriteParamTest {
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

    // A concrete ImageWriteParam that advertises every optional feature.
    static class TestParam extends ImageWriteParam {
        TestParam() {
            super(Locale.US);
            canWriteTiles = true;
            canOffsetTiles = true;
            canWriteCompressed = true;
            compressionTypes = new String[]{"fast", "best"};
            canWriteProgressive = true;
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.imageio.ImageWriteParam");
        int mods = c.getModifiers();
        check(!c.isInterface(), "not interface");
        check(Modifier.isPublic(mods), "public");
        check(!Modifier.isAbstract(mods), "not abstract");
        check(c.getSuperclass().getName().equals("javax.imageio.IIOParam"), "extends IIOParam");
        // MODE constants
        check(ImageWriteParam.MODE_DISABLED == 0, "MODE_DISABLED 0");
        check(ImageWriteParam.MODE_DEFAULT == 1, "MODE_DEFAULT 1");
        check(ImageWriteParam.MODE_EXPLICIT == 2, "MODE_EXPLICIT 2");
        check(ImageWriteParam.MODE_COPY_FROM_METADATA == 3, "MODE_COPY_FROM_METADATA 3");
        java.util.Set<String> dm = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("getLocale"), "method getLocale");
        check(dm.contains("canWriteTiles"), "method canWriteTiles");
        check(dm.contains("canOffsetTiles"), "method canOffsetTiles");
        check(dm.contains("setTilingMode"), "method setTilingMode");
        check(dm.contains("getTilingMode"), "method getTilingMode");
        check(dm.contains("setTiling"), "method setTiling");
        check(dm.contains("unsetTiling"), "method unsetTiling");
        check(dm.contains("getTileWidth"), "method getTileWidth");
        check(dm.contains("getTileHeight"), "method getTileHeight");
        check(dm.contains("getTileGridXOffset"), "method getTileGridXOffset");
        check(dm.contains("getTileGridYOffset"), "method getTileGridYOffset");
        check(dm.contains("canWriteProgressive"), "method canWriteProgressive");
        check(dm.contains("setProgressiveMode"), "method setProgressiveMode");
        check(dm.contains("getProgressiveMode"), "method getProgressiveMode");
        check(dm.contains("canWriteCompressed"), "method canWriteCompressed");
        check(dm.contains("setCompressionMode"), "method setCompressionMode");
        check(dm.contains("getCompressionMode"), "method getCompressionMode");
        check(dm.contains("getCompressionTypes"), "method getCompressionTypes");
        check(dm.contains("setCompressionType"), "method setCompressionType");
        check(dm.contains("getCompressionType"), "method getCompressionType");
        check(dm.contains("unsetCompression"), "method unsetCompression");
        check(dm.contains("isCompressionLossless"), "method isCompressionLossless");
        check(dm.contains("setCompressionQuality"), "method setCompressionQuality");
        check(dm.contains("getCompressionQuality"), "method getCompressionQuality");
        check(dm.contains("getBitRate"), "method getBitRate");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // default (featureless) param
        ImageWriteParam plain = new ImageWriteParam(Locale.US);
        check(plain.getLocale() == Locale.US, "locale stored");
        check(!plain.canWriteTiles(), "default canWriteTiles false");
        check(!plain.canOffsetTiles(), "default canOffsetTiles false");
        check(!plain.canWriteCompressed(), "default canWriteCompressed false");
        check(!plain.canWriteProgressive(), "default canWriteProgressive false");
        expectThrows(UnsupportedOperationException.class, () -> plain.setCompressionMode(ImageWriteParam.MODE_DEFAULT), "setCompressionMode unsupported");
        expectThrows(UnsupportedOperationException.class, () -> plain.getCompressionMode(), "getCompressionMode unsupported");
        expectThrows(UnsupportedOperationException.class, () -> plain.setTilingMode(ImageWriteParam.MODE_DEFAULT), "setTilingMode unsupported");
        expectThrows(UnsupportedOperationException.class, () -> plain.setProgressiveMode(ImageWriteParam.MODE_DEFAULT), "setProgressiveMode unsupported");

        // fully featured param
        TestParam p = new TestParam();
        check(p.canWriteTiles(), "canWriteTiles true");
        check(p.canOffsetTiles(), "canOffsetTiles true");
        check(p.canWriteCompressed(), "canWriteCompressed true");
        check(p.canWriteProgressive(), "canWriteProgressive true");

        // tiling
        check(p.getTilingMode() == ImageWriteParam.MODE_COPY_FROM_METADATA, "default tiling mode COPY_FROM_METADATA");
        p.setTilingMode(ImageWriteParam.MODE_EXPLICIT);
        check(p.getTilingMode() == ImageWriteParam.MODE_EXPLICIT, "tiling mode EXPLICIT");
        p.setTiling(8, 16, 1, 2);
        check(p.getTileWidth() == 8, "tile width 8");
        check(p.getTileHeight() == 16, "tile height 16");
        check(p.getTileGridXOffset() == 1, "tile grid x 1");
        check(p.getTileGridYOffset() == 2, "tile grid y 2");
        p.unsetTiling();
        expectThrows(IllegalStateException.class, () -> p.getTileWidth(), "getTileWidth after unsetTiling");
        expectThrows(IllegalStateException.class, () -> p.getTileHeight(), "getTileHeight after unsetTiling");

        // progressive
        check(p.getProgressiveMode() == ImageWriteParam.MODE_COPY_FROM_METADATA, "default progressive mode");
        p.setProgressiveMode(ImageWriteParam.MODE_DEFAULT);
        check(p.getProgressiveMode() == ImageWriteParam.MODE_DEFAULT, "progressive mode DEFAULT");
        expectThrows(IllegalArgumentException.class, () -> p.setProgressiveMode(ImageWriteParam.MODE_EXPLICIT), "progressive MODE_EXPLICIT rejected");

        // compression
        check(p.getCompressionMode() == ImageWriteParam.MODE_COPY_FROM_METADATA, "default compression mode");
        p.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        check(p.getCompressionMode() == ImageWriteParam.MODE_EXPLICIT, "compression mode EXPLICIT");
        String[] types = p.getCompressionTypes();
        check(types.length == 2 && "fast".equals(types[0]) && "best".equals(types[1]), "compression types");
        check(p.getCompressionType() == null, "compression type initially null");
        p.setCompressionType("fast");
        check("fast".equals(p.getCompressionType()), "compression type fast");
        check("fast".equals(p.getLocalizedCompressionTypeName()), "localized compression type name");
        check(p.isCompressionLossless(), "default impl lossless");
        p.setCompressionQuality(0.5f);
        check(p.getCompressionQuality() == 0.5f, "compression quality 0.5");
        check(p.getBitRate(0.5f) == -1.0f, "getBitRate no estimate");
        expectThrows(IllegalArgumentException.class, () -> p.setCompressionType("bogus"), "unknown compression type");
        expectThrows(IllegalArgumentException.class, () -> p.setCompressionQuality(1.5f), "quality > 1");
        expectThrows(IllegalArgumentException.class, () -> p.setCompressionQuality(-0.1f), "quality < 0");
        p.unsetCompression();
        check(p.getCompressionType() == null, "unsetCompression clears type");
        expectThrows(IllegalStateException.class, () -> p.getCompressionQuality(), "getCompressionQuality without compression type");

        // non-EXPLICIT compression mode forbids parameter access
        p.setCompressionMode(ImageWriteParam.MODE_COPY_FROM_METADATA);
        check(p.getCompressionMode() == ImageWriteParam.MODE_COPY_FROM_METADATA, "compression mode COPY_FROM_METADATA");
        expectThrows(IllegalStateException.class, () -> p.getCompressionType(), "getCompressionType outside EXPLICIT");
        expectThrows(IllegalArgumentException.class, () -> p.setCompressionMode(99), "invalid compression mode");
    }

    // ---- additions (CORE-B): exact-exception probes, full state machines ----
    static void expectExact(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        boolean thrown = false;
        try { r.run(); } catch (Throwable t) {
            thrown = true;
            check(expected == t.getClass(),
                  msg + " expected exactly " + expected.getSimpleName() + " but got " + t.getClass().getSimpleName());
        }
        check(thrown, msg + " should throw " + expected.getSimpleName());
    }

    // Compressible param without a fixed compression type list
    static class FreeTypeParam extends ImageWriteParam {
        FreeTypeParam() {
            super(Locale.US);
            canWriteCompressed = true; // compressionTypes stays null
        }
    }

    // Tiling-capable param that does not allow tile offsets
    static class NoOffsetTilesParam extends ImageWriteParam {
        NoOffsetTilesParam() {
            super(Locale.US);
            canWriteTiles = true; // canOffsetTiles stays false
        }
    }

    // Tiling-capable param with a fixed preferredTileSizes array
    static class PreferredTilesParam extends ImageWriteParam {
        final Dimension[] sizes;
        PreferredTilesParam(Dimension[] sizes) {
            super(Locale.US);
            canWriteTiles = true;
            preferredTileSizes = sizes;
            this.sizes = sizes;
        }
    }

    static void testCoreBAdditions() throws Exception {
        // protected no-arg ctor exists; public ctor tolerates a null Locale
        Class<?> c = Class.forName("javax.imageio.ImageWriteParam");
        check(Modifier.isProtected(c.getDeclaredConstructor().getModifiers()), "protected no-arg ctor");
        check(new ImageWriteParam(null).getLocale() == null, "null locale tolerated");

        // featureless param: every optional accessor is UOE, even for invalid args
        ImageWriteParam plain = new ImageWriteParam(null);
        expectExact(UnsupportedOperationException.class, () -> plain.setTiling(8, 8, 0, 0), "setTiling unsupported");
        expectExact(UnsupportedOperationException.class, () -> plain.unsetTiling(), "unsetTiling unsupported");
        expectExact(UnsupportedOperationException.class, () -> plain.getTileWidth(), "getTileWidth unsupported");
        expectExact(UnsupportedOperationException.class, () -> plain.getTileGridXOffset(), "getTileGridXOffset unsupported");
        expectExact(UnsupportedOperationException.class, () -> plain.getPreferredTileSizes(), "getPreferredTileSizes unsupported");
        expectExact(UnsupportedOperationException.class, () -> plain.getProgressiveMode(), "getProgressiveMode unsupported");
        expectExact(UnsupportedOperationException.class, () -> plain.setCompressionMode(99), "setCompressionMode invalid still UOE");
        expectExact(UnsupportedOperationException.class, () -> plain.getCompressionTypes(), "getCompressionTypes unsupported");
        expectExact(UnsupportedOperationException.class, () -> plain.setCompressionType("x"), "setCompressionType unsupported");
        expectExact(UnsupportedOperationException.class, () -> plain.getCompressionType(), "getCompressionType unsupported");
        expectExact(UnsupportedOperationException.class, () -> plain.unsetCompression(), "unsetCompression unsupported");
        expectExact(UnsupportedOperationException.class, () -> plain.getLocalizedCompressionTypeName(), "localized name unsupported");
        expectExact(UnsupportedOperationException.class, () -> plain.isCompressionLossless(), "isCompressionLossless unsupported");
        expectExact(UnsupportedOperationException.class, () -> plain.setCompressionQuality(0.5f), "setCompressionQuality unsupported");
        expectExact(UnsupportedOperationException.class, () -> plain.getCompressionQuality(), "getCompressionQuality unsupported");
        expectExact(UnsupportedOperationException.class, () -> plain.getBitRate(0.5f), "getBitRate unsupported");
        expectExact(UnsupportedOperationException.class, () -> plain.getCompressionQualityDescriptions(), "descriptions unsupported");
        expectExact(UnsupportedOperationException.class, () -> plain.getCompressionQualityValues(), "values unsupported");

        // tiling state machine
        TestParam tp = new TestParam();
        expectExact(IllegalArgumentException.class, () -> tp.setTilingMode(-1), "tiling mode -1");
        expectExact(IllegalArgumentException.class, () -> tp.setTilingMode(4), "tiling mode 4");
        tp.setTilingMode(ImageWriteParam.MODE_DISABLED);
        expectExact(IllegalStateException.class, () -> tp.setTiling(8, 8, 0, 0), "setTiling outside EXPLICIT");
        expectExact(IllegalStateException.class, () -> tp.getTileWidth(), "getTileWidth outside EXPLICIT");
        expectExact(IllegalStateException.class, () -> tp.unsetTiling(), "unsetTiling outside EXPLICIT");
        tp.setTilingMode(ImageWriteParam.MODE_EXPLICIT);
        expectExact(IllegalArgumentException.class, () -> tp.setTiling(-1, 8, 0, 0), "negative tile width");
        expectExact(IllegalArgumentException.class, () -> tp.setTiling(8, 0, 0, 0), "zero tile height");
        tp.setTiling(8, 8, 1, 2);
        check(tp.getTileGridXOffset() == 1 && tp.getTileGridYOffset() == 2, "tile grid offsets round-trip");
        tp.unsetTiling();
        expectExact(IllegalStateException.class, () -> tp.getTileGridXOffset(), "getTileGridXOffset after unsetTiling");
        // offsets rejected when the subclass does not allow tile offsets
        NoOffsetTilesParam noOffset = new NoOffsetTilesParam();
        check(noOffset.canWriteTiles() && !noOffset.canOffsetTiles(), "tiles without offsets");
        noOffset.setTilingMode(ImageWriteParam.MODE_EXPLICIT);
        expectExact(UnsupportedOperationException.class, () -> noOffset.setTiling(8, 8, 1, 0), "offsets without canOffsetTiles");
        noOffset.setTiling(8, 8, 0, 0);
        check(noOffset.getTileGridXOffset() == 0, "zero offsets fine without canOffsetTiles");
        Dimension[] sizes = new Dimension[]{new Dimension(4, 4), new Dimension(16, 16)};
        PreferredTilesParam pref = new PreferredTilesParam(sizes);
        Dimension[] prefGot = pref.getPreferredTileSizes();
        check(prefGot.length == 2 && prefGot[0].width == 4 && prefGot[1].height == 16,
              "preferred tile sizes round-trip");
        check(prefGot != pref.sizes, "getPreferredTileSizes returns a copy");

        // progressive: mode range probes
        TestParam pr = new TestParam();
        pr.setProgressiveMode(ImageWriteParam.MODE_DISABLED);
        check(pr.getProgressiveMode() == ImageWriteParam.MODE_DISABLED, "progressive DISABLED");
        pr.setProgressiveMode(ImageWriteParam.MODE_COPY_FROM_METADATA);
        check(pr.getProgressiveMode() == ImageWriteParam.MODE_COPY_FROM_METADATA, "progressive COPY");
        expectExact(IllegalArgumentException.class, () -> pr.setProgressiveMode(7), "progressive mode 7");
        expectExact(IllegalArgumentException.class, () -> pr.setProgressiveMode(-1), "progressive mode -1");

        // compression state machine corners
        TestParam cp = new TestParam();
        expectExact(IllegalArgumentException.class, () -> cp.setCompressionMode(-1), "compression mode -1");
        expectExact(IllegalArgumentException.class, () -> cp.setCompressionMode(4), "compression mode 4");
        expectExact(IllegalStateException.class, () -> cp.setCompressionType("fast"), "setCompressionType outside EXPLICIT");
        expectExact(IllegalStateException.class, () -> cp.getCompressionType(), "getCompressionType outside EXPLICIT");
        expectExact(IllegalStateException.class, () -> cp.unsetCompression(), "unsetCompression outside EXPLICIT");
        expectExact(IllegalStateException.class, () -> cp.getCompressionQuality(), "getCompressionQuality outside EXPLICIT");
        cp.setCompressionMode(ImageWriteParam.MODE_DISABLED);
        check(cp.getCompressionMode() == ImageWriteParam.MODE_DISABLED, "compression DISABLED");
        cp.setCompressionMode(ImageWriteParam.MODE_DEFAULT);
        check(cp.getCompressionMode() == ImageWriteParam.MODE_DEFAULT, "compression DEFAULT");
        cp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        String[] types = cp.getCompressionTypes();
        check(types != null && types.length == 2 && "fast".equals(types[0]), "compression types copy");
        types[0] = "mutated";
        check(cp.getCompressionTypes()[0].equals("fast"), "getCompressionTypes returns a copy");
        cp.setCompressionType("fast");
        cp.setCompressionQuality(0.0f);
        check(cp.getCompressionQuality() == 0.0f, "quality 0.0 boundary");
        cp.setCompressionQuality(1.0f);
        check(cp.getCompressionQuality() == 1.0f, "quality 1.0 boundary");
        expectExact(IllegalArgumentException.class, () -> cp.getBitRate(1.5f), "bit rate quality > 1");
        expectExact(IllegalArgumentException.class, () -> cp.getBitRate(-0.1f), "bit rate quality < 0");
        check(cp.getCompressionQualityDescriptions() == null, "descriptions null by default");
        check(cp.getCompressionQualityValues() == null, "values null by default");
        cp.setCompressionType(null);
        check(cp.getCompressionType() == null, "setCompressionType(null) clears");
        cp.setCompressionType("fast");
        cp.setCompressionQuality(0.25f);
        cp.unsetCompression();
        check(cp.getCompressionMode() == ImageWriteParam.MODE_EXPLICIT, "unsetCompression keeps EXPLICIT");
        expectExact(IllegalStateException.class, () -> cp.getCompressionQuality(), "quality after unset");
        cp.setCompressionType("best");
        cp.setCompressionQuality(0.25f);
        check(cp.getCompressionQuality() == 0.25f, "re-set type then quality works");
        cp.setCompressionMode(ImageWriteParam.MODE_COPY_FROM_METADATA);
        cp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        check(cp.getCompressionType() == null, "re-entering EXPLICIT clears type");

        // compressible without a type list: type-less quality allowed
        FreeTypeParam fp = new FreeTypeParam();
        check(fp.getCompressionTypes() == null, "no compression type list");
        fp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        fp.setCompressionQuality(0.5f);
        check(fp.getCompressionQuality() == 0.5f, "quality without type list");
        expectExact(UnsupportedOperationException.class, () -> fp.setCompressionType("anything"),
                    "setCompressionType without type list");
        expectExact(IllegalStateException.class, () -> fp.getLocalizedCompressionTypeName(),
                    "localized name without type even with null list");
        check(fp.getCompressionQualityDescriptions() == null, "descriptions null with null type list");
        check(fp.getBitRate(0.5f) == -1.0f, "bit rate -1 with null type list");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testCoreBAdditions();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
