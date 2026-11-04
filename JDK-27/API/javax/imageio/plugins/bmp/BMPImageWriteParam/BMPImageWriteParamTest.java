/*
 * @test
 * @summary javax.imageio.plugins.bmp.BMPImageWriteParam conformance: white-box structure + black-box behavior.
 * @run main BMPImageWriteParamTest
 */

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Modifier;
import java.util.Locale;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.plugins.bmp.BMPImageWriteParam;

public class BMPImageWriteParamTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.imageio.plugins.bmp.BMPImageWriteParam");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(Modifier.isAbstract(mods) == false, "not abstract");
        check(c.getSuperclass().getName().equals("javax.imageio.ImageWriteParam"), "extends ImageWriteParam");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(c.getConstructors().length == 2, "two constructors");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        BMPImageWriteParam p = new BMPImageWriteParam();
        check(!p.isTopDown(), "default topDown false");
        p.setTopDown(true);
        check(p.isTopDown(), "setTopDown true round-trip");
        p.setTopDown(false);
        check(!p.isTopDown(), "setTopDown false round-trip");

        BMPImageWriteParam localeP = new BMPImageWriteParam(Locale.US);
        check(!localeP.isTopDown(), "locale ctor default topDown false");

        // inherited compression state defaults
        check(p.canWriteCompressed(), "canWriteCompressed true");
        check(p.getCompressionMode() == ImageWriteParam.MODE_COPY_FROM_METADATA,
              "default compression mode COPY_FROM_METADATA");
        String[] types = p.getCompressionTypes();
        boolean hasRGB = false, hasRLE8 = false;
        for (String t : types) {
            if ("BI_RGB".equals(t)) hasRGB = true;
            if ("BI_RLE8".equals(t)) hasRLE8 = true;
        }
        check(hasRGB && hasRLE8, "compression types include BI_RGB and BI_RLE8");

        // explicit compression type round-trip (requires MODE_EXPLICIT)
        p.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        check(p.getCompressionType() == null, "MODE_EXPLICIT unsets compression type");
        p.setCompressionType("BI_RGB");
        check("BI_RGB".equals(p.getCompressionType()), "explicit compression type BI_RGB");
        check(p.getCompressionQuality() == 1.0f, "default compression quality 1.0");

        // real BMP writer + in-memory write/read round-trip with top-down
        ImageWriter writer = ImageIO.getImageWritersByFormatName("bmp").next();
        check(writer != null, "bmp writer present");
        check(writer.getDefaultWriteParam() instanceof BMPImageWriteParam,
              "writer default param is BMPImageWriteParam");
        BMPImageWriteParam wp = (BMPImageWriteParam) writer.getDefaultWriteParam();
        wp.setTopDown(true);

        BufferedImage img = new BufferedImage(8, 6, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < 6; y++) {
            for (int x = 0; x < 8; x++) {
                img.setRGB(x, y, (x * 31 << 16) | (y * 41 << 8) | 0x3f);
            }
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
        writer.setOutput(ios);
        writer.write(null, new IIOImage(img, null, null), wp);
        ios.close();
        writer.dispose();
        byte[] bytes = baos.toByteArray();
        check(bytes.length > 0, "bmp bytes written");

        BufferedImage read = ImageIO.read(new ByteArrayInputStream(bytes));
        check(read != null, "bmp read back non-null");
        check(read.getWidth() == 8 && read.getHeight() == 6, "bmp dimensions round-trip");
        check(read.getRGB(3, 2) == img.getRGB(3, 2), "bmp pixel round-trip");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
