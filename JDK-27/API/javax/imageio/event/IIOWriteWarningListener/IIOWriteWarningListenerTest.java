/*
 * @test
 * @summary javax.imageio.event.IIOWriteWarningListener conformance: white-box structure + black-box behavior.
 * @run main IIOWriteWarningListenerTest
 */

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.event.IIOWriteWarningListener;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;

public class IIOWriteWarningListenerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.imageio.event.IIOWriteWarningListener");
        check(c.isInterface(), "isInterface");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getInterfaces().length == 1, "one direct superinterface");
        check(c.getInterfaces()[0].getName().equals("java.util.EventListener"), "extends EventListener");
        Method[] ms = c.getDeclaredMethods();
        check(ms.length == 1, "single method");
        check(ms[0].getName().equals("warningOccurred"), "method warningOccurred");
        check(ms[0].getReturnType().getName().equals("void"), "warningOccurred returns void");
        check(ms[0].getParameterTypes().length == 3, "three parameters");
        check(ms[0].getParameterTypes()[0].getName().equals("javax.imageio.ImageWriter"), "first param ImageWriter");
        check(ms[0].getParameterTypes()[1].getName().equals("int"), "second param int");
        check(ms[0].getParameterTypes()[2].getName().equals("java.lang.String"), "third param String");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        final int[] calls = new int[1];
        final String[] seen = new String[1];
        final int[] index = new int[1];
        IIOWriteWarningListener l = new IIOWriteWarningListener() {
            public void warningOccurred(ImageWriter source, int imageIndex, String warning) {
                calls[0]++; index[0] = imageIndex; seen[0] = warning;
            }
        };
        // direct invocation is a no-op and must not throw
        ImageWriter dummy = ImageIO.getImageWritersByFormatName("png").next();
        l.warningOccurred(dummy, 0, "boom");
        check(calls[0] == 1, "warningOccurred callable");
        check(index[0] == 0, "image index delivered");
        check("boom".equals(seen[0]), "warning string delivered");

        // attach to a real writer; a clean png produces no warning but add/remove/write is safe
        BufferedImage bi = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);
        dummy.addIIOWriteWarningListener(l);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageOutputStream ios = new MemoryCacheImageOutputStream(bos);
        dummy.setOutput(ios);
        dummy.write(bi);
        check(calls[0] == 1, "no warning fired for a clean png write");
        dummy.removeIIOWriteWarningListener(l);
        dummy.dispose();
        ios.close();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
