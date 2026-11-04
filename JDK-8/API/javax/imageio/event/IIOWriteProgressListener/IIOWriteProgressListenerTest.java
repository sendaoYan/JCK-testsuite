/*
 * @test
 * @summary javax.imageio.event.IIOWriteProgressListener conformance: interface hierarchy and methods.
 * @run main IIOWriteProgressListenerTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.event.IIOWriteProgressListener;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;

public class IIOWriteProgressListenerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.imageio.event.IIOWriteProgressListener");
        check(c.isInterface(), "isInterface");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getInterfaces().length == 1, "direct interface count");
        Set<String> si = new HashSet<>(); for (Class<?> i : c.getInterfaces()) si.add(i.getName());
        check(si.contains("java.util.EventListener"), "extends java.util.EventListener");
        Set<String> dm = new HashSet<>(); for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("imageStarted"), "method imageStarted");
        check(dm.contains("imageProgress"), "method imageProgress");
        check(dm.contains("imageComplete"), "method imageComplete");
        check(dm.contains("thumbnailStarted"), "method thumbnailStarted");
        check(dm.contains("thumbnailProgress"), "method thumbnailProgress");
        check(dm.contains("thumbnailComplete"), "method thumbnailComplete");
        check(dm.contains("writeAborted"), "method writeAborted");
        testSignatureExactSet();
        testPngWriteEndToEnd();
        testJpegWriteEndToEnd();
        testListenerRegistrationSemantics();
        testCallbackExceptionRelay();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ================= appended: contract deepening (evtplug) =================

    static void testSignatureExactSet() throws Exception {
        Class<?> c = Class.forName("javax.imageio.event.IIOWriteProgressListener");
        Method[] ms = c.getDeclaredMethods();
        check(ms.length == 7, "exactly seven declared methods, got " + ms.length);
        String w = "javax.imageio.ImageWriter";
        Set<String> want = new HashSet<String>(Arrays.asList(
            "imageStarted(" + w + ",int)",
            "imageProgress(" + w + ",float)",
            "imageComplete(" + w + ")",
            "thumbnailStarted(" + w + ",int,int)",
            "thumbnailProgress(" + w + ",float)",
            "thumbnailComplete(" + w + ")",
            "writeAborted(" + w + ")"));
        Set<String> got = new HashSet<String>();
        for (Method m : ms) {
            StringBuilder sb = new StringBuilder(m.getName()).append('(');
            Class<?>[] ps = m.getParameterTypes();
            for (int i = 0; i < ps.length; i++) {
                if (i > 0) sb.append(',');
                sb.append(ps[i].getName());
            }
            sb.append(')');
            got.add(sb.toString());
            check(m.getReturnType() == void.class, m.getName() + " returns void");
            check(Modifier.isPublic(m.getModifiers()) && Modifier.isAbstract(m.getModifiers()),
                  m.getName() + " public abstract");
            check(!m.isDefault(), m.getName() + " not default");
            check(m.getExceptionTypes().length == 0, m.getName() + " declares no checked exceptions");
        }
        Set<String> missing = new HashSet<String>(want); missing.removeAll(got);
        Set<String> extra = new HashSet<String>(got); extra.removeAll(want);
        check(missing.isEmpty() && extra.isEmpty(), "exact signature set (missing=" + missing + " extra=" + extra + ")");
        check(c.getDeclaredFields().length == 0, "no declared fields");
        check(c.getDeclaredClasses().length == 0, "no member types");
    }

    /** Recording implementation: counts and captures all parameters. */
    static class RecWriteProgress implements IIOWriteProgressListener {
        int imageStartedCnt, imageProgressCnt, imageCompleteCnt;
        int thumbStartedCnt, thumbProgressCnt, thumbCompleteCnt, writeAbortedCnt;
        ImageWriter imgSource; List<Integer> imageIndexes = new ArrayList<Integer>();
        List<Float> percentages = new ArrayList<Float>();
        boolean monotonic = true; private float last = Float.NaN;
        public void imageStarted(ImageWriter s, int imageIndex) { imageStartedCnt++; imgSource = s; imageIndexes.add(Integer.valueOf(imageIndex)); }
        public void imageProgress(ImageWriter s, float percentageDone) {
            imageProgressCnt++; percentages.add(Float.valueOf(percentageDone));
            if (!Float.isNaN(last) && percentageDone < last) monotonic = false;
            last = percentageDone;
        }
        public void imageComplete(ImageWriter s) { imageCompleteCnt++; }
        public void thumbnailStarted(ImageWriter s, int i, int t) { thumbStartedCnt++; }
        public void thumbnailProgress(ImageWriter s, float p) { thumbProgressCnt++; }
        public void thumbnailComplete(ImageWriter s) { thumbCompleteCnt++; }
        public void writeAborted(ImageWriter s) { writeAbortedCnt++; }
        void reset() { imageStartedCnt = imageProgressCnt = imageCompleteCnt = 0;
            thumbStartedCnt = thumbProgressCnt = thumbCompleteCnt = writeAbortedCnt = 0;
            percentages.clear(); monotonic = true; last = Float.NaN; }
    }

    static BufferedImage gradientImage(int w, int h) {
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++)
                bi.setRGB(x, y, (((x * 17) & 0xff) << 16) | (((y * 23) & 0xff) << 8) | 0x44);
        return bi;
    }

    /** PNG encode driven end-to-end: started(0), many monotonic progress updates ending at 100, complete. */
    static void testPngWriteEndToEnd() throws Exception {
        ImageReader pngReader = ImageIO.getImageReadersByFormatName("png").next();
        ImageWriter writer = ImageIO.getImageWriter(pngReader); // getImageWriter pairing path
        check(writer != null, "getImageWriter(reader) yields a PNG writer");
        RecWriteProgress rec = new RecWriteProgress();
        writer.addIIOWriteProgressListener(rec);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageOutputStream ios = new MemoryCacheImageOutputStream(bos);
        writer.setOutput(ios);
        writer.write(gradientImage(16, 16));
        check(rec.imageStartedCnt == 1, "imageStarted fired exactly once");
        check(rec.imgSource == writer, "imageStarted source is the writer");
        check(rec.imageIndexes.equals(Arrays.asList(Integer.valueOf(0))), "imageStarted imageIndex 0");
        check(rec.imageProgressCnt >= 1, "imageProgress fired at least once (got " + rec.imageProgressCnt + ")");
        check(rec.monotonic, "percentages monotonic non-decreasing");
        boolean rangeOk = true;
        for (float p : rec.percentages) if (p < 0.0f || p > 100.0f) rangeOk = false;
        check(rangeOk, "all percentages within [0,100]");
        check(rec.percentages.get(rec.percentages.size() - 1).floatValue() == 100.0f,
              "PNG encode reaches 100.0 on completion");
        check(rec.imageCompleteCnt == 1, "imageComplete fired exactly once");
        check(rec.thumbStartedCnt == 0 && rec.thumbProgressCnt == 0 && rec.thumbCompleteCnt == 0,
              "no thumbnail events");
        check(rec.writeAbortedCnt == 0, "no writeAborted for a plain write");
        check(bos.size() > 0, "bytes produced");
        ios.close();
        writer.removeAllIIOWriteProgressListeners();
        writer.dispose(); pngReader.dispose();
    }

    /** JPEG encode: same contract, except the last percentage need not be 100 (verified <100 on both JDKs). */
    static void testJpegWriteEndToEnd() throws Exception {
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
        RecWriteProgress rec = new RecWriteProgress();
        writer.addIIOWriteProgressListener(rec);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageOutputStream ios = new MemoryCacheImageOutputStream(bos);
        writer.setOutput(ios);
        writer.write(gradientImage(16, 16));
        check(rec.imageStartedCnt == 1 && rec.imageIndexes.get(0).intValue() == 0,
              "jpeg imageStarted(0) once");
        check(rec.imageProgressCnt >= 1, "jpeg imageProgress fired");
        check(rec.monotonic, "jpeg percentages monotonic");
        check(rec.imageCompleteCnt == 1, "jpeg imageComplete once");
        check(bos.size() > 0, "jpeg bytes produced");
        ios.close();
        writer.removeAllIIOWriteProgressListeners();
        writer.dispose();
    }

    static void testListenerRegistrationSemantics() throws Exception {
        ImageWriter writer = ImageIO.getImageWritersByFormatName("png").next();
        RecWriteProgress rec = new RecWriteProgress();
        writer.addIIOWriteProgressListener(rec);
        writer.addIIOWriteProgressListener(rec); // duplicate registration -> two occurrences
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageOutputStream ios = new MemoryCacheImageOutputStream(bos);
        writer.setOutput(ios);
        writer.write(gradientImage(8, 8));
        check(rec.imageStartedCnt == 2, "duplicate add: imageStarted twice");
        check(rec.imageCompleteCnt == 2, "duplicate add: imageComplete twice");
        writer.removeIIOWriteProgressListener(rec);
        rec.reset();
        ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
        ImageOutputStream ios2 = new MemoryCacheImageOutputStream(bos2);
        writer.setOutput(ios2);
        writer.write(gradientImage(8, 8));
        check(rec.imageStartedCnt == 1, "one remove: single callback again");
        writer.removeIIOWriteProgressListener(rec);
        writer.removeIIOWriteProgressListener(rec);                            // no-op
        writer.removeIIOWriteProgressListener(new RecWriteProgress());         // never registered: no-op
        rec.reset();
        ByteArrayOutputStream bos3 = new ByteArrayOutputStream();
        ImageOutputStream ios3 = new MemoryCacheImageOutputStream(bos3);
        writer.setOutput(ios3);
        writer.write(gradientImage(8, 8));
        check(rec.imageStartedCnt == 0 && rec.imageProgressCnt == 0 && rec.imageCompleteCnt == 0,
              "detached listener receives no callbacks");
        writer.addIIOWriteProgressListener(null);   // documented no-op
        writer.removeIIOWriteProgressListener(null); // documented no-op
        writer.addIIOWriteProgressListener(rec);
        writer.removeAllIIOWriteProgressListeners();
        rec.reset();
        ByteArrayOutputStream bos4 = new ByteArrayOutputStream();
        ImageOutputStream ios4 = new MemoryCacheImageOutputStream(bos4);
        writer.setOutput(ios4);
        writer.write(gradientImage(8, 8));
        check(rec.imageStartedCnt == 0, "removeAll clears write progress listeners");
        ios.close(); ios2.close(); ios3.close(); ios4.close();
        writer.dispose();
    }

    /**
     * Exception relay on the write side: unlike the read path, a RuntimeException from a
     * callback propagates out of ImageWriter.write() unwrapped, as the very same instance.
     */
    static void testCallbackExceptionRelay() throws Exception {
        final RuntimeException sentinel = new RuntimeException("evtplug-sentinel");
        ImageWriter writer = ImageIO.getImageWritersByFormatName("png").next();
        writer.addIIOWriteProgressListener(new RecWriteProgress() {
            public void imageProgress(ImageWriter s, float p) { throw sentinel; }
        });
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageOutputStream ios = new MemoryCacheImageOutputStream(bos);
        writer.setOutput(ios);
        try {
            writer.write(gradientImage(8, 8));
            check(false, "exception from imageProgress must propagate out of write()");
        } catch (RuntimeException e) {
            check(e == sentinel, "write() rethrows the original RuntimeException instance");
        }
        ios.close();
        writer.removeAllIIOWriteProgressListeners();
        writer.dispose();
    }
}
