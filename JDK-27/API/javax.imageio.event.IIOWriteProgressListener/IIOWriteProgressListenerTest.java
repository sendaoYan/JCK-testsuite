/*
 * @test
 * @summary javax.imageio.event.IIOWriteProgressListener conformance: white-box structure + black-box behavior.
 * @run main IIOWriteProgressListenerTest
 */

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.event.IIOWriteProgressListener;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;

public class IIOWriteProgressListenerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.imageio.event.IIOWriteProgressListener");
        check(c.isInterface(), "isInterface");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getInterfaces().length == 1, "one direct superinterface");
        check(c.getInterfaces()[0].getName().equals("java.util.EventListener"), "extends EventListener");
        Method[] ms = c.getDeclaredMethods();
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (Method m : ms) names.add(m.getName());
        check(names.contains("imageStarted"), "method imageStarted");
        check(names.contains("imageProgress"), "method imageProgress");
        check(names.contains("imageComplete"), "method imageComplete");
        check(names.contains("thumbnailStarted"), "method thumbnailStarted");
        check(names.contains("thumbnailProgress"), "method thumbnailProgress");
        check(names.contains("thumbnailComplete"), "method thumbnailComplete");
        check(names.contains("writeAborted"), "method writeAborted");
        for (Method m : ms) check(Modifier.isPublic(m.getModifiers()), m.getName() + " public");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        final int[] calls = new int[1];
        IIOWriteProgressListener l = new IIOWriteProgressListener() {
            public void imageStarted(ImageWriter source, int imageIndex) { calls[0]++; }
            public void imageProgress(ImageWriter source, float percentageDone) { calls[0]++; }
            public void imageComplete(ImageWriter source) { calls[0]++; }
            public void thumbnailStarted(ImageWriter source, int imageIndex, int thumbnailIndex) { calls[0]++; }
            public void thumbnailProgress(ImageWriter source, float percentageDone) { calls[0]++; }
            public void thumbnailComplete(ImageWriter source) { calls[0]++; }
            public void writeAborted(ImageWriter source) { calls[0]++; }
        };
        // direct invocation is a no-op and must not throw
        ImageWriter dummy = ImageIO.getImageWritersByFormatName("png").next();
        l.imageStarted(dummy, 0);
        l.imageProgress(dummy, 0.5f);
        l.imageComplete(dummy);
        l.thumbnailStarted(dummy, 0, 0);
        l.thumbnailProgress(dummy, 0.5f);
        l.thumbnailComplete(dummy);
        l.writeAborted(dummy);
        check(calls[0] == 7, "all seven methods callable without throwing");

        // attach to a real writer and observe an encode
        BufferedImage bi = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);
        final int[] started = new int[1];
        final int[] completed = new int[1];
        IIOWriteProgressListener real = new IIOWriteProgressListener() {
            public void imageStarted(ImageWriter w, int index) { started[0]++; }
            public void imageProgress(ImageWriter w, float pct) {}
            public void imageComplete(ImageWriter w) { completed[0]++; }
            public void thumbnailStarted(ImageWriter w, int i, int t) {}
            public void thumbnailProgress(ImageWriter w, float pct) {}
            public void thumbnailComplete(ImageWriter w) {}
            public void writeAborted(ImageWriter w) {}
        };
        dummy.addIIOWriteProgressListener(real);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageOutputStream ios = new MemoryCacheImageOutputStream(bos);
        dummy.setOutput(ios);
        dummy.write(bi);
        check(started[0] == 1, "imageStarted fired by real write");
        check(completed[0] == 1, "imageComplete fired by real write");
        dummy.removeIIOWriteProgressListener(real);
        dummy.dispose();
        ios.close();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
