/*
 * @test
 * @summary JNI function matrix - exceptions: ExceptionOccurred (NULL when
 *          clean, the pending throwable otherwise), Throw (delivers an existing
 *          throwable, and ExceptionOccurred returns that exact object), ThrowNew
 *          (occurred/check/clear state machine), and ExceptionDescribe (prints
 *          and clears the pending exception).
 * @library /lib
 * @run main/othervm --enable-native-access=ALL-UNNAMED JniExceptionTest
 */

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

/**
 * Conformance test for the JNIEnv exception functions against a real native
 * library compiled at run time (same pipeline as {@code JniSemanticsTest}: the
 * canonical C source {@code jniexception.c} is embedded below, written to the
 * scratch dir, built with gcc against the JDK under test's own jni.h, and
 * loaded via System.load).
 */
public class JniExceptionTest {
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

    // ------------------------------------------------------------------
    // Native library source (canonical copy of jniexception.c)
    // ------------------------------------------------------------------
    static final String C_SRC =
                "/*\n" +
        " * Canonical native source for JniExceptionTest (JNI exception matrix).\n" +
        " *\n" +
        " * Covers: ExceptionOccurred (null when clean, pending throwable otherwise),\n" +
        " * Throw (delivers an existing throwable, and ExceptionOccurred returns that\n" +
        " * exact object), ThrowNew (re-checked for the occurred/check/clear state\n" +
        " * machine), and ExceptionDescribe (prints and clears the pending exception).\n" +
        " *\n" +
        " * This file is the single source of truth: `make native` compiles it into\n" +
        " * build/native/<side>-<arch>/libjniexception.so, and JniExceptionTest.java\n" +
        " * embeds an identical copy compiled at run time when no -nativepath is given.\n" +
        " *\n" +
        " * Manual build command:\n" +
        " *   gcc -shared -fPIC -I<jdk>/include -I<jdk>/include/linux \\\n" +
        " *       -o libjniexception.so jniexception.c\n" +
        " */\n" +
        "#include <jni.h>\n" +
        "\n" +
        "/* ExceptionOccurred returns NULL when no exception is pending. */\n" +
        "JNIEXPORT jint JNICALL Java_JniExceptionTest_occurredClean(JNIEnv* e, jclass c) {\n" +
        "    return (*e)->ExceptionOccurred(e) == 0 ? 0 : 1;\n" +
        "}\n" +
        "\n" +
        "/* ThrowNew + ExceptionOccurred/ExceptionCheck/ExceptionClear state machine.\n" +
        "   Returns bit 0 = occurred non-null, bit 1 = check true, bit 2 = clean after. */\n" +
        "JNIEXPORT jint JNICALL Java_JniExceptionTest_occurredAfterThrowNew(JNIEnv* e, jclass c) {\n" +
        "    jclass k = (*e)->FindClass(e, \"java/lang/IllegalStateException\");\n" +
        "    jthrowable t;\n" +
        "    jint r = 0;\n" +
        "    if (k == 0) return -1;\n" +
        "    (*e)->ThrowNew(e, k, \"jck-msg\");\n" +
        "    t = (*e)->ExceptionOccurred(e);\n" +
        "    if (t != 0) r |= 1;\n" +
        "    if ((*e)->ExceptionCheck(e)) r |= 2;\n" +
        "    (*e)->ExceptionClear(e);\n" +
        "    if ((*e)->ExceptionOccurred(e) == 0) r |= 4;\n" +
        "    return r;\n" +
        "}\n" +
        "\n" +
        "/* Throw an existing throwable object; ExceptionOccurred must return that exact\n" +
        "   object (IsSameObject).  Returns 1 on success. */\n" +
        "JNIEXPORT jint JNICALL Java_JniExceptionTest_throwAndOccurred(JNIEnv* e, jclass c, jstring msg) {\n" +
        "    jclass k = (*e)->FindClass(e, \"java/lang/IllegalArgumentException\");\n" +
        "    jmethodID ctor;\n" +
        "    jthrowable t;\n" +
        "    jthrowable occurred;\n" +
        "    jint r = 0;\n" +
        "    if (k == 0) return -1;\n" +
        "    ctor = (*e)->GetMethodID(e, k, \"<init>\", \"(Ljava/lang/String;)V\");\n" +
        "    if (ctor == 0) return -2;\n" +
        "    t = (jthrowable)(*e)->NewObject(e, k, ctor, msg);\n" +
        "    (*e)->Throw(e, t);\n" +
        "    occurred = (*e)->ExceptionOccurred(e);\n" +
        "    if ((*e)->IsSameObject(e, t, occurred)) r |= 1;\n" +
        "    (*e)->ExceptionClear(e);\n" +
        "    return r;\n" +
        "}\n" +
        "\n" +
        "/* Throw an existing throwable that propagates out to the Java caller. */\n" +
        "JNIEXPORT void JNICALL Java_JniExceptionTest_throwCustom(JNIEnv* e, jclass c, jstring msg) {\n" +
        "    jclass k = (*e)->FindClass(e, \"java/lang/IllegalArgumentException\");\n" +
        "    jmethodID ctor = (*e)->GetMethodID(e, k, \"<init>\", \"(Ljava/lang/String;)V\");\n" +
        "    jthrowable t = (jthrowable)(*e)->NewObject(e, k, ctor, msg);\n" +
        "    (*e)->Throw(e, t);\n" +
        "}\n" +
        "\n" +
        "/* ExceptionDescribe prints the pending exception to stderr and, on the\n" +
        "   reference implementation, clears the pending exception as a side effect.\n" +
        "   Returns bit 0 = pending before the call, bit 1 = cleared after the call. */\n" +
        "JNIEXPORT jint JNICALL Java_JniExceptionTest_describe(JNIEnv* e, jclass c) {\n" +
        "    jclass k = (*e)->FindClass(e, \"java/lang/IllegalStateException\");\n" +
        "    jint r = 0;\n" +
        "    if (k == 0) return -1;\n" +
        "    (*e)->ThrowNew(e, k, \"describe-me\");\n" +
        "    if ((*e)->ExceptionCheck(e)) r |= 1;\n" +
        "    (*e)->ExceptionDescribe(e);\n" +
        "    if (!(*e)->ExceptionCheck(e)) r |= 2;\n" +
        "    if ((*e)->ExceptionCheck(e)) (*e)->ExceptionClear(e);\n" +
        "    return r;\n" +
        "}\n";

    // ------------------------------------------------------------------
    // Native method declarations (bound by the library above)
    // ------------------------------------------------------------------
    native static int occurredClean();
    native static int occurredAfterThrowNew();
    native static int throwAndOccurred(String msg);
    native static void throwCustom(String msg);
    native static int describe();

    // ------------------------------------------------------------------
    // Build helpers: locate jni.h for the JDK under test and compile
    // ------------------------------------------------------------------
    static Path jdkHome() {
        Path p = Paths.get(System.getProperty("java.home"));
        return p.endsWith("jre") ? p.getParent() : p;
    }

    static void compileAndLoad() throws Exception {
        // Cross-arch support: with jtreg -nativepath set, load the prebuilt
        // library directly (built by `make native` from jniexception.c).
        String np = System.getProperty("test.nativepath");
        if (np != null) {
            Path pre = Paths.get(np).resolve("libjniexception.so");
            if (Files.exists(pre)) {
                System.load(pre.toAbsolutePath().toString());
                return;
            }
            throw new AssertionError("test.nativepath set but no prebuilt JNI library found (run: make native)");
        }
        Path dir = Paths.get(System.getProperty("user.dir"));
        Path cFile = dir.resolve("jniexception.c");
        Path so = dir.resolve("libjniexception.so");
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
        System.load(so.toAbsolutePath().toString());
    }

    static byte[] readAll(java.io.InputStream in) throws java.io.IOException {
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
        return out.toByteArray();
    }

    // ------------------------------------------------------------------
    public static void main(String[] args) throws Exception {
        if (System.getProperty("test.nativepath") == null && !gccAvailable()) {
            throw new jtreg.SkippedException("no C compiler (gcc) available; native library cannot be built");
        }
        compileAndLoad();

        testExceptions();

        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testExceptions() {
        check(occurredClean() == 0, "ExceptionOccurred returns NULL when clean");
        check(occurredAfterThrowNew() == 7,
                "ThrowNew -> occurred non-null + check true + clean after clear");
        check(throwAndOccurred("jck") == 1,
                "Throw then ExceptionOccurred returns the same throwable");

        try {
            throwCustom("boom");
            check(false, "Throw must deliver the throwable to the Java caller");
        } catch (IllegalArgumentException e) {
            check(true, "Throw delivers the exact exception class");
            check("boom".equals(e.getMessage()), "Throw preserves the message");
        }

        check(describe() == 3, "ExceptionDescribe prints and clears the pending exception");
    }
}
