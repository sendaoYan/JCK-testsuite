/*
 * @test
 * @summary javax.imageio.ImageWriteParam conformance: white-box structure + black-box behavior.
 * @run main ImageWriteParamTest
 */

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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
