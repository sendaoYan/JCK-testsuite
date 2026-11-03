/*
 * @test
 * @summary javax.imageio.event.IIOReadUpdateListener conformance: white-box structure + black-box behavior.
 * @run main IIOReadUpdateListenerTest
 */

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.event.IIOReadUpdateListener;
import javax.imageio.stream.ImageInputStream;

public class IIOReadUpdateListenerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.imageio.event.IIOReadUpdateListener");
        check(c.isInterface(), "isInterface");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getInterfaces().length == 1, "one direct superinterface");
        check(c.getInterfaces()[0].getName().equals("java.util.EventListener"), "extends EventListener");
        Method[] ms = c.getDeclaredMethods();
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (Method m : ms) names.add(m.getName());
        check(names.contains("passStarted"), "method passStarted");
        check(names.contains("imageUpdate"), "method imageUpdate");
        check(names.contains("passComplete"), "method passComplete");
        check(names.contains("thumbnailPassStarted"), "method thumbnailPassStarted");
        check(names.contains("thumbnailUpdate"), "method thumbnailUpdate");
        check(names.contains("thumbnailPassComplete"), "method thumbnailPassComplete");
        for (Method m : ms) check(Modifier.isPublic(m.getModifiers()), m.getName() + " public");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        final int[] calls = new int[1];
        IIOReadUpdateListener l = new IIOReadUpdateListener() {
            public void passStarted(ImageReader src, BufferedImage im, int pass, int minPass, int numPasses,
                                    int minX, int minY, int periodX, int periodY, int[] bands) { calls[0]++; }
            public void imageUpdate(ImageReader src, BufferedImage im, int minX, int minY,
                                    int width, int height, int periodX, int periodY, int[] bands) { calls[0]++; }
            public void passComplete(ImageReader src, BufferedImage im) { calls[0]++; }
            public void thumbnailPassStarted(ImageReader src, BufferedImage im, int pass, int minPass, int numPasses,
                                             int minX, int minY, int periodX, int periodY, int[] bands) { calls[0]++; }
            public void thumbnailUpdate(ImageReader src, BufferedImage im, int minX, int minY,
                                        int width, int height, int periodX, int periodY, int[] bands) { calls[0]++; }
            public void thumbnailPassComplete(ImageReader src, BufferedImage im) { calls[0]++; }
        };
        // direct invocation is a no-op and must not throw
        ImageReader dummy = ImageIO.getImageReadersByFormatName("png").next();
        BufferedImage bi = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);
        l.passStarted(dummy, bi, 0, 0, 1, 0, 0, 1, 1, null);
        l.imageUpdate(dummy, bi, 0, 0, 4, 4, 1, 1, null);
        l.passComplete(dummy, bi);
        l.thumbnailPassStarted(dummy, bi, 0, 0, 1, 0, 0, 1, 1, null);
        l.thumbnailUpdate(dummy, bi, 0, 0, 4, 4, 1, 1, null);
        l.thumbnailPassComplete(dummy, bi);
        check(calls[0] == 6, "all six methods callable without throwing");

        // attach to a real reader and observe a decode
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(bi, "png", bos);
        final int[] passStarted = new int[1];
        final int[] passComplete = new int[1];
        IIOReadUpdateListener real = new IIOReadUpdateListener() {
            public void passStarted(ImageReader src, BufferedImage im, int pass, int minPass, int numPasses,
                                    int minX, int minY, int periodX, int periodY, int[] bands) { passStarted[0]++; }
            public void imageUpdate(ImageReader src, BufferedImage im, int minX, int minY,
                                    int width, int height, int periodX, int periodY, int[] bands) {}
            public void passComplete(ImageReader src, BufferedImage im) { passComplete[0]++; }
            public void thumbnailPassStarted(ImageReader src, BufferedImage im, int pass, int minPass, int numPasses,
                                             int minX, int minY, int periodX, int periodY, int[] bands) {}
            public void thumbnailUpdate(ImageReader src, BufferedImage im, int minX, int minY,
                                        int width, int height, int periodX, int periodY, int[] bands) {}
            public void thumbnailPassComplete(ImageReader src, BufferedImage im) {}
        };
        dummy.addIIOReadUpdateListener(real);
        ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(bos.toByteArray()));
        dummy.setInput(iis);
        dummy.read(0);
        check(passStarted[0] == 1, "passStarted fired by real read");
        check(passComplete[0] == 1, "passComplete fired by real read");
        dummy.removeIIOReadUpdateListener(real);
        dummy.dispose();
        iis.close();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
