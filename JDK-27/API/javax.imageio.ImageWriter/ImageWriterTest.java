/*
 * @test
 * @summary javax.imageio.ImageWriter conformance: white-box structure + black-box behavior.
 * @run main ImageWriterTest
 */

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.Locale;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.event.IIOWriteProgressListener;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;

public class ImageWriterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.imageio.ImageWriter");
        int mods = c.getModifiers();
        check(!c.isInterface(), "not interface");
        check(Modifier.isPublic(mods), "public");
        check(Modifier.isAbstract(mods), "abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        java.util.Set<String> si = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) si.add(i.getName());
        check(si.contains("javax.imageio.ImageTranscoder"), "implements ImageTranscoder");
        java.util.Set<String> dm = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("getOriginatingProvider"), "method getOriginatingProvider");
        check(dm.contains("setOutput"), "method setOutput");
        check(dm.contains("getOutput"), "method getOutput");
        check(dm.contains("getAvailableLocales"), "method getAvailableLocales");
        check(dm.contains("setLocale"), "method setLocale");
        check(dm.contains("getLocale"), "method getLocale");
        check(dm.contains("getDefaultWriteParam"), "method getDefaultWriteParam");
        check(dm.contains("getDefaultStreamMetadata"), "method getDefaultStreamMetadata");
        check(dm.contains("getDefaultImageMetadata"), "method getDefaultImageMetadata");
        check(dm.contains("convertStreamMetadata"), "method convertStreamMetadata");
        check(dm.contains("convertImageMetadata"), "method convertImageMetadata");
        check(dm.contains("canWriteRasters"), "method canWriteRasters");
        check(dm.contains("write"), "method write");
        check(dm.contains("canWriteSequence"), "method canWriteSequence");
        check(dm.contains("abort"), "method abort");
        check(dm.contains("addIIOWriteWarningListener"), "method addIIOWriteWarningListener");
        check(dm.contains("removeIIOWriteWarningListener"), "method removeIIOWriteWarningListener");
        check(dm.contains("addIIOWriteProgressListener"), "method addIIOWriteProgressListener");
        check(dm.contains("removeIIOWriteProgressListener"), "method removeIIOWriteProgressListener");
        check(dm.contains("reset"), "method reset");
        check(dm.contains("dispose"), "method dispose");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        BufferedImage bi = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);
        bi.setRGB(0, 0, 0xFF0000FF);
        bi.setRGB(3, 3, 0xFF00FF00);

        Iterator<ImageWriter> it = ImageIO.getImageWritersByFormatName("png");
        check(it.hasNext(), "png writer available");
        ImageWriter writer = it.next();

        // identity/info
        check(writer.getOriginatingProvider() != null, "getOriginatingProvider non-null");
        check(writer.getLocale() == null, "default locale null");
        check(writer.getAvailableLocales() == null, "PNG writer has no available locales");

        // default write param
        ImageWriteParam param = writer.getDefaultWriteParam();
        check(param != null, "getDefaultWriteParam non-null");
        check(param.canWriteCompressed(), "PNG writer supports compression");

        // metadata accessors
        ImageTypeSpecifier its = ImageTypeSpecifier.createFromRenderedImage(bi);
        check(writer.getDefaultStreamMetadata(param) == null, "PNG has no stream metadata");
        check(writer.getDefaultImageMetadata(its, param) != null, "default image metadata non-null");

        // output wiring + write(RenderedImage)
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageOutputStream ios = new MemoryCacheImageOutputStream(bos);
        writer.setOutput(ios);
        check(writer.getOutput() == ios, "getOutput identity after setOutput");
        writer.write(bi);
        check(bos.size() > 0, "write(RenderedImage) produced bytes");

        // write(IIOImage)
        int afterFirst = bos.size();
        writer.write(new IIOImage(bi, null, null));
        check(bos.size() > afterFirst, "write(IIOImage) appended bytes");

        // progress listener fired during write
        final int[] started = new int[1];
        final int[] completed = new int[1];
        IIOWriteProgressListener l = new IIOWriteProgressListener() {
            public void imageStarted(ImageWriter w, int index) { started[0]++; }
            public void imageProgress(ImageWriter w, float pct) {}
            public void imageComplete(ImageWriter w) { completed[0]++; }
            public void thumbnailStarted(ImageWriter w, int i, int t) {}
            public void thumbnailProgress(ImageWriter w, float pct) {}
            public void thumbnailComplete(ImageWriter w) {}
            public void writeAborted(ImageWriter w) {}
        };
        writer.addIIOWriteProgressListener(l);
        writer.write(bi);
        check(started[0] == 1, "imageStarted fired once");
        check(completed[0] == 1, "imageComplete fired once");
        writer.removeIIOWriteProgressListener(l);

        writer.reset();
        check(writer.getOutput() == null, "reset clears output");
        writer.dispose();
        ios.close();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
