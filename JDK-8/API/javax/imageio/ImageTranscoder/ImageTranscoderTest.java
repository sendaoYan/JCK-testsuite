/*
 * @test
 * @summary javax.imageio.ImageTranscoder conformance: interface hierarchy and methods.
 * @run main ImageTranscoderTest
 */

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageTranscoder;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.MemoryCacheImageInputStream;

public class ImageTranscoderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface Act { void run() throws Exception; }
    static void expectExact(Class<? extends Throwable> expected, Act a, String msg) {
        try {
            a.run();
            check(false, msg + ": expected " + expected.getSimpleName() + " but nothing thrown");
        } catch (Throwable t) {
            check(t.getClass() == expected,
                  msg + ": expected " + expected.getSimpleName() + " but got " + t.getClass().getName());
        }
    }
    static byte[] pngBytes() throws java.io.IOException {
        BufferedImage bi = new BufferedImage(3, 3, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < 3; y++)
            for (int x = 0; x < 3; x++)
                bi.setRGB(x, y, 0xFF000000 | (x * 40 << 16) | (y * 40 << 8) | (x + y));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(bi, "png", bos);
        return bos.toByteArray();
    }

    // legacy assertions of the original file (kept)
    static void testLegacy() throws Exception {
        Class<?> c = Class.forName("javax.imageio.ImageTranscoder");
        check(c.isInterface(), "isInterface");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        Set<String> dm = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("convertStreamMetadata"), "method convertStreamMetadata");
        check(dm.contains("convertImageMetadata"), "method convertImageMetadata");
    }

    // ---- structure ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.imageio.ImageTranscoder");
        Method[] ms = c.getDeclaredMethods();
        check(ms.length == 2, "two methods");
        for (Method m : ms) check(Modifier.isPublic(m.getModifiers()), m.getName() + " public");
    }

    // ---- basic behavior ----
    static void testBlackBox() throws Exception {
        ImageReader reader = ImageIO.getImageReadersByFormatName("png").next();
        ImageWriter writer = ImageIO.getImageWritersByFormatName("png").next();
        Iterator<ImageTranscoder> transcoders = ImageIO.getImageTranscoders(reader, writer);
        check(transcoders != null, "getImageTranscoders non-null");
        check(writer instanceof ImageTranscoder, "ImageWriter implements ImageTranscoder");
        ImageTranscoder t = writer;
        check(t.convertStreamMetadata(null, null) == null, "convertStreamMetadata(null, null) null");
        ImageTypeSpecifier its = ImageTypeSpecifier.createFromRenderedImage(new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB));
        check(t.convertImageMetadata(null, its, null) != null, "convertImageMetadata(null, its, null) non-null");
        reader.dispose();
        writer.dispose();
    }

    // ---- deep: precise interface shape ----
    static void testInterfaceContract() throws Exception {
        Class<?> c = Class.forName("javax.imageio.ImageTranscoder");
        check(c.getFields().length == 0, "interface declares no constants");
        Method[] ms = c.getDeclaredMethods();
        check(ms.length == 2, "exactly two methods");
        Method sm = c.getMethod("convertStreamMetadata", IIOMetadata.class, ImageWriteParam.class);
        check(Modifier.isPublic(sm.getModifiers()) && Modifier.isAbstract(sm.getModifiers()),
              "convertStreamMetadata is public abstract");
        check(sm.getReturnType() == IIOMetadata.class, "convertStreamMetadata returns IIOMetadata");
        Method im = c.getMethod("convertImageMetadata", IIOMetadata.class, ImageTypeSpecifier.class,
                                ImageWriteParam.class);
        check(Modifier.isPublic(im.getModifiers()) && Modifier.isAbstract(im.getModifiers()),
              "convertImageMetadata is public abstract");
        check(im.getReturnType() == IIOMetadata.class, "convertImageMetadata returns IIOMetadata");
        check(im.getParameterTypes().length == 3, "convertImageMetadata takes 3 params");
        // ImageWriter is the only platform implementor
        check(c.isAssignableFrom(javax.imageio.ImageWriter.class), "ImageWriter implements ImageTranscoder");
        check(!c.isAssignableFrom(javax.imageio.ImageReader.class), "ImageReader does not implement it");
        // a transcoder-typed variable accepts any writer
        ImageWriter w = ImageIO.getImageWritersByFormatName("png").next();
        ImageTranscoder t = w;
        check(t != null, "writer usable through the interface type");
        w.dispose();
    }

    // ---- deep: PNG metadata end-to-end through the transcoder API ----
    static void testPngMetadataChain() throws Exception {
        ImageReader reader = ImageIO.getImageReadersByFormatName("png").next();
        reader.setInput(new MemoryCacheImageInputStream(new ByteArrayInputStream(pngBytes())), true, true);
        IIOMetadata imageMeta = reader.getImageMetadata(0);
        check(imageMeta != null, "PNG image metadata non-null");
        check(imageMeta.isStandardMetadataFormatSupported(), "PNG metadata supports standard format");
        Set<String> fmts = new HashSet<String>(Arrays.asList(imageMeta.getMetadataFormatNames()));
        check(fmts.contains("javax_imageio_png_1.0"), "native PNG format name present");
        check(fmts.contains("javax_imageio_1.0"), "standard format name present");
        check(imageMeta.getAsTree("javax_imageio_1.0").getNodeName().equals("javax_imageio_1.0"),
              "standard tree root name");
        check(reader.getStreamMetadata() == null, "plain PNG stream has no stream metadata (probed)");
        ImageWriter writer = ImageIO.getImageWritersByFormatName("png").next();
        ImageWriteParam wp = writer.getDefaultWriteParam();
        ImageTypeSpecifier its = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB);
        ImageTranscoder t = writer;
        // null inData for a stream: no default stream metadata exists for PNG (probed)
        check(t.convertStreamMetadata(null, wp) == null, "convertStreamMetadata(null) -> null for PNG");
        check(t.convertStreamMetadata(reader.getStreamMetadata(), wp) == null,
              "convertStreamMetadata(pngStreamMeta) -> null");
        check(writer.getDefaultStreamMetadata(null) == null, "PNG defaultStreamMetadata is null");
        // image metadata conversion produces a fresh object exposing both formats
        IIOMetadata conv = t.convertImageMetadata(imageMeta, its, wp);
        check(conv != null, "convertImageMetadata(pngMeta) non-null");
        check(conv != imageMeta, "conversion returns a new object, not the same reference");
        check(conv.isReadOnly() == false, "converted metadata is writable");
        Set<String> cfmts = new HashSet<String>(Arrays.asList(conv.getMetadataFormatNames()));
        check(cfmts.contains("javax_imageio_png_1.0"), "converted keeps native format");
        // null inData falls back to a default image metadata object
        IIOMetadata convNull = t.convertImageMetadata(null, its, wp);
        check(convNull != null, "convertImageMetadata(null) non-null for PNG");
        IIOMetadata def = writer.getDefaultImageMetadata(its, null);
        check(def != null, "getDefaultImageMetadata non-null");
        check(Arrays.asList(def.getMetadataFormatNames()).contains("javax_imageio_png_1.0"),
              "default image metadata exposes PNG native format");
        reader.dispose();
        writer.dispose();
    }

    // ---- deep: built-in writers' quirks around null metadata (probed) ----
    static void testOtherWritersNullSemantics() throws Exception {
        ImageTypeSpecifier its = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB);
        // BMP: no stream metadata and no null-input default (probed: returns null, no exception)
        ImageWriter bmp = ImageIO.getImageWritersByFormatName("bmp").next();
        ImageTranscoder bt = bmp;
        check(bt.convertStreamMetadata(null, null) == null, "bmp convertStreamMetadata(null) -> null");
        check(bt.convertImageMetadata(null, its, null) == null, "bmp convertImageMetadata(null) -> null");
        bmp.dispose();
        // GIF: violates the javadoc "null inData -> default" wording (probed: IAE)
        final ImageWriter gif = ImageIO.getImageWritersByFormatName("gif").next();
        final ImageTranscoder gt = gif;
        expectExact(IllegalArgumentException.class, () -> gt.convertStreamMetadata(null, null),
                    "gif convertStreamMetadata(null) precise");
        expectExact(IllegalArgumentException.class, () -> gt.convertImageMetadata(null, its, null),
                    "gif convertImageMetadata(null) precise");
        gif.dispose();
        // JPEG: convertStreamMetadata(null) -> null, but convertImageMetadata(null) NPEs (probed)
        final ImageWriter jpg = ImageIO.getImageWritersByFormatName("jpg").next();
        final ImageTranscoder jt = jpg;
        check(jt.convertStreamMetadata(null, null) == null, "jpeg convertStreamMetadata(null) -> null");
        expectExact(NullPointerException.class, () -> jt.convertImageMetadata(null, its, null),
                    "jpeg convertImageMetadata(null) precise");
        // feeding PNG metadata into the JPEG transcoder works (cross-format conversion)
        ImageReader reader = ImageIO.getImageReadersByFormatName("png").next();
        reader.setInput(new MemoryCacheImageInputStream(new ByteArrayInputStream(pngBytes())), true, true);
        IIOMetadata pngMeta = reader.getImageMetadata(0);
        IIOMetadata cross = jt.convertImageMetadata(pngMeta, its, jpg.getDefaultWriteParam());
        check(cross != null, "jpeg convertImageMetadata(pngMeta) non-null (cross-format)");
        reader.dispose();
        jpg.dispose();
    }

    // ---- deep: transcoder lookup via ImageIO ----
    static void testTranscoderLookup() throws Exception {
        ImageReader r = ImageIO.getImageReadersByFormatName("png").next();
        ImageWriter w = ImageIO.getImageWritersByFormatName("png").next();
        check(!ImageIO.getImageTranscoders(r, w).hasNext(),
              "built-in registry has no standalone png->png transcoders (probed)");
        expectExact(IllegalArgumentException.class, () -> ImageIO.getImageTranscoders(null, w),
                    "getImageTranscoders(null reader)");
        expectExact(IllegalArgumentException.class, () -> ImageIO.getImageTranscoders(r, null),
                    "getImageTranscoders(null writer)");
        r.dispose();
        w.dispose();
    }

    public static void main(String[] a) throws Exception {
        testLegacy();
        testWhiteBox();
        testBlackBox();
        testInterfaceContract();
        testPngMetadataChain();
        testOtherWritersNullSemantics();
        testTranscoderLookup();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
