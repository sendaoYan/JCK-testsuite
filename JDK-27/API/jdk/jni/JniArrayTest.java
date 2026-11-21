/*
 * @test
 * @summary JNI function matrix - primitive arrays: NewLongArray / NewDoubleArray
 *          / NewByteArray, Get/SetLongArrayRegion and Get/SetDoubleArrayRegion,
 *          GetLongArrayElements/ReleaseLongArrayElements (copy-back) and
 *          GetByteArrayElements/ReleaseByteArrayElements (JNI_ABORT), and
 *          GetPrimitiveArrayCritical/ReleasePrimitiveArrayCritical.
 * @library /lib
 * @run main/othervm --enable-native-access=ALL-UNNAMED JniArrayTest
 */

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Conformance test for the JNIEnv primitive-array functions against a real
 * native library compiled at run time (same pipeline as
 * {@code JniSemanticsTest}: the canonical C source {@code jniarray.c} is
 * embedded below, written to the scratch dir, built with gcc against the JDK
 * under test's own jni.h, and loaded via System.load).
 */
public class JniArrayTest {
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
    // Native library source (canonical copy of jniarray.c)
    // ------------------------------------------------------------------
    static final String C_SRC =
                "/*\n" +
        " * Canonical native source for JniArrayTest (JNI primitive-array matrix).\n" +
        " *\n" +
        " * Covers: NewLongArray / NewDoubleArray / NewByteArray, Get/SetLongArrayRegion,\n" +
        " * Get/SetDoubleArrayRegion, GetLongArrayElements / ReleaseLongArrayElements,\n" +
        " * GetByteArrayElements / ReleaseByteArrayElements, and\n" +
        " * GetPrimitiveArrayCritical / ReleasePrimitiveArrayCritical.\n" +
        " *\n" +
        " * This file is the single source of truth: `make native` compiles it into\n" +
        " * build/native/<side>-<arch>/libjniarray.so, and JniArrayTest.java embeds an\n" +
        " * identical copy compiled at run time when no -nativepath is supplied.\n" +
        " *\n" +
        " * Manual build command:\n" +
        " *   gcc -shared -fPIC -I<jdk>/include -I<jdk>/include/linux \\\n" +
        " *       -o libjniarray.so jniarray.c\n" +
        " */\n" +
        "#include <jni.h>\n" +
        "#include <stdlib.h>\n" +
        "\n" +
        "JNIEXPORT jlongArray JNICALL Java_JniArrayTest_copyLongs(JNIEnv* e, jclass c, jlongArray a) {\n" +
        "    jsize n = (*e)->GetArrayLength(e, a);\n" +
        "    jlongArray r = (*e)->NewLongArray(e, n);\n" +
        "    jlong* tmp;\n" +
        "    if (r == 0) return 0;\n" +
        "    if (n == 0) return r;\n" +
        "    tmp = (jlong*)malloc((size_t)n * sizeof(jlong));\n" +
        "    if (tmp == 0) return 0;\n" +
        "    (*e)->GetLongArrayRegion(e, a, 0, n, tmp);\n" +
        "    (*e)->SetLongArrayRegion(e, r, 0, n, tmp);\n" +
        "    free(tmp);\n" +
        "    return r;\n" +
        "}\n" +
        "\n" +
        "JNIEXPORT void JNICALL Java_JniArrayTest_bumpLongs(JNIEnv* e, jclass c, jlongArray a) {\n" +
        "    jsize n = (*e)->GetArrayLength(e, a);\n" +
        "    jlong* p = (*e)->GetLongArrayElements(e, a, 0);\n" +
        "    jsize i;\n" +
        "    if (p == 0) return;\n" +
        "    for (i = 0; i < n; i++) p[i] += 1;\n" +
        "    (*e)->ReleaseLongArrayElements(e, a, p, 0);\n" +
        "}\n" +
        "\n" +
        "JNIEXPORT jdoubleArray JNICALL Java_JniArrayTest_copyDoubles(JNIEnv* e, jclass c, jdoubleArray a) {\n" +
        "    jsize n = (*e)->GetArrayLength(e, a);\n" +
        "    jdoubleArray r = (*e)->NewDoubleArray(e, n);\n" +
        "    jdouble* tmp;\n" +
        "    if (r == 0) return 0;\n" +
        "    if (n == 0) return r;\n" +
        "    tmp = (jdouble*)malloc((size_t)n * sizeof(jdouble));\n" +
        "    if (tmp == 0) return 0;\n" +
        "    (*e)->GetDoubleArrayRegion(e, a, 0, n, tmp);\n" +
        "    (*e)->SetDoubleArrayRegion(e, r, 0, n, tmp);\n" +
        "    free(tmp);\n" +
        "    return r;\n" +
        "}\n" +
        "\n" +
        "JNIEXPORT jbyteArray JNICALL Java_JniArrayTest_copyBytes(JNIEnv* e, jclass c, jbyteArray a) {\n" +
        "    jsize n = (*e)->GetArrayLength(e, a);\n" +
        "    jbyteArray r = (*e)->NewByteArray(e, n);\n" +
        "    jbyte* p = (*e)->GetByteArrayElements(e, a, 0);\n" +
        "    if (r == 0) return 0;\n" +
        "    if (n == 0) return r;\n" +
        "    if (p == 0) return 0;\n" +
        "    (*e)->SetByteArrayRegion(e, r, 0, n, p);\n" +
        "    (*e)->ReleaseByteArrayElements(e, a, p, JNI_ABORT);\n" +
        "    return r;\n" +
        "}\n" +
        "\n" +
        "/* NewLongArray of a given length; the length is read back via GetArrayLength. */\n" +
        "JNIEXPORT jint JNICALL Java_JniArrayTest_newLongArrayLen(JNIEnv* e, jclass c, jint n) {\n" +
        "    jlongArray a = (*e)->NewLongArray(e, n);\n" +
        "    if (a == 0) return -1;\n" +
        "    return (*e)->GetArrayLength(e, a);\n" +
        "}\n" +
        "\n" +
        "/* GetPrimitiveArrayCritical / ReleasePrimitiveArrayCritical.  GetArrayLength is\n" +
        "   read BEFORE the critical region; no JNI calls are made inside it. */\n" +
        "JNIEXPORT jint JNICALL Java_JniArrayTest_criticalSum(JNIEnv* e, jclass c, jintArray a) {\n" +
        "    jsize n = (*e)->GetArrayLength(e, a);\n" +
        "    jint* p = (*e)->GetPrimitiveArrayCritical(e, a, 0);\n" +
        "    jint sum = 0;\n" +
        "    jsize i;\n" +
        "    if (p == 0) return -1;\n" +
        "    for (i = 0; i < n; i++) sum += p[i];\n" +
        "    (*e)->ReleasePrimitiveArrayCritical(e, a, p, 0);\n" +
        "    return sum;\n" +
        "}\n" +
        "\n" +
        "JNIEXPORT void JNICALL Java_JniArrayTest_criticalBump(JNIEnv* e, jclass c, jintArray a) {\n" +
        "    jsize n = (*e)->GetArrayLength(e, a);\n" +
        "    jint* p = (*e)->GetPrimitiveArrayCritical(e, a, 0);\n" +
        "    jsize i;\n" +
        "    if (p == 0) return;\n" +
        "    for (i = 0; i < n; i++) p[i] += 1;\n" +
        "    (*e)->ReleasePrimitiveArrayCritical(e, a, p, 0);\n" +
        "}\n";

    // ------------------------------------------------------------------
    // Native method declarations (bound by the library above)
    // ------------------------------------------------------------------
    native static long[] copyLongs(long[] a);
    native static void bumpLongs(long[] a);
    native static double[] copyDoubles(double[] a);
    native static byte[] copyBytes(byte[] a);
    native static int newLongArrayLen(int n);
    native static int criticalSum(int[] a);
    native static void criticalBump(int[] a);

    // ------------------------------------------------------------------
    // Build helpers: locate jni.h for the JDK under test and compile
    // ------------------------------------------------------------------
    static Path jdkHome() {
        Path p = Paths.get(System.getProperty("java.home"));
        return p.endsWith("jre") ? p.getParent() : p;
    }

    static void compileAndLoad() throws Exception {
        // Cross-arch support: with jtreg -nativepath set, load the prebuilt
        // library directly (built by `make native` from jniarray.c).
        String np = System.getProperty("test.nativepath");
        if (np != null) {
            Path pre = Paths.get(np).resolve("libjniarray.so");
            if (Files.exists(pre)) {
                System.load(pre.toAbsolutePath().toString());
                return;
            }
            throw new AssertionError("test.nativepath set but no prebuilt JNI library found (run: make native)");
        }
        Path dir = Paths.get(System.getProperty("user.dir"));
        Path cFile = dir.resolve("jniarray.c");
        Path so = dir.resolve("libjniarray.so");
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

        testArrays();

        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testArrays() {
        long[] src = {0L, Long.MIN_VALUE, Long.MAX_VALUE, -1L, 42L};
        long[] copy = copyLongs(src);
        check(copy != src, "copyLongs returns a new array");
        check(Arrays.equals(src, copy), "copyLongs contents equal");
        check(copyLongs(new long[0]).length == 0, "copyLongs of empty array");
        long[] big = new long[257];
        for (int i = 0; i < big.length; i++) big[i] = i * 1000000009L - 5L;
        check(Arrays.equals(big, copyLongs(big)), "copyLongs 257 elements");
        long[] bump = {10L, 20L, 30L};
        bumpLongs(bump);
        check(bump[0] == 11L && bump[1] == 21L && bump[2] == 31L,
                "GetLongArrayElements + Release(0) copies back");

        double[] ds = {0.5, -3.25, Double.NaN, Double.POSITIVE_INFINITY, 0.0};
        double[] dc = copyDoubles(ds);
        check(dc != ds, "copyDoubles returns a new array");
        check(Arrays.equals(ds, dc), "copyDoubles contents equal (NaN preserved)");
        check(copyDoubles(new double[0]).length == 0, "copyDoubles of empty array");

        byte[] bs = {0, -1, 127, -128, 100};
        byte[] bc = copyBytes(bs);
        check(bc != bs, "copyBytes returns a new array");
        check(Arrays.equals(bs, bc), "copyBytes contents equal");

        check(newLongArrayLen(5) == 5, "NewLongArray + GetArrayLength");
        check(newLongArrayLen(0) == 0, "NewLongArray(0)");

        check(criticalSum(new int[]{3, 1, 4}) == 8, "GetPrimitiveArrayCritical sum");
        check(criticalSum(new int[0]) == 0, "GetPrimitiveArrayCritical empty");
        int[] cb = {1, 2, 3};
        criticalBump(cb);
        check(cb[0] == 2 && cb[1] == 3 && cb[2] == 4,
                "ReleasePrimitiveArrayCritical(0) copies back");
    }
}
