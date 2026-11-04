/*
 * @test
 * @summary javax.imageio.ImageTranscoder conformance: white-box structure + black-box behavior.
 * @run main ImageTranscoderTest
 */

import java.awt.image.BufferedImage;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageTranscoder;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;

public class ImageTranscoderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.imageio.ImageTranscoder");
        check(c.isInterface(), "isInterface");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        Method[] ms = c.getDeclaredMethods();
        check(ms.length == 2, "two methods");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (Method m : ms) names.add(m.getName());
        check(names.contains("convertStreamMetadata"), "method convertStreamMetadata");
        check(names.contains("convertImageMetadata"), "method convertImageMetadata");
        for (Method m : ms) check(Modifier.isPublic(m.getModifiers()), m.getName() + " public");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ImageReader reader = ImageIO.getImageReadersByFormatName("png").next();
        ImageWriter writer = ImageIO.getImageWritersByFormatName("png").next();

        // ImageIO.getImageTranscoders returns a non-null iterator (may be empty)
        Iterator<ImageTranscoder> transcoders = ImageIO.getImageTranscoders(reader, writer);
        check(transcoders != null, "getImageTranscoders non-null");

        // ImageWriter implements ImageTranscoder, so a writer is a concrete transcoder
        check(writer instanceof ImageTranscoder, "ImageWriter implements ImageTranscoder");
        ImageTranscoder t = writer;

        // converting null stream metadata returns null for the built-in PNG writer
        check(t.convertStreamMetadata(null, null) == null, "convertStreamMetadata(null, null) null");

        // converting null image metadata to a known type returns a metadata object
        ImageTypeSpecifier its = ImageTypeSpecifier.createFromRenderedImage(new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB));
        check(t.convertImageMetadata(null, its, null) != null, "convertImageMetadata(null, its, null) non-null");

        reader.dispose();
        writer.dispose();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
