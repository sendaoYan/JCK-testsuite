/*
 * @test
 * @summary javax.imageio.plugins.jpeg.JPEGImageReadParam conformance: white-box structure + black-box behavior.
 * @run main JPEGImageReadParamTest
 */

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Modifier;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
