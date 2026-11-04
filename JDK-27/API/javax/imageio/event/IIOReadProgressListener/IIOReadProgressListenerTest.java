/*
 * @test
 * @summary javax.imageio.event.IIOReadProgressListener conformance: white-box structure + black-box behavior.
 * @run main IIOReadProgressListenerTest
 */

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.event.IIOReadProgressListener;
import javax.imageio.stream.ImageInputStream;

public class IIOReadProgressListenerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.imageio.event.IIOReadProgressListener");
        check(c.isInterface(), "isInterface");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getInterfaces().length == 1, "one direct superinterface");
        check(c.getInterfaces()[0].getName().equals("java.util.EventListener"), "extends EventListener");
        Method[] ms = c.getDeclaredMethods();
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (Method m : ms) names.add(m.getName());
        check(names.contains("sequenceStarted"), "method sequenceStarted");
        check(names.contains("sequenceComplete"), "method sequenceComplete");
        check(names.contains("imageStarted"), "method imageStarted");
        check(names.contains("imageProgress"), "method imageProgress");
        check(names.contains("imageComplete"), "method imageComplete");
        check(names.contains("thumbnailStarted"), "method thumbnailStarted");
        check(names.contains("thumbnailProgress"), "method thumbnailProgress");
        check(names.contains("thumbnailComplete"), "method thumbnailComplete");
        check(names.contains("readAborted"), "method readAborted");
        for (Method m : ms) check(Modifier.isPublic(m.getModifiers()), m.getName() + " public");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        final int[] calls = new int[1];
        IIOReadProgressListener l = new IIOReadProgressListener() {
            public void sequenceStarted(ImageReader src, int minIndex) { calls[0]++; }
            public void sequenceComplete(ImageReader src) { calls[0]++; }
            public void imageStarted(ImageReader src, int imageIndex) { calls[0]++; }
            public void imageProgress(ImageReader src, float percentageDone) { calls[0]++; }
            public void imageComplete(ImageReader src) { calls[0]++; }
            public void thumbnailStarted(ImageReader src, int imageIndex, int thumbnailIndex) { calls[0]++; }
            public void thumbnailProgress(ImageReader src, float percentageDone) { calls[0]++; }
            public void thumbnailComplete(ImageReader src) { calls[0]++; }
            public void readAborted(ImageReader src) { calls[0]++; }
        };
        // direct invocation is a no-op and must not throw
        ImageReader dummy = ImageIO.getImageReadersByFormatName("png").next();
        l.sequenceStarted(dummy, 0);
        l.sequenceComplete(dummy);
        l.imageStarted(dummy, 0);
        l.imageProgress(dummy, 0.5f);
        l.imageComplete(dummy);
        l.thumbnailStarted(dummy, 0, 0);
        l.thumbnailProgress(dummy, 0.5f);
        l.thumbnailComplete(dummy);
        l.readAborted(dummy);
        check(calls[0] == 9, "all nine methods callable without throwing");

        // attach to a real reader and observe a decode
        BufferedImage bi = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(bi, "png", bos);
        final int[] started = new int[1];
        final int[] completed = new int[1];
        IIOReadProgressListener real = new IIOReadProgressListener() {
            public void sequenceStarted(ImageReader r, int min) {}
            public void sequenceComplete(ImageReader r) {}
            public void imageStarted(ImageReader r, int index) { started[0]++; }
            public void imageProgress(ImageReader r, float pct) {}
            public void imageComplete(ImageReader r) { completed[0]++; }
            public void thumbnailStarted(ImageReader r, int i, int t) {}
            public void thumbnailProgress(ImageReader r, float pct) {}
            public void thumbnailComplete(ImageReader r) {}
            public void readAborted(ImageReader r) {}
        };
        dummy.addIIOReadProgressListener(real);
        ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(bos.toByteArray()));
        dummy.setInput(iis);
        dummy.read(0);
        check(started[0] == 1, "imageStarted fired by real read");
        check(completed[0] == 1, "imageComplete fired by real read");
        dummy.removeIIOReadProgressListener(real);
        dummy.dispose();
        iis.close();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
