/*
 * @test
 * @summary javax.imageio.plugins.jpeg.JPEGImageWriteParam conformance: white-box structure + black-box behavior.
 * @run main JPEGImageWriteParamTest
 */

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Locale;
import java.util.HashSet;
import java.util.Set;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.ImageReader;
import javax.imageio.ImageReadParam;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import javax.imageio.plugins.jpeg.JPEGHuffmanTable;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.plugins.jpeg.JPEGQTable;

public class JPEGImageWriteParamTest {
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
        Class<?> c = Class.forName("javax.imageio.plugins.jpeg.JPEGImageWriteParam");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(Modifier.isAbstract(mods) == false, "not abstract");
        check(c.getSuperclass().getName().equals("javax.imageio.ImageWriteParam"), "extends ImageWriteParam");
        check(c.getConstructors().length == 1, "one public constructor");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        JPEGImageWriteParam p = new JPEGImageWriteParam(Locale.US);

        // compression defaults
        check(p.getCompressionMode() == ImageWriteParam.MODE_COPY_FROM_METADATA,
              "default compression MODE_COPY_FROM_METADATA");
        check(p.canWriteCompressed(), "canWriteCompressed true");
        check(p.canWriteProgressive(), "canWriteProgressive true");

        p.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        check("JPEG".equals(p.getCompressionType()), "default compression type JPEG");
        check(p.isCompressionLossless() == false, "jpeg compression lossy");
        check(Math.abs(p.getCompressionQuality() - 0.75f) < 1e-6f, "default quality 0.75");

        // compression quality round-trip
        p.setCompressionQuality(0.5f);
        check(Math.abs(p.getCompressionQuality() - 0.5f) < 1e-6f, "set/get compression quality");
        p.setCompressionQuality(1.0f);
        check(p.getCompressionQuality() == 1.0f, "quality 1.0 exact");
        p.setCompressionQuality(0.75f);

        // quality descriptions and values arrays
        String[] descs = p.getCompressionQualityDescriptions();
        float[] vals = p.getCompressionQualityValues();
        check(descs.length == 3, "quality descriptions length 3");
        check(vals.length == 4, "quality values length 4");
        check(vals[0] < vals[1] && vals[1] < vals[2] && vals[2] < vals[3], "quality values increasing");

        // unsetCompression resets quality to default
        p.setCompressionQuality(0.25f);
        p.unsetCompression();
        check(Math.abs(p.getCompressionQuality() - 0.75f) < 1e-6f, "unsetCompression resets to 0.75");

        // optimize Huffman tables
        check(!p.getOptimizeHuffmanTables(), "default optimizeHuffman false");
        p.setOptimizeHuffmanTables(true);
        check(p.getOptimizeHuffmanTables(), "setOptimizeHuffmanTables true round-trip");
        p.setOptimizeHuffmanTables(false);
        check(!p.getOptimizeHuffmanTables(), "setOptimizeHuffmanTables false round-trip");

        // encode tables
        check(!p.areTablesSet(), "default areTablesSet false");
        check(p.getQTables() == null, "default qTables null");
        JPEGQTable[] qs = { JPEGQTable.K1Luminance, JPEGQTable.K2Chrominance };
        JPEGHuffmanTable[] dcs = { JPEGHuffmanTable.StdDCLuminance, JPEGHuffmanTable.StdDCChrominance };
        JPEGHuffmanTable[] acs = { JPEGHuffmanTable.StdACLuminance, JPEGHuffmanTable.StdACChrominance };
        p.setEncodeTables(qs, dcs, acs);
        check(p.areTablesSet(), "areTablesSet after setEncodeTables");
        check(p.getQTables().length == 2 && p.getQTables()[0] == JPEGQTable.K1Luminance, "qTables round-trip");
        check(p.getDCHuffmanTables().length == 2, "DC tables round-trip");
        check(p.getACHuffmanTables().length == 2, "AC tables round-trip");
        p.unsetEncodeTables();
        check(!p.areTablesSet(), "areTablesSet false after unsetEncodeTables");
        check(p.getQTables() == null, "qTables null after unset");

        expectThrows(IllegalArgumentException.class,
            () -> p.setEncodeTables(null, dcs, acs), "null qTables");
        expectThrows(IllegalArgumentException.class,
            () -> p.setEncodeTables(qs, dcs, new JPEGHuffmanTable[]{ JPEGHuffmanTable.StdACLuminance }),
            "DC/AC length mismatch");

        // real JPEG writer with default write param
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
        check(writer != null, "jpeg writer present");
        check(writer.getDefaultWriteParam() instanceof JPEGImageWriteParam,
              "writer default param is JPEGImageWriteParam");
        JPEGImageWriteParam wp = (JPEGImageWriteParam) writer.getDefaultWriteParam();
        wp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        wp.setCompressionQuality(0.9f);

        BufferedImage img = new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < 8; y++) for (int x = 0; x < 8; x++) img.setRGB(x, y, (x * 32 << 16) | (y * 32 << 8) | 0x11);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
        writer.setOutput(ios);
        writer.write(null, new IIOImage(img, null, null), wp);
        ios.close();
        writer.dispose();
        check(baos.toByteArray().length > 0, "jpeg bytes written");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testApiSurface();
        testProgressiveModeMatrix();
        testCompressionMatrix();
        testQualityScales();
        testEncodeTablesMatrix();
        testOptimizeHuffmanTables();
        testEndToEndProgressiveWrite();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ================= appended: contract deepening (evtplug) =================

    static void testApiSurface() throws Exception {
        Class<?> c = Class.forName("javax.imageio.plugins.jpeg.JPEGImageWriteParam");
        check(c.getSuperclass().getName().equals("javax.imageio.ImageWriteParam"), "extends ImageWriteParam");
        Constructor<?>[] ctors = c.getConstructors();
        check(ctors.length == 1 && ctors[0].getParameterTypes().length == 1
              && ctors[0].getParameterTypes()[0] == Locale.class,
              "single public constructor (Locale)");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) names.add(m.getName());
        Set<String> want = new HashSet<String>(Arrays.asList(
            "unsetCompression", "isCompressionLossless",
            "getCompressionQualityDescriptions", "getCompressionQualityValues",
            "areTablesSet", "setEncodeTables", "unsetEncodeTables",
            "getQTables", "getDCHuffmanTables", "getACHuffmanTables",
            "setOptimizeHuffmanTables", "getOptimizeHuffmanTables"));
        check(names.equals(want), "declared method set exact: " + names);
        for (java.lang.reflect.Field f : c.getDeclaredFields()) {
            check(!Modifier.isPublic(f.getModifiers()), "no new public fields: " + f.getName());
        }
        JPEGImageWriteParam nullLocale = new JPEGImageWriteParam(null);
        check(nullLocale != null && nullLocale.getLocale() == null, "null locale accepted, stays null");
    }

    /** JPEG supports progressive output, but never MODE_EXPLICIT (IllegalArgumentException). */
    static void testProgressiveModeMatrix() {
        JPEGImageWriteParam p = new JPEGImageWriteParam(Locale.US);
        check(p.canWriteProgressive(), "canWriteProgressive true");
        check(p.getProgressiveMode() == ImageWriteParam.MODE_DISABLED, "initial progressive mode DISABLED");
        p.setProgressiveMode(ImageWriteParam.MODE_DEFAULT);
        check(p.getProgressiveMode() == ImageWriteParam.MODE_DEFAULT, "MODE_DEFAULT round-trip");
        // NB: MODE_EXPLICIT is rejected with IllegalArgumentException, not UnsupportedOperationException
        expectThrows(IllegalArgumentException.class,
            () -> p.setProgressiveMode(ImageWriteParam.MODE_EXPLICIT), "setProgressiveMode(MODE_EXPLICIT)");
        p.setProgressiveMode(ImageWriteParam.MODE_DISABLED);
        check(p.getProgressiveMode() == ImageWriteParam.MODE_DISABLED, "MODE_DISABLED round-trip");
        p.setProgressiveMode(ImageWriteParam.MODE_COPY_FROM_METADATA);
        check(p.getProgressiveMode() == ImageWriteParam.MODE_COPY_FROM_METADATA,
              "MODE_COPY_FROM_METADATA round-trip");
        expectThrows(IllegalArgumentException.class, () -> p.setProgressiveMode(9), "mode 9 out of range");
        expectThrows(IllegalArgumentException.class, () -> p.setProgressiveMode(-1), "mode -1 out of range");
        // progressive state is independent of compression state
        p.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        check(p.getProgressiveMode() == ImageWriteParam.MODE_COPY_FROM_METADATA,
              "compression mode change leaves progressive mode intact");
    }

    static void testCompressionMatrix() {
        JPEGImageWriteParam p = new JPEGImageWriteParam(Locale.US);
        check(p.canWriteCompressed(), "canWriteCompressed true");
        check(p.getCompressionMode() == ImageWriteParam.MODE_COPY_FROM_METADATA,
              "initial compression mode COPY_FROM_METADATA");
        expectThrows(IllegalStateException.class, () -> p.getCompressionType(),
              "getCompressionType outside MODE_EXPLICIT");
        expectThrows(IllegalStateException.class, () -> p.setCompressionQuality(0.5f),
              "setCompressionQuality outside MODE_EXPLICIT");
        expectThrows(IllegalStateException.class, () -> p.unsetCompression(),
              "unsetCompression outside MODE_EXPLICIT");
        // entering MODE_EXPLICIT restores the JPEG defaults (type JPEG, quality 0.75)
        p.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        check("JPEG".equals(p.getCompressionType()), "explicit mode default type JPEG");
        check(Math.abs(p.getCompressionQuality() - 0.75f) < 1e-6f, "explicit mode default quality 0.75");
        check(!p.isCompressionLossless(), "JPEG compression is lossy");
        // only type "JPEG" is legal
        expectThrows(IllegalArgumentException.class, () -> p.setCompressionType("PNG"),
              "setCompressionType(PNG)");
        expectThrows(IllegalArgumentException.class, () -> p.setCompressionType("jpeg"),
              "type names are case sensitive");
        p.setCompressionType("JPEG");
        check("JPEG".equals(p.getCompressionType()), "type JPEG round-trip");
        p.setCompressionType(null);
        check(p.getCompressionType() == null, "setCompressionType(null) clears");
        expectThrows(IllegalStateException.class, () -> p.setCompressionQuality(0.5f),
              "setCompressionQuality with no type");
        p.setCompressionType("JPEG");
        // quality bounds: exact class only; message text differs between JDK-8 and JDK-27
        expectThrows(IllegalArgumentException.class, () -> p.setCompressionQuality(-0.1f), "quality -0.1f");
        expectThrows(IllegalArgumentException.class, () -> p.setCompressionQuality(1.1f), "quality 1.1f");
        p.setCompressionQuality(0.0f);
        check(p.getCompressionQuality() == 0.0f, "quality 0.0 boundary accepted");
        p.setCompressionQuality(1.0f);
        check(p.getCompressionQuality() == 1.0f, "quality 1.0 boundary accepted");
        // unsetCompression restores the JPEG defaults (type "JPEG", quality 0.75) — not null
        p.setCompressionQuality(0.25f);
        p.unsetCompression();
        check("JPEG".equals(p.getCompressionType()), "unsetCompression resets type to JPEG");
        check(Math.abs(p.getCompressionQuality() - 0.75f) < 1e-6f, "unsetCompression resets quality 0.75");
        // leaving and re-entering EXPLICIT restores defaults again
        p.setCompressionQuality(0.5f);
        p.setCompressionMode(ImageWriteParam.MODE_COPY_FROM_METADATA);
        p.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        check("JPEG".equals(p.getCompressionType()), "re-entering EXPLICIT resets type to JPEG");
        check(Math.abs(p.getCompressionQuality() - 0.75f) < 1e-6f, "re-entering EXPLICIT resets quality");
        expectThrows(IllegalArgumentException.class, () -> p.setCompressionMode(9), "mode 9 rejected");
    }

    static void testQualityScales() {
        JPEGImageWriteParam p = new JPEGImageWriteParam(Locale.US);
        p.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        // localized (Locale.US) scale: 3 descriptions over 4 quality anchors
        String[] descs = p.getCompressionQualityDescriptions();
        float[] vals = p.getCompressionQualityValues();
        check(descs != null && descs.length == 3, "quality descriptions length 3");
        check(vals != null && vals.length == 4, "quality values length 4");
        boolean increasing = true;
        for (int i = 1; i < vals.length; i++) if (vals[i] <= vals[i - 1]) increasing = false;
        check(increasing, "quality values strictly increasing");
        check(vals[0] == 0.0f && vals[3] == 1.0f, "quality scale spans [0.0, 1.0]");
        for (String d : descs) check(d != null && !d.isEmpty(), "description non-empty");
        // scale queries are independent of the actual chosen quality
        p.setCompressionQuality(0.5f);
        check(Arrays.equals(vals, p.getCompressionQualityValues()), "values unchanged by setCompressionQuality");
        // outside MODE_EXPLICIT the query throws
        expectThrows(IllegalStateException.class,
            () -> new JPEGImageWriteParam(Locale.US).getCompressionQualityDescriptions(),
            "descriptions outside MODE_EXPLICIT");
        expectThrows(IllegalStateException.class,
            () -> new JPEGImageWriteParam(Locale.US).getCompressionQualityValues(),
            "values outside MODE_EXPLICIT");
    }

    static final JPEGQTable[] Q2 = {JPEGQTable.K1Luminance, JPEGQTable.K2Chrominance};
    static final JPEGHuffmanTable[] DC2 = {JPEGHuffmanTable.StdDCLuminance, JPEGHuffmanTable.StdDCChrominance};
    static final JPEGHuffmanTable[] AC2 = {JPEGHuffmanTable.StdACLuminance, JPEGHuffmanTable.StdACChrominance};

    static void testEncodeTablesMatrix() {
        JPEGImageWriteParam p = new JPEGImageWriteParam(Locale.US);
        check(!p.areTablesSet(), "fresh param tables unset");
        check(p.getQTables() == null && p.getDCHuffmanTables() == null && p.getACHuffmanTables() == null,
              "fresh param getters null");
        expectThrows(IllegalArgumentException.class, () -> p.setEncodeTables(null, DC2, AC2), "null qTables");
        expectThrows(IllegalArgumentException.class, () -> p.setEncodeTables(Q2, null, AC2), "null DC");
        expectThrows(IllegalArgumentException.class, () -> p.setEncodeTables(Q2, DC2, null), "null AC");
        expectThrows(IllegalArgumentException.class,
            () -> p.setEncodeTables(new JPEGQTable[]{Q2[0], Q2[1], Q2[0], Q2[1], Q2[0]}, DC2, AC2),
            "five qTables");
        expectThrows(IllegalArgumentException.class,
            () -> p.setEncodeTables(Q2, DC2, new JPEGHuffmanTable[]{AC2[0]}), "DC/AC mismatch");
        check(!p.areTablesSet(), "rejected sets leave state untouched");
        p.setEncodeTables(Q2, DC2, AC2);
        check(p.areTablesSet(), "valid set flips areTablesSet");
        check(p.getQTables().length == 2 && p.getQTables()[0] == JPEGQTable.K1Luminance,
              "qTables round-trip");
        check(p.getDCHuffmanTables().length == 2
              && p.getDCHuffmanTables()[0] == JPEGHuffmanTable.StdDCLuminance, "DC round-trip");
        check(p.getACHuffmanTables().length == 2
              && p.getACHuffmanTables()[1] == JPEGHuffmanTable.StdACChrominance, "AC round-trip");
        // defensive copies both ways
        JPEGQTable[] qIn = {JPEGQTable.K1Luminance};
        p.setEncodeTables(qIn, new JPEGHuffmanTable[]{DC2[0]}, new JPEGHuffmanTable[]{AC2[0]});
        qIn[0] = JPEGQTable.K2Chrominance;
        check(p.getQTables()[0] == JPEGQTable.K1Luminance, "setEncodeTables clones input");
        p.getQTables()[0] = null;
        check(p.getQTables()[0] == JPEGQTable.K1Luminance, "getQTables defensive copy");
        p.unsetEncodeTables();
        check(!p.areTablesSet(), "unsetEncodeTables clears flag");
        check(p.getQTables() == null && p.getDCHuffmanTables() == null && p.getACHuffmanTables() == null,
              "all getters null after unset");
        p.unsetEncodeTables(); // idempotent
        check(!p.areTablesSet(), "unsetEncodeTables idempotent");
        // zero-length arrays legal
        p.setEncodeTables(new JPEGQTable[0], new JPEGHuffmanTable[0], new JPEGHuffmanTable[0]);
        check(p.areTablesSet() && p.getQTables().length == 0, "zero-length tables accepted");
    }

    static void testOptimizeHuffmanTables() {
        JPEGImageWriteParam p = new JPEGImageWriteParam(Locale.US);
        check(!p.getOptimizeHuffmanTables(), "default optimize false");
        p.setOptimizeHuffmanTables(true);
        check(p.getOptimizeHuffmanTables(), "optimize true round-trip");
        p.setOptimizeHuffmanTables(true);
        check(p.getOptimizeHuffmanTables(), "idempotent true");
        p.setOptimizeHuffmanTables(false);
        check(!p.getOptimizeHuffmanTables(), "optimize false round-trip");
        // independent of compression mode changes
        p.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        check(!p.getOptimizeHuffmanTables(), "compression mode change leaves optimize flag");
    }

    static void testEndToEndProgressiveWrite() throws Exception {
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
        check(writer.getDefaultWriteParam() instanceof JPEGImageWriteParam,
              "jpeg writer default param is JPEGImageWriteParam");
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < 16; y++)
            for (int x = 0; x < 16; x++)
                img.setRGB(x, y, (((x * 31) & 0xff) << 16) | (((y * 41) & 0xff) << 8) | 0x33);
        JPEGImageWriteParam p = new JPEGImageWriteParam(Locale.US);
        p.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        p.setCompressionQuality(0.9f);
        p.setProgressiveMode(ImageWriteParam.MODE_DEFAULT);   // progressive scan script
        p.setOptimizeHuffmanTables(true);
        check(bos2Bytes(writer, img, p).length > 0, "progressive optimized jpeg written");
        // baseline for contrast
        JPEGImageWriteParam base = new JPEGImageWriteParam(Locale.US);
        base.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        byte[] baseline = bos2BytesPlain(img);
        check(baseline.length > 0, "baseline jpeg written");
        // progressive output decodes to the same dimensions (lossy: no pixel equality claimed)
        ImageReader reader = ImageIO.getImageReadersByFormatName("jpeg").next();
        ImageInputStream iis = new MemoryCacheImageInputStream(
            new ByteArrayInputStream(bos2Bytes(writer, img, p)));
        reader.setInput(iis);
        BufferedImage back = reader.read(0, (ImageReadParam) null);
        check(back != null && back.getWidth() == 16 && back.getHeight() == 16,
              "progressive jpeg dims round-trip");
        iis.close();
        reader.dispose();
        writer.dispose();
    }

    static byte[] bos2Bytes(ImageWriter w, BufferedImage img, JPEGImageWriteParam p) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageOutputStream ios = new MemoryCacheImageOutputStream(bos);
        w.setOutput(ios);
        w.write(null, new IIOImage(img, null, null), p);
        ios.close();
        return bos.toByteArray();
    }

    static byte[] bos2BytesPlain(BufferedImage img) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(img, "jpeg", bos);
        return bos.toByteArray();
    }
}
