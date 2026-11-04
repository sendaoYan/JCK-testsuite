/*
 * @test
 * @summary javax.imageio.spi.ImageInputStreamSpi conformance: abstract hierarchy and methods.
 * @run main ImageInputStreamSpiTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageInputStreamSpi;
import javax.imageio.stream.ImageInputStream;

public class ImageInputStreamSpiTest {
    static int passed;
    static int tmpCounter;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.imageio.spi.ImageInputStreamSpi");
        check(!c.isInterface(), "not interface");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("javax.imageio.spi.IIOServiceProvider"), "extends javax.imageio.spi.IIOServiceProvider");
        Set<String> dm = new HashSet<>(); for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("getInputClass"), "method getInputClass");
        check(dm.contains("canUseCacheFile"), "method canUseCacheFile");
        check(dm.contains("needsCacheFile"), "method needsCacheFile");
        check(dm.contains("createInputStreamInstance"), "method createInputStreamInstance");
        check(dm.contains("createInputStreamInstance"), "method createInputStreamInstance");
        testRegisteredStreamSpis();
        testNullAndWrongInputRejected();
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
    static class RecordingInputSpi extends ImageInputStreamSpi {
        int calls; Object lastInput; Boolean lastUseCache; File lastCacheDir;
        RecordingInputSpi() { super("TinyVendor", "2.5", InputStream.class); }
        public String getDescription(Locale l) { return "recording input spi"; }
        public ImageInputStream createInputStreamInstance(Object input, boolean useCache, File cacheDir) {
            calls++; lastInput = input; lastUseCache = useCache; lastCacheDir = cacheDir;
            return new javax.imageio.stream.MemoryCacheImageInputStream(
                new ByteArrayInputStream(new byte[] { 42 }));
        }
    }

    static List<ImageInputStreamSpi> allInputSpis() {
        List<ImageInputStreamSpi> out = new ArrayList<ImageInputStreamSpi>();
        Iterator<ImageInputStreamSpi> it =
            IIORegistry.getDefaultInstance().getServiceProviders(ImageInputStreamSpi.class, false);
        while (it.hasNext()) out.add(it.next());
        return out;
    }

    static ImageInputStreamSpi findByInputClass(Class<?> wanted) {
        for (ImageInputStreamSpi sp : allInputSpis()) if (sp.getInputClass() == wanted) return sp;
        throw new AssertionError("no registered spi with input class " + wanted.getName());
    }

    // structure and identity of the JDK-registered input stream providers
    static void testRegisteredStreamSpis() {
        List<ImageInputStreamSpi> all = allInputSpis();
        check(all.size() >= 3, "at least three registered ImageInputStreamSpi (InputStream/File/RAF)");
        Set<Class<?>> inputClasses = new HashSet<Class<?>>();
        for (ImageInputStreamSpi sp : all) {
            check(sp.getInputClass() != null, "getInputClass non-null");
            inputClasses.add(sp.getInputClass());
            check(sp.getVendorName() != null, "vendor non-null");
            check("Oracle Corporation".equals(sp.getVendorName()),
                  "OpenJDK-built stream spi vendor Oracle Corporation");
            check(sp.getVersion() != null, "version non-null");
            check(sp.getDescription(Locale.US) != null, "description non-null");
        }
        check(inputClasses.contains(InputStream.class), "InputStream input class present");
        check(inputClasses.contains(File.class), "File input class present");
        check(inputClasses.contains(RandomAccessFile.class), "RandomAccessFile input class present");

        ImageInputStreamSpi inSpi = findByInputClass(InputStream.class);
        check(inSpi.canUseCacheFile() == true, "InputStream spi canUseCacheFile true");
        check(inSpi.needsCacheFile() == false, "InputStream spi needsCacheFile false");
        ImageInputStreamSpi fileSpi = findByInputClass(File.class);
        check(fileSpi.canUseCacheFile() == false, "File spi canUseCacheFile false");
        check(fileSpi.needsCacheFile() == false, "File spi needsCacheFile false");
        ImageInputStreamSpi rafSpi = findByInputClass(RandomAccessFile.class);
        check(rafSpi.needsCacheFile() == false, "RAF spi needsCacheFile false");
    }

    // null input and wrong input type are rejected with exactly IllegalArgumentException
    static void testNullAndWrongInputRejected() {
        for (final ImageInputStreamSpi sp : allInputSpis()) {
            expectExact(IllegalArgumentException.class,
                        () -> sp.createInputStreamInstance(null, false, null),
                        "createInputStreamInstance(null) on " + sp.getClass().getSimpleName());
            expectExact(IllegalArgumentException.class,
                        () -> sp.createInputStreamInstance("not-a-stream", false, null),
                        "createInputStreamInstance(wrong type) on " + sp.getClass().getSimpleName());
            expectExact(IllegalArgumentException.class,
                        () -> sp.createInputStreamInstance(null),
                        "1-arg createInputStreamInstance(null) on " + sp.getClass().getSimpleName());
        }
    }

    // subclass default flags + 1-arg convenience overload delegation
    static void testTinySpiDefaultsAndDelegation() throws Exception {
        RecordingInputSpi sp = new RecordingInputSpi();
        check(sp.getInputClass() == InputStream.class, "tiny spi input class round-trips");
        check(sp.getVendorName().equals("TinyVendor"), "tiny spi vendor round-trips");
        check(sp.getVersion().equals("2.5"), "tiny spi version round-trips");
        check(sp.canUseCacheFile() == false, "canUseCacheFile default false in subclass");
        check(sp.needsCacheFile() == false, "needsCacheFile default false in subclass");
        check(sp.getDescription(Locale.US).equals("recording input spi"), "description round-trips");
        check(sp.getDescription(null).equals("recording input spi"), "description null locale ok");

        // 3-arg call records exact arguments
        ImageInputStream s3 = sp.createInputStreamInstance("SRC", false, null);
        check(sp.calls == 1, "3-arg create called once");
        check("SRC".equals(sp.lastInput), "3-arg passes input unchanged");
        check(sp.lastUseCache == Boolean.FALSE, "3-arg passes useCache=false");
        check(sp.lastCacheDir == null, "3-arg passes cacheDir null");
        check(s3.read() == 42 && s3.read() == -1, "returned stream readable then exhausted");

        // 1-arg convenience overload delegates with useCache=true and cacheDir=null (recorded behavior)
        ImageInputStream s1 = sp.createInputStreamInstance("ONE");
        check(sp.calls == 2, "1-arg delegates to 3-arg");
        check("ONE".equals(sp.lastInput), "1-arg passes input unchanged");
        check(sp.lastUseCache == Boolean.TRUE, "1-arg hardcodes useCache=true");
        check(sp.lastCacheDir == null, "1-arg hardcodes cacheDir=null");
        check(s1.read() == 42, "1-arg returned stream readable");
        s3.close();
        s1.close();
    }

    // FileImageInputStreamSpi creates a working stream from a real temp file
    static void testFileSpiRoundTrip() throws Exception {
        ImageInputStreamSpi fileSpi = findByInputClass(File.class);
        File f = new File(System.getProperty("java.io.tmpdir"),
                          "imgio-spi-ImageInputStreamSpi-" + (tmpCounter++) + ".bin");
        f.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(f);
        fos.write(new byte[] { 7, 8, 9 });
        fos.close();
        try {
            ImageInputStream iis = fileSpi.createInputStreamInstance(f, false, null);
            check(iis != null, "File spi creates a stream from a File");
            check(iis.read() == 7, "first byte round-trips");
            check(iis.readShort() == (8 << 8 | 9), "remaining bytes round-trip big-endian");
            check(iis.read() == -1, "stream exhausted");
            check(iis.isCached() == false, "FileImageInputStream is not cached");
            iis.close();
        } finally {
            f.delete();
        }
    }
}
