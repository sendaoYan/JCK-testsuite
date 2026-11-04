/*
 * @test
 * @summary javax.imageio.ImageWriteParam conformance: behavioral.
 * @run main ImageWriteParamTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.awt.Dimension;
import javax.imageio.ImageWriteParam;

public class ImageWriteParamTest {
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

    // Concrete param exposing every optional feature via protected fields
    static class FullParam extends ImageWriteParam {
        FullParam() {
            super(Locale.US);
            canWriteTiles = true;
            canOffsetTiles = true;
            canWriteCompressed = true;
            compressionTypes = new String[]{"fast", "best"};
            canWriteProgressive = true;
        }
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

    public static void main(String[] a) throws Exception {
        java.util.Locale loc = java.util.Locale.US;
        javax.imageio.ImageWriteParam p = new javax.imageio.ImageWriteParam(loc);
        check(p.getLocale() == loc, "locale");
        check(!p.canWriteTiles(), "canWriteTiles false");
        check(!p.canWriteCompressed(), "canWriteCompressed false");
        check(!p.canWriteProgressive(), "canWriteProgressive false");
        check(!p.canOffsetTiles(), "canOffsetTiles false");
        check(!p.canWriteCompressed(), "canWriteCompressed false again");
        testStructure();
        testPlainParamUnsupported();
        testModeConstants();
        testTilingContract();
        testProgressiveContract();
        testCompressionSequence();
        testCompressionWithoutTypeList();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testStructure() throws Exception {
        Class<?> c = Class.forName("javax.imageio.ImageWriteParam");
        check(!c.isInterface(), "not interface");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass().getName().equals("javax.imageio.IIOParam"), "extends IIOParam");
        Set<String> dm = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
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
        check(dm.contains("getPreferredTileSizes"), "method getPreferredTileSizes");
        check(dm.contains("getLocalizedCompressionTypeName"), "method getLocalizedCompressionTypeName");
        check(dm.contains("getCompressionQualityDescriptions"), "method getCompressionQualityDescriptions");
        check(dm.contains("getCompressionQualityValues"), "method getCompressionQualityValues");
        check(Modifier.isProtected(c.getDeclaredConstructor().getModifiers()), "protected no-arg ctor");
        check(Modifier.isPublic(c.getConstructor(Locale.class).getModifiers()), "public Locale ctor");
        // null Locale is tolerated by the public constructor
        check(new ImageWriteParam(null).getLocale() == null, "null locale tolerated");
    }

    // featureless param: every optional-feature accessor throws UOE (probed)
    static void testPlainParamUnsupported() {
        ImageWriteParam plain = new ImageWriteParam(null);
        expectThrows(UnsupportedOperationException.class, () -> plain.setTilingMode(ImageWriteParam.MODE_DEFAULT), "setTilingMode unsupported");
        expectThrows(UnsupportedOperationException.class, () -> plain.getTilingMode(), "getTilingMode unsupported");
        expectThrows(UnsupportedOperationException.class, () -> plain.setTiling(8, 8, 0, 0), "setTiling unsupported");
        expectThrows(UnsupportedOperationException.class, () -> plain.unsetTiling(), "unsetTiling unsupported");
        expectThrows(UnsupportedOperationException.class, () -> plain.getTileWidth(), "getTileWidth unsupported");
        expectThrows(UnsupportedOperationException.class, () -> plain.getTileHeight(), "getTileHeight unsupported");
        expectThrows(UnsupportedOperationException.class, () -> plain.getTileGridXOffset(), "getTileGridXOffset unsupported");
        expectThrows(UnsupportedOperationException.class, () -> plain.getTileGridYOffset(), "getTileGridYOffset unsupported");
        expectThrows(UnsupportedOperationException.class, () -> plain.getPreferredTileSizes(), "getPreferredTileSizes unsupported");
        expectThrows(UnsupportedOperationException.class, () -> plain.setProgressiveMode(ImageWriteParam.MODE_DEFAULT), "setProgressiveMode unsupported");
        expectThrows(UnsupportedOperationException.class, () -> plain.getProgressiveMode(), "getProgressiveMode unsupported");
        expectThrows(UnsupportedOperationException.class, () -> plain.setCompressionMode(ImageWriteParam.MODE_DEFAULT), "setCompressionMode unsupported");
        // even a syntactically invalid mode is rejected with UOE first (support precedes validation)
        expectThrows(UnsupportedOperationException.class, () -> plain.setCompressionMode(99), "setCompressionMode invalid still UOE");
        expectThrows(UnsupportedOperationException.class, () -> plain.getCompressionMode(), "getCompressionMode unsupported");
        expectThrows(UnsupportedOperationException.class, () -> plain.getCompressionTypes(), "getCompressionTypes unsupported");
        expectThrows(UnsupportedOperationException.class, () -> plain.setCompressionType("x"), "setCompressionType unsupported");
        expectThrows(UnsupportedOperationException.class, () -> plain.getCompressionType(), "getCompressionType unsupported");
        expectThrows(UnsupportedOperationException.class, () -> plain.unsetCompression(), "unsetCompression unsupported");
        expectThrows(UnsupportedOperationException.class, () -> plain.getLocalizedCompressionTypeName(), "getLocalizedCompressionTypeName unsupported");
        expectThrows(UnsupportedOperationException.class, () -> plain.isCompressionLossless(), "isCompressionLossless unsupported");
        expectThrows(UnsupportedOperationException.class, () -> plain.setCompressionQuality(0.5f), "setCompressionQuality unsupported");
        expectThrows(UnsupportedOperationException.class, () -> plain.getCompressionQuality(), "getCompressionQuality unsupported");
        expectThrows(UnsupportedOperationException.class, () -> plain.getBitRate(0.5f), "getBitRate unsupported");
        expectThrows(UnsupportedOperationException.class, () -> plain.getCompressionQualityDescriptions(), "getCompressionQualityDescriptions unsupported");
        expectThrows(UnsupportedOperationException.class, () -> plain.getCompressionQualityValues(), "getCompressionQualityValues unsupported");
    }

    static void testModeConstants() {
        check(ImageWriteParam.MODE_DISABLED == 0, "MODE_DISABLED 0");
        check(ImageWriteParam.MODE_DEFAULT == 1, "MODE_DEFAULT 1");
        check(ImageWriteParam.MODE_EXPLICIT == 2, "MODE_EXPLICIT 2");
        check(ImageWriteParam.MODE_COPY_FROM_METADATA == 3, "MODE_COPY_FROM_METADATA 3");
        check(ImageWriteParam.MODE_DISABLED != ImageWriteParam.MODE_DEFAULT, "modes distinct 0/1");
        check(ImageWriteParam.MODE_EXPLICIT != ImageWriteParam.MODE_COPY_FROM_METADATA, "modes distinct 2/3");
    }

    // tiling state machine on a fully featured param (probed exceptions)
    static void testTilingContract() {
        FullParam p = new FullParam();
        check(p.canWriteTiles(), "canWriteTiles true");
        check(p.canOffsetTiles(), "canOffsetTiles true");
        check(p.getTilingMode() == ImageWriteParam.MODE_COPY_FROM_METADATA, "default tiling mode COPY_FROM_METADATA");
        expectThrows(IllegalArgumentException.class, () -> p.setTilingMode(-1), "tiling mode -1");
        expectThrows(IllegalArgumentException.class, () -> p.setTilingMode(4), "tiling mode 4");
        p.setTilingMode(ImageWriteParam.MODE_DISABLED);
        check(p.getTilingMode() == ImageWriteParam.MODE_DISABLED, "tiling mode DISABLED");
        expectThrows(IllegalStateException.class, () -> p.setTiling(8, 8, 0, 0), "setTiling outside EXPLICIT");
        expectThrows(IllegalStateException.class, () -> p.getTileWidth(), "getTileWidth outside EXPLICIT");
        expectThrows(IllegalStateException.class, () -> p.unsetTiling(), "unsetTiling outside EXPLICIT");
        p.setTilingMode(ImageWriteParam.MODE_EXPLICIT);
        check(p.getTilingMode() == ImageWriteParam.MODE_EXPLICIT, "tiling mode EXPLICIT");
        expectThrows(IllegalArgumentException.class, () -> p.setTiling(-1, 8, 0, 0), "negative tile width");
        expectThrows(IllegalArgumentException.class, () -> p.setTiling(8, 0, 0, 0), "zero tile height");
        expectThrows(IllegalArgumentException.class, () -> p.setTiling(8, -8, 0, 0), "negative tile height");
        p.setTiling(8, 16, 1, 2);
        check(p.getTileWidth() == 8, "tile width 8");
        check(p.getTileHeight() == 16, "tile height 16");
        check(p.getTileGridXOffset() == 1, "tile grid x offset 1");
        check(p.getTileGridYOffset() == 2, "tile grid y offset 2");
        p.unsetTiling();
        expectThrows(IllegalStateException.class, () -> p.getTileWidth(), "getTileWidth after unsetTiling");
        expectThrows(IllegalStateException.class, () -> p.getTileHeight(), "getTileHeight after unsetTiling");
        expectThrows(IllegalStateException.class, () -> p.getTileGridXOffset(), "getTileGridXOffset after unsetTiling");
        expectThrows(IllegalStateException.class, () -> p.getTileGridYOffset(), "getTileGridYOffset after unsetTiling");
        p.setTilingMode(ImageWriteParam.MODE_COPY_FROM_METADATA);
        expectThrows(IllegalStateException.class, () -> p.getTileWidth(), "getTileWidth after mode change");
        check(p.getPreferredTileSizes() == null, "preferred tile sizes null when unset");
        // offsets rejected when the subclass does not allow tile offsets
        NoOffsetTilesParam noOffset = new NoOffsetTilesParam();
        check(noOffset.canWriteTiles() && !noOffset.canOffsetTiles(), "tiles without offsets");
        noOffset.setTilingMode(ImageWriteParam.MODE_EXPLICIT);
        expectThrows(UnsupportedOperationException.class, () -> noOffset.setTiling(8, 8, 1, 0), "offsets without canOffsetTiles");
        noOffset.setTiling(8, 8, 0, 0);
        check(noOffset.getTileGridXOffset() == 0, "zero offsets fine without canOffsetTiles");
        // preferredTileSizes is returned as a copy of the installed array
        Dimension[] sizes = new Dimension[]{new Dimension(4, 4), new Dimension(16, 16)};
        PreferredTilesParam pref = new PreferredTilesParam(sizes);
        Dimension[] prefGot = pref.getPreferredTileSizes();
        check(prefGot.length == 2 && prefGot[0].width == 4 && prefGot[1].height == 16,
              "preferred tile sizes round-trip");
        check(prefGot != pref.sizes, "getPreferredTileSizes returns a copy");
    }

    // progressive: only DISABLED/DEFAULT/COPY_FROM_METADATA are legal (probed)
    static void testProgressiveContract() {
        FullParam p = new FullParam();
        check(p.canWriteProgressive(), "canWriteProgressive true");
        check(p.getProgressiveMode() == ImageWriteParam.MODE_COPY_FROM_METADATA, "default progressive mode");
        p.setProgressiveMode(ImageWriteParam.MODE_DISABLED);
        check(p.getProgressiveMode() == ImageWriteParam.MODE_DISABLED, "progressive DISABLED");
        p.setProgressiveMode(ImageWriteParam.MODE_DEFAULT);
        check(p.getProgressiveMode() == ImageWriteParam.MODE_DEFAULT, "progressive DEFAULT");
        p.setProgressiveMode(ImageWriteParam.MODE_COPY_FROM_METADATA);
        check(p.getProgressiveMode() == ImageWriteParam.MODE_COPY_FROM_METADATA, "progressive COPY_FROM_METADATA");
        expectThrows(IllegalArgumentException.class,
                     () -> p.setProgressiveMode(ImageWriteParam.MODE_EXPLICIT), "progressive MODE_EXPLICIT rejected");
        expectThrows(IllegalArgumentException.class, () -> p.setProgressiveMode(7), "progressive mode 7");
        expectThrows(IllegalArgumentException.class, () -> p.setProgressiveMode(-1), "progressive mode -1");
    }

    // compression state machine (probed exceptions, javadoc order preserved)
    static void testCompressionSequence() {
        FullParam p = new FullParam();
        check(p.canWriteCompressed(), "canWriteCompressed true");
        check(p.getCompressionMode() == ImageWriteParam.MODE_COPY_FROM_METADATA, "default compression mode");
        expectThrows(IllegalArgumentException.class, () -> p.setCompressionMode(-1), "compression mode -1");
        expectThrows(IllegalArgumentException.class, () -> p.setCompressionMode(4), "compression mode 4");
        expectThrows(IllegalStateException.class, () -> p.setCompressionType("fast"), "setCompressionType outside EXPLICIT");
        expectThrows(IllegalStateException.class, () -> p.getCompressionType(), "getCompressionType outside EXPLICIT");
        expectThrows(IllegalStateException.class, () -> p.unsetCompression(), "unsetCompression outside EXPLICIT");
        expectThrows(IllegalStateException.class, () -> p.getCompressionQuality(), "getCompressionQuality outside EXPLICIT");
        p.setCompressionMode(ImageWriteParam.MODE_DISABLED);
        check(p.getCompressionMode() == ImageWriteParam.MODE_DISABLED, "compression DISABLED");
        p.setCompressionMode(ImageWriteParam.MODE_DEFAULT);
        check(p.getCompressionMode() == ImageWriteParam.MODE_DEFAULT, "compression DEFAULT");
        p.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        check(p.getCompressionMode() == ImageWriteParam.MODE_EXPLICIT, "compression EXPLICIT");
        String[] types = p.getCompressionTypes();
        check(types != null && types.length == 2, "two compression types");
        check("fast".equals(types[0]) && "best".equals(types[1]), "compression types content");
        check(p.getCompressionType() == null, "compression type initially null");
        // EXPLICIT without a type: quality accessors throw IllegalStateException (probed)
        expectThrows(IllegalStateException.class, () -> p.setCompressionQuality(0.5f), "quality without type");
        expectThrows(IllegalStateException.class, () -> p.getCompressionQuality(), "getQuality without type");
        expectThrows(IllegalStateException.class, () -> p.getLocalizedCompressionTypeName(), "localized name without type");
        expectThrows(IllegalStateException.class, () -> p.isCompressionLossless(), "lossless without type");
        expectThrows(IllegalStateException.class, () -> p.getBitRate(0.5f), "bit rate without type");
        expectThrows(IllegalStateException.class, () -> p.getCompressionQualityDescriptions(), "descriptions without type");
        expectThrows(IllegalArgumentException.class, () -> p.setCompressionType("bogus"), "unknown compression type");
        p.setCompressionType("fast");
        check("fast".equals(p.getCompressionType()), "compression type set");
        check("fast".equals(p.getLocalizedCompressionTypeName()), "localized name echoes type");
        check(p.isCompressionLossless(), "default lossless");
        check(p.getBitRate(0.5f) == -1.0f, "default bit rate -1");
        p.setCompressionQuality(0.0f);
        check(p.getCompressionQuality() == 0.0f, "quality 0.0 boundary");
        p.setCompressionQuality(1.0f);
        check(p.getCompressionQuality() == 1.0f, "quality 1.0 boundary");
        expectThrows(IllegalArgumentException.class, () -> p.setCompressionQuality(1.5f), "quality > 1");
        expectThrows(IllegalArgumentException.class, () -> p.setCompressionQuality(-0.1f), "quality < 0");
        expectThrows(IllegalArgumentException.class, () -> p.getBitRate(1.5f), "bit rate quality > 1");
        expectThrows(IllegalArgumentException.class, () -> p.getBitRate(-0.1f), "bit rate quality < 0");
        check(p.getCompressionQuality() == 1.0f, "failed quality update leaves state intact");
        check(p.getCompressionQualityDescriptions() == null, "descriptions null by default");
        check(p.getCompressionQualityValues() == null, "values null by default");
        p.setCompressionType("best");
        check("best".equals(p.getCompressionType()), "type switch");
        // setCompressionType(null) in EXPLICIT mode silently clears the type
        p.setCompressionType(null);
        check(p.getCompressionType() == null, "setCompressionType(null) clears");
        p.setCompressionType("fast");
        // unsetCompression keeps the mode EXPLICIT, clears type and resets quality
        p.setCompressionQuality(0.25f);
        p.unsetCompression();
        check(p.getCompressionType() == null, "unsetCompression clears type");
        check(p.getCompressionMode() == ImageWriteParam.MODE_EXPLICIT, "unsetCompression keeps EXPLICIT mode");
        expectThrows(IllegalStateException.class, () -> p.getCompressionQuality(), "quality after unset");
        p.setCompressionType("best");
        p.setCompressionQuality(0.25f);
        check(p.getCompressionQuality() == 0.25f, "re-set type then quality works");
        p.setCompressionMode(ImageWriteParam.MODE_COPY_FROM_METADATA);
        expectThrows(IllegalStateException.class, () -> p.getCompressionType(), "getCompressionType after mode change");
        p.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        check(p.getCompressionType() == null, "leaving and re-entering EXPLICIT clears type");
    }

    // compressible subclass without a type list: type-less operation is legal
    static void testCompressionWithoutTypeList() {
        FreeTypeParam p = new FreeTypeParam();
        check(p.canWriteCompressed(), "free param compressible");
        check(p.getCompressionTypes() == null, "no compression type list");
        p.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        // with a null type list, quality setters work without any type
        p.setCompressionQuality(0.5f);
        check(p.getCompressionQuality() == 0.5f, "quality without type list");
        check(p.getCompressionType() == null, "type stays null");
        expectThrows(UnsupportedOperationException.class, () -> p.setCompressionType("anything"),
                     "setCompressionType without type list");
        expectThrows(IllegalStateException.class, () -> p.getLocalizedCompressionTypeName(),
                     "localized name without type even with null list");
        check(p.getCompressionQualityDescriptions() == null, "descriptions null with null type list");
        check(p.getBitRate(0.5f) == -1.0f, "bit rate -1 with null type list");
        p.unsetCompression();
        check(p.getCompressionType() == null, "unsetCompression fine");
    }
}
