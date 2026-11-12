/*
 * @test
 * @summary JVMS 5.6 / System.load javadoc: a native library is loaded into
 *          the name space of the class loader on whose behalf the load was
 *          initiated.  A library path already loaded on behalf of one loader
 *          cannot be loaded on behalf of a different loader - the attempt
 *          fails with UnsatisfiedLinkError reporting the library as already
 *          loaded - while the same loader may load the same path repeatedly
 *          and different loaders may each load their own copy of distinct
 *          paths.  loadLibrary also maps the bare library name to
 *          lib<name>.so through java.library.path.
 * @library /lib
 * @run main/othervm -Djava.library.path=. NativeLibraryNamespaceTest
 */

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

public class NativeLibraryNamespaceTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean gccAvailable() {
        try {
            Process p = new ProcessBuilder("gcc", "--version").start();
            try { p.getInputStream().close(); } catch (Exception ignore) { }
            try { p.getOutputStream().close(); } catch (Exception ignore) { }
            try { p.getErrorStream().close(); } catch (Exception ignore) { }
            return p.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    static final String C_SRC =
        "#include <jni.h>\n" +
        "JNIEXPORT jint JNICALL Java_NativeLibraryNamespaceTest_nsAdd(JNIEnv* e, jclass c, jint a, jint b) {\n" +
        "    return a + b;\n" +
        "}\n";

    static native int nsAdd(int a, int b);

    static Path jdkHome() {
        Path p = Paths.get(System.getProperty("java.home"));
        return p.endsWith("jre") ? p.getParent() : p;
    }

    static Path compile(String soName) throws Exception {
        Path dir = Paths.get(System.getProperty("user.dir"));
        Path cFile = dir.resolve("jckns.c");
        Path so = dir.resolve(soName);
        Files.write(cFile, C_SRC.getBytes("UTF-8"));
        Path inc = jdkHome().resolve("include");
        ProcessBuilder pb = new ProcessBuilder("gcc", "-shared", "-fPIC",
                "-I" + inc, "-I" + inc.resolve("linux"),
                "-o", so.toString(), cFile.toString());
        pb.redirectErrorStream(true);
        Process p = pb.start();
        byte[] all = readAll(p.getInputStream());
        if (!p.waitFor(60, TimeUnit.SECONDS)) {
            p.destroyForcibly();
            throw new AssertionError("gcc did not finish within 60s");
        }
        if (p.exitValue() != 0)
            throw new AssertionError("gcc failed rc=" + p.exitValue()
                    + " output: " + new String(all, "UTF-8"));
        return so;
    }

    static byte[] readAll(java.io.InputStream in) throws java.io.IOException {
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
        return out.toByteArray();
    }

    // A tiny loader-visible helper compiled at run time into its own
    // directory, so two URLClassLoaders over that directory each define
    // their own copy of the class and System.load is initiated on behalf
    // of two distinct defining loaders.
    static final String LIB_LOAD_SRC =
        "public class LibLoad {\n" +
        "    public static void load(String p) { System.load(p); }\n" +
        "}\n";

    static void invokeLoad(Class<?> libLoad, String path) throws Throwable {
        Method load = libLoad.getMethod("load", String.class);
        try {
            load.invoke(null, path);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    public static void main(String[] args) throws Throwable {
        if (!gccAvailable()) {
            throw new jtreg.SkippedException("no C compiler (gcc) available; native library cannot be built");
        }
        Path libA = compile("libjcknsa.so");
        Path libB = compile("libjcknsb.so");
        String pathA = libA.toAbsolutePath().toString();
        String pathB = libB.toAbsolutePath().toString();

        // Library-name mapping: loadLibrary finds lib<name>.so on
        // java.library.path and loads it on behalf of this class's loader.
        String mapped = System.mapLibraryName("jcknsa");
        if (System.getProperty("os.name").toLowerCase().contains("linux")) {
            check(mapped.equals("libjcknsa.so"),
                    "mapLibraryName on Linux is lib<name>.so (got: " + mapped + ")");
        }
        check(mapped.equals(libA.getFileName().toString()),
                "loadLibrary name maps to the file the JVM loads");
        System.loadLibrary("jcknsa");
        check(nsAdd(2, 3) == 5, "loadLibrary by name binds the method");

        // Build the helper class off the test classpath.
        Path dir = Paths.get(System.getProperty("user.dir"));
        Path nsDir = dir.resolve("nsloader");
        Files.createDirectories(nsDir);
        Files.write(nsDir.resolve("LibLoad.java"), LIB_LOAD_SRC.getBytes("UTF-8"));
        javax.tools.JavaCompiler jc = javax.tools.ToolProvider.getSystemJavaCompiler();
        check(jc != null, "system Java compiler is available");
        int rc = jc.run(null, null, System.err,
                "-d", nsDir.toString(), nsDir.resolve("LibLoad.java").toString());
        check(rc == 0, "LibLoad compiles");

        URL url = nsDir.toUri().toURL();
        ClassLoader l1 = new URLClassLoader(new URL[]{url},
                NativeLibraryNamespaceTest.class.getClassLoader());
        ClassLoader l2 = new URLClassLoader(new URL[]{url},
                NativeLibraryNamespaceTest.class.getClassLoader());
        Class<?> lib1 = l1.loadClass("LibLoad");
        Class<?> lib2 = l2.loadClass("LibLoad");
        check(lib1 != lib2, "two URLClassLoaders define distinct helper classes");

        // libA is already loaded on behalf of the test's loader; a second
        // load on behalf of l1 is a namespace violation.
        try {
            invokeLoad(lib1, pathA);
            check(false, "loading on behalf of a second loader must fail");
        } catch (UnsatisfiedLinkError e) {
            check(String.valueOf(e.getMessage()).contains("already loaded"),
                    "cross-loader ULE reports the library as already loaded (got: " + e.getMessage() + ")");
        }

        // A distinct path is a distinct library: l1 may load its own copy.
        invokeLoad(lib1, pathB);
        check(true, "loader 1 loads a distinct library path");

        // The same path cannot be loaded on behalf of l2 either.
        try {
            invokeLoad(lib2, pathB);
            check(false, "second loader must not load loader 1's library");
        } catch (UnsatisfiedLinkError e) {
            check(String.valueOf(e.getMessage()).contains("already loaded"),
                    "second cross-loader ULE reports already loaded (got: " + e.getMessage() + ")");
        }

        // The same loader loading its own path again is idempotent.
        invokeLoad(lib1, pathB);
        check(true, "same loader reloads its own path without error");

        ((URLClassLoader) l1).close();
        ((URLClassLoader) l2).close();

        System.out.println("PASS (" + passed + " assertions)");
    }
}
