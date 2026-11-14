/*
 * @test
 * @summary javax.imageio.spi.ImageOutputStreamSpi conformance: white-box structure + black-box behavior.
 * @run main ImageOutputStreamSpiTest
 */

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageOutputStreamSpi;
import javax.imageio.stream.ImageOutputStream;

public class ImageOutputStreamSpiTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    interface ThrowingRunnable { void run() throws Exception; }
    static void expectThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        boolean thrown = false;
        try { r.run(); } catch (Throwable t) {
            thrown = true;
            check(expected.isAssignableFrom(t.getClass()),
                  msg + " expected " + expected.getSimpleName() + " but got " + t.getClass().getSimpleName());
        }
        check(thrown, msg + " should throw " + expected.getSimpleName());
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.imageio.spi.ImageOutputStreamSpi");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "not interface");
        check(!c.isAnnotation(), "not annotation");
        check(!c.isEnum(), "not enum");
        check(Modifier.isPublic(mods), "public");
        check(Modifier.isAbstract(mods), "abstract");
        check(c.getSuperclass() != null &&
              c.getSuperclass().getName().equals("javax.imageio.spi.IIOServiceProvider"),
              "extends IIOServiceProvider");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Iterator<ImageOutputStreamSpi> it =
            IIORegistry.getDefaultInstance().getServiceProviders(ImageOutputStreamSpi.class, false);
        check(it != null, "providers iterator non-null");
        ImageOutputStreamSpi outputStreamSpi = null;
        int count = 0;
        while (it.hasNext()) {
            ImageOutputStreamSpi sp = it.next();
            check(sp.getOutputClass() != null, "getOutputClass non-null");
            sp.canUseCacheFile();
            sp.needsCacheFile();
            check(true, "cache-file queries return normally");
            if (sp.getOutputClass() == OutputStream.class) outputStreamSpi = sp;
            count++;
        }
        check(count > 0, "at least one ImageOutputStreamSpi registered");
        check(outputStreamSpi != null, "OutputStream provider present");

        check(outputStreamSpi.getOutputClass() == OutputStream.class,
              "getOutputClass == OutputStream.class");
        check(outputStreamSpi.canUseCacheFile() == true, "OutputStream SPI can use cache file");
        check(outputStreamSpi.needsCacheFile() == false, "OutputStream SPI does not need cache file");

        // 3-arg overload: memory cache (no temp file)
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageOutputStream ios = outputStreamSpi.createOutputStreamInstance(bos, false, null);
        check(ios != null, "createOutputStreamInstance 3-arg non-null");
        ios.write(new byte[] { 6, 7, 8 });
        ios.flush();
        ios.close();
        check(java.util.Arrays.equals(bos.toByteArray(), new byte[] { 6, 7, 8 }),
              "round-trip bytes via 3-arg overload");

        // 1-arg convenience overload
        ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
        ImageOutputStream ios2 = outputStreamSpi.createOutputStreamInstance(bos2);
        check(ios2 != null, "createOutputStreamInstance 1-arg non-null");
        ios2.close();

        // wrong output type rejected
        final ImageOutputStreamSpi osp = outputStreamSpi;
        expectThrows(IllegalArgumentException.class,
                     () -> osp.createOutputStreamInstance("not-a-stream", false, null),
                     "wrong output type");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testRegisteredStreamSpiIdentity();
        testNullAndWrongOutputRejected();
        testTinySpiDefaultsAndDelegation();
        testFileSpiRoundTrip();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void expectExact(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        Throwable caught = null;
        try { r.run(); } catch (Throwable t) { caught = t; }
        check(caught != null, msg + ": nothing thrown, expected " + expected.getSimpleName());
        if (caught == null) return;
        check(caught.getClass() == expected,
              msg + ": expected " + expected.getSimpleName() + " but got "
              + caught.getClass().getName() + " (" + caught.getMessage() + ")");
    }

    /** Self-written SPI recording the arguments its create method was called with. */
    static class RecordingOutputSpi extends ImageOutputStreamSpi {
        int calls; Object lastOutput; Boolean lastUseCache; java.io.File lastCacheDir;
        RecordingOutputSpi() { super("TinyVendor", "3.5", java.io.OutputStream.class); }
        public String getDescription(java.util.Locale l) { return "recording output spi"; }
        public ImageOutputStream createOutputStreamInstance(Object output, boolean useCache, java.io.File cacheDir) {
            calls++; lastOutput = output; lastUseCache = useCache; lastCacheDir = cacheDir;
            return new javax.imageio.stream.MemoryCacheImageOutputStream(
                new java.io.ByteArrayOutputStream());
        }
    }

    static java.util.List<ImageOutputStreamSpi> allOutputSpis() {
        java.util.List<ImageOutputStreamSpi> out = new java.util.ArrayList<ImageOutputStreamSpi>();
        Iterator<ImageOutputStreamSpi> it =
            IIORegistry.getDefaultInstance().getServiceProviders(ImageOutputStreamSpi.class, false);
        while (it.hasNext()) out.add(it.next());
        return out;
    }

    static ImageOutputStreamSpi findByOutputClass(Class<?> wanted) {
        for (ImageOutputStreamSpi sp : allOutputSpis()) if (sp.getOutputClass() == wanted) return sp;
        throw new AssertionError("no registered spi with output class " + wanted.getName());
    }

    // identity: output classes, vendor and cache-file flags of the JDK-registered providers
    static void testRegisteredStreamSpiIdentity() {
        java.util.List<ImageOutputStreamSpi> all = allOutputSpis();
        check(all.size() >= 3, "at least three registered ImageOutputStreamSpi (OutputStream/File/RAF)");
        java.util.Set<Class<?>> outputClasses = new java.util.HashSet<Class<?>>();
        for (ImageOutputStreamSpi sp : all) {
            check(sp.getOutputClass() != null, "getOutputClass non-null");
            outputClasses.add(sp.getOutputClass());
            check("Oracle Corporation".equals(sp.getVendorName()),
                  "OpenJDK-built stream spi vendor Oracle Corporation");
            check(sp.getVersion() != null, "version non-null");
        }
        check(outputClasses.contains(java.io.OutputStream.class), "OutputStream output class present");
        check(outputClasses.contains(java.io.File.class), "File output class present");
        check(outputClasses.contains(java.io.RandomAccessFile.class), "RandomAccessFile output class present");
        ImageOutputStreamSpi fileSpi = findByOutputClass(java.io.File.class);
        check(fileSpi.canUseCacheFile() == false, "File spi canUseCacheFile false");
        check(fileSpi.needsCacheFile() == false, "File spi needsCacheFile false");
        ImageOutputStreamSpi rafSpi = findByOutputClass(java.io.RandomAccessFile.class);
        check(rafSpi.needsCacheFile() == false, "RAF spi needsCacheFile false");
    }

    // null output and wrong output type are rejected with exactly IllegalArgumentException
    static void testNullAndWrongOutputRejected() {
        for (final ImageOutputStreamSpi sp : allOutputSpis()) {
            expectExact(IllegalArgumentException.class,
                        () -> sp.createOutputStreamInstance(null, false, null),
                        "createOutputStreamInstance(null) on " + sp.getClass().getSimpleName());
            expectExact(IllegalArgumentException.class,
                        () -> sp.createOutputStreamInstance("not-a-stream", false, null),
                        "createOutputStreamInstance(wrong type) on " + sp.getClass().getSimpleName());
            expectExact(IllegalArgumentException.class,
                        () -> sp.createOutputStreamInstance(null),
                        "1-arg createOutputStreamInstance(null) on " + sp.getClass().getSimpleName());
        }
    }

    // subclass default flags + 1-arg convenience overload delegation
    static void testTinySpiDefaultsAndDelegation() throws Exception {
        RecordingOutputSpi sp = new RecordingOutputSpi();
        check(sp.getOutputClass() == java.io.OutputStream.class, "tiny spi output class round-trips");
        check(sp.getVendorName().equals("TinyVendor"), "tiny spi vendor round-trips");
        check(sp.getVersion().equals("3.5"), "tiny spi version round-trips");
        check(sp.canUseCacheFile() == false, "canUseCacheFile default false in subclass");
        check(sp.needsCacheFile() == false, "needsCacheFile default false in subclass");
        check(sp.getDescription(java.util.Locale.US).equals("recording output spi"), "description round-trips");
        check(sp.getDescription(null).equals("recording output spi"), "description null locale ok");

        ImageOutputStream s3 = sp.createOutputStreamInstance("DST", false, null);
        check(sp.calls == 1, "3-arg create called once");
        check("DST".equals(sp.lastOutput), "3-arg passes output unchanged");
        check(sp.lastUseCache == Boolean.FALSE, "3-arg passes useCache=false");
        check(sp.lastCacheDir == null, "3-arg passes cacheDir null");
        s3.writeInt(0x0A0B0C0D);
        s3.flush();
        s3.close();

        // 1-arg convenience overload delegates with useCache=true and cacheDir=null (recorded behavior)
        ImageOutputStream s1 = sp.createOutputStreamInstance("ONE");
        check(sp.calls == 2, "1-arg delegates to 3-arg");
        check("ONE".equals(sp.lastOutput), "1-arg passes output unchanged");
        check(sp.lastUseCache == Boolean.TRUE, "1-arg hardcodes useCache=true");
        check(sp.lastCacheDir == null, "1-arg hardcodes cacheDir=null");
        s1.close();
    }

    static int tmpCounter;
    // Per-run fixture suffix: concurrent configurations must not share paths.
    static final String RUN = Long.toHexString(System.nanoTime());

    // FileImageOutputStreamSpi writes to a real temp file
    static void testFileSpiRoundTrip() throws Exception {
        ImageOutputStreamSpi fileSpi = findByOutputClass(java.io.File.class);
        java.io.File f = new java.io.File(System.getProperty("java.io.tmpdir"),
                          "imgio-spi-ImageOutputStreamSpi-" + RUN + "-" + (tmpCounter++) + ".bin");
        try {
            ImageOutputStream ios = fileSpi.createOutputStreamInstance(f, false, null);
            check(ios != null, "File spi creates a stream from a File");
            ios.write(new byte[] { 7, 8, 9 });
            ios.flush();
            ios.close();
            java.io.DataInputStream dis = new java.io.DataInputStream(new java.io.FileInputStream(f));
            check(dis.read() == 7 && dis.read() == 8 && dis.read() == 9, "written bytes round-trip");
            check(dis.read() == -1, "exactly three bytes written");
            dis.close();
        } finally {
            f.delete();
        }
    }
}
