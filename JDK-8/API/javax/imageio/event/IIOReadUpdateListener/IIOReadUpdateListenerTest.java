/*
 * @test
 * @summary javax.imageio.event.IIOReadUpdateListener conformance: interface hierarchy and methods.
 * @run main IIOReadUpdateListenerTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.awt.image.BufferedImage;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.event.IIOReadUpdateListener;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;

public class IIOReadUpdateListenerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.imageio.event.IIOReadUpdateListener");
        check(c.isInterface(), "isInterface");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getInterfaces().length == 1, "direct interface count");
        Set<String> si = new HashSet<>(); for (Class<?> i : c.getInterfaces()) si.add(i.getName());
        check(si.contains("java.util.EventListener"), "extends java.util.EventListener");
        Set<String> dm = new HashSet<>(); for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("passStarted"), "method passStarted");
        check(dm.contains("imageUpdate"), "method imageUpdate");
        check(dm.contains("passComplete"), "method passComplete");
        check(dm.contains("thumbnailPassStarted"), "method thumbnailPassStarted");
        check(dm.contains("thumbnailUpdate"), "method thumbnailUpdate");
        check(dm.contains("thumbnailPassComplete"), "method thumbnailPassComplete");
        testSignatureExactSet();
        testNonInterlacedPngUpdateFlow();
        testInterlacedPngAdam7Passes();
        testProgressiveJpegScanPasses();
        testBaselineJpegSinglePass();
        testListenerRegistrationSemantics();
        testCallbackExceptionRelay();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ================= appended: contract deepening (evtplug) =================

    static void testSignatureExactSet() throws Exception {
        Class<?> c = Class.forName("javax.imageio.event.IIOReadUpdateListener");
        Method[] ms = c.getDeclaredMethods();
        check(ms.length == 6, "exactly six declared methods, got " + ms.length);
        String r = "javax.imageio.ImageReader";
        String b = "java.awt.image.BufferedImage";
        Set<String> want = new HashSet<String>(Arrays.asList(
            "passStarted(" + r + "," + b + ",int,int,int,int,int,int,int,int[])",
            "imageUpdate(" + r + "," + b + ",int,int,int,int,int,int,int[])",
            "passComplete(" + r + "," + b + ")",
            "thumbnailPassStarted(" + r + "," + b + ",int,int,int,int,int,int,int,int[])",
            "thumbnailUpdate(" + r + "," + b + ",int,int,int,int,int,int,int[])",
            "thumbnailPassComplete(" + r + "," + b + ")"));
        Set<String> got = new HashSet<String>();
        for (Method m : ms) {
            StringBuilder sb = new StringBuilder(m.getName()).append('(');
            Class<?>[] ps = m.getParameterTypes();
            for (int i = 0; i < ps.length; i++) {
                if (i > 0) sb.append(',');
                sb.append(ps[i].getName().equals("[I") ? "int[]" : ps[i].getName());
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
    }

    /** Recording implementation capturing all parameters of every callback. */
    static class RecReadUpdate implements IIOReadUpdateListener {
        int passStartedCnt, imageUpdateCnt, passCompleteCnt, thumbPassStartedCnt, thumbUpdateCnt, thumbPassCompleteCnt;
        ImageReader lastSource;
        BufferedImage passImage;
        List<int[]> passParams = new ArrayList<int[]>();       // pass,minPass,numPasses,minX,minY,periodX,periodY
        List<int[]> updateParams = new ArrayList<int[]>();      // minX,minY,w,h,periodX,periodY
        int nullBandsPasses, nullBandsUpdates;
        public void passStarted(ImageReader s, BufferedImage im, int pass, int minPass, int numPasses,
                                int minX, int minY, int periodX, int periodY, int[] bands) {
            passStartedCnt++; lastSource = s; passImage = im;
            passParams.add(new int[]{pass, minPass, numPasses, minX, minY, periodX, periodY});
            if (bands == null) nullBandsPasses++;
        }
        public void imageUpdate(ImageReader s, BufferedImage im, int minX, int minY,
                                int w, int h, int periodX, int periodY, int[] bands) {
            imageUpdateCnt++;
            updateParams.add(new int[]{minX, minY, w, h, periodX, periodY});
            if (bands == null) nullBandsUpdates++;
        }
        public void passComplete(ImageReader s, BufferedImage im) { passCompleteCnt++; }
        public void thumbnailPassStarted(ImageReader s, BufferedImage im, int pass, int minPass, int numPasses,
                                         int minX, int minY, int periodX, int periodY, int[] bands) { thumbPassStartedCnt++; }
        public void thumbnailUpdate(ImageReader s, BufferedImage im, int minX, int minY,
                                    int w, int h, int periodX, int periodY, int[] bands) { thumbUpdateCnt++; }
        public void thumbnailPassComplete(ImageReader s, BufferedImage im) { thumbPassCompleteCnt++; }
        void reset() { passStartedCnt = imageUpdateCnt = passCompleteCnt = 0;
            thumbPassStartedCnt = thumbUpdateCnt = thumbPassCompleteCnt = 0;
            passParams.clear(); updateParams.clear(); nullBandsPasses = nullBandsUpdates = 0; }
    }

    static byte[] encodePng(int w, int h, boolean interlaced) throws IOException {
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++)
                bi.setRGB(x, y, (((x * 29) & 0xff) << 16) | (((y * 37) & 0xff) << 8) | 0x55);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        if (interlaced) {
            ImageWriter wr = ImageIO.getImageWritersByFormatName("png").next();
            ImageWriteParam p = wr.getDefaultWriteParam();
            check(p.canWriteProgressive(), "PNG write param supports progressive");
            p.setProgressiveMode(ImageWriteParam.MODE_DEFAULT); // -> Adam7 interlaced
            ImageOutputStream ios = new MemoryCacheImageOutputStream(bos);
            wr.setOutput(ios);
            wr.write(null, new IIOImage(bi, null, null), p);
            ios.close(); wr.dispose();
        } else {
            ImageIO.write(bi, "png", bos);
        }
        return bos.toByteArray();
    }

    /** Non-interlaced PNG: exactly one pass (pass 0), row-by-row updates, param shapes legal. */
    static void testNonInterlacedPngUpdateFlow() throws Exception {
        byte[] png = encodePng(8, 8, false);
        ImageReader reader = ImageIO.getImageReadersByFormatName("png").next();
        RecReadUpdate rec = new RecReadUpdate();
        reader.addIIOReadUpdateListener(rec);
        ImageInputStream in = new MemoryCacheImageInputStream(new ByteArrayInputStream(png));
        reader.setInput(in);
        BufferedImage bi = reader.read(0, null);
        check(rec.passStartedCnt == 1, "single passStarted for non-interlaced PNG");
        int[] p = rec.passParams.get(0);
        check(p[0] == 0, "pass id 0 (non-interlaced decodes in a single pass)");
        check(p[1] == 0, "minimumPass 0");
        check(p[2] == 6, "maxPass reported as 6 (PNG reader default bound)");
        check(p[3] == 0 && p[4] == 0, "minX/minY 0");
        check(p[5] == 1 && p[6] == 1, "periodX/periodY 1");
        check(rec.nullBandsPasses == 1, "bands null for PNG passStarted");
        check(rec.passImage != null && rec.passImage.getWidth() == 8 && rec.passImage.getHeight() == 8,
              "passStarted delivers the destination image (8x8)");
        check(rec.lastSource == reader, "passStarted source is the reader");
        check(rec.imageUpdateCnt >= 1, "imageUpdate fired");
        boolean rowsOk = true;
        for (int[] u : rec.updateParams) {
            if (!(u[0] >= 0 && u[2] > 0 && u[0] + u[2] <= 8)) rowsOk = false;
            if (!(u[1] >= 0 && u[3] > 0 && u[1] + u[3] <= 8)) rowsOk = false;
            if (u[4] < 1 || u[5] < 1) rowsOk = false;
        }
        check(rowsOk, "all imageUpdate rectangles inside 8x8 with positive size and period");
        check(rec.nullBandsUpdates == rec.imageUpdateCnt, "bands null for every PNG imageUpdate");
        check(rec.passCompleteCnt == 1, "single passComplete");
        check(rec.thumbPassStartedCnt == 0 && rec.thumbUpdateCnt == 0 && rec.thumbPassCompleteCnt == 0,
              "no thumbnail updates for PNG");
        check(bi.getWidth() == 8 && bi.getHeight() == 8, "decoded dims 8x8");
        in.close();
        reader.removeAllIIOReadUpdateListeners();
        reader.dispose();
    }

    // Adam7 pass geometry observed on both target JDKs: {minX, minY, periodX, periodY} per pass 0..6
    static final int[][] ADAM7 = {
        {0, 0, 8, 8}, {4, 0, 8, 8}, {0, 4, 4, 8}, {2, 0, 4, 4},
        {0, 2, 2, 4}, {1, 0, 2, 2}, {0, 1, 1, 2}};

    /** Interlaced PNG (Adam7): seven passes 0..6 with exact per-pass geometry. */
    static void testInterlacedPngAdam7Passes() throws Exception {
        byte[] png = encodePng(16, 16, true);
        ImageReader reader = ImageIO.getImageReadersByFormatName("png").next();
        RecReadUpdate rec = new RecReadUpdate();
        reader.addIIOReadUpdateListener(rec);
        ImageInputStream in = new MemoryCacheImageInputStream(new ByteArrayInputStream(png));
        reader.setInput(in);
        BufferedImage bi = reader.read(0, null);
        check(rec.passStartedCnt == 7, "seven passStarted for Adam7 interlaced PNG (got " + rec.passStartedCnt + ")");
        check(rec.passCompleteCnt == 7, "seven passComplete");
        boolean orderOk = rec.passParams.size() == 7;
        for (int i = 0; i < 7 && orderOk; i++) {
            int[] p = rec.passParams.get(i);
            if (p[0] != i) orderOk = false;                    // pass index strictly 0..6
            if (p[1] != 0 || p[2] != 6) orderOk = false;       // minPass 0, maxPass 6
            if (p[3] != ADAM7[i][0] || p[4] != ADAM7[i][1]
                || p[5] != ADAM7[i][2] || p[6] != ADAM7[i][3]) orderOk = false;
        }
        check(orderOk, "each pass reports its Adam7 pass index, bounds 0..6, and exact pass geometry");
        check(rec.imageUpdateCnt >= 7, "imageUpdates fired across all passes");
        boolean shapeOk = true;
        for (int[] u : rec.updateParams) {
            if (!(u[0] >= 0 && u[2] > 0 && u[0] + u[2] <= 16)) shapeOk = false;
            if (!(u[1] >= 0 && u[3] > 0 && u[1] + u[3] <= 16)) shapeOk = false;
        }
        check(shapeOk, "all update rectangles inside 16x16");
        check(bi.getWidth() == 16 && bi.getHeight() == 16, "decoded dims 16x16");
        boolean lossless = true;
        for (int y = 0; y < 16 && lossless; y++)
            for (int x = 0; x < 16 && lossless; x++)
                if (bi.getRGB(x, y) != (0xff000000 | (((x * 29) & 0xff) << 16) | (((y * 37) & 0xff) << 8) | 0x55)) lossless = false;
        check(lossless, "interlaced PNG round-trip is pixel-exact");
        in.close();
        reader.removeAllIIOReadUpdateListeners();
        reader.dispose();
    }

    static byte[] encodeJpeg(int w, int h, boolean progressive) throws IOException {
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++)
                bi.setRGB(x, y, (((x * 31) & 0xff) << 16) | (((y * 41) & 0xff) << 8) | 0x33);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        if (progressive) {
            ImageWriter wr = ImageIO.getImageWritersByFormatName("jpeg").next();
            JPEGImageWriteParam p = new JPEGImageWriteParam(java.util.Locale.US);
            p.setProgressiveMode(ImageWriteParam.MODE_DEFAULT);
            ImageOutputStream ios = new MemoryCacheImageOutputStream(bos);
            wr.setOutput(ios);
            wr.write(null, new IIOImage(bi, null, null), p);
            ios.close(); wr.dispose();
        } else {
            ImageIO.write(bi, "jpeg", bos);
        }
        return bos.toByteArray();
    }

    /** Progressive JPEG: multiple scan passes, strictly increasing, numPasses unknown (MAX_VALUE). */
    static void testProgressiveJpegScanPasses() throws Exception {
        byte[] jpg = encodeJpeg(16, 16, true);
        ImageReader reader = ImageIO.getImageReadersByFormatName("jpeg").next();
        RecReadUpdate rec = new RecReadUpdate();
        reader.addIIOReadUpdateListener(rec);
        ImageInputStream in = new MemoryCacheImageInputStream(new ByteArrayInputStream(jpg));
        reader.setInput(in);
        BufferedImage bi = reader.read(0, null);
        // libjpeg default progressive script = 10 scans on both target JDKs; keep >= 5 for safety
        check(rec.passStartedCnt >= 5, "multiple scan passes for progressive JPEG (got " + rec.passStartedCnt + ")");
        boolean seqOk = true;
        for (int i = 0; i < rec.passParams.size() && seqOk; i++) {
            int[] p = rec.passParams.get(i);
            if (p[0] != i) seqOk = false;                        // contiguous, ascending
            if (p[1] != 0) seqOk = false;                        // minimumPass 0
            if (p[2] != Integer.MAX_VALUE) seqOk = false;        // pass count unknown while decoding
        }
        check(seqOk, "progressive passes ascend from 0 with minimumPass=0 and numPasses=MAX_VALUE");
        check(rec.imageUpdateCnt >= rec.passStartedCnt, "pixel updates accompany each scan");
        check(rec.passCompleteCnt == rec.passStartedCnt, "every pass completes");
        check(bi != null && bi.getWidth() == 16 && bi.getHeight() == 16, "progressive JPEG dims round-trip");
        in.close();
        reader.removeAllIIOReadUpdateListeners();
        reader.dispose();
    }

    /** Baseline JPEG: exactly one pass with pass id 0. */
    static void testBaselineJpegSinglePass() throws Exception {
        byte[] jpg = encodeJpeg(16, 16, false);
        ImageReader reader = ImageIO.getImageReadersByFormatName("jpeg").next();
        RecReadUpdate rec = new RecReadUpdate();
        reader.addIIOReadUpdateListener(rec);
        ImageInputStream in = new MemoryCacheImageInputStream(new ByteArrayInputStream(jpg));
        reader.setInput(in);
        reader.read(0, null);
        check(rec.passStartedCnt == 1, "baseline JPEG reports a single pass");
        check(rec.passParams.get(0)[0] == 0, "baseline pass id 0");
        check(rec.passCompleteCnt == 1, "baseline single passComplete");
        in.close();
        reader.removeAllIIOReadUpdateListeners();
        reader.dispose();
    }

    static void testListenerRegistrationSemantics() throws Exception {
        byte[] png = encodePng(4, 4, false);
        ImageReader reader = ImageIO.getImageReadersByFormatName("png").next();
        RecReadUpdate rec = new RecReadUpdate();
        reader.addIIOReadUpdateListener(rec);
        reader.addIIOReadUpdateListener(rec); // duplicate registration -> two occurrences
        ImageInputStream in = new MemoryCacheImageInputStream(new ByteArrayInputStream(png));
        reader.setInput(in);
        reader.read(0, null);
        check(rec.passStartedCnt == 2, "duplicate add: passStarted fired twice");
        reader.removeIIOReadUpdateListener(rec);
        rec.reset();
        ImageInputStream in2 = new MemoryCacheImageInputStream(new ByteArrayInputStream(png));
        reader.setInput(in2);
        reader.read(0, null);
        check(rec.passStartedCnt == 1, "one remove: single callback again");
        reader.removeIIOReadUpdateListener(rec);
        reader.removeIIOReadUpdateListener(rec);          // already gone: no-op
        reader.removeIIOReadUpdateListener(new RecReadUpdate()); // never registered: no-op
        rec.reset();
        ImageInputStream in3 = new MemoryCacheImageInputStream(new ByteArrayInputStream(png));
        reader.setInput(in3);
        reader.read(0, null);
        check(rec.passStartedCnt == 0 && rec.imageUpdateCnt == 0, "detached listener gets no callbacks");
        reader.addIIOReadUpdateListener(null);   // documented no-op
        reader.removeIIOReadUpdateListener(null); // documented no-op
        reader.addIIOReadUpdateListener(rec);
        reader.removeAllIIOReadUpdateListeners();
        rec.reset();
        ImageInputStream in4 = new MemoryCacheImageInputStream(new ByteArrayInputStream(png));
        reader.setInput(in4);
        reader.read(0, null);
        check(rec.passStartedCnt == 0, "removeAll clears update listeners");
        in.close(); in2.close(); in3.close(); in4.close();
        reader.dispose();
    }

    /** A callback exception surfaces wrapped in IIOException with the original as cause. */
    static void testCallbackExceptionRelay() throws Exception {
        byte[] png = encodePng(4, 4, false);
        final RuntimeException sentinel = new RuntimeException("evtplug-sentinel");
        ImageReader reader = ImageIO.getImageReadersByFormatName("png").next();
        reader.addIIOReadUpdateListener(new RecReadUpdate() {
            public void passStarted(ImageReader s, BufferedImage im, int pass, int minPass, int numPasses,
                                    int minX, int minY, int periodX, int periodY, int[] bands) { throw sentinel; }
        });
        ImageInputStream in = new MemoryCacheImageInputStream(new ByteArrayInputStream(png));
        reader.setInput(in);
        try {
            reader.read(0, null);
            check(false, "exception from passStarted must propagate");
        } catch (javax.imageio.IIOException e) {
            check(e.getCause() == sentinel, "IIOException cause is the original callback exception");
        }
        in.close();
        reader.removeAllIIOReadUpdateListeners();
        reader.dispose();
    }
}
