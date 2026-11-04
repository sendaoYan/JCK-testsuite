/*
 * @test
 * @summary javax.imageio.ImageIO conformance: white-box structure + black-box behavior.
 * @run main ImageIOTest
 */

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageTranscoder;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;

public class ImageIOTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.imageio.ImageIO");
        int mods = c.getModifiers();
        check(!c.isInterface(), "not interface");
        check(Modifier.isPublic(mods), "public");
        check(Modifier.isFinal(mods), "final");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        check(c.getConstructors().length == 0, "no public constructors");
        java.util.Set<String> dm = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("getReaderFormatNames"), "method getReaderFormatNames");
        check(dm.contains("getWriterFormatNames"), "method getWriterFormatNames");
        check(dm.contains("getImageReadersByFormatName"), "method getImageReadersByFormatName");
        check(dm.contains("getImageWritersByFormatName"), "method getImageWritersByFormatName");
        check(dm.contains("getImageReaders"), "method getImageReaders");
        check(dm.contains("getImageWriters"), "method getImageWriters");
        check(dm.contains("getImageTranscoders"), "method getImageTranscoders");
        check(dm.contains("read"), "method read");
        check(dm.contains("write"), "method write");
        check(dm.contains("createImageInputStream"), "method createImageInputStream");
        check(dm.contains("createImageOutputStream"), "method createImageOutputStream");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // plugin inventory (deterministic: at least png, built in)
        String[] readerNames = ImageIO.getReaderFormatNames();
        check(readerNames != null && readerNames.length > 0, "reader format names non-empty");
        check(Arrays.asList(readerNames).contains("png"), "reader names contain png");
        String[] writerNames = ImageIO.getWriterFormatNames();
        check(writerNames != null && writerNames.length > 0, "writer format names non-empty");
        check(Arrays.asList(writerNames).contains("png"), "writer names contain png");
        check(ImageIO.getReaderMIMETypes() != null && ImageIO.getReaderMIMETypes().length > 0, "reader MIME types");
        check(ImageIO.getWriterMIMETypes() != null && ImageIO.getWriterMIMETypes().length > 0, "writer MIME types");
        check(ImageIO.getReaderFileSuffixes() != null && ImageIO.getReaderFileSuffixes().length > 0, "reader suffixes");
        check(Arrays.asList(ImageIO.getWriterFileSuffixes()).contains("png"), "writer suffixes contain png");

        // generate a small image and round-trip through png
        BufferedImage bi = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);
        bi.setRGB(0, 0, 0xFF0000FF);
        bi.setRGB(3, 3, 0xFF00FF00);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        check(ImageIO.write(bi, "png", bos), "write png returns true");
        check(bos.size() > 0, "write produced bytes");

        BufferedImage back = ImageIO.read(new ByteArrayInputStream(bos.toByteArray()));
        check(back != null, "read png non-null");
        check(back.getWidth() == 4 && back.getHeight() == 4, "round-trip dimensions");
        check(back.getRGB(0, 0) == 0xFF0000FF, "round-trip pixel (0,0)");
        check(back.getRGB(3, 3) == 0xFF00FF00, "round-trip pixel (3,3)");

        // reading an empty stream yields null
        BufferedImage empty = ImageIO.read(new ByteArrayInputStream(new byte[0]));
        check(empty == null, "read empty stream null");

        // readers/writers by format name
        Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("png");
        check(readers.hasNext(), "png readers available");
        ImageReader reader = readers.next();
        check("png".equals(reader.getFormatName()), "reader format name png");
        reader.dispose();

        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("png");
        check(writers.hasNext(), "png writers available");
        ImageWriter writer = writers.next();
        writer.dispose();

        // getImageReaders over a real stream discovers a reader
        ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(bos.toByteArray()));
        check(iis != null, "createImageInputStream non-null");
        Iterator<ImageReader> discovered = ImageIO.getImageReaders(iis);
        check(discovered.hasNext(), "getImageReaders discovers a reader");
        iis.close();

        // image transcoders: iterator is non-null (may be empty)
        ImageReader r2 = ImageIO.getImageReadersByFormatName("png").next();
        ImageWriter w2 = ImageIO.getImageWritersByFormatName("png").next();
        Iterator<ImageTranscoder> transcoders = ImageIO.getImageTranscoders(r2, w2);
        check(transcoders != null, "getImageTranscoders non-null");
        r2.dispose();
        w2.dispose();

        // in-memory output stream
        ImageOutputStream ios = ImageIO.createImageOutputStream(new ByteArrayOutputStream());
        check(ios != null, "createImageOutputStream non-null");
        ios.close();

        // ImageIO.write to an ImageOutputStream round-trips
        ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
        ImageOutputStream ios2 = new MemoryCacheImageOutputStream(bos2);
        check(ImageIO.write(bi, "png", ios2), "write to ImageOutputStream returns true");
        check(bos2.size() > 0, "ImageOutputStream write produced bytes");
        ios2.close();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
