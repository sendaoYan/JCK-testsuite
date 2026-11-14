/*
 * @test
 * @summary javax.imageio.spi.ImageOutputStreamSpi conformance: abstract hierarchy and methods.
 * @run main ImageOutputStreamSpiTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageOutputStreamSpi;
import javax.imageio.stream.ImageOutputStream;

public class ImageOutputStreamSpiTest {
    static int passed;
    static int tmpCounter;
    // Per-run fixture suffix: concurrent configurations must not share paths.
    static final String RUN = Long.toHexString(System.nanoTime());
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.imageio.spi.ImageOutputStreamSpi");
        check(!c.isInterface(), "not interface");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("javax.imageio.spi.IIOServiceProvider"), "extends javax.imageio.spi.IIOServiceProvider");
        Set<String> dm = new HashSet<>(); for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("getOutputClass"), "method getOutputClass");
        check(dm.contains("canUseCacheFile"), "method canUseCacheFile");
        check(dm.contains("needsCacheFile"), "method needsCacheFile");
        check(dm.contains("createOutputStreamInstance"), "method createOutputStreamInstance");
        check(dm.contains("createOutputStreamInstance"), "method createOutputStreamInstance");
        testRegisteredStreamSpis();
        testNullAndWrongOutputRejected();
        testTinySpiDefaultsAndDelegation();
        testFileSpiRoundTrip();
        System.out.println("PASS (" + passed + " assertions)");
    }

    interface ThrowingRunnable { void run() throws Exception; }
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
        int calls; Object lastOutput; Boolean lastUseCache; File lastCacheDir;
        RecordingOutputSpi() { super("TinyVendor", "3.5", OutputStream.class); }
        public String getDescription(Locale l) { return "recording output spi"; }
        public ImageOutputStream createOutputStreamInstance(Object output, boolean useCache, File cacheDir) {
            calls++; lastOutput = output; lastUseCache = useCache; lastCacheDir = cacheDir;
            return new javax.imageio.stream.MemoryCacheImageOutputStream(
                new ByteArrayOutputStream());
        }
    }

    static List<ImageOutputStreamSpi> allOutputSpis() {
        List<ImageOutputStreamSpi> out = new ArrayList<ImageOutputStreamSpi>();
        Iterator<ImageOutputStreamSpi> it =
            IIORegistry.getDefaultInstance().getServiceProviders(ImageOutputStreamSpi.class, false);
        while (it.hasNext()) out.add(it.next());
        return out;
    }

    static ImageOutputStreamSpi findByOutputClass(Class<?> wanted) {
        for (ImageOutputStreamSpi sp : allOutputSpis()) if (sp.getOutputClass() == wanted) return sp;
        throw new AssertionError("no registered spi with output class " + wanted.getName());
    }

    // structure and identity of the JDK-registered output stream providers
    static void testRegisteredStreamSpis() {
        List<ImageOutputStreamSpi> all = allOutputSpis();
        check(all.size() >= 3, "at least three registered ImageOutputStreamSpi (OutputStream/File/RAF)");
        Set<Class<?>> outputClasses = new HashSet<Class<?>>();
        for (ImageOutputStreamSpi sp : all) {
            check(sp.getOutputClass() != null, "getOutputClass non-null");
            outputClasses.add(sp.getOutputClass());
            check("Oracle Corporation".equals(sp.getVendorName()),
                  "OpenJDK-built stream spi vendor Oracle Corporation");
            check(sp.getVersion() != null, "version non-null");
            check(sp.getDescription(Locale.US) != null, "description non-null");
        }
        check(outputClasses.contains(OutputStream.class), "OutputStream output class present");
        check(outputClasses.contains(File.class), "File output class present");
        check(outputClasses.contains(RandomAccessFile.class), "RandomAccessFile output class present");

        ImageOutputStreamSpi outSpi = findByOutputClass(OutputStream.class);
        check(outSpi.canUseCacheFile() == true, "OutputStream spi canUseCacheFile true");
        check(outSpi.needsCacheFile() == false, "OutputStream spi needsCacheFile false");
        ImageOutputStreamSpi fileSpi = findByOutputClass(File.class);
        check(fileSpi.canUseCacheFile() == false, "File spi canUseCacheFile false");
        check(fileSpi.needsCacheFile() == false, "File spi needsCacheFile false");
        ImageOutputStreamSpi rafSpi = findByOutputClass(RandomAccessFile.class);
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
        check(sp.getOutputClass() == OutputStream.class, "tiny spi output class round-trips");
        check(sp.getVendorName().equals("TinyVendor"), "tiny spi vendor round-trips");
        check(sp.getVersion().equals("3.5"), "tiny spi version round-trips");
        check(sp.canUseCacheFile() == false, "canUseCacheFile default false in subclass");
        check(sp.needsCacheFile() == false, "needsCacheFile default false in subclass");
        check(sp.getDescription(Locale.US).equals("recording output spi"), "description round-trips");
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

    // FileImageOutputStreamSpi writes to a real temp file
    static void testFileSpiRoundTrip() throws Exception {
        ImageOutputStreamSpi fileSpi = findByOutputClass(File.class);
        File f = new File(System.getProperty("java.io.tmpdir"),
                          "imgio-spi-ImageOutputStreamSpi-" + RUN + "-" + (tmpCounter++) + ".bin");
        try {
            ImageOutputStream ios = fileSpi.createOutputStreamInstance(f, false, null);
            check(ios != null, "File spi creates a stream from a File");
            ios.write(new byte[] { 7, 8, 9 });
            ios.flush();
            ios.close();
            DataInputStream dis = new DataInputStream(new FileInputStream(f));
            check(dis.read() == 7 && dis.read() == 8 && dis.read() == 9, "written bytes round-trip");
            check(dis.read() == -1, "exactly three bytes written");
            dis.close();
        } finally {
            f.delete();
        }
    }
}
