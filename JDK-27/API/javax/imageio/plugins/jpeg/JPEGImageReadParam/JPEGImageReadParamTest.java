/*
 * @test
 * @summary javax.imageio.plugins.jpeg.JPEGImageReadParam conformance: white-box structure + black-box behavior.
 * @run main JPEGImageReadParamTest
 */

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import javax.imageio.plugins.jpeg.JPEGHuffmanTable;
import javax.imageio.plugins.jpeg.JPEGImageReadParam;
import javax.imageio.plugins.jpeg.JPEGQTable;

public class JPEGImageReadParamTest {
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
        Class<?> c = Class.forName("javax.imageio.plugins.jpeg.JPEGImageReadParam");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(Modifier.isAbstract(mods) == false, "not abstract");
        check(c.getSuperclass().getName().equals("javax.imageio.ImageReadParam"), "extends ImageReadParam");
        check(c.getConstructors().length == 1, "one public constructor");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        JPEGImageReadParam p = new JPEGImageReadParam();
        check(!p.areTablesSet(), "default areTablesSet false");
        check(p.getQTables() == null, "default qTables null");
        check(p.getDCHuffmanTables() == null, "default DC null");
        check(p.getACHuffmanTables() == null, "default AC null");

        // set/get decode tables round-trip
        JPEGQTable[] qs = { JPEGQTable.K1Luminance, JPEGQTable.K2Chrominance };
        JPEGHuffmanTable[] dcs = { JPEGHuffmanTable.StdDCLuminance, JPEGHuffmanTable.StdDCChrominance };
        JPEGHuffmanTable[] acs = { JPEGHuffmanTable.StdACLuminance, JPEGHuffmanTable.StdACChrominance };
        p.setDecodeTables(qs, dcs, acs);
        check(p.areTablesSet(), "areTablesSet after setDecodeTables");
        JPEGQTable[] gotQ = p.getQTables();
        JPEGHuffmanTable[] gotDC = p.getDCHuffmanTables();
        JPEGHuffmanTable[] gotAC = p.getACHuffmanTables();
        check(gotQ.length == 2 && gotQ[0] == JPEGQTable.K1Luminance && gotQ[1] == JPEGQTable.K2Chrominance,
              "qTables round-trip");
        check(gotDC.length == 2 && gotDC[0] == JPEGHuffmanTable.StdDCLuminance, "DC tables round-trip");
        check(gotAC.length == 2 && gotAC[0] == JPEGHuffmanTable.StdACLuminance, "AC tables round-trip");

        p.unsetDecodeTables();
        check(!p.areTablesSet(), "areTablesSet false after unsetDecodeTables");
        check(p.getQTables() == null && p.getDCHuffmanTables() == null && p.getACHuffmanTables() == null,
              "tables null after unset");

        // invalid arguments
        expectThrows(IllegalArgumentException.class,
            () -> p.setDecodeTables(null, dcs, acs), "null qTables");
        expectThrows(IllegalArgumentException.class,
            () -> p.setDecodeTables(qs, dcs, new JPEGHuffmanTable[]{ JPEGHuffmanTable.StdACLuminance }),
            "DC/AC length mismatch");

        // inherited source subsampling combination
        p.setSourceSubsampling(2, 3, 1, 1);
        check(p.getSourceXSubsampling() == 2, "getSourceXSubsampling");
        check(p.getSourceYSubsampling() == 3, "getSourceYSubsampling");
        check(p.getSubsamplingXOffset() == 1, "getSubsamplingXOffset");
        check(p.getSubsamplingYOffset() == 1, "getSubsamplingYOffset");

        // real JPEG writer + reader with default read param
        BufferedImage img = new BufferedImage(16, 12, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < 12; y++) for (int x = 0; x < 16; x++) img.setRGB(x, y, (x * 13 << 16) | (y * 17 << 8) | 7);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
        check(writer != null, "jpeg writer present");
        ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
        writer.setOutput(ios);
        writer.write(null, new IIOImage(img, null, null), null);
        ios.close();
        writer.dispose();

        ImageReader reader = ImageIO.getImageReadersByFormatName("jpeg").next();
        check(reader != null, "jpeg reader present");
        check(reader.getDefaultReadParam() instanceof JPEGImageReadParam,
              "reader default read param is JPEGImageReadParam");
        ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(baos.toByteArray()));
        reader.setInput(iis);
        BufferedImage read = reader.read(0);
        check(read != null && read.getWidth() == 16 && read.getHeight() == 12,
              "jpeg read dimensions round-trip");
        reader.dispose();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testApiSurface();
        testTableStateDefaults();
        testSetDecodeTablesValidation();
        testTableStateInvariants();
        testInheritedParamContract();
        testEndToEndDecode();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ================= appended: contract deepening (evtplug) =================

    static final JPEGQTable[] Q4 = {
        JPEGQTable.K1Luminance, JPEGQTable.K1Div2Luminance,
        JPEGQTable.K2Chrominance, JPEGQTable.K2Div2Chrominance};
    static final JPEGHuffmanTable[] DC2 = {JPEGHuffmanTable.StdDCLuminance, JPEGHuffmanTable.StdDCChrominance};
    static final JPEGHuffmanTable[] AC2 = {JPEGHuffmanTable.StdACLuminance, JPEGHuffmanTable.StdACChrominance};

    static void testApiSurface() throws Exception {
        Class<?> c = Class.forName("javax.imageio.plugins.jpeg.JPEGImageReadParam");
        check(c.getSuperclass().getName().equals("javax.imageio.ImageReadParam"), "extends ImageReadParam");
        Constructor<?>[] ctors = c.getConstructors();
        check(ctors.length == 1 && ctors[0].getParameterTypes().length == 0,
              "single public no-arg constructor");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) names.add(m.getName());
        Set<String> want = new HashSet<String>(Arrays.asList(
            "areTablesSet", "setDecodeTables", "unsetDecodeTables",
            "getQTables", "getDCHuffmanTables", "getACHuffmanTables"));
        check(names.equals(want), "declared method set exact: " + names);
        for (java.lang.reflect.Field f : c.getDeclaredFields()) {
            check(Modifier.isPublic(f.getModifiers()) == false, "no new public fields: " + f.getName());
        }
        // no setDestinationTables-style extra API (verified by javap diff JDK-8 vs JDK-27)
        for (Method m : c.getMethods()) {
            check(!m.getName().equals("setDestinationTables"), "no setDestinationTables method");
        }
    }

    static void testTableStateDefaults() {
        JPEGImageReadParam p = new JPEGImageReadParam();
        check(!p.areTablesSet(), "fresh param: areTablesSet false");
        check(p.getQTables() == null, "fresh param: qTables null");
        check(p.getDCHuffmanTables() == null, "fresh param: DC tables null");
        check(p.getACHuffmanTables() == null, "fresh param: AC tables null");
        // unset on a fresh param is a safe no-op
        p.unsetDecodeTables();
        check(!p.areTablesSet(), "unsetDecodeTables on fresh param still false");
        p.unsetDecodeTables(); // idempotent
        check(!p.areTablesSet(), "unsetDecodeTables idempotent");
    }

    /** All invalid combinations throw IllegalArgumentException "Invalid JPEG table arrays". */
    static void testSetDecodeTablesValidation() {
        JPEGImageReadParam p = new JPEGImageReadParam();
        expectThrows(IllegalArgumentException.class, () -> p.setDecodeTables(null, DC2, AC2), "null qTables");
        expectThrows(IllegalArgumentException.class, () -> p.setDecodeTables(Q4, null, AC2), "null DC tables");
        expectThrows(IllegalArgumentException.class, () -> p.setDecodeTables(Q4, DC2, null), "null AC tables");
        expectThrows(IllegalArgumentException.class,
            () -> p.setDecodeTables(new JPEGQTable[]{Q4[0], Q4[1], Q4[2], Q4[3], JPEGQTable.K1Luminance}, DC2, AC2),
            "five qTables");
        expectThrows(IllegalArgumentException.class,
            () -> p.setDecodeTables(Q4,
                new JPEGHuffmanTable[]{DC2[0], DC2[1], DC2[0], DC2[1], DC2[0]},
                new JPEGHuffmanTable[]{AC2[0], AC2[1], AC2[0], AC2[1], AC2[0]}),
            "five Huffman tables");
        expectThrows(IllegalArgumentException.class,
            () -> p.setDecodeTables(Q4, DC2, new JPEGHuffmanTable[]{AC2[0]}),
            "DC/AC length mismatch");
        expectThrows(IllegalArgumentException.class,
            () -> p.setDecodeTables(Q4, DC2, new JPEGHuffmanTable[0]),
            "DC 2 vs AC 0 mismatch");
        check(!p.areTablesSet(), "all rejected calls leave areTablesSet false");
        check(p.getQTables() == null, "no partial state after rejections");
    }

    static void testTableStateInvariants() {
        JPEGImageReadParam p = new JPEGImageReadParam();
        // 4/2/2 is the natural full set
        p.setDecodeTables(Q4, DC2, AC2);
        check(p.areTablesSet(), "areTablesSet true after valid set");
        check(p.getQTables().length == 4, "qTables length 4");
        check(p.getDCHuffmanTables().length == 2, "DC length 2");
        check(p.getACHuffmanTables().length == 2, "AC length 2");
        check(p.getQTables()[0] == JPEGQTable.K1Luminance && p.getQTables()[3] == JPEGQTable.K2Div2Chrominance,
              "qTables content round-trip");
        check(p.getDCHuffmanTables()[1] == JPEGHuffmanTable.StdDCChrominance, "DC content round-trip");
        check(p.getACHuffmanTables()[1] == JPEGHuffmanTable.StdACChrominance, "AC content round-trip");
        // arrays are cloned on the way in: mutating the caller's array has no effect
        JPEGQTable[] qIn = {JPEGQTable.K1Luminance};
        JPEGHuffmanTable[] dcIn = {JPEGHuffmanTable.StdDCLuminance};
        JPEGHuffmanTable[] acIn = {JPEGHuffmanTable.StdACLuminance};
        p.setDecodeTables(qIn, dcIn, acIn);
        qIn[0] = JPEGQTable.K2Chrominance;
        check(p.getQTables()[0] == JPEGQTable.K1Luminance, "setDecodeTables clones the input arrays");
        // getters return fresh arrays: mutating a result does not affect the next call
        p.getQTables()[0] = null;
        check(p.getQTables()[0] == JPEGQTable.K1Luminance, "getQTables returns a defensive copy");
        p.getDCHuffmanTables()[0] = null;
        check(p.getDCHuffmanTables()[0] == JPEGHuffmanTable.StdDCLuminance, "getDCHuffmanTables defensive copy");
        p.getACHuffmanTables()[0] = null;
        check(p.getACHuffmanTables()[0] == JPEGHuffmanTable.StdACLuminance, "getACHuffmanTables defensive copy");
        // zero-length arrays are legal (subset semantics) and flip the state bit
        p.unsetDecodeTables();
        check(!p.areTablesSet() && p.getQTables() == null, "unset restores pristine state");
        p.setDecodeTables(new JPEGQTable[0], new JPEGHuffmanTable[0], new JPEGHuffmanTable[0]);
        check(p.areTablesSet(), "zero-length arrays are accepted");
        check(p.getQTables().length == 0 && p.getDCHuffmanTables().length == 0 && p.getACHuffmanTables().length == 0,
              "zero-length arrays round-trip");
        // a rejected call after a valid set leaves the previous tables intact
        p.unsetDecodeTables();
        p.setDecodeTables(Q4, DC2, AC2);
        expectThrows(IllegalArgumentException.class,
            () -> p.setDecodeTables(Q4, DC2, new JPEGHuffmanTable[]{AC2[0]}), "mismatch after valid set");
        check(p.areTablesSet(), "failed reset keeps tables set");
        check(p.getQTables().length == 4 && p.getACHuffmanTables().length == 2,
              "failed reset keeps previous table contents");
        p.unsetDecodeTables();
        check(!p.areTablesSet(), "final unset clears state");
        check(p.getQTables() == null && p.getDCHuffmanTables() == null && p.getACHuffmanTables() == null,
              "all getters null after final unset");
    }

    static void testInheritedParamContract() {
        JPEGImageReadParam p = new JPEGImageReadParam();
        // source region round-trip coexists with the table state machine
        java.awt.Rectangle r = new java.awt.Rectangle(1, 2, 3, 4);
        p.setSourceRegion(r);
        check(p.getSourceRegion().equals(r), "source region round-trip");
        p.setDecodeTables(Q4, DC2, AC2);
        check(p.getSourceRegion().equals(r), "setting tables leaves source region intact");
        check(p.areTablesSet(), "setting source region leaves tables intact");
        p.setSourceRegion(null);
        check(p.getSourceRegion() == null, "null source region clears");
        p.setSourceSubsampling(2, 3, 1, 1);
        check(p.getSourceXSubsampling() == 2 && p.getSourceYSubsampling() == 3
              && p.getSubsamplingXOffset() == 1 && p.getSubsamplingYOffset() == 1,
              "subsampling round-trip");
        expectThrows(IllegalArgumentException.class, () -> p.setSourceSubsampling(0, 1, 0, 0),
              "zero subsampling rejected");
        expectThrows(IllegalArgumentException.class, () -> p.setSourceSubsampling(-1, 1, 0, 0),
              "negative subsampling rejected");
        check(p.areTablesSet(), "subsampling changes leave tables intact");
    }

    static void testEndToEndDecode() throws Exception {
        // encode a small jpeg with the stock writer
        BufferedImage img = new BufferedImage(16, 12, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < 12; y++)
            for (int x = 0; x < 16; x++)
                img.setRGB(x, y, (((x * 13) & 0xff) << 16) | (((y * 17) & 0xff) << 8) | 7);
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageOutputStream ios = new MemoryCacheImageOutputStream(bos);
        writer.setOutput(ios);
        writer.write(null, new IIOImage(img, null, null), null);
        ios.close(); writer.dispose();
        byte[] jpg = bos.toByteArray();
        check(jpg.length > 0, "jpeg bytes produced");
        // reader default param is a JPEGImageReadParam
        ImageReader reader = ImageIO.getImageReadersByFormatName("jpeg").next();
        check(reader.getDefaultReadParam() instanceof JPEGImageReadParam,
              "jpeg reader default param is JPEGImageReadParam");
        // decode with tables attached: harmless for a self-contained jpeg
        JPEGImageReadParam p = new JPEGImageReadParam();
        p.setDecodeTables(Q4, DC2, AC2);
        check(p.areTablesSet(), "tables attached for decode");
        ImageInputStream iis = new MemoryCacheImageInputStream(new ByteArrayInputStream(jpg));
        reader.setInput(iis);
        BufferedImage back = reader.read(0, p);
        check(back != null && back.getWidth() == 16 && back.getHeight() == 12,
              "decode with explicit tables round-trips dimensions");
        // decode again with the same param object (param is reusable)
        ImageInputStream iis2 = new MemoryCacheImageInputStream(new ByteArrayInputStream(jpg));
        reader.setInput(iis2);
        BufferedImage back2 = reader.read(0, p);
        check(back2 != null && back2.getWidth() == 16, "param object reusable across reads");
        iis.close(); iis2.close();
        reader.dispose();
    }
}
