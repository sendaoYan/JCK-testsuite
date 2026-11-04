/*
 * @test
 * @summary javax.imageio.plugins.bmp.BMPImageWriteParam conformance: behavioral.
 * @run main BMPImageWriteParamTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.awt.image.BufferedImage;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import javax.imageio.plugins.bmp.BMPImageWriteParam;

public class BMPImageWriteParamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        javax.imageio.plugins.bmp.BMPImageWriteParam p = new javax.imageio.plugins.bmp.BMPImageWriteParam();
        check(!p.isTopDown(), "isTopDown default false");
        p.setTopDown(true);
        check(p.isTopDown(), "setTopDown true");
        testConstructors();
        testTopDownRoundTrip();
        testCompressionTypeUniverse();
        testCompressionModeMatrix();
        testProgressiveUnsupported();
        testEndToEndWrite();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ================= appended: contract deepening (evtplug) =================

    interface ThrowingRunnable { void run() throws Exception; }
    static void expectThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        boolean thrown = false;
        try { r.run(); } catch (Throwable t) {
            thrown = true;
            check(t.getClass() == expected,
                  msg + ": expected " + expected.getSimpleName() + " but got "
                  + t.getClass().getSimpleName() + " (" + t.getMessage() + ")");
        }
        check(thrown, msg + ": should throw " + expected.getSimpleName());
    }
    static void expectNoThrow(ThrowingRunnable r, String msg) throws Exception {
        r.run(); check(true, msg);
    }

    static void testConstructors() throws Exception {
        BMPImageWriteParam noArg = new BMPImageWriteParam();
        check(noArg != null, "no-arg constructor");
        check(noArg.getLocale() == null, "no-arg locale null");
        BMPImageWriteParam us = new BMPImageWriteParam(Locale.US);
        check(us.getLocale() == Locale.US, "Locale.US round-trip");
        BMPImageWriteParam nullLocale = new BMPImageWriteParam(null);
        check(nullLocale != null, "null locale accepted");
        check(nullLocale.getLocale() == null, "null locale stays null");
        check(noArg.isTopDown() == false && us.isTopDown() == false && nullLocale.isTopDown() == false,
              "topDown defaults false in every constructor");
        // structural: exactly two public constructors
        Constructor<?>[] ctors = BMPImageWriteParam.class.getConstructors();
        check(ctors.length == 2, "two public constructors");
        boolean hasNoArg = false, hasLocale = false;
        for (Constructor<?> c : ctors) {
            Class<?>[] ps = c.getParameterTypes();
            if (ps.length == 0) hasNoArg = true;
            if (ps.length == 1 && ps[0] == Locale.class) hasLocale = true;
        }
        check(hasNoArg && hasLocale, "constructor overloads () and (Locale)");
    }

    static void testTopDownRoundTrip() {
        BMPImageWriteParam p = new BMPImageWriteParam();
        check(!p.isTopDown(), "default topDown false");
        p.setTopDown(true);
        check(p.isTopDown(), "setTopDown(true)");
        p.setTopDown(true);
        check(p.isTopDown(), "idempotent setTopDown(true)");
        p.setTopDown(false);
        check(!p.isTopDown(), "setTopDown(false)");
        p.setTopDown(false);
        check(!p.isTopDown(), "idempotent setTopDown(false)");
        // topDown is independent of the compression state machine
        p.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        p.setCompressionType("BI_RGB");
        check(p.isTopDown() == false, "compression mode change leaves topDown untouched");
        p.setTopDown(true);
        check("BI_RGB".equals(p.getCompressionType()), "topDown change leaves compression type untouched");
    }

    static void testCompressionTypeUniverse() {
        BMPImageWriteParam p = new BMPImageWriteParam();
        check(p.canWriteCompressed(), "canWriteCompressed true");
        check(!p.canWriteProgressive(), "canWriteProgressive false");
        check(!p.canWriteTiles(), "canWriteTiles false");
        check(!p.canOffsetTiles(), "canOffsetTiles false");
        String[] types = p.getCompressionTypes();
        // exact universe, verified identical on JDK-8 and JDK-27
        List<String> expected = Arrays.asList("BI_RGB", "BI_RLE8", "BI_RLE4", "BI_BITFIELDS", "BI_JPEG", "BI_PNG");
        check(types != null && types.length == 6, "six BMP compression types");
        check(Arrays.asList(types).equals(expected), "compression types exact set+order: " + Arrays.toString(types));
        // fresh clone per call; mutating the result does not poison later calls
        String[] t1 = p.getCompressionTypes();
        String[] t2 = p.getCompressionTypes();
        check(t1 != t2, "getCompressionTypes returns a fresh array each call");
        t1[0] = "MUTATED";
        check("BI_RGB".equals(p.getCompressionTypes()[0]), "mutating the returned array has no effect");
        // localized quality descriptions are not provided by BMP
        p.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        p.setCompressionType("BI_RGB");
        check(p.getCompressionQualityDescriptions() == null, "no localized quality descriptions");
        check(p.getCompressionQualityValues() == null, "no localized quality values");
        check(p.getBitRate(0.5f) == -1.0f, "bit rate unknown (-1.0)");
        check(p.getCompressionQuality() == 1.0f, "default quality 1.0 once a type is set");
    }

    /** Inherited ImageWriteParam MODE state machine exercised end-to-end on the BMP subclass. */
    static void testCompressionModeMatrix() throws Exception {
        BMPImageWriteParam p = new BMPImageWriteParam();
        check(p.getCompressionMode() == ImageWriteParam.MODE_COPY_FROM_METADATA,
              "initial mode MODE_COPY_FROM_METADATA");
        // setCompressionType/getCompressionQuality outside MODE_EXPLICIT -> IllegalStateException
        expectThrows(IllegalStateException.class, () -> p.setCompressionType("BI_RGB"),
              "setCompressionType before MODE_EXPLICIT");
        expectThrows(IllegalStateException.class, () -> p.getCompressionType(),
              "getCompressionType before MODE_EXPLICIT");
        expectThrows(IllegalStateException.class, () -> p.getCompressionQuality(),
              "getCompressionQuality before MODE_EXPLICIT");
        expectThrows(IllegalStateException.class, () -> p.setCompressionQuality(0.5f),
              "setCompressionQuality before MODE_EXPLICIT");
        // entering MODE_EXPLICIT resets the compression type
        p.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        check(p.getCompressionType() == null, "MODE_EXPLICIT clears compression type");
        // unknown type string -> IllegalArgumentException
        expectThrows(IllegalArgumentException.class, () -> p.setCompressionType("bogus"),
              "setCompressionType(bogus)");
        expectThrows(IllegalArgumentException.class, () -> p.setCompressionType("bi_rgb"),
              "setCompressionType is case sensitive");
        // all six types accepted
        String[] all = {"BI_RGB", "BI_RLE8", "BI_RLE4", "BI_BITFIELDS", "BI_JPEG", "BI_PNG"};
        for (String t : all) {
            p.setCompressionType(t);
            check(t.equals(p.getCompressionType()), "round-trip " + t);
        }
        // null clears
        p.setCompressionType(null);
        check(p.getCompressionType() == null, "setCompressionType(null) clears");
        expectThrows(IllegalStateException.class, () -> p.setCompressionQuality(0.5f),
              "setCompressionQuality with no type set");
        // quality bounds
        p.setCompressionType("BI_RGB");
        expectThrows(IllegalArgumentException.class, () -> p.setCompressionQuality(-0.1f),
              "setCompressionQuality(-0.1f)");
        expectThrows(IllegalArgumentException.class, () -> p.setCompressionQuality(1.5f),
              "setCompressionQuality(1.5f)");
        // NaN passes the "< 0 || > 1" bounds check unchanged (observed on both JDKs)
        expectNoThrow(() -> p.setCompressionQuality(Float.NaN), "setCompressionQuality(NaN) accepted");
        check(Float.isNaN(p.getCompressionQuality()), "NaN quality stored verbatim");
        p.setCompressionQuality(0.0f);
        check(p.getCompressionQuality() == 0.0f, "quality 0.0 accepted");
        p.setCompressionQuality(1.0f);
        check(p.getCompressionQuality() == 1.0f, "quality 1.0 accepted");
        p.setCompressionQuality(0.25f);
        check(p.getCompressionQuality() == 0.25f, "quality 0.25 round-trip");
        // unsetCompression requires MODE_EXPLICIT and resets type/quality
        p.unsetCompression();
        check(p.getCompressionType() == null, "unsetCompression clears type (null in MODE_EXPLICIT)");
        expectThrows(IllegalStateException.class, () -> p.getCompressionQuality(),
              "getCompressionQuality after unsetCompression (no type -> ISE)");
        p.setCompressionType("BI_RGB");
        check(p.getCompressionQuality() == 1.0f, "unsetCompression resets quality to 1.0");
        expectThrows(IllegalStateException.class, () -> new BMPImageWriteParam().unsetCompression(),
              "unsetCompression outside MODE_EXPLICIT");
        // mode validation
        expectThrows(IllegalArgumentException.class, () -> p.setCompressionMode(9),
              "setCompressionMode(9)");
        expectThrows(IllegalArgumentException.class, () -> p.setCompressionMode(-1),
              "setCompressionMode(-1)");
        p.setCompressionMode(ImageWriteParam.MODE_DISABLED);
        check(p.getCompressionMode() == ImageWriteParam.MODE_DISABLED, "MODE_DISABLED round-trip");
        expectThrows(IllegalStateException.class, () -> p.getCompressionType(),
              "getCompressionType in MODE_DISABLED");
        p.setCompressionMode(ImageWriteParam.MODE_COPY_FROM_METADATA);
        check(p.getCompressionMode() == ImageWriteParam.MODE_COPY_FROM_METADATA,
              "MODE_COPY_FROM_METADATA round-trip");
        // failed calls leave the rest of the state untouched
        BMPImageWriteParam q = new BMPImageWriteParam();
        q.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        q.setCompressionType("BI_BITFIELDS");
        q.setCompressionQuality(0.5f);
        boolean threw = false;
        try { q.setCompressionType("nope"); } catch (IllegalArgumentException e) { threw = true; }
        check(threw, "invalid type rejected");
        check("BI_BITFIELDS".equals(q.getCompressionType()), "rejected setCompressionType leaves type intact");
        check(q.getCompressionQuality() == 0.5f, "rejected setCompressionType leaves quality intact");
    }

    static void testProgressiveUnsupported() {
        BMPImageWriteParam p = new BMPImageWriteParam();
        expectThrows(UnsupportedOperationException.class, () -> p.setProgressiveMode(ImageWriteParam.MODE_DEFAULT),
              "setProgressiveMode(MODE_DEFAULT) unsupported");
        expectThrows(UnsupportedOperationException.class, () -> p.setProgressiveMode(ImageWriteParam.MODE_DISABLED),
              "setProgressiveMode(MODE_DISABLED) unsupported");
        expectThrows(UnsupportedOperationException.class, () -> p.getProgressiveMode(),
              "getProgressiveMode unsupported");
    }

    static void testEndToEndWrite() throws Exception {
        ImageWriter writer = ImageIO.getImageWritersByFormatName("bmp").next();
        check(writer.getDefaultWriteParam() instanceof BMPImageWriteParam,
              "BMP writer default param is BMPImageWriteParam");
        // top-down write round-trips pixels
        BMPImageWriteParam td = new BMPImageWriteParam();
        td.setTopDown(true);
        BufferedImage img = new BufferedImage(8, 6, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < 6; y++)
            for (int x = 0; x < 8; x++)
                img.setRGB(x, y, (((x * 31) & 0xff) << 16) | (((y * 41) & 0xff) << 8) | 0x3f);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageOutputStream ios = new MemoryCacheImageOutputStream(bos);
        writer.setOutput(ios);
        writer.write(null, new IIOImage(img, null, null), td);
        ios.close();
        BufferedImage back = ImageIO.read(new ByteArrayInputStream(bos.toByteArray()));
        check(back != null && back.getWidth() == 8 && back.getHeight() == 6, "top-down BMP dims round-trip");
        check(back.getRGB(3, 2) == img.getRGB(3, 2), "top-down BMP pixel round-trip");
        // BI_RLE8 on a 24-bit image: writer rejects with IOException (message text differs across JDKs)
        writer = ImageIO.getImageWritersByFormatName("bmp").next();
        BMPImageWriteParam rle = new BMPImageWriteParam();
        rle.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        rle.setCompressionType("BI_RLE8");
        ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
        ImageOutputStream ios2 = new MemoryCacheImageOutputStream(bos2);
        writer.setOutput(ios2);
        try {
            writer.write(null, new IIOImage(img, null, null), rle);
            check(false, "BI_RLE8 on 24-bit image must be rejected");
        } catch (IOException e) {
            check(true, "BI_RLE8 on 24-bit image rejected with IOException");
        }
        ios2.close();
        writer.dispose();
        // BI_RLE8 on an indexed 8-bit image: supported, round-trips dimensions
        writer = ImageIO.getImageWritersByFormatName("bmp").next();
        BufferedImage pal = new BufferedImage(5, 4, BufferedImage.TYPE_BYTE_INDEXED);
        for (int y = 0; y < 4; y++)
            for (int x = 0; x < 5; x++)
                pal.setRGB(x, y, (((x * 40) & 0xff) << 16) | (((y * 50) & 0xff) << 8) | 0x30);
        BMPImageWriteParam rleOk = new BMPImageWriteParam();
        rleOk.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        rleOk.setCompressionType("BI_RLE8");
        ByteArrayOutputStream bos3 = new ByteArrayOutputStream();
        ImageOutputStream ios3 = new MemoryCacheImageOutputStream(bos3);
        writer.setOutput(ios3);
        writer.write(null, new IIOImage(pal, null, null), rleOk);
        ios3.close();
        BufferedImage palBack = ImageIO.read(new ByteArrayInputStream(bos3.toByteArray()));
        check(palBack != null && palBack.getWidth() == 5 && palBack.getHeight() == 4,
              "BI_RLE8 indexed BMP dims round-trip");
        check(palBack.getRGB(2, 1) == pal.getRGB(2, 1), "BI_RLE8 indexed BMP pixel round-trip");
        writer.dispose();
    }
}
