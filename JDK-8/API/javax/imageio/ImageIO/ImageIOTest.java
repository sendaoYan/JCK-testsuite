/*
 * @test
 * @summary javax.imageio.ImageIO conformance: behavioral.
 * @run main ImageIOTest
 */

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageTranscoder;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileCacheImageInputStream;
import javax.imageio.stream.FileCacheImageOutputStream;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;

public class ImageIOTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface Act { void run() throws Exception; }
    static void expectExact(Class<? extends Throwable> expected, Act a, String msg) {
        try {
            a.run();
            check(false, msg + ": expected " + expected.getSimpleName() + " but nothing thrown");
        } catch (Throwable t) {
            check(t.getClass() == expected,
                  msg + ": expected " + expected.getSimpleName() + " but got " + t.getClass().getName());
        }
    }
    static int tmpCounter = 0;
    static File tmpFile(String ext) {
        return new File(System.getProperty("java.io.tmpdir"),
                        "imgio-corea-ImageIO-" + (tmpCounter++) + ext);
    }
    static BufferedImage pattern(int w, int h, int type) {
        BufferedImage bi = new BufferedImage(w, h, type);
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++)
                bi.setRGB(x, y, 0xFF000000 | (((x * 40 + 13) & 0xFF) << 16)
                          | (((y * 40 + 77) & 0xFF) << 8) | (((x + y) * 23 + 5) & 0xFF));
        return bi;
    }
    static boolean pixelsEqual(BufferedImage a, BufferedImage b) {
        if (a.getWidth() != b.getWidth() || a.getHeight() != b.getHeight()) return false;
        for (int y = 0; y < a.getHeight(); y++)
            for (int x = 0; x < a.getWidth(); x++)
                if (a.getRGB(x, y) != b.getRGB(x, y)) return false;
        return true;
    }
    static byte[] encode(int type, String format) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        if (!ImageIO.write(pattern(5, 4, type), format, bos))
            return null;
        return bos.toByteArray();
    }

    // legacy assertions of the original file (kept verbatim in semantics)
    static void testLegacy() throws Exception {
        String[] rn = javax.imageio.ImageIO.getReaderFormatNames();
        check(rn != null && rn.length > 0, "getReaderFormatNames non-empty");
        String[] wm = javax.imageio.ImageIO.getWriterFormatNames();
        check(wm != null && wm.length > 0, "getWriterFormatNames non-empty");
        check(javax.imageio.ImageIO.getReaderMIMETypes() != null, "getReaderMIMETypes");
        check(javax.imageio.ImageIO.getWriterMIMETypes() != null, "getWriterMIMETypes");
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(4, 4, java.awt.image.BufferedImage.TYPE_INT_RGB);
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        check(javax.imageio.ImageIO.write(img, "png", bos), "write png");
        java.awt.image.BufferedImage back = javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(bos.toByteArray()));
        check(back != null && back.getWidth() == 4 && back.getHeight() == 4, "read png");
        javax.imageio.stream.ImageInputStream iis = javax.imageio.ImageIO.createImageInputStream(new java.io.ByteArrayInputStream(bos.toByteArray()));
        check(iis != null, "createImageInputStream");
        iis.close();
        javax.imageio.stream.ImageOutputStream ios = javax.imageio.ImageIO.createImageOutputStream(new java.io.ByteArrayOutputStream());
        check(ios != null, "createImageOutputStream");
        ios.close();
    }

    // ---- deep: plugin inventory and case sensitivity ----
    static void testFormatInventory() {
        String[] rn = ImageIO.getReaderFormatNames();
        check(Arrays.asList(rn).contains("png"), "reader format names contain lowercase png");
        check(Arrays.asList(rn).contains("PNG"), "reader format names contain uppercase PNG");
        check(Arrays.asList(rn).contains("jpeg"), "reader format names contain jpeg");
        check(Arrays.asList(rn).contains("bmp"), "reader format names contain bmp");
        check(Arrays.asList(rn).contains("gif"), "reader format names contain gif");
        String[] wn = ImageIO.getWriterFormatNames();
        check(Arrays.asList(wn).contains("png"), "writer format names contain png");
        check(Arrays.asList(wn).contains("PNG"), "writer format names contain PNG");
        check(Arrays.asList(wn).contains("jpeg"), "writer format names contain jpeg");
        check(Arrays.asList(wn).contains("bmp"), "writer format names contain bmp");
        check(Arrays.asList(wn).contains("gif"), "writer format names contain gif");
        String[] rm = ImageIO.getReaderMIMETypes();
        check(Arrays.asList(rm).contains("image/png"), "reader MIME contains image/png");
        check(Arrays.asList(rm).contains("image/jpeg"), "reader MIME contains image/jpeg");
        check(Arrays.asList(rm).contains("image/gif"), "reader MIME contains image/gif");
        check(Arrays.asList(rm).contains("image/bmp"), "reader MIME contains image/bmp");
        String[] wm = ImageIO.getWriterMIMETypes();
        check(Arrays.asList(wm).contains("image/png"), "writer MIME contains image/png");
        check(Arrays.asList(wm).contains("image/gif"), "writer MIME contains image/gif");
        String[] rs = ImageIO.getReaderFileSuffixes();
        check(Arrays.asList(rs).contains("png"), "reader suffixes contain png");
        check(Arrays.asList(rs).contains("jpg"), "reader suffixes contain jpg");
        check(Arrays.asList(ImageIO.getWriterFileSuffixes()).contains("gif"),
              "writer suffixes contain gif");
    }

    // ---- arrays handed out are defensive copies ----
    static void testDefensiveCopies() {
        String[] a1 = ImageIO.getReaderFormatNames();
        a1[0] = "ZZZ";
        check(!Arrays.asList(ImageIO.getReaderFormatNames()).contains("ZZZ"),
              "mutating returned reader-format array does not poison next call");
        String[] a2 = ImageIO.getWriterFormatNames();
        a2[0] = "QQQ";
        check(!Arrays.asList(ImageIO.getWriterFormatNames()).contains("QQQ"),
              "mutating returned writer-format array does not poison next call");
        String[] a3 = ImageIO.getReaderMIMETypes();
        a3[0] = "zzz";
        check(!Arrays.asList(ImageIO.getReaderMIMETypes()).contains("zzz"),
              "mutating returned reader-MIME array does not poison next call");
        String[] a4 = ImageIO.getWriterFileSuffixes();
        a4[0] = "zzz";
        check(!Arrays.asList(ImageIO.getWriterFileSuffixes()).contains("zzz"),
              "mutating returned writer-suffix array does not poison next call");
    }

    // ---- reader discovery over raw streams ----
    static void testReaderDiscovery() throws Exception {
        byte[] png = encode(BufferedImage.TYPE_INT_RGB, "png");
        // magic-only header is decodable by the PNG spi
        ImageInputStream magic = new MemoryCacheImageInputStream(new ByteArrayInputStream(
            new byte[]{(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'}));
        Iterator<ImageReader> it = ImageIO.getImageReaders(magic);
        check(it.hasNext(), "getImageReaders over PNG magic header finds a reader");
        ImageReader r = it.next();
        check(r.getOriginatingProvider() != null
              && r.getOriginatingProvider().canDecodeInput(magic),
              "first reader's spi canDecodeInput PNG magic");
        try { it.remove(); check(false, "iterator remove should throw"); }
        catch (UnsupportedOperationException e) { passed++; }
        // a full PNG stream also discovers a reader that can report dimensions
        ImageInputStream full = new MemoryCacheImageInputStream(new ByteArrayInputStream(png));
        Iterator<ImageReader> it2 = ImageIO.getImageReaders(full);
        check(it2.hasNext(), "getImageReaders over full PNG stream non-empty");
        ImageReader first = it2.next();
        first.setInput(full, true, true);
        check(first.getWidth(0) == 5, "discovered reader reports width 5");
        check(first.getHeight(0) == 4, "discovered reader reports height 4");
        // junk stream discovers nothing
        ImageInputStream junk = new MemoryCacheImageInputStream(
            new ByteArrayInputStream("definitely not an image".getBytes("US-ASCII")));
        Iterator<ImageReader> jt = ImageIO.getImageReaders(junk);
        check(!jt.hasNext(), "getImageReaders over junk stream is empty");
        full.close(); junk.close(); magic.close();
        // negative inputs (probed: IllegalArgumentException, not NPE)
        expectExact(IllegalArgumentException.class, () -> ImageIO.getImageReaders(null),
                    "getImageReaders(null)");
        expectExact(IllegalArgumentException.class, () -> ImageIO.getImageReadersByFormatName(null),
                    "getImageReadersByFormatName(null)");
        expectExact(IllegalArgumentException.class, () -> ImageIO.getImageReadersBySuffix(null),
                    "getImageReadersBySuffix(null)");
        expectExact(IllegalArgumentException.class, () -> ImageIO.getImageReadersByMIMEType(null),
                    "getImageReadersByMIMEType(null)");
        expectExact(NoSuchElementException.class,
                    () -> ImageIO.getImageReadersByFormatName("no_such_format_xyz").next(),
                    "empty reader iterator next");
        expectExact(IllegalArgumentException.class,
                    () -> ImageIO.getImageWriters(null, "png"), "getImageWriters(null type)");
    }

    // ---- writer capability matrix and reader/writer pairing ----
    static void testWriterMatrixAndPairing() throws Exception {
        // probed capability matrix (getImageWriters(its, format).hasNext())
        boolean[][] m = {
            // ARGB, GRAY, 3BGR, BIDX, 565, BBIN, UGRAY
            {true,  true,  true,  true,  true,  true,  true },  // png
            {true,  true,  true,  true,  true,  true,  false},  // jpg (spi claims alpha ok; native write fails)
            {false, true,  true,  true,  true,  true,  false},  // bmp
            {true,  true,  true,  true,  true,  true,  true },  // gif
        };
        int[] types = {BufferedImage.TYPE_INT_ARGB, BufferedImage.TYPE_BYTE_GRAY,
                       BufferedImage.TYPE_3BYTE_BGR, BufferedImage.TYPE_BYTE_INDEXED,
                       BufferedImage.TYPE_USHORT_565_RGB, BufferedImage.TYPE_BYTE_BINARY,
                       BufferedImage.TYPE_USHORT_GRAY};
        String[] fmts = {"png", "jpg", "bmp", "gif"};
        for (int f = 0; f < fmts.length; f++)
            for (int t = 0; t < types.length; t++) {
                ImageTypeSpecifier its = ImageTypeSpecifier.createFromBufferedImageType(types[t]);
                check(ImageIO.getImageWriters(its, fmts[f]).hasNext() == m[f][t],
                      "writer capability " + fmts[f] + " x type" + types[t]);
            }
        check(!ImageIO.getImageWriters(ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB),
                                       "no_such_format_xyz").hasNext(),
              "no writers for unknown format");
        // pairing
        ImageReader pngR = ImageIO.getImageReadersByFormatName("png").next();
        ImageWriter pngW = ImageIO.getImageWritersByFormatName("png").next();
        ImageWriter wFromR = ImageIO.getImageWriter(pngR);
        check(wFromR != null, "getImageWriter(pngReader) non-null");
        ImageReader rFromW = ImageIO.getImageReader(pngW);
        check(rFromW != null, "getImageReader(pngWriter) non-null");
        check("png".equals(rFromW.getFormatName()), "paired reader has format png");
        ImageWriter w2 = ImageIO.getImageWriter(rFromW);
        check(w2 != null, "pairing is repeatable");
        expectExact(IllegalArgumentException.class, () -> ImageIO.getImageWriter(null), "getImageWriter(null)");
        expectExact(IllegalArgumentException.class, () -> ImageIO.getImageReader(null), "getImageReader(null)");
        expectExact(IllegalArgumentException.class, () -> ImageIO.getImageWritersByFormatName(null),
                    "getImageWritersByFormatName(null)");
        expectExact(IllegalArgumentException.class, () -> ImageIO.getImageTranscoders(null, pngW),
                    "getImageTranscoders(null reader)");
        expectExact(IllegalArgumentException.class, () -> ImageIO.getImageTranscoders(pngR, null),
                    "getImageTranscoders(null writer)");
        check(!ImageIO.getImageTranscoders(pngR, pngW).hasNext(),
              "no registered transcoders for png->png (built-in set)");
        pngR.dispose(); pngW.dispose(); wFromR.dispose(); rFromW.dispose(); w2.dispose();
    }

    // ---- format-name lookup is case-insensitive ----
    static void testFormatCaseInsensitivity() throws Exception {
        check(ImageIO.getImageReadersByFormatName("PNG").hasNext(), "reader by format PNG uppercase");
        check(ImageIO.getImageReadersByFormatName("Png").hasNext(), "reader by format Png mixed");
        check(ImageIO.getImageWritersByFormatName("PNG").hasNext(), "writer by format PNG uppercase");
        check(ImageIO.getImageReadersBySuffix("PNG").hasNext(), "reader by suffix PNG uppercase");
        check(ImageIO.getImageWritersBySuffix("JPG").hasNext(), "writer by suffix JPG uppercase");
        check(ImageIO.getImageReadersByMIMEType("image/png").hasNext(), "reader by MIME image/png");
        check(ImageIO.getImageWritersByMIMEType("image/png").hasNext(), "writer by MIME image/png");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        check(ImageIO.write(pattern(3, 3, BufferedImage.TYPE_INT_RGB), "PNG", bos),
              "write with uppercase format name PNG");
        check(ImageIO.read(new ByteArrayInputStream(bos.toByteArray())) != null,
              "round-trip of uppercase-PNG write");
        ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
        check(ImageIO.write(pattern(3, 3, BufferedImage.TYPE_INT_RGB), "GIF", bos2),
              "write with uppercase format name GIF");
        check(ImageIO.read(new ByteArrayInputStream(bos2.toByteArray())) != null,
              "read back uppercase-GIF write");
    }

    // ---- full lossless round-trip matrix ----
    static void testPngRoundTripMatrix() throws Exception {
        int[] types = {BufferedImage.TYPE_INT_RGB, BufferedImage.TYPE_INT_ARGB,
                       BufferedImage.TYPE_INT_ARGB_PRE, BufferedImage.TYPE_INT_BGR,
                       BufferedImage.TYPE_3BYTE_BGR, BufferedImage.TYPE_4BYTE_ABGR,
                       BufferedImage.TYPE_4BYTE_ABGR_PRE, BufferedImage.TYPE_USHORT_565_RGB,
                       BufferedImage.TYPE_USHORT_555_RGB, BufferedImage.TYPE_BYTE_GRAY,
                       BufferedImage.TYPE_USHORT_GRAY, BufferedImage.TYPE_BYTE_BINARY,
                       BufferedImage.TYPE_BYTE_INDEXED};
        int[] readBack = {5, 6, 6, 5, 5, 6, 6, 5, 5, 10, 11, 12, 13};
        for (int i = 0; i < types.length; i++) {
            BufferedImage src = pattern(5, 4, types[i]);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            check(ImageIO.write(src, "png", bos), "png write type " + types[i] + " returns true");
            BufferedImage back = ImageIO.read(new ByteArrayInputStream(bos.toByteArray()));
            check(back != null, "png read-back type " + types[i] + " non-null");
            check(back.getWidth() == 5 && back.getHeight() == 4, "png round-trip dims type " + types[i]);
            check(back.getType() == readBack[i], "png read-back bufferedImageType " + types[i] + " -> " + readBack[i]);
            check(pixelsEqual(src, back), "png round-trip pixel-exact for type " + types[i]);
        }
    }

    static void testBmpRoundTrip() throws Exception {
        int[] ok = {BufferedImage.TYPE_INT_RGB, BufferedImage.TYPE_INT_BGR, BufferedImage.TYPE_3BYTE_BGR,
                    BufferedImage.TYPE_USHORT_565_RGB, BufferedImage.TYPE_USHORT_555_RGB,
                    BufferedImage.TYPE_BYTE_GRAY, BufferedImage.TYPE_BYTE_BINARY,
                    BufferedImage.TYPE_BYTE_INDEXED};
        int[] readBack = {5, 5, 5, 8, 9, 10, 12, 13};
        for (int i = 0; i < ok.length; i++) {
            BufferedImage src = pattern(5, 4, ok[i]);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            check(ImageIO.write(src, "bmp", bos), "bmp write type " + ok[i] + " returns true");
            BufferedImage back = ImageIO.read(new ByteArrayInputStream(bos.toByteArray()));
            check(back != null, "bmp read-back type " + ok[i] + " non-null");
            check(back.getWidth() == 5 && back.getHeight() == 4, "bmp round-trip dims type " + ok[i]);
            check(back.getType() == readBack[i], "bmp read-back bufferedImageType " + ok[i] + " -> " + readBack[i]);
            check(pixelsEqual(src, back), "bmp round-trip pixel-exact for type " + ok[i]);
        }
        // BMP writer rejects alpha and 16-bit gray (returns false, no exception)
        int[] no = {BufferedImage.TYPE_INT_ARGB, BufferedImage.TYPE_INT_ARGB_PRE,
                    BufferedImage.TYPE_4BYTE_ABGR, BufferedImage.TYPE_4BYTE_ABGR_PRE,
                    BufferedImage.TYPE_USHORT_GRAY};
        for (int t : no) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            check(!ImageIO.write(pattern(2, 2, t), "bmp", bos), "bmp rejects alpha/ushort-gray type " + t);
            check(bos.size() == 0, "bmp rejected write leaves stream empty for type " + t);
        }
    }

    static void testGifRoundTrip() throws Exception {
        // GIF writer accepts every buffered type (it quantizes internally); lossy, dims only
        int[] types = {BufferedImage.TYPE_INT_RGB, BufferedImage.TYPE_INT_ARGB,
                       BufferedImage.TYPE_3BYTE_BGR, BufferedImage.TYPE_BYTE_GRAY,
                       BufferedImage.TYPE_BYTE_INDEXED, BufferedImage.TYPE_BYTE_BINARY};
        for (int t : types) {
            BufferedImage src = pattern(5, 4, t);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            check(ImageIO.write(src, "gif", bos), "gif write type " + t + " returns true");
            BufferedImage back = ImageIO.read(new ByteArrayInputStream(bos.toByteArray()));
            check(back != null, "gif read-back type " + t + " non-null");
            check(back.getWidth() == 5 && back.getHeight() == 4, "gif round-trip dims type " + t);
            check(back.getType() == (t == BufferedImage.TYPE_BYTE_BINARY
                                     ? BufferedImage.TYPE_BYTE_BINARY : BufferedImage.TYPE_BYTE_INDEXED),
                  "gif read-back is an indexed type for " + t);
        }
    }

    static void testJpegRoundTrip() throws Exception {
        // JPEG is lossy: only dims, non-null and write() result are asserted
        int[] ok = {BufferedImage.TYPE_INT_RGB, BufferedImage.TYPE_INT_BGR,
                    BufferedImage.TYPE_3BYTE_BGR, BufferedImage.TYPE_BYTE_GRAY};
        for (int t : ok) {
            BufferedImage src = pattern(5, 4, t);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            check(ImageIO.write(src, "jpg", bos), "jpeg write type " + t + " returns true");
            BufferedImage back = ImageIO.read(new ByteArrayInputStream(bos.toByteArray()));
            check(back != null, "jpeg read-back type " + t + " non-null");
            check(back.getWidth() == 5 && back.getHeight() == 4, "jpeg round-trip dims type " + t);
        }
        // this JDK's JPEG spi claims it can encode ARGB, but the native coder rejects it
        // (probed: IIOException "Invalid argument to native writeImage")
        final ByteArrayOutputStream alphaBos = new ByteArrayOutputStream();
        expectExact(IIOException.class,
                    () -> ImageIO.write(pattern(2, 2, BufferedImage.TYPE_INT_ARGB), "jpg", alphaBos),
                    "jpeg write TYPE_INT_ARGB fails in native coder");
    }

    // ---- write() negative contracts ----
    static void testWriteNegatives() throws Exception {
        BufferedImage img = pattern(3, 3, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        check(!ImageIO.write(img, "no_such_format_xyz", bos), "write unknown format returns false");
        check(bos.size() == 0, "unknown-format write leaves stream empty");
        expectExact(IllegalArgumentException.class, () -> ImageIO.write(null, "png", bos),
                    "write(null image)");
        expectExact(IllegalArgumentException.class, () -> ImageIO.write(img, null, bos),
                    "write(null formatName)");
        expectExact(IllegalArgumentException.class,
                    () -> ImageIO.write(img, "png", (java.io.OutputStream) null),
                    "write(null OutputStream)");
        expectExact(IllegalArgumentException.class,
                    () -> ImageIO.write(img, "png", (ImageOutputStream) null),
                    "write(null ImageOutputStream)");
        // writing to a File under a missing directory: ImageIO wraps the failure (probed)
        File badDir = new File(System.getProperty("java.io.tmpdir"), "imgio-corea-ImageIO-missing-dir/x.png");
        expectExact(IIOException.class, () -> ImageIO.write(img, "png", badDir),
                    "write(File under missing directory)");
        // unknown format to File returns false and does not create the file
        File bogus = tmpFile(".png");
        check(!ImageIO.write(img, "no_such_format_xyz", bogus), "write(File) unknown format false");
        check(!bogus.exists(), "unknown-format File write creates nothing");
    }

    // ---- read() negative contracts and stream-close semantics ----
    static void testReadNegativesAndStreamClose() throws Exception {
        expectExact(IllegalArgumentException.class, () -> ImageIO.read((File) null), "read((File) null)");
        expectExact(IllegalArgumentException.class, () -> ImageIO.read((java.io.InputStream) null),
                    "read((InputStream) null)");
        expectExact(IllegalArgumentException.class, () -> ImageIO.read((java.net.URL) null),
                    "read((URL) null)");
        expectExact(IllegalArgumentException.class, () -> ImageIO.read((ImageInputStream) null),
                    "read((ImageInputStream) null)");
        // nonexistent file: probed -> IIOException "Can't read input file!"
        File noSuch = new File(System.getProperty("java.io.tmpdir"), "imgio-corea-ImageIO-nosuch.png");
        expectExact(IIOException.class, () -> ImageIO.read(noSuch), "read(nonexistent file)");
        // empty and junk streams return null (no exception) - probed
        check(ImageIO.read(new ByteArrayInputStream(new byte[0])) == null, "read empty stream returns null");
        check(ImageIO.read(new ByteArrayInputStream("garbage!!".getBytes("US-ASCII"))) == null,
              "read junk stream returns null");
        // read(ImageInputStream) closes the stream when done (probed)
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(pattern(3, 3, BufferedImage.TYPE_INT_RGB), "png", bos);
        ImageInputStream mem = new MemoryCacheImageInputStream(new ByteArrayInputStream(bos.toByteArray()));
        check(ImageIO.read(mem) != null, "read over MemoryCacheImageInputStream");
        expectExact(IOException.class, () -> mem.read(), "read(ImageInputStream) closes the stream");
        // read(InputStream) does NOT close the underlying stream (probed)
        final boolean[] closed = {false};
        ByteArrayInputStream counting = new ByteArrayInputStream(bos.toByteArray()) {
            @Override public void close() { closed[0] = true; }
        };
        check(ImageIO.read(counting) != null, "read over InputStream");
        check(!closed[0], "read(InputStream) leaves underlying stream open");
    }

    // ---- cache global state (restored in finally) ----
    static void testCacheSettings() throws Exception {
        boolean savedUse = ImageIO.getUseCache();
        File savedDir = ImageIO.getCacheDirectory();
        try {
            ImageIO.setUseCache(true);
            check(ImageIO.getUseCache(), "setUseCache(true) round-trip");
            ImageIO.setUseCache(false);
            check(!ImageIO.getUseCache(), "setUseCache(false) round-trip");
            File tmpDir = new File(System.getProperty("java.io.tmpdir"));
            ImageIO.setCacheDirectory(tmpDir);
            check(ImageIO.getCacheDirectory() == tmpDir, "setCacheDirectory identity round-trip");
            // a plain file is rejected (probed: IAE "Not a directory!")
            File plain = tmpFile(".txt");
            check(plain.createNewFile(), "setup plain file");
            expectExact(IllegalArgumentException.class, () -> ImageIO.setCacheDirectory(plain),
                        "setCacheDirectory(plain file)");
            check(ImageIO.getCacheDirectory() == tmpDir, "failed setCacheDirectory leaves state intact");
            expectExact(IllegalArgumentException.class,
                        () -> ImageIO.setCacheDirectory(new File(tmpDir, "imgio-corea-nodir")),
                        "setCacheDirectory(nonexistent dir)");
            ImageIO.setCacheDirectory(null);
            check(ImageIO.getCacheDirectory() == null, "setCacheDirectory(null) resets to null");
            plain.delete();
        } finally {
            ImageIO.setCacheDirectory(savedDir);
            ImageIO.setUseCache(savedUse);
        }
        check(ImageIO.getUseCache() == savedUse, "useCache restored");
        check(ImageIO.getCacheDirectory() == savedDir, "cacheDirectory restored");
    }

    // ---- stream factories honor useCache; null is rejected (probed IAE) ----
    static void testStreamFactories() throws Exception {
        boolean savedUse = ImageIO.getUseCache();
        try {
            ImageIO.setUseCache(false);
            Object s1 = ImageIO.createImageInputStream(new ByteArrayInputStream(new byte[8]));
            check(s1 instanceof MemoryCacheImageInputStream, "useCache=false -> MemoryCacheImageInputStream");
            ((ImageInputStream) s1).close();
            Object s2 = ImageIO.createImageOutputStream(new ByteArrayOutputStream());
            check(s2 instanceof MemoryCacheImageOutputStream, "useCache=false -> MemoryCacheImageOutputStream");
            ((ImageOutputStream) s2).close();
            ImageIO.setUseCache(true);
            Object s3 = ImageIO.createImageInputStream(new ByteArrayInputStream(new byte[8]));
            check(s3 instanceof FileCacheImageInputStream, "useCache=true -> FileCacheImageInputStream");
            ((ImageInputStream) s3).close();
            Object s4 = ImageIO.createImageOutputStream(new ByteArrayOutputStream());
            check(s4 instanceof FileCacheImageOutputStream, "useCache=true -> FileCacheImageOutputStream");
            ((ImageOutputStream) s4).close();
        } finally {
            ImageIO.setUseCache(savedUse);
        }
        // File-backed streams
        File f = tmpFile(".png");
        ImageIO.write(pattern(3, 3, BufferedImage.TYPE_INT_RGB), "png", f);
        ImageInputStream fis = ImageIO.createImageInputStream(f);
        check(fis instanceof javax.imageio.stream.FileImageInputStream,
              "createImageInputStream(File) -> FileImageInputStream");
        fis.close();
        File g = tmpFile(".png");
        ImageOutputStream fos = ImageIO.createImageOutputStream(g);
        check(fos instanceof javax.imageio.stream.FileImageOutputStream,
              "createImageOutputStream(File) -> FileImageOutputStream");
        fos.close();
        f.delete(); g.delete();
        // null inputs are IAE in this JDK although javadoc implies null return (probed)
        expectExact(IllegalArgumentException.class, () -> ImageIO.createImageInputStream(null),
                    "createImageInputStream(null)");
        expectExact(IllegalArgumentException.class, () -> ImageIO.createImageOutputStream(null),
                    "createImageOutputStream(null)");
    }

    // ---- scanForPlugins is a safe no-op for the built-in set ----
    static void testScanForPlugins() {
        ImageIO.scanForPlugins();
        check(ImageIO.getImageReadersByFormatName("png").hasNext(), "png readers survive scanForPlugins");
        check(ImageIO.getImageWritersByFormatName("png").hasNext(), "png writers survive scanForPlugins");
    }

    // ---- File and file: URL paths ----
    static void testFileAndUrlIO() throws Exception {
        File f = tmpFile(".png");
        BufferedImage src = pattern(4, 3, BufferedImage.TYPE_INT_RGB);
        try {
            check(ImageIO.write(src, "png", f), "write to File returns true");
            check(f.length() > 0, "written File is non-empty");
            BufferedImage back = ImageIO.read(f);
            check(back != null && back.getWidth() == 4 && back.getHeight() == 3, "read(File) dims");
            check(pixelsEqual(src, back), "File round-trip pixel-exact");
            BufferedImage fromUrl = ImageIO.read(f.toURI().toURL());
            check(fromUrl != null && pixelsEqual(src, fromUrl), "read(file: URL) pixel-exact");
        } finally {
            f.delete();
        }
    }

    public static void main(String[] a) throws Exception {
        testLegacy();
        testFormatInventory();
        testDefensiveCopies();
        testReaderDiscovery();
        testWriterMatrixAndPairing();
        testFormatCaseInsensitivity();
        testPngRoundTripMatrix();
        testBmpRoundTrip();
        testGifRoundTrip();
        testJpegRoundTrip();
        testWriteNegatives();
        testReadNegativesAndStreamClose();
        testCacheSettings();
        testStreamFactories();
        testScanForPlugins();
        testFileAndUrlIO();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
