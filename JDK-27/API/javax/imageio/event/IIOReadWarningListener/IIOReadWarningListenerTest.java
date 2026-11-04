/*
 * @test
 * @summary javax.imageio.event.IIOReadWarningListener conformance: white-box structure + black-box behavior.
 * @run main IIOReadWarningListenerTest
 */

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.event.IIOReadWarningListener;
import javax.imageio.stream.ImageInputStream;

public class IIOReadWarningListenerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.imageio.event.IIOReadWarningListener");
        check(c.isInterface(), "isInterface");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getInterfaces().length == 1, "one direct superinterface");
        check(c.getInterfaces()[0].getName().equals("java.util.EventListener"), "extends EventListener");
        Method[] ms = c.getDeclaredMethods();
        check(ms.length == 1, "single method");
        check(ms[0].getName().equals("warningOccurred"), "method warningOccurred");
        check(ms[0].getReturnType().getName().equals("void"), "warningOccurred returns void");
        check(ms[0].getParameterTypes().length == 2, "two parameters");
        check(ms[0].getParameterTypes()[0].getName().equals("javax.imageio.ImageReader"), "first param ImageReader");
        check(ms[0].getParameterTypes()[1].getName().equals("java.lang.String"), "second param String");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        final int[] calls = new int[1];
        final String[] seen = new String[1];
        IIOReadWarningListener l = new IIOReadWarningListener() {
            public void warningOccurred(ImageReader source, String warning) { calls[0]++; seen[0] = warning; }
        };
        // direct invocation is a no-op and must not throw
        ImageReader dummy = ImageIO.getImageReadersByFormatName("png").next();
        l.warningOccurred(dummy, "hello");
        check(calls[0] == 1, "warningOccurred callable");
        check("hello".equals(seen[0]), "warning string delivered");

        // attach to a real reader; a clean png produces no warning but add/remove/read is safe
        BufferedImage bi = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(bi, "png", bos);
        dummy.addIIOReadWarningListener(l);
        ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(bos.toByteArray()));
        dummy.setInput(iis);
        dummy.read(0);
        check(calls[0] == 1, "no warning fired for a clean png read");
        dummy.removeIIOReadWarningListener(l);
        dummy.dispose();
        iis.close();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
