/*
 * @test
 * @summary java.lang.System native library lifecycle (behavioural): a real
 *          shared library compiled at run time with gcc -shared -fPIC is
 *          loaded with System.load, the second load of the same absolute path
 *          by the same class loader is idempotent, a non-ELF file is rejected
 *          with UnsatisfiedLinkError, a library without the expected symbol
 *          loads but the call fails with UnsatisfiedLinkError naming the
 *          method, loadLibrary resolves lib<name>.so through
 *          java.library.path, relative and empty paths are rejected, and null
 *          arguments throw NullPointerException.
 * @library /lib
 * @run main/othervm -Djava.library.path=. SystemLoadNativeTest full
 * @run main/othervm SystemLoadNativeTest bare
 */

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

/**
 * System.load / System.loadLibrary conformance against real native libraries.
 *
 * Two libraries are produced into the jtreg scratch directory (user.dir):
 * libjcksyslife.so implements Java_SystemLoadNativeTest_jckAdd, and
 * libjcknosym.so deliberately exports no JNI symbols at all.  The C sources
 * are embedded as string constants so the repository keeps no binaries; each
 * JDK under test compiles the library against its own jni.h.
 */
public class SystemLoadNativeTest {
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
        "JNIEXPORT jint JNICALL Java_SystemLoadNativeTest_jckAdd(JNIEnv* e, jclass c, jint a, jint b) {\n" +
        "    return a + b;\n" +
        "}\n";

    static final String C_SRC_NO_SYMBOL =
        "/* exports no JNI symbol: loading succeeds, binding must fail */\n" +
        "int jck_no_symbol_dummy(void) { return 1; }\n";

    static native int jckAdd(int a, int b);

    static native void jckMissing();

    static native long jckMissing2(int a, long b);

    // ------------------------------------------------------------------
    // Library acquisition: prefer a prebuilt library from jtreg's
    // -nativepath (property test.nativepath) so cross-arch runs (e.g. a
    // riscv64 JDK under qemu) never invoke the host gcc; the prebuilt .so is
    // staged into user.dir so the loadLibrary/java.library.path assertions
    // keep working unchanged. Without test.nativepath the test self-compiles
    // from the embedded C source against the JDK under test's own jni.h.
    // ------------------------------------------------------------------
    static Path obtain() throws Exception {
        String np = System.getProperty("test.nativepath");
        if (np != null) {
            Path pre = Paths.get(np).resolve("libjcksyslife.so");
            if (!Files.exists(pre))
                throw new AssertionError("test.nativepath set but libjcksyslife.so missing (run: make native)");
            Path staged = Paths.get(System.getProperty("user.dir")).resolve("libjcksyslife.so");
            Files.copy(pre, staged, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            // keep the assertion count identical to the self-compile mode,
            // whose compile() contributes a produced-file check
            check(Files.exists(staged), "prebuilt library staged into user.dir");
            return staged;
        }
        return compile(C_SRC, "jcksyslife.c", "libjcksyslife.so");
    }

    // The no-symbol library: in nativepath mode reuse a sibling prebuilt
    // library (real ELF exporting a different test's JNI symbols, hence none
    // of ours); otherwise self-compile the deliberately bare source.
    static Path obtainNoSym() throws Exception {
        String np = System.getProperty("test.nativepath");
        if (np != null) {
            for (String alt : new String[] {"libjckrtlife.so", "libjckjni.so", "libjnisemantics.so"}) {
                Path pre = Paths.get(np).resolve(alt);
                if (Files.exists(pre)) {
                    // count parity with the self-compile mode (see obtain())
                    check(Files.size(pre) > 0, "sibling library present for the no-symbol case");
                    return pre;
                }
            }
            throw new AssertionError("test.nativepath set but no sibling library found for the no-symbol case");
        }
        return compile(C_SRC_NO_SYMBOL, "jcknosym.c", "libjcknosym.so");
    }

    static Path jdkHome() {
        Path p = Paths.get(System.getProperty("java.home"));
        return p.endsWith("jre") ? p.getParent() : p;
    }

    static Path compile(String cSource, String cName, String soName) throws Exception {
        Path dir = Paths.get(System.getProperty("user.dir"));
        Path cFile = dir.resolve(cName);
        Path so = dir.resolve(soName);
        Files.write(cFile, cSource.getBytes("UTF-8"));
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
        check(Files.exists(so), soName + " produced");
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
            throw new jtreg.SkippedException("no C compiler (gcc) available; native library cannot be built");
        }
        // "bare": a second action without -Djava.library.path and, on newer
        // JDKs, without --enable-native-access: System.load must still work.
        boolean bare = args.length > 0 && args[0].equals("bare");
        if (bare) {
            Path so = obtain();
            System.load(so.toAbsolutePath().toString());
            check(jckAdd(20, 22) == 42, "bare run: load and call");
            System.load(so.toAbsolutePath().toString());
            check(jckAdd(1, 1) == 2, "bare run: idempotent second load");
            System.out.println("PASS (" + passed + " assertions)");
            return;
        }

        Path so = obtain();

        // System.load of an absolute path succeeds and binds the native method.
        System.load(so.toAbsolutePath().toString());
        check(jckAdd(20, 22) == 42, "bound jckAdd after load");
        check(jckAdd(-5, 5) == 0, "jckAdd negative operands");

        // Loading the same path through the same class loader is a no-op.
        System.load(so.toAbsolutePath().toString());
        check(jckAdd(1, 2) == 3, "still bound after the idempotent second load");

        // loadLibrary resolves the very same library by name through
        // java.library.path and recognises it as already loaded for this loader.
        System.loadLibrary("jcksyslife");
        check(jckAdd(7, 8) == 15, "loadLibrary by name recognises the loaded path");

        // A non-shared-object file is rejected when loaded.
        Path junk = Paths.get(System.getProperty("user.dir")).resolve("jckjunk.so");
        Files.write(junk, new byte[]{0x12, 0x34, 0x56, 0x78, 0x1a, 0x2b, 0x00, 0x42, 0x7f});
        try {
            System.load(junk.toAbsolutePath().toString());
            check(false, "loading a non-library file must fail");
        } catch (UnsatisfiedLinkError e) {
            check(String.valueOf(e.getMessage()).contains(junk.getFileName().toString()),
                    "non-library ULE mentions the file (got: " + e.getMessage() + ")");
        }

        // A library that exports none of the expected symbols still loads;
        // each unresolved native method fails at call time naming the method.
        Path noSym = obtainNoSym();
        System.load(noSym.toAbsolutePath().toString());
        try {
            jckMissing();
            check(false, "missing symbol must fail at call time");
        } catch (UnsatisfiedLinkError e) {
            String msg = String.valueOf(e.getMessage());
            check(msg.contains("jckMissing"),
                    "ULE names the missing method (got: " + msg + ")");
            // JDK 8 formats the descriptor "SystemLoadNativeTest.jckMissing()V";
            // newer releases print 'void SystemLoadNativeTest.jckMissing()'.
            check(msg.contains("("), "ULE carries the method signature (got: " + msg + ")");
        }
        try {
            jckMissing2(1, 2L);
            check(false, "second missing symbol must fail too");
        } catch (UnsatisfiedLinkError e) {
            check(String.valueOf(e.getMessage()).contains("jckMissing2"),
                    "ULE names the second missing method (got: " + e.getMessage() + ")");
        }

        // loadLibrary failures and argument validation.
        try {
            System.loadLibrary("jck_no_such_library_xyz");
            check(false, "loadLibrary of an unknown library must fail");
        } catch (UnsatisfiedLinkError expected) {
            check(true, "loadLibrary of unknown library -> UnsatisfiedLinkError");
        }
        try {
            System.load(null);
            check(false, "load(null) must throw");
        } catch (NullPointerException expected) {
            check(true, "load(null) -> NullPointerException");
        }
        try {
            System.loadLibrary(null);
            check(false, "loadLibrary(null) must throw");
        } catch (NullPointerException expected) {
            check(true, "loadLibrary(null) -> NullPointerException");
        }
        try {
            System.load("");
            check(false, "load(\"\") must throw");
        } catch (UnsatisfiedLinkError e) {
            check(String.valueOf(e.getMessage()).contains("absolute path"),
                    "load(\"\") -> ULE demanding an absolute path (got: " + e.getMessage() + ")");
        }
        try {
            System.load("relative-name.so");
            check(false, "load(relative) must throw");
        } catch (UnsatisfiedLinkError e) {
            check(String.valueOf(e.getMessage()).contains("absolute path"),
                    "load(relative) -> ULE demanding an absolute path (got: " + e.getMessage() + ")");
        }

        // mapLibraryName: platform library-name mapping; on Linux this is
        // lib<name>.so, on other platforms only non-identity is guaranteed.
        String mapped = System.mapLibraryName("jcksyslife");
        check(mapped != null && !mapped.isEmpty() && !mapped.equals("jcksyslife"),
                "mapLibraryName maps the bare name (got: " + mapped + ")");
        if (System.getProperty("os.name").toLowerCase().contains("linux")) {
            check(mapped.equals("libjcksyslife.so"),
                    "mapLibraryName on Linux prefixes lib and appends .so (got: " + mapped + ")");
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
