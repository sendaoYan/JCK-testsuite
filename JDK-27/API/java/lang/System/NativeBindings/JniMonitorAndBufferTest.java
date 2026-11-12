/*
 * @test
 * @summary JNI function matrix - monitor operations (MonitorEnter/MonitorExit
 *          re-entrancy and same-lock identity with Java synchronized), NIO
 *          direct-buffer access (GetDirectBufferCapacity/GetDirectBufferAddress
 *          byte counts, heap / non-buffer -> -1, NULL -> NPE, NewDirectByteBuffer
 *          round-trip), and object-array ops (NewObjectArray /
 *          GetObjectArrayElement / SetObjectArrayElement / IsSameObject, OOB ->
 *          ArrayIndexOutOfBoundsException).
 * @library /lib
 * @run main/othervm --enable-native-access=ALL-UNNAMED JniMonitorAndBufferTest
 */

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Conformance test for the JNIEnv monitor, NIO direct-buffer, and object-array
 * functions against a real native library compiled at run time (same pipeline
 * as {@code JniSemanticsTest}: embedded C written to the scratch dir, built
 * with gcc against the JDK under test's own jni.h, loaded via System.load).
 */
public class JniMonitorAndBufferTest {
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
    // Native library source (C89/C99-portable, jni.h from the JDK under test)
    // ------------------------------------------------------------------
    static final String C_SRC =
        "#include <jni.h>\n" +
        "#include <stdlib.h>\n" +
        "#include <stdint.h>\n" +
        "\n" +
        "/* JNI function matrix: monitor operations, NIO direct-buffer access, and\n" +
        "   object-array handling.  Compiled at run time against the JDK under test's\n" +
        "   own jni.h; see JniMonitorAndBufferTest.java for the Java side.  All\n" +
        "   entries are static natives, so the second argument is the class. */\n" +
        "\n" +
        "/* ---- monitor operations (JNI spec \"Monitor Operations\") ---- */\n" +
        "\n" +
        "JNIEXPORT jint JNICALL Java_JniMonitorAndBufferTest_monitorEnterExit(JNIEnv* e, jclass c, jobject o) {\n" +
        "    if ((*e)->MonitorEnter(e, o) != 0) return -1;\n" +
        "    if ((*e)->MonitorExit(e, o) != 0) return -2;\n" +
        "    return 0;\n" +
        "}\n" +
        "\n" +
        "JNIEXPORT jint JNICALL Java_JniMonitorAndBufferTest_monitorEnterTwiceExitOnce(JNIEnv* e, jclass c, jobject o) {\n" +
        "    if ((*e)->MonitorEnter(e, o) != 0) return -1;\n" +
        "    if ((*e)->MonitorEnter(e, o) != 0) return -2;\n" +
        "    if ((*e)->MonitorExit(e, o) != 0) return -3;\n" +
        "    return 0; /* recursion count is now 1: the caller's monitor is still held */\n" +
        "}\n" +
        "\n" +
        "JNIEXPORT jint JNICALL Java_JniMonitorAndBufferTest_monitorExitOnce(JNIEnv* e, jclass c, jobject o) {\n" +
        "    return (*e)->MonitorExit(e, o);\n" +
        "}\n" +
        "\n" +
        "JNIEXPORT jint JNICALL Java_JniMonitorAndBufferTest_monitorEnterNull(JNIEnv* e, jclass c) {\n" +
        "    (*e)->MonitorEnter(e, 0); /* NULL object */\n" +
        "    {\n" +
        "        jint pending = (*e)->ExceptionCheck(e) ? 1 : 0;\n" +
        "        if ((*e)->ExceptionCheck(e)) (*e)->ExceptionClear(e);\n" +
        "        return pending;\n" +
        "    }\n" +
        "}\n" +
        "\n" +
        "/* Enters o's monitor, signals the entered latch, blocks until the release\n" +
        "   latch fires (via a callback into Java), then exits the monitor. */\n" +
        "JNIEXPORT void JNICALL Java_JniMonitorAndBufferTest_monitorEnterHoldExit(JNIEnv* e, jclass c, jobject o, jobject entered, jobject release) {\n" +
        "    jclass k = (*e)->FindClass(e, \"JniMonitorAndBufferTest\");\n" +
        "    jmethodID signal;\n" +
        "    jmethodID await;\n" +
        "    if (k == 0) return;\n" +
        "    signal = (*e)->GetStaticMethodID(e, k, \"signalEntered\", \"(Ljava/util/concurrent/CountDownLatch;)V\");\n" +
        "    await  = (*e)->GetStaticMethodID(e, k, \"awaitRelease\", \"(Ljava/util/concurrent/CountDownLatch;)V\");\n" +
        "    if (signal == 0 || await == 0) return;\n" +
        "    if ((*e)->MonitorEnter(e, o) != 0) return;\n" +
        "    (*e)->CallStaticVoidMethod(e, k, signal, entered);\n" +
        "    (*e)->CallStaticVoidMethod(e, k, await, release);\n" +
        "    (*e)->MonitorExit(e, o);\n" +
        "}\n" +
        "\n" +
        "/* ---- NIO direct buffers (JNI spec \"NIO Support\") ---- */\n" +
        "\n" +
        "JNIEXPORT jlong JNICALL Java_JniMonitorAndBufferTest_directBufferCapacity(JNIEnv* e, jclass c, jobject buf) {\n" +
        "    return (jlong)(*e)->GetDirectBufferCapacity(e, buf);\n" +
        "}\n" +
        "\n" +
        "JNIEXPORT jlong JNICALL Java_JniMonitorAndBufferTest_directBufferAddress(JNIEnv* e, jclass c, jobject buf) {\n" +
        "    return (jlong)(intptr_t)(*e)->GetDirectBufferAddress(e, buf);\n" +
        "}\n" +
        "\n" +
        "/* NULL is treated as \"not a direct buffer\": returns -1 with NO exception\n" +
        "   (unlike MonitorEnter(NULL), which throws NPE).  GetDirectBufferAddress is\n" +
        "   deliberately NOT probed with NULL here because that path dereferences the\n" +
        "   handle and crashes the VM - a documented do-not-call. */\n" +
        "JNIEXPORT jlong JNICALL Java_JniMonitorAndBufferTest_directBufferCapacityNull(JNIEnv* e, jclass c) {\n" +
        "    return (jlong)(*e)->GetDirectBufferCapacity(e, 0);\n" +
        "}\n" +
        "\n" +
        "/* Wraps freshly malloc'd memory into a direct ByteBuffer with a known byte\n" +
        "   pattern (byte i == i % 251). */\n" +
        "JNIEXPORT jobject JNICALL Java_JniMonitorAndBufferTest_newDirectByteBuffer(JNIEnv* e, jclass c, jint cap) {\n" +
        "    unsigned char* buf = (unsigned char*)malloc((size_t)cap);\n" +
        "    jint i;\n" +
        "    if (buf == 0) return 0;\n" +
        "    for (i = 0; i < cap; i++) buf[i] = (unsigned char)(i % 251);\n" +
        "    return (*e)->NewDirectByteBuffer(e, buf, (jlong)cap);\n" +
        "}\n" +
        "\n" +
        "/* ---- object arrays (JNI spec \"Operations on Arrays\") ---- */\n" +
        "\n" +
        "JNIEXPORT jint JNICALL Java_JniMonitorAndBufferTest_objectArrayLength(JNIEnv* e, jclass c, jobjectArray arr) {\n" +
        "    return (*e)->GetArrayLength(e, arr);\n" +
        "}\n" +
        "\n" +
        "/* Returns element 0 and overwrites element 1 with a native-built string. */\n" +
        "JNIEXPORT jobject JNICALL Java_JniMonitorAndBufferTest_objectArrayReadWrite(JNIEnv* e, jclass c, jobjectArray arr) {\n" +
        "    jobject first = (*e)->GetObjectArrayElement(e, arr, 0);\n" +
        "    jstring w = (*e)->NewStringUTF(e, \"written-by-native\");\n" +
        "    (*e)->SetObjectArrayElement(e, arr, 1, w);\n" +
        "    return first;\n" +
        "}\n" +
        "\n" +
        "/* Out-of-bounds read must leave a pending ArrayIndexOutOfBoundsException. */\n" +
        "JNIEXPORT jint JNICALL Java_JniMonitorAndBufferTest_objectArrayOOB(JNIEnv* e, jclass c, jobjectArray arr) {\n" +
        "    (*e)->GetObjectArrayElement(e, arr, -1);\n" +
        "    {\n" +
        "        jint pending = (*e)->ExceptionCheck(e) ? 1 : 0;\n" +
        "        if ((*e)->ExceptionCheck(e)) (*e)->ExceptionClear(e);\n" +
        "        return pending;\n" +
        "    }\n" +
        "}\n" +
        "\n" +
        "JNIEXPORT jobjectArray JNICALL Java_JniMonitorAndBufferTest_newObjectArray(JNIEnv* e, jclass c, jclass elemCls, jobject fill, jint len) {\n" +
        "    return (*e)->NewObjectArray(e, len, elemCls, fill);\n" +
        "}\n" +
        "\n" +
        "JNIEXPORT jboolean JNICALL Java_JniMonitorAndBufferTest_isSameObject(JNIEnv* e, jclass c, jobject a, jobject b) {\n" +
        "    return (*e)->IsSameObject(e, a, b);\n" +
        "}\n";

    // ------------------------------------------------------------------
    // Native method declarations (bound by the library above)
    // ------------------------------------------------------------------
    native static int monitorEnterExit(Object o);
    native static int monitorEnterTwiceExitOnce(Object o);
    native static int monitorExitOnce(Object o);
    native static int monitorEnterNull();
    native static void monitorEnterHoldExit(Object o, CountDownLatch entered, CountDownLatch release);
    native static long directBufferCapacity(Object buf);
    native static long directBufferAddress(Object buf);
    native static long directBufferCapacityNull();
    native static Object newDirectByteBuffer(int cap);
    native static int objectArrayLength(Object[] arr);
    native static Object objectArrayReadWrite(Object[] arr);
    native static int objectArrayOOB(Object[] arr);
    native static Object[] newObjectArray(Class<?> elemCls, Object fill, int len);
    native static boolean isSameObject(Object a, Object b);

    // ------------------------------------------------------------------
    // Static callbacks the native monitor holder reaches into by method id
    // ------------------------------------------------------------------
    static void signalEntered(CountDownLatch l) { l.countDown(); }
    static void awaitRelease(CountDownLatch l) {
        try { l.await(); } catch (InterruptedException e) { throw new AssertionError(e); }
    }

    // ------------------------------------------------------------------
    // Build helpers: locate jni.h for the JDK under test and compile
    // ------------------------------------------------------------------
    static Path jdkHome() {
        Path p = Paths.get(System.getProperty("java.home"));
        return p.endsWith("jre") ? p.getParent() : p;
    }

    static void compileAndLoad() throws Exception {
        // Cross-arch support: with jtreg -nativepath set (property
        // test.nativepath) load the prebuilt library directly; the build
        // system names it after the C source (libjnimonitorbuffer.so).
        String np = System.getProperty("test.nativepath");
        if (np != null) {
            Path pre = Paths.get(np).resolve("libjnimonitorbuffer.so");
            if (Files.exists(pre)) {
                System.load(pre.toAbsolutePath().toString());
                return;
            }
            throw new AssertionError("test.nativepath set but no prebuilt JNI library found (run: make native)");
        }
        Path dir = Paths.get(System.getProperty("user.dir"));
        Path cFile = dir.resolve("jnimonitorbuffer.c");
        Path so = dir.resolve("libjnimonitorbuffer.so");
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

        testMonitorSameLock();
        testMonitorReentrancy();
        testMonitorCrossThreadExclusion();
        testMonitorNull();
        testDirectBuffer();
        testObjectArray();

        System.out.println("PASS (" + passed + " assertions)");
    }

    // ------------------------------------------------------------------
    // Monitor: native MonitorEnter/MonitorExit act on the SAME monitor as
    // Java synchronized (re-entrant), and only decrement the recursion count.
    // ------------------------------------------------------------------
    static void testMonitorSameLock() {
        Object o = new Object();
        check(!Thread.holdsLock(o), "not held initially");
        synchronized (o) {
            check(Thread.holdsLock(o), "Java holds the monitor");
            // Native MonitorEnter + MonitorExit inside the Java-held monitor
            // must NOT deadlock (same lock, re-entrant) and must NOT release
            // Java's hold (only decrements recursion count).
            check(monitorEnterExit(o) == 0, "native enter/exit succeeds inside synchronized");
            check(Thread.holdsLock(o), "Java still holds after native MonitorExit");
        }
        check(!Thread.holdsLock(o), "released after synchronized block");
    }

    static void testMonitorReentrancy() {
        Object o = new Object();
        // enter twice, exit once -> recursion count is 1 -> still held.
        check(monitorEnterTwiceExitOnce(o) == 0, "native enter 2x exit 1x");
        check(Thread.holdsLock(o), "still held after 2 enters + 1 exit");
        // final exit releases it.
        check(monitorExitOnce(o) == 0, "native final exit");
        check(!Thread.holdsLock(o), "released after final exit");
    }

    static void testMonitorCrossThreadExclusion() throws Exception {
        Object o = new Object();
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        CountDownLatch gotLock = new CountDownLatch(1);
        Thread holder = new Thread(() -> monitorEnterHoldExit(o, entered, release));
        holder.start();
        check(entered.await(10, TimeUnit.SECONDS), "native holder entered the monitor");
        // A probe thread must be excluded while the native thread holds the
        // monitor; it can only count down after the holder releases.
        Thread probe = new Thread(() -> { synchronized (o) { gotLock.countDown(); } });
        probe.start();
        check(gotLock.getCount() == 1, "probe did not acquire before release");
        release.countDown();
        check(gotLock.await(10, TimeUnit.SECONDS), "probe acquired after native MonitorExit");
        holder.join(10000);
        check(!holder.isAlive(), "holder thread finished");
    }

    static void testMonitorNull() {
        check(monitorEnterNull() == 1, "MonitorEnter(NULL) leaves a pending NPE");
    }

    // ------------------------------------------------------------------
    // NIO direct buffers: capacity is in BYTES (not elements); heap and
    // non-buffer objects report -1; NULL throws NPE; NewDirectByteBuffer
    // round-trips a native buffer into Java.
    // ------------------------------------------------------------------
    static void testDirectBuffer() {
        ByteBuffer bb = ByteBuffer.allocateDirect(1024);
        check(directBufferCapacity(bb) == 1024, "DirectByteBuffer capacity in bytes");
        check(directBufferAddress(bb) != 0, "DirectByteBuffer has a non-null address");

        // GetDirectBufferCapacity returns the buffer's capacity() in ELEMENTS
        // for typed view buffers (verified identical on JDK 8 and 27): a
        // 32-byte direct buffer viewed as shorts has 16 elements, not 32 bytes.
        ShortBuffer sb = ByteBuffer.allocateDirect(32).asShortBuffer();
        check(directBufferCapacity(sb) == 16, "DirectShortBuffer view capacity = 16 elements");
        check(directBufferAddress(sb) != 0, "DirectShortBuffer view has a non-null address");

        IntBuffer ib = ByteBuffer.allocateDirect(64).asIntBuffer();
        check(directBufferCapacity(ib) == 16, "DirectIntBuffer view capacity = 16 elements");
        check(directBufferAddress(ib) != 0, "DirectIntBuffer view has a non-null address");

        ByteBuffer heap = ByteBuffer.allocate(1024);
        check(directBufferCapacity(heap) == -1, "heap ByteBuffer capacity = -1");
        check(directBufferAddress(heap) == 0, "heap ByteBuffer address = NULL");

        Object notABuffer = new Object();
        check(directBufferCapacity(notABuffer) == -1, "non-buffer object capacity = -1");
        check(directBufferAddress(notABuffer) == 0, "non-buffer object address = NULL");

        check(directBufferCapacityNull() == -1, "GetDirectBufferCapacity(NULL) returns -1 (no NPE)");

        ByteBuffer wrapped = (ByteBuffer) newDirectByteBuffer(16);
        check(wrapped.isDirect(), "NewDirectByteBuffer returns a direct buffer");
        check(wrapped.capacity() == 16, "NewDirectByteBuffer capacity");
        check(directBufferCapacity(wrapped) == 16, "wrapped buffer capacity round-trip");
        check(directBufferAddress(wrapped) != 0, "wrapped buffer address non-null");
        check((wrapped.get(0) & 0xFF) == 0, "wrapped byte 0 == 0");
        check((wrapped.get(1) & 0xFF) == 1, "wrapped byte 1 == 1");
        check((wrapped.get(15) & 0xFF) == 15, "wrapped byte 15 == 15");
    }

    // ------------------------------------------------------------------
    // Object arrays: length, element get/set, out-of-bounds exception,
    // NewObjectArray, IsSameObject identity.
    // ------------------------------------------------------------------
    static void testObjectArray() {
        String[] arr = new String[] { "zero", "one", "two" };
        check(objectArrayLength(arr) == 3, "object array length");

        Object first = objectArrayReadWrite(arr);
        check("zero".equals(first), "GetObjectArrayElement returns element 0");
        check("written-by-native".equals(arr[1]), "SetObjectArrayElement wrote element 1");

        check(objectArrayOOB(arr) == 1, "GetObjectArrayElement(-1) leaves a pending AIOOBE");

        Object[] made = newObjectArray(String.class, "fill", 4);
        check(made.length == 4, "NewObjectArray length");
        check("fill".equals(made[0]) && "fill".equals(made[3]), "NewObjectArray fills all slots");
        check(made[1] == made[0], "NewObjectArray fills with the same initial element");

        check(isSameObject(arr, arr), "IsSameObject same reference");
        check(!isSameObject(arr, new String[] { "zero", "one", "two" }), "IsSameObject different reference");
    }
}
