/*
 * @test
 * @summary java.lang.Runtime native library lifecycle (behavioural): a real
 *          shared library compiled at run time is loaded through
 *          Runtime.load with the same semantics as System.load - success and
 *          idempotency for the same absolute path and loader, rejection of
 *          relative and empty paths with UnsatisfiedLinkError naming the
 *          requirement of an absolute path, NullPointerException for null,
 *          UnsatisfiedLinkError for a missing file, loadLibrary resolving
 *          lib<name>.so through java.library.path for both the success and
 *          failure paths, and the observation that System.load and
 *          Runtime.load share one registry per class loader.
 * @run main/othervm -Djava.library.path=. RuntimeLoadNativeTest full
 * @run main/othervm RuntimeLoadNativeTest bare
 */

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

/**
 * Runtime.load / Runtime.loadLibrary conformance against a real native
 * library compiled into the jtreg scratch directory.  The C source is
 * embedded; each JDK under test builds the library against its own jni.h.
 */
public class RuntimeLoadNativeTest {
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
        "JNIEXPORT jint JNICALL Java_RuntimeLoadNativeTest_jckMul(JNIEnv* e, jclass c, jint a, jint b) {\n" +
        "    return a * b;\n" +
        "}\n";

    static native int jckMul(int a, int b);

    static Path jdkHome() {
        Path p = Paths.get(System.getProperty("java.home"));
        return p.endsWith("jre") ? p.getParent() : p;
    }

    // Cross-arch support: with jtreg -nativepath set (property
    // test.nativepath) stage the prebuilt library into user.dir (keeping the
    // rt.loadLibrary/java.library.path assertions working) instead of
    // invoking the host gcc; without it the test self-compiles as before.
    static Path obtain() throws Exception {
        String np = System.getProperty("test.nativepath");
        if (np != null) {
            Path pre = Paths.get(np).resolve("libjckrtlife.so");
            if (!Files.exists(pre))
                throw new AssertionError("test.nativepath set but libjckrtlife.so missing (run: make native)");
            Path staged = Paths.get(System.getProperty("user.dir")).resolve("libjckrtlife.so");
            Files.copy(pre, staged, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            return staged;
        }
        return compile();
    }

    static Path compile() throws Exception {
        Path dir = Paths.get(System.getProperty("user.dir"));
        Path cFile = dir.resolve("jckrtlife.c");
        Path so = dir.resolve("libjckrtlife.so");
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

    public static void main(String[] args) throws Exception {
        if (System.getProperty("test.nativepath") == null && !gccAvailable()) {
            System.out.println("SKIP: no C compiler (gcc) available; native library cannot be built");
            return;
        }
        Runtime rt = Runtime.getRuntime();
        // "bare": second action without -Djava.library.path and, on newer JDKs,
        // without --enable-native-access: Runtime.load must still work.
        boolean bare = args.length > 0 && args[0].equals("bare");
        if (bare) {
            Path so = obtain();
            rt.load(so.toAbsolutePath().toString());
            check(jckMul(6, 7) == 42, "bare run: load and call");
            rt.load(so.toAbsolutePath().toString());
            check(jckMul(2, 3) == 6, "bare run: idempotent second load");
            System.out.println("PASS (" + passed + " assertions)");
            return;
        }

        Path so = obtain();
        String abs = so.toAbsolutePath().toString();

        // Runtime.load of an absolute path succeeds and binds the method.
        rt.load(abs);
        check(jckMul(6, 7) == 42, "bound jckMul after Runtime.load");
        check(jckMul(-3, 5) == -15, "jckMul negative operands");

        // Same absolute path, same loader: idempotent.
        rt.load(abs);
        check(jckMul(3, 3) == 9, "still bound after the idempotent second load");

        // System.load and Runtime.load share the same per-loader registry:
        // re-loading the identical path through System is also a no-op.
        System.load(abs);
        check(jckMul(4, 4) == 16, "System.load of the same path interoperates");

        // loadLibrary resolves the same library by name through java.library.path.
        rt.loadLibrary("jckrtlife");
        check(jckMul(5, 5) == 25, "Runtime.loadLibrary by name recognises the loaded path");

        // Non-absolute, empty, null and missing arguments.
        try {
            rt.load("relative-name.so");
            check(false, "Runtime.load(relative) must throw");
        } catch (UnsatisfiedLinkError e) {
            check(String.valueOf(e.getMessage()).contains("absolute path"),
                    "Runtime.load(relative) -> ULE demanding an absolute path (got: " + e.getMessage() + ")");
        }
        try {
            rt.load("");
            check(false, "Runtime.load(\"\") must throw");
        } catch (UnsatisfiedLinkError e) {
            check(String.valueOf(e.getMessage()).contains("absolute path"),
                    "Runtime.load(\"\") -> ULE demanding an absolute path (got: " + e.getMessage() + ")");
        }
        try {
            rt.load(null);
            check(false, "Runtime.load(null) must throw");
        } catch (NullPointerException expected) {
            check(true, "Runtime.load(null) -> NullPointerException");
        }
        try {
            rt.loadLibrary(null);
            check(false, "Runtime.loadLibrary(null) must throw");
        } catch (NullPointerException expected) {
            check(true, "Runtime.loadLibrary(null) -> NullPointerException");
        }
        try {
            rt.loadLibrary("");
            check(false, "Runtime.loadLibrary(\"\") must throw");
        } catch (UnsatisfiedLinkError expected) {
            check(true, "Runtime.loadLibrary(\"\") -> UnsatisfiedLinkError");
        }
        try {
            rt.load(Paths.get(System.getProperty("user.dir"))
                    .resolve("libjckdefinitelymissing.so").toAbsolutePath().toString());
            check(false, "Runtime.load(missing file) must throw");
        } catch (UnsatisfiedLinkError expected) {
            check(true, "Runtime.load(missing file) -> UnsatisfiedLinkError");
        }
        try {
            rt.loadLibrary("jck_no_such_rt_library");
            check(false, "Runtime.loadLibrary(unknown) must throw");
        } catch (UnsatisfiedLinkError expected) {
            check(true, "Runtime.loadLibrary(unknown) -> UnsatisfiedLinkError");
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
