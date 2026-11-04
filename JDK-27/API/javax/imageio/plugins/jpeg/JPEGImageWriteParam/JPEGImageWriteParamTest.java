/*
 * @test
 * @summary javax.imageio.plugins.jpeg.JPEGImageWriteParam conformance: white-box structure + black-box behavior.
 * @run main JPEGImageWriteParamTest
 */

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Modifier;
import java.util.Locale;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
