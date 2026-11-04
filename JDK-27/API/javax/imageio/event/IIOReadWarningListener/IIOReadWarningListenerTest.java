/*
 * @test
 * @summary javax.imageio.event.IIOReadWarningListener conformance: white-box structure + black-box behavior.
 * @run main IIOReadWarningListenerTest
 */

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.event.IIOReadWarningListener;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;

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
        testSignatureExactSet();
        testWarningEndToEndCorruptJpeg();
        testCleanDecodesNoWarnings();
        testListenerRegistrationSemantics();
        testDirectInvocationContract();
        testMultiListenerDelivery();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ================= appended: contract deepening (evtplug) =================

    static void testSignatureExactSet() throws Exception {
        Class<?> c = Class.forName("javax.imageio.event.IIOReadWarningListener");
        Method[] ms = c.getDeclaredMethods();
        check(ms.length == 1, "exactly one declared method");
        Method m = ms[0];
        check(m.getName().equals("warningOccurred"), "method named warningOccurred");
        check(m.getReturnType() == void.class, "returns void");
        Class<?>[] ps = m.getParameterTypes();
        check(ps.length == 2, "two parameters");
        check(ps[0].getName().equals("javax.imageio.ImageReader"), "param 0 ImageReader");
        check(ps[1].getName().equals("java.lang.String"), "param 1 String");
        check(Modifier.isPublic(m.getModifiers()) && Modifier.isAbstract(m.getModifiers()),
              "public abstract");
        check(!m.isDefault(), "not a default method");
        check(m.getExceptionTypes().length == 0, "no checked exceptions declared");
        check(c.getDeclaredFields().length == 0, "no declared fields");
        check(c.getDeclaredClasses().length == 0, "no member types");
    }

    /** Recording implementation. */
    static class RecReadWarning implements IIOReadWarningListener {
        int count;
        List<String> messages = new ArrayList<String>();
        ImageReader lastSource;
        public void warningOccurred(ImageReader source, String warning) {
            count++; messages.add(warning); lastSource = source;
        }
    }

    /** Valid baseline JPEG followed by 3 stray bytes before the final EOI marker. */
    static byte[] corruptJpegWithExtraneousBytes() throws IOException {
        BufferedImage bi = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < 16; y++)
            for (int x = 0; x < 16; x++)
                bi.setRGB(x, y, (((x * 31) & 0xff) << 16) | (((y * 41) & 0xff) << 8) | 0x33);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(bi, "jpeg", bos);
        byte[] jpg = bos.toByteArray();
        int eoi = -1;
        for (int i = jpg.length - 2; i >= 0; i--) {
            if ((jpg[i] & 0xff) == 0xFF && (jpg[i + 1] & 0xff) == 0xD9) { eoi = i; break; }
        }
        check(eoi > 0, "EOI marker located in encoded jpeg");
        byte[] out = new byte[jpg.length + 3];
        System.arraycopy(jpg, 0, out, 0, eoi);
        out[eoi] = 0x42; out[eoi + 1] = 0x42; out[eoi + 2] = 0x42;
        System.arraycopy(jpg, eoi, out, eoi + 3, jpg.length - eoi);
        return out;
    }

    /**
     * End-to-end: libjpeg reports recoverable trailing garbage as a warning;
     * decoding still succeeds. Verified identical on JDK-8 and JDK-27.
     */
    static void testWarningEndToEndCorruptJpeg() throws Exception {
        byte[] bad = corruptJpegWithExtraneousBytes();
        ImageReader reader = ImageIO.getImageReadersByFormatName("jpeg").next();
        RecReadWarning rec = new RecReadWarning();
        reader.addIIOReadWarningListener(rec);
        ImageInputStream in = new MemoryCacheImageInputStream(new ByteArrayInputStream(bad));
        reader.setInput(in);
        BufferedImage bi = reader.read(0, null);
        check(bi != null && bi.getWidth() == 16 && bi.getHeight() == 16,
              "corrupt-but-recoverable jpeg still decodes to 16x16");
        check(rec.count >= 1, "warningOccurred fired for recoverable corruption");
        boolean allNonNull = true;
        for (String w : rec.messages) if (w == null) allNonNull = false;
        check(allNonNull, "every warning message non-null");
        check(rec.messages.get(0).contains("extraneous bytes before marker"),
              "warning message mentions extraneous bytes: " + rec.messages.get(0));
        check(rec.lastSource == reader, "warningOccurred source is the reader");
        // remove -> re-decode a fresh stream -> zero callbacks
        reader.removeIIOReadWarningListener(rec);
        rec.count = 0; rec.messages.clear();
        ImageInputStream in2 = new MemoryCacheImageInputStream(new ByteArrayInputStream(bad));
        reader.setInput(in2);
        reader.read(0, null);
        check(rec.count == 0, "removed listener receives no warning");
        in.close(); in2.close();
        reader.removeAllIIOReadWarningListeners();
        reader.dispose();
    }

    static void testCleanDecodesNoWarnings() throws Exception {
        // clean PNG
        BufferedImage bi = new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(bi, "png", bos);
        ImageReader pngReader = ImageIO.getImageReadersByFormatName("png").next();
        RecReadWarning rec = new RecReadWarning();
        pngReader.addIIOReadWarningListener(rec);
        ImageInputStream pin = new MemoryCacheImageInputStream(new ByteArrayInputStream(bos.toByteArray()));
        pngReader.setInput(pin);
        pngReader.read(0, null);
        check(rec.count == 0, "clean PNG decode produces no warnings");
        pin.close(); pngReader.dispose();
        // clean baseline JPEG
        ByteArrayOutputStream jos = new ByteArrayOutputStream();
        ImageIO.write(bi, "jpeg", jos);
        ImageReader jpegReader = ImageIO.getImageReadersByFormatName("jpeg").next();
        RecReadWarning jrec = new RecReadWarning();
        jpegReader.addIIOReadWarningListener(jrec);
        ImageInputStream jin = new MemoryCacheImageInputStream(new ByteArrayInputStream(jos.toByteArray()));
        jpegReader.setInput(jin);
        jpegReader.read(0, null);
        check(jrec.count == 0, "clean baseline JPEG decode produces no warnings");
        jin.close(); jpegReader.dispose();
    }

    static void testListenerRegistrationSemantics() throws Exception {
        byte[] bad = corruptJpegWithExtraneousBytes();
        ImageReader reader = ImageIO.getImageReadersByFormatName("jpeg").next();
        RecReadWarning rec = new RecReadWarning();
        reader.addIIOReadWarningListener(rec);
        reader.addIIOReadWarningListener(rec); // duplicate registration doubles delivery
        ImageInputStream in = new MemoryCacheImageInputStream(new ByteArrayInputStream(bad));
        reader.setInput(in);
        reader.read(0, null);
        check(rec.count == 2, "duplicate add: warning delivered twice");
        reader.removeIIOReadWarningListener(rec); // removes one of the two occurrences
        rec.count = 0;
        ImageInputStream in2 = new MemoryCacheImageInputStream(new ByteArrayInputStream(bad));
        reader.setInput(in2);
        reader.read(0, null);
        check(rec.count == 1, "after one remove: single delivery again");
        reader.removeIIOReadWarningListener(rec);                       // last occurrence
        reader.removeIIOReadWarningListener(rec);                       // already removed: no-op
        reader.removeIIOReadWarningListener(new RecReadWarning());      // never registered: no-op
        reader.addIIOReadWarningListener(null);   // documented no-op, must not throw
        reader.removeIIOReadWarningListener(null); // documented no-op, must not throw
        reader.addIIOReadWarningListener(rec);
        reader.removeAllIIOReadWarningListeners();
        rec.count = 0;
        ImageInputStream in3 = new MemoryCacheImageInputStream(new ByteArrayInputStream(bad));
        reader.setInput(in3);
        reader.read(0, null);
        check(rec.count == 0, "removeAll clears warning listeners");
        in.close(); in2.close(); in3.close();
        reader.dispose();
    }

    /** Direct invocation: parameters are relayed verbatim, nothing validates them. */
    static void testDirectInvocationContract() throws Exception {
        ImageReader reader = ImageIO.getImageReadersByFormatName("png").next();
        RecReadWarning rec = new RecReadWarning();
        rec.warningOccurred(reader, "msg-1");
        check(rec.count == 1, "direct call counted");
        check("msg-1".equals(rec.messages.get(0)), "message relayed verbatim");
        check(rec.lastSource == reader, "source instance relayed verbatim");
        rec.warningOccurred(null, null); // interface contract does not reject nulls
        check(rec.count == 2, "null source/message accepted by interface");
        check(rec.messages.get(1) == null, "null message stored as-is");
        rec.warningOccurred(reader, "");
        check(rec.messages.get(2).equals(""), "empty message relayed");
        // the interface method is reachable through the public API surface
        Method m = Class.forName("javax.imageio.event.IIOReadWarningListener")
            .getMethod("warningOccurred", ImageReader.class, String.class);
        check(m != null, "getMethod resolves warningOccurred(ImageReader,String)");
        check(java.util.EventListener.class.isAssignableFrom(RecReadWarning.class),
              "implementations are EventListeners");
        reader.dispose();
    }

    /** Two distinct listeners both receive the same warning; delivery follows registration order. */
    static void testMultiListenerDelivery() throws Exception {
        byte[] bad = corruptJpegWithExtraneousBytes();
        ImageReader reader = ImageIO.getImageReadersByFormatName("jpeg").next();
        final List<String> order = new ArrayList<String>();
        RecReadWarning first = new RecReadWarning() {
            public void warningOccurred(ImageReader s, String w) { super.warningOccurred(s, w); order.add("first"); }
        };
        RecReadWarning second = new RecReadWarning() {
            public void warningOccurred(ImageReader s, String w) { super.warningOccurred(s, w); order.add("second"); }
        };
        reader.addIIOReadWarningListener(first);
        reader.addIIOReadWarningListener(second);
        ImageInputStream in = new MemoryCacheImageInputStream(new ByteArrayInputStream(bad));
        reader.setInput(in);
        reader.read(0, null);
        check(first.count >= 1 && second.count >= 1, "both listeners notified");
        check(first.count == second.count, "all listeners see the same warning count");
        check(first.messages.get(0).equals(second.messages.get(0)),
              "all listeners see the same message string");
        boolean orderOk = order.size() >= 2 && order.get(0).equals("first") && order.get(1).equals("second");
        check(orderOk, "listeners notified in registration order: " + order);
        reader.removeAllIIOReadWarningListeners();
        in.close();
        reader.dispose();
    }
}
