/*
 * @test
 * @summary JNI function matrix - objects and references: NewObject (constructor
 *          invocation), AllocObject (uninitialized, default field values),
 *          NewLocalRef/DeleteLocalRef, EnsureLocalCapacity, and
 *          GetObjectRefType (local / global / weak-global / invalid).
 * @library /lib
 * @run main/othervm --enable-native-access=ALL-UNNAMED JniObjectTest
 */

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

/**
 * Conformance test for the JNIEnv object and reference functions against a real
 * native library compiled at run time (same pipeline as
 * {@code JniSemanticsTest}: the canonical C source {@code jniobject.c} is
 * embedded below, written to the scratch dir, built with gcc against the JDK
 * under test's own jni.h, and loaded via System.load).
 */
public class JniObjectTest {
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
    // Native library source (canonical copy of jniobject.c)
    // ------------------------------------------------------------------
    static final String C_SRC =
                "/*\n" +
        " * Canonical native source for JniObjectTest (JNI object & reference matrix).\n" +
        " *\n" +
        " * Covers: NewObject (constructor invocation), AllocObject (uninitialized),\n" +
        " * NewLocalRef / DeleteLocalRef, EnsureLocalCapacity, GetObjectRefType\n" +
        " * (local / global / weak-global / invalid reference classification).\n" +
        " *\n" +
        " * This file is the single source of truth: `make native` compiles it into\n" +
        " * build/native/<side>-<arch>/libjniobject.so, and JniObjectTest.java embeds an\n" +
        " * identical copy compiled at run time when no -nativepath is supplied.\n" +
        " *\n" +
        " * Manual build command:\n" +
        " *   gcc -shared -fPIC -I<jdk>/include -I<jdk>/include/linux \\\n" +
        " *       -o libjniobject.so jniobject.c\n" +
        " */\n" +
        "#include <jni.h>\n" +
        "\n" +
        "static jobject g_global = 0;\n" +
        "static jweak g_weak = 0;\n" +
        "\n" +
        "/* NewObject: invoke the (I)V constructor of the class. */\n" +
        "JNIEXPORT jobject JNICALL Java_JniObjectTest_newObject(JNIEnv* e, jclass c, jclass k, jint x) {\n" +
        "    jmethodID ctor = (*e)->GetMethodID(e, k, \"<init>\", \"(I)V\");\n" +
        "    if (ctor == 0) return 0;\n" +
        "    return (*e)->NewObject(e, k, ctor, x);\n" +
        "}\n" +
        "\n" +
        "/* AllocObject: allocate without running any constructor. */\n" +
        "JNIEXPORT jobject JNICALL Java_JniObjectTest_allocObject(JNIEnv* e, jclass c, jclass k) {\n" +
        "    return (*e)->AllocObject(e, k);\n" +
        "}\n" +
        "\n" +
        "/* NewLocalRef: create a new local reference (NULL in -> NULL out). */\n" +
        "JNIEXPORT jobject JNICALL Java_JniObjectTest_newLocalRef(JNIEnv* e, jclass c, jobject o) {\n" +
        "    return (*e)->NewLocalRef(e, o);\n" +
        "}\n" +
        "\n" +
        "/* DeleteLocalRef: free the local reference. */\n" +
        "JNIEXPORT void JNICALL Java_JniObjectTest_deleteLocalRef(JNIEnv* e, jclass c, jobject o) {\n" +
        "    (*e)->DeleteLocalRef(e, o);\n" +
        "}\n" +
        "\n" +
        "/* EnsureLocalCapacity: reserve room for cap local references. */\n" +
        "JNIEXPORT jint JNICALL Java_JniObjectTest_ensureLocalCapacity(JNIEnv* e, jclass c, jint cap) {\n" +
        "    return (*e)->EnsureLocalCapacity(e, cap);\n" +
        "}\n" +
        "\n" +
        "/* GetObjectRefType on the argument (a local reference). */\n" +
        "JNIEXPORT jint JNICALL Java_JniObjectTest_localRefType(JNIEnv* e, jclass c, jobject o) {\n" +
        "    return (jint)(*e)->GetObjectRefType(e, o);\n" +
        "}\n" +
        "\n" +
        "/* GetObjectRefType on NULL (a null reference is invalid). */\n" +
        "JNIEXPORT jint JNICALL Java_JniObjectTest_nullRefType(JNIEnv* e, jclass c) {\n" +
        "    return (jint)(*e)->GetObjectRefType(e, 0);\n" +
        "}\n" +
        "\n" +
        "JNIEXPORT void JNICALL Java_JniObjectTest_keepGlobalRef(JNIEnv* e, jclass c, jobject o) {\n" +
        "    if (g_global != 0) (*e)->DeleteGlobalRef(e, g_global);\n" +
        "    g_global = (*e)->NewGlobalRef(e, o);\n" +
        "}\n" +
        "\n" +
        "JNIEXPORT jint JNICALL Java_JniObjectTest_globalRefType(JNIEnv* e, jclass c) {\n" +
        "    return (jint)(*e)->GetObjectRefType(e, g_global);\n" +
        "}\n" +
        "\n" +
        "JNIEXPORT void JNICALL Java_JniObjectTest_makeWeakRef(JNIEnv* e, jclass c, jobject o) {\n" +
        "    if (g_weak != 0) (*e)->DeleteWeakGlobalRef(e, g_weak);\n" +
        "    g_weak = (*e)->NewWeakGlobalRef(e, o);\n" +
        "}\n" +
        "\n" +
        "JNIEXPORT jint JNICALL Java_JniObjectTest_weakRefType(JNIEnv* e, jclass c) {\n" +
        "    return (jint)(*e)->GetObjectRefType(e, g_weak);\n" +
        "}\n";

    // ------------------------------------------------------------------
    // Native method declarations (bound by the library above)
    // ------------------------------------------------------------------
    native static Object newObject(Class<?> k, int x);
    native static Object allocObject(Class<?> k);
    native static Object newLocalRef(Object o);
    native static void deleteLocalRef(Object o);
    native static int ensureLocalCapacity(int cap);
    native static int localRefType(Object o);
    native static int nullRefType();
    native static void keepGlobalRef(Object o);
    native static int globalRefType();
    native static void makeWeakRef(Object o);
    native static int weakRefType();

    // ------------------------------------------------------------------
    // Helper class the native code allocates and constructs
    // ------------------------------------------------------------------
    public static class Box {
        public int v;

        public Box(int v) {
            this.v = v;
        }
    }

    // ------------------------------------------------------------------
    // Build helpers: locate jni.h for the JDK under test and compile
    // ------------------------------------------------------------------
    static Path jdkHome() {
        Path p = Paths.get(System.getProperty("java.home"));
        return p.endsWith("jre") ? p.getParent() : p;
    }

    static void compileAndLoad() throws Exception {
        // Cross-arch support: with jtreg -nativepath set, load the prebuilt
        // library directly (built by `make native` from jniobject.c).
        String np = System.getProperty("test.nativepath");
        if (np != null) {
            Path pre = Paths.get(np).resolve("libjniobject.so");
            if (Files.exists(pre)) {
                System.load(pre.toAbsolutePath().toString());
                return;
            }
            throw new AssertionError("test.nativepath set but no prebuilt JNI library found (run: make native)");
        }
        Path dir = Paths.get(System.getProperty("user.dir"));
        Path cFile = dir.resolve("jniobject.c");
        Path so = dir.resolve("libjniobject.so");
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

        testObjects();

        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testObjects() {
        Box b = (Box) newObject(Box.class, 42);
        check(b != null, "NewObject returns an instance");
        check(b.v == 42, "NewObject invokes the constructor");

        Box uninit = (Box) allocObject(Box.class);
        check(uninit != null, "AllocObject returns an instance");
        check(uninit.v == 0, "AllocObject leaves fields at defaults (no constructor)");

        Object o = new Object();
        check(newLocalRef(o) == o, "NewLocalRef returns the same reference");
        check(newLocalRef(null) == null, "NewLocalRef(NULL) returns NULL");
        deleteLocalRef(o);
        check(o.getClass() == Object.class,
                "DeleteLocalRef does not invalidate the caller's reference");

        check(ensureLocalCapacity(16) == 0, "EnsureLocalCapacity(16) returns JNI_OK");
        check(ensureLocalCapacity(0) == 0, "EnsureLocalCapacity(0) returns JNI_OK");

        check(localRefType(new Object()) == 1, "GetObjectRefType local = JNILocalRefType(1)");
        check(nullRefType() == 0, "GetObjectRefType(NULL) = JNIInvalidRefType(0)");

        keepGlobalRef(o);
        check(globalRefType() == 2, "GetObjectRefType global = JNIGlobalRefType(2)");
        makeWeakRef(o);
        check(weakRefType() == 3, "GetObjectRefType weak global = JNIWeakGlobalRefType(3)");
    }
}
