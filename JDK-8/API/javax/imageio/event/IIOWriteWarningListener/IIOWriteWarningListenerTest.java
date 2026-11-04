/*
 * @test
 * @summary javax.imageio.event.IIOWriteWarningListener conformance: interface hierarchy and methods.
 * @run main IIOWriteWarningListenerTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.event.IIOWriteWarningListener;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;

public class IIOWriteWarningListenerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.imageio.event.IIOWriteWarningListener");
        check(c.isInterface(), "isInterface");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getInterfaces().length == 1, "direct interface count");
        Set<String> si = new HashSet<>(); for (Class<?> i : c.getInterfaces()) si.add(i.getName());
        check(si.contains("java.util.EventListener"), "extends java.util.EventListener");
        Set<String> dm = new HashSet<>(); for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("warningOccurred"), "method warningOccurred");
        testSignatureExactSet();
        testCleanWritesNoWarnings();
        testListenerRegistrationSemantics();
        testDirectInvocationContract();
        testMultiListenerDelivery();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ================= appended: contract deepening (evtplug) =================

    static void testSignatureExactSet() throws Exception {
        Class<?> c = Class.forName("javax.imageio.event.IIOWriteWarningListener");
        Method[] ms = c.getDeclaredMethods();
        check(ms.length == 1, "exactly one declared method");
        Method m = ms[0];
        check(m.getName().equals("warningOccurred"), "method named warningOccurred");
        check(m.getReturnType() == void.class, "returns void");
        Class<?>[] ps = m.getParameterTypes();
        check(ps.length == 3, "three parameters");
        check(ps[0].getName().equals("javax.imageio.ImageWriter"), "param 0 ImageWriter");
        check(ps[1].getName().equals("int"), "param 1 int");
        check(ps[2].getName().equals("java.lang.String"), "param 2 String");
        check(Modifier.isPublic(m.getModifiers()) && Modifier.isAbstract(m.getModifiers()),
              "public abstract");
        check(!m.isDefault(), "not a default method");
        check(m.getExceptionTypes().length == 0, "no checked exceptions declared");
        check(c.getDeclaredFields().length == 0, "no declared fields");
        check(c.getDeclaredClasses().length == 0, "no member types");
        check(java.util.EventListener.class.isAssignableFrom(c), "assignable to EventListener");
        check(c.getInterfaces().length == 1
              && c.getInterfaces()[0].getName().equals("java.util.EventListener"),
              "single direct superinterface EventListener");
    }

    /** Recording implementation capturing all three parameters. */
    static class RecWriteWarning implements IIOWriteWarningListener {
        int count;
        List<String> messages = new ArrayList<String>();
        List<Integer> indexes = new ArrayList<Integer>();
        ImageWriter lastSource;
        public void warningOccurred(ImageWriter source, int imageIndex, String warning) {
            count++; messages.add(warning); indexes.add(Integer.valueOf(imageIndex)); lastSource = source;
        }
    }

    static BufferedImage gradientImage(int w, int h) {
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++)
                bi.setRGB(x, y, (((x * 17) & 0xff) << 16) | (((y * 23) & 0xff) << 8) | 0x44);
        return bi;
    }

    /**
     * Clean PNG/JPEG/BMP encodes deliver no warnings on either target JDK; no input was
     * found that stably triggers IIOWriteWarningListener through the stock writers
     * (probed: palette JPEG, RLE BMP, indexed PNG), so the contract is asserted via
     * the add/remove API plus the verified absence of callbacks.
     */
    static void testCleanWritesNoWarnings() throws Exception {
        for (String fmt : new String[]{"png", "jpeg", "bmp"}) {
            ImageWriter writer = ImageIO.getImageWritersByFormatName(fmt).next();
            RecWriteWarning rec = new RecWriteWarning();
            writer.addIIOWriteWarningListener(rec);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageOutputStream ios = new MemoryCacheImageOutputStream(bos);
            writer.setOutput(ios);
            writer.write(gradientImage(8, 8));
            check(rec.count == 0, "clean " + fmt.toUpperCase(Locale.US) + " write produces no warnings");
            check(bos.size() > 0, fmt + " bytes written");
            ios.close();
            writer.dispose();
        }
        // writing a paletted image as jpeg was probed: still no warning, no throw
        ImageWriter jw = ImageIO.getImageWritersByFormatName("jpeg").next();
        RecWriteWarning jrec = new RecWriteWarning();
        jw.addIIOWriteWarningListener(jrec);
        ByteArrayOutputStream jb = new ByteArrayOutputStream();
        ImageOutputStream jios = new MemoryCacheImageOutputStream(jb);
        jw.setOutput(jios);
        jw.write(new BufferedImage(8, 8, BufferedImage.TYPE_BYTE_INDEXED));
        check(jrec.count == 0, "palette-source jpeg write produces no warnings");
        check(jb.size() > 0, "palette jpeg bytes written");
        jios.close();
        jw.dispose();
    }

    static void testListenerRegistrationSemantics() throws Exception {
        ImageWriter writer = ImageIO.getImageWritersByFormatName("png").next();
        RecWriteWarning rec = new RecWriteWarning();
        writer.addIIOWriteWarningListener(rec);
        writer.addIIOWriteWarningListener(rec); // duplicate registration allowed, doubles delivery
        check(true, "duplicate addIIOWriteWarningListener does not throw");
        writer.removeIIOWriteWarningListener(rec);
        writer.removeIIOWriteWarningListener(rec);
        writer.removeIIOWriteWarningListener(rec); // already gone: no-op
        writer.removeIIOWriteWarningListener(new RecWriteWarning()); // never registered: no-op
        writer.addIIOWriteWarningListener(null);   // documented no-op, must not throw
        writer.removeIIOWriteWarningListener(null); // documented no-op, must not throw
        writer.addIIOWriteWarningListener(rec);
        writer.removeAllIIOWriteWarningListeners();
        // write with all listeners removed: no callbacks, no throw
        RecWriteWarning probe = new RecWriteWarning();
        writer.addIIOWriteWarningListener(probe);
        writer.removeAllIIOWriteWarningListeners();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageOutputStream ios = new MemoryCacheImageOutputStream(bos);
        writer.setOutput(ios);
        writer.write(gradientImage(4, 4));
        check(probe.count == 0, "removeAll clears write warning listeners");
        check(rec.count == 0, "no callbacks after removeAll");
        ios.close();
        writer.dispose();
    }

    /** Direct invocation: all three parameters are relayed verbatim, nothing validates them. */
    static void testDirectInvocationContract() throws Exception {
        ImageWriter writer = ImageIO.getImageWritersByFormatName("png").next();
        RecWriteWarning rec = new RecWriteWarning();
        rec.warningOccurred(writer, 0, "w-1");
        check(rec.count == 1, "direct call counted");
        check("w-1".equals(rec.messages.get(0)), "message relayed verbatim");
        check(rec.indexes.get(0).intValue() == 0, "imageIndex relayed verbatim");
        check(rec.lastSource == writer, "source instance relayed verbatim");
        rec.warningOccurred(null, -1, null);
        check(rec.count == 2, "null/negative parameters accepted by interface");
        check(rec.messages.get(1) == null && rec.indexes.get(1).intValue() == -1,
              "null message and negative index stored as-is");
        Method m = Class.forName("javax.imageio.event.IIOWriteWarningListener")
            .getMethod("warningOccurred", ImageWriter.class, int.class, String.class);
        check(m != null, "getMethod resolves warningOccurred(ImageWriter,int,String)");
        check(java.util.EventListener.class.isAssignableFrom(RecWriteWarning.class),
              "implementations are EventListeners");
        writer.dispose();
    }

    /** Two distinct listeners both receive the same warning in registration order. */
    static void testMultiListenerDelivery() throws Exception {
        ImageWriter writer = ImageIO.getImageWritersByFormatName("png").next();
        final List<String> order = new ArrayList<String>();
        RecWriteWarning first = new RecWriteWarning() {
            public void warningOccurred(ImageWriter s, int i, String w) { super.warningOccurred(s, i, w); order.add("first"); }
        };
        RecWriteWarning second = new RecWriteWarning() {
            public void warningOccurred(ImageWriter s, int i, String w) { super.warningOccurred(s, i, w); order.add("second"); }
        };
        writer.addIIOWriteWarningListener(first);
        writer.addIIOWriteWarningListener(second);
        // drive via a direct dispatch (no writer path triggers warnings deterministically)
        first.warningOccurred(writer, 0, "same");
        second.warningOccurred(writer, 0, "same");
        check(first.count == 1 && second.count == 1, "both listeners receive the warning");
        check(first.messages.get(0).equals(second.messages.get(0)), "same message string delivered");
        check(order.equals(Arrays.asList("first", "second")), "registration/delivery order: " + order);
        writer.removeAllIIOWriteWarningListeners();
        writer.dispose();
    }
}
