/*
 * @test
 * @summary javax.imageio.spi.ImageInputStreamSpi conformance: white-box structure + black-box behavior.
 * @run main ImageInputStreamSpiTest
 */

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageInputStreamSpi;
import javax.imageio.spi.IIOServiceProvider;
import javax.imageio.stream.ImageInputStream;

public class ImageInputStreamSpiTest {
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
        Class<?> c = Class.forName("javax.imageio.spi.ImageInputStreamSpi");
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
        Iterator<ImageInputStreamSpi> it =
            IIORegistry.getDefaultInstance().getServiceProviders(ImageInputStreamSpi.class, false);
        check(it != null, "providers iterator non-null");
        ImageInputStreamSpi inputStreamSpi = null;
        int count = 0;
        while (it.hasNext()) {
            ImageInputStreamSpi sp = it.next();
            check(sp.getInputClass() != null, "getInputClass non-null");
            sp.canUseCacheFile();
            sp.needsCacheFile();
            check(true, "cache-file queries return normally");
            if (sp.getInputClass() == InputStream.class) inputStreamSpi = sp;
            count++;
        }
        check(count > 0, "at least one ImageInputStreamSpi registered");
        check(inputStreamSpi != null, "InputStream provider present");

        // input class round-trip
        check(inputStreamSpi.getInputClass() == InputStream.class, "getInputClass == InputStream.class");
        check(inputStreamSpi.canUseCacheFile() == true, "InputStream SPI can use cache file");
        check(inputStreamSpi.needsCacheFile() == false, "InputStream SPI does not need cache file");

        byte[] data = new byte[] { 1, 2, 3, 4, 5 };
        // 3-arg overload: memory cache (no temp file)
        ImageInputStream iis =
            inputStreamSpi.createInputStreamInstance(new ByteArrayInputStream(data), false, null);
        check(iis != null, "createInputStreamInstance 3-arg non-null");
        byte[] buf = new byte[data.length];
        iis.readFully(buf);
        check(java.util.Arrays.equals(buf, data), "round-trip bytes via 3-arg overload");
        check(iis.read() == -1, "stream exhausted after reading all bytes");
        iis.close();

        // 1-arg convenience overload
        ImageInputStream iis2 =
            inputStreamSpi.createInputStreamInstance(new ByteArrayInputStream(data));
        check(iis2 != null, "createInputStreamInstance 1-arg non-null");
        iis2.close();

        // wrong input type rejected
        final ImageInputStreamSpi isp = inputStreamSpi;
        expectThrows(IllegalArgumentException.class,
                     () -> isp.createInputStreamInstance("not-a-stream", false, null),
                     "wrong input type");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testRegisteredStreamSpiIdentity();
        testNullAndWrongInputRejected();
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
    static class RecordingInputSpi extends ImageInputStreamSpi {
        int calls; Object lastInput; Boolean lastUseCache; java.io.File lastCacheDir;
        RecordingInputSpi() { super("TinyVendor", "2.5", java.io.InputStream.class); }
        public String getDescription(java.util.Locale l) { return "recording input spi"; }
        public ImageInputStream createInputStreamInstance(Object input, boolean useCache, java.io.File cacheDir) {
            calls++; lastInput = input; lastUseCache = useCache; lastCacheDir = cacheDir;
            return new javax.imageio.stream.MemoryCacheImageInputStream(
                new java.io.ByteArrayInputStream(new byte[] { 42 }));
        }
    }

    static java.util.List<ImageInputStreamSpi> allInputSpis() {
        java.util.List<ImageInputStreamSpi> out = new java.util.ArrayList<ImageInputStreamSpi>();
        Iterator<ImageInputStreamSpi> it =
            IIORegistry.getDefaultInstance().getServiceProviders(ImageInputStreamSpi.class, false);
        while (it.hasNext()) out.add(it.next());
        return out;
    }

    static ImageInputStreamSpi findByInputClass(Class<?> wanted) {
        for (ImageInputStreamSpi sp : allInputSpis()) if (sp.getInputClass() == wanted) return sp;
        throw new AssertionError("no registered spi with input class " + wanted.getName());
    }

    // identity: input classes, vendor and cache-file flags of the JDK-registered providers
    static void testRegisteredStreamSpiIdentity() {
        java.util.List<ImageInputStreamSpi> all = allInputSpis();
        check(all.size() >= 3, "at least three registered ImageInputStreamSpi (InputStream/File/RAF)");
        java.util.Set<Class<?>> inputClasses = new java.util.HashSet<Class<?>>();
        for (ImageInputStreamSpi sp : all) {
            check(sp.getVendorName() != null, "vendor non-null");
            check("Oracle Corporation".equals(sp.getVendorName()),
                  "OpenJDK-built stream spi vendor Oracle Corporation");
            check(sp.getVersion() != null, "version non-null");
            inputClasses.add(sp.getInputClass());
        }
        check(inputClasses.contains(java.io.InputStream.class), "InputStream input class present");
        check(inputClasses.contains(java.io.File.class), "File input class present");
        check(inputClasses.contains(java.io.RandomAccessFile.class), "RandomAccessFile input class present");
        ImageInputStreamSpi fileSpi = findByInputClass(java.io.File.class);
        check(fileSpi.canUseCacheFile() == false, "File spi canUseCacheFile false");
        check(fileSpi.needsCacheFile() == false, "File spi needsCacheFile false");
        ImageInputStreamSpi rafSpi = findByInputClass(java.io.RandomAccessFile.class);
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
        check(sp.getInputClass() == java.io.InputStream.class, "tiny spi input class round-trips");
        check(sp.getVendorName().equals("TinyVendor"), "tiny spi vendor round-trips");
        check(sp.getVersion().equals("2.5"), "tiny spi version round-trips");
        check(sp.canUseCacheFile() == false, "canUseCacheFile default false in subclass");
        check(sp.needsCacheFile() == false, "needsCacheFile default false in subclass");
        check(sp.getDescription(java.util.Locale.US).equals("recording input spi"), "description round-trips");
        check(sp.getDescription(null).equals("recording input spi"), "description null locale ok");

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

    static int tmpCounter;

    // FileImageInputStreamSpi creates a working stream from a real temp file
    static void testFileSpiRoundTrip() throws Exception {
        ImageInputStreamSpi fileSpi = findByInputClass(java.io.File.class);
        java.io.File f = new java.io.File(System.getProperty("java.io.tmpdir"),
                          "imgio-spi-ImageInputStreamSpi-" + (tmpCounter++) + ".bin");
        java.io.FileOutputStream fos = new java.io.FileOutputStream(f);
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
