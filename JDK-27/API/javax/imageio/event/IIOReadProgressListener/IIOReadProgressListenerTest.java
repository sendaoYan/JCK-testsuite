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
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.event.IIOReadProgressListener;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
        testSignatureExactSet();
        testReadAllEndToEnd();
        testSingleReadNoSequenceEvents();
        testListenerRegistrationSemantics();
        testCallbackExceptionRelay();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ================= appended: contract deepening (evtplug) =================
    // ================= appended: contract deepening (evtplug) =================

    /** Exact method-set / signature verification against javap-diffed reference. */
    static void testSignatureExactSet() throws Exception {
        Class<?> c = Class.forName("javax.imageio.event.IIOReadProgressListener");
        Method[] ms = c.getDeclaredMethods();
        check(ms.length == 9, "exactly nine declared methods, got " + ms.length);
        String r = "javax.imageio.ImageReader";
        Set<String> want = new HashSet<String>(Arrays.asList(
            "sequenceStarted(" + r + ",int)",
            "sequenceComplete(" + r + ")",
            "imageStarted(" + r + ",int)",
            "imageProgress(" + r + ",float)",
            "imageComplete(" + r + ")",
            "thumbnailStarted(" + r + ",int,int)",
            "thumbnailProgress(" + r + ",float)",
            "thumbnailComplete(" + r + ")",
            "readAborted(" + r + ")"));
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
            check(Modifier.isPublic(m.getModifiers()), m.getName() + " public");
            check(Modifier.isAbstract(m.getModifiers()), m.getName() + " abstract");
            check(!m.isDefault(), m.getName() + " not default");
            check(m.getExceptionTypes().length == 0, m.getName() + " declares no checked exceptions");
        }
        check(got.equals(want), "exact signature set matches javap reference (missing=" + diff(want, got) + ")");
        check(c.getDeclaredFields().length == 0, "no declared fields");
        check(c.getDeclaredClasses().length == 0, "no member types");
    }

    static String diff(Set<String> want, Set<String> got) {
        Set<String> missing = new HashSet<String>(want); missing.removeAll(got);
        Set<String> extra = new HashSet<String>(got); extra.removeAll(want);
        return "missing=" + missing + " extra=" + extra;
    }

    /** Recording implementation: counts every callback and captures all parameters. */
    static class RecReadProgress implements IIOReadProgressListener {
        int seqStarted, seqComplete, imageStarted, imageProgressCnt, imageComplete;
        int thumbStarted, thumbProgressCnt, thumbComplete, readAborted;
        ImageReader seqSource; int seqMinIndex = -1;
        ImageReader imgSource; List<Integer> imageIndexes = new ArrayList<Integer>();
        List<Float> percentages = new ArrayList<Float>();
        boolean monotonic = true; private float last = Float.NaN;
        public void sequenceStarted(ImageReader s, int minIndex) { seqStarted++; seqSource = s; seqMinIndex = minIndex; }
        public void sequenceComplete(ImageReader s) { seqComplete++; }
        public void imageStarted(ImageReader s, int index) { imageStarted++; imgSource = s; imageIndexes.add(Integer.valueOf(index)); }
        public void imageProgress(ImageReader s, float percentageDone) {
            imageProgressCnt++; percentages.add(Float.valueOf(percentageDone));
            if (!Float.isNaN(last) && percentageDone < last) monotonic = false;
            last = percentageDone;
        }
        public void imageComplete(ImageReader s) { imageComplete++; }
        public void thumbnailStarted(ImageReader s, int i, int t) { thumbStarted++; }
        public void thumbnailProgress(ImageReader s, float p) { thumbProgressCnt++; }
        public void thumbnailComplete(ImageReader s) { thumbComplete++; }
        public void readAborted(ImageReader s) { readAborted++; }
        void reset() { seqStarted = seqComplete = imageStarted = imageProgressCnt = imageComplete = 0;
            thumbStarted = thumbProgressCnt = thumbComplete = readAborted = 0;
            percentages.clear(); monotonic = true; last = Float.NaN; }
    }

    static byte[] encodePng(int w, int h) throws IOException {
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++)
                bi.setRGB(x, y, (((x * 31) & 0xff) << 16) | (((y * 41) & 0xff) << 8) | 0x55);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        if (!ImageIO.write(bi, "png", bos)) throw new AssertionError("png writer missing");
        return bos.toByteArray();
    }

    /** End-to-end event flow driven by the real PNG codec through readAll(). */
    static void testReadAllEndToEnd() throws Exception {
        byte[] png = encodePng(8, 8);
        ImageWriter writer = ImageIO.getImageWritersByFormatName("png").next();
        ImageReader reader = ImageIO.getImageReader(writer);
        check(reader != null, "getImageReader(writer) non-null");
        RecReadProgress rec = new RecReadProgress();
        reader.addIIOReadProgressListener(rec);
        ImageInputStream in = new MemoryCacheImageInputStream(new ByteArrayInputStream(png));
        reader.setInput(in);
        List<IIOImage> out = new ArrayList<IIOImage>();
        Iterator<IIOImage> it = reader.readAll(Collections.<ImageReadParam>emptyList().iterator());
        while (it.hasNext()) out.add(it.next());
        check(rec.seqStarted == 1, "readAll fires sequenceStarted exactly once");
        check(rec.seqSource == reader, "sequenceStarted source is the reader");
        check(rec.seqMinIndex == 0, "sequenceStarted minIndex 0");
        check(rec.imageStarted == 1, "imageStarted fired once");
        check(rec.imgSource == reader, "imageStarted source is the reader");
        check(rec.imageIndexes.equals(Arrays.asList(Integer.valueOf(0))), "imageStarted imageIndex 0");
        check(rec.imageProgressCnt >= 1, "imageProgress fired at least once (got " + rec.imageProgressCnt + ")");
        check(rec.monotonic, "imageProgress percentages monotonic non-decreasing");
        for (float p : rec.percentages) {
            check(p >= 0.0f && p <= 100.0f, "percentage in [0,100]: " + p);
        }
        check(rec.percentages.get(rec.percentages.size() - 1).floatValue() == 100.0f,
              "final percentage is 100.0");
        check(rec.imageComplete == 1, "imageComplete fired once");
        check(rec.seqComplete == 1, "sequenceComplete fired exactly once");
        check(rec.thumbStarted == 0 && rec.thumbProgressCnt == 0 && rec.thumbComplete == 0,
              "no thumbnail events for PNG");
        check(rec.readAborted == 0, "no readAborted");
        check(out.size() == 1, "readAll yields exactly one image");
        BufferedImage bi = (BufferedImage) out.get(0).getRenderedImage();
        check(bi.getWidth() == 8 && bi.getHeight() == 8, "decoded dims 8x8");
        // read before removeAll: detach, re-read, assert zero callbacks
        reader.removeIIOReadProgressListener(rec);
        rec.reset();
        ImageInputStream in2 = new MemoryCacheImageInputStream(new ByteArrayInputStream(png));
        reader.setInput(in2);
        reader.read(0, null);
        check(rec.seqStarted == 0 && rec.imageStarted == 0 && rec.imageProgressCnt == 0
              && rec.imageComplete == 0 && rec.seqComplete == 0,
              "no callbacks after listener removed");
        in.close(); in2.close();
        reader.removeAllIIOReadProgressListeners();
        reader.dispose();
    }

    /** Plain read() must not fire sequence events (single-image protocol). */
    static void testSingleReadNoSequenceEvents() throws Exception {
        byte[] png = encodePng(4, 4);
        ImageReader reader = ImageIO.getImageReadersByFormatName("png").next();
        RecReadProgress rec = new RecReadProgress();
        reader.addIIOReadProgressListener(rec);
        ImageInputStream in = new MemoryCacheImageInputStream(new ByteArrayInputStream(png));
        reader.setInput(in);
        BufferedImage bi = reader.read(0, null);
        check(rec.seqStarted == 0, "read() does not fire sequenceStarted");
        check(rec.seqComplete == 0, "read() does not fire sequenceComplete");
        check(rec.imageStarted == 1, "read() fires imageStarted once");
        check(rec.imageProgressCnt >= 1, "read() fires imageProgress");
        check(rec.monotonic, "read() percentages monotonic");
        check(rec.imageComplete == 1, "read() fires imageComplete once");
        check(bi != null && bi.getWidth() == 4 && bi.getHeight() == 4, "decoded dims 4x4");
        in.close();
        reader.removeAllIIOReadProgressListeners();
        reader.dispose();
    }

    /** add/remove contract: duplicate add doubles callbacks, removal is per-occurrence. */
    static void testListenerRegistrationSemantics() throws Exception {
        byte[] png = encodePng(4, 4);
        ImageReader reader = ImageIO.getImageReadersByFormatName("png").next();
        RecReadProgress rec = new RecReadProgress();
        reader.addIIOReadProgressListener(rec);
        reader.addIIOReadProgressListener(rec); // same instance twice -> two occurrences
        ImageInputStream in = new MemoryCacheImageInputStream(new ByteArrayInputStream(png));
        reader.setInput(in);
        reader.read(0, null);
        check(rec.imageStarted == 2, "duplicate add: imageStarted fired twice");
        check(rec.imageComplete == 2, "duplicate add: imageComplete fired twice");
        reader.removeIIOReadProgressListener(rec); // removes one occurrence
        rec.reset();
        ImageInputStream in2 = new MemoryCacheImageInputStream(new ByteArrayInputStream(png));
        reader.setInput(in2);
        reader.read(0, null);
        check(rec.imageStarted == 1, "after one remove: single callback again");
        reader.removeIIOReadProgressListener(rec); // second occurrence
        reader.removeIIOReadProgressListener(rec); // now a no-op, must not throw
        reader.removeIIOReadProgressListener(new RecReadProgress()); // never registered: no-op
        rec.reset();
        ImageInputStream in3 = new MemoryCacheImageInputStream(new ByteArrayInputStream(png));
        reader.setInput(in3);
        reader.read(0, null);
        check(rec.imageStarted == 0 && rec.imageComplete == 0, "fully removed: zero callbacks");
        reader.addIIOReadProgressListener(null); // null add: documented no-op
        reader.removeIIOReadProgressListener(null); // null remove: documented no-op
        reader.addIIOReadProgressListener(rec);
        reader.removeAllIIOReadProgressListeners();
        rec.reset();
        ImageInputStream in4 = new MemoryCacheImageInputStream(new ByteArrayInputStream(png));
        reader.setInput(in4);
        reader.read(0, null);
        check(rec.imageStarted == 0 && rec.imageProgressCnt == 0 && rec.imageComplete == 0,
              "removeAll: zero callbacks afterwards");
        in.close(); in2.close(); in3.close(); in4.close();
        reader.dispose();
    }

    /**
     * Exception relay: a RuntimeException thrown from a callback is caught by the
     * PNG reader and rethrown wrapped in IIOException with the original as cause.
     * (Observed behavior; javadoc is silent.)
     */
    static void testCallbackExceptionRelay() throws Exception {
        byte[] png = encodePng(4, 4);
        final RuntimeException sentinel = new RuntimeException("evtplug-sentinel");
        ImageReader reader = ImageIO.getImageReadersByFormatName("png").next();
        reader.addIIOReadProgressListener(new RecReadProgress() {
            public void imageProgress(ImageReader s, float p) { throw sentinel; }
        });
        ImageInputStream in = new MemoryCacheImageInputStream(new ByteArrayInputStream(png));
        reader.setInput(in);
        try {
            reader.read(0, null);
            check(false, "exception from imageProgress must propagate out of read()");
        } catch (javax.imageio.IIOException e) {
            check(e.getCause() == sentinel, "IIOException wraps the original callback exception");
        }
        in.close();
        reader.removeAllIIOReadProgressListeners();
        reader.dispose();
    }
}
