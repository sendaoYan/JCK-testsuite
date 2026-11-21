/*
 * @test
 * @summary JNI function matrix - class lookup and reflection: FindClass (binary
 *          name and array descriptors; a missing class yields NULL plus a
 *          pending exception), GetMethodID (a missing method yields NULL plus a
 *          pending exception), IsInstanceOf, and the reflected-method/field
 *          bridges FromReflectedMethod/ToReflectedMethod and
 *          FromReflectedField/ToReflectedField.
 * @library /lib
 * @run main/othervm --enable-native-access=ALL-UNNAMED JniLookupTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

/**
 * Conformance test for the JNIEnv class-lookup and reflection functions against
 * a real native library compiled at run time (same pipeline as
 * {@code JniSemanticsTest}: the canonical C source {@code jnilookup.c} is
 * embedded below, written to the scratch dir, built with gcc against the JDK
 * under test's own jni.h, and loaded via System.load).
 */
public class JniLookupTest {
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
    // Native library source (canonical copy of jnilookup.c)
    // ------------------------------------------------------------------
    static final String C_SRC =
                "/*\n" +
        " * Canonical native source for JniLookupTest (JNI lookup & reflection matrix).\n" +
        " *\n" +
        " * Covers: FindClass (found, and missing -> NULL plus a pending exception),\n" +
        " * GetMethodID (missing -> NULL plus a pending exception), IsInstanceOf,\n" +
        " * FromReflectedMethod/ToReflectedMethod, FromReflectedField/ToReflectedField.\n" +
        " *\n" +
        " * This file is the single source of truth: `make native` compiles it into\n" +
        " * build/native/<side>-<arch>/libjnilookup.so, and JniLookupTest.java embeds an\n" +
        " * identical copy that is compiled at run time when no -nativepath is supplied.\n" +
        " *\n" +
        " * Manual build command:\n" +
        " *   gcc -shared -fPIC -I<jdk>/include -I<jdk>/include/linux \\\n" +
        " *       -o libjnilookup.so jnilookup.c\n" +
        " */\n" +
        "#include <jni.h>\n" +
        "#include <stdlib.h>\n" +
        "\n" +
        "/* FindClass: resolve a binary name or an array descriptor. */\n" +
        "JNIEXPORT jclass JNICALL Java_JniLookupTest_findClass(JNIEnv* e, jclass c, jstring name) {\n" +
        "    const char* n = (*e)->GetStringUTFChars(e, name, 0);\n" +
        "    jclass r;\n" +
        "    if (n == 0) return 0;\n" +
        "    r = (*e)->FindClass(e, n);\n" +
        "    (*e)->ReleaseStringUTFChars(e, name, n);\n" +
        "    return r;\n" +
        "}\n" +
        "\n" +
        "/* FindClass of a missing class: NULL result plus a pending exception.\n" +
        "   Returns bit 0 = a pending throwable is present, bit 1 = result is NULL. */\n" +
        "JNIEXPORT jint JNICALL Java_JniLookupTest_findClassMissing(JNIEnv* e, jclass c) {\n" +
        "    jclass r = (*e)->FindClass(e, \"no/such/class/JckMissing\");\n" +
        "    jthrowable t = (*e)->ExceptionOccurred(e);\n" +
        "    jint bits = 0;\n" +
        "    if (r == 0) bits |= 2;\n" +
        "    if (t != 0) bits |= 1;\n" +
        "    if ((*e)->ExceptionCheck(e)) (*e)->ExceptionClear(e);\n" +
        "    return bits;\n" +
        "}\n" +
        "\n" +
        "/* IsInstanceOf(obj, clazz). */\n" +
        "JNIEXPORT jboolean JNICALL Java_JniLookupTest_isInstanceOf(JNIEnv* e, jclass c, jobject o, jclass k) {\n" +
        "    return (*e)->IsInstanceOf(e, o, k);\n" +
        "}\n" +
        "\n" +
        "/* GetMethodID of a missing method: NULL plus a pending exception. */\n" +
        "JNIEXPORT jint JNICALL Java_JniLookupTest_badMethodProbe(JNIEnv* e, jclass c, jobject p) {\n" +
        "    jclass k = (*e)->GetObjectClass(e, p);\n" +
        "    jmethodID m = (*e)->GetMethodID(e, k, \"noSuchMethodJck\", \"()V\");\n" +
        "    jthrowable t = (*e)->ExceptionOccurred(e);\n" +
        "    jint bits = 0;\n" +
        "    if (m == 0) bits |= 2;\n" +
        "    if (t != 0) bits |= 1;\n" +
        "    if ((*e)->ExceptionCheck(e)) (*e)->ExceptionClear(e);\n" +
        "    return bits;\n" +
        "}\n" +
        "\n" +
        "/* FromReflectedMethod: obtain a jmethodID from a java.lang.reflect.Method and\n" +
        "   prove it is usable by invoking the method through it. */\n" +
        "JNIEXPORT jint JNICALL Java_JniLookupTest_callViaReflectedMethod(JNIEnv* e, jclass c, jobject target, jobject m) {\n" +
        "    jmethodID id = (*e)->FromReflectedMethod(e, m);\n" +
        "    if (id == 0) return -1;\n" +
        "    return (*e)->CallIntMethod(e, target, id, 20, 22);\n" +
        "}\n" +
        "\n" +
        "/* ToReflectedMethod: turn a jmethodID (looked up by name+signature) back into a\n" +
        "   java.lang.reflect.Method object. */\n" +
        "JNIEXPORT jobject JNICALL Java_JniLookupTest_toReflectedMethod(JNIEnv* e, jclass c, jclass k, jstring name, jstring sig) {\n" +
        "    const char* n = (*e)->GetStringUTFChars(e, name, 0);\n" +
        "    const char* s = (*e)->GetStringUTFChars(e, sig, 0);\n" +
        "    jmethodID id;\n" +
        "    jobject r;\n" +
        "    if (n == 0 || s == 0) return 0;\n" +
        "    id = (*e)->GetMethodID(e, k, n, s);\n" +
        "    (*e)->ReleaseStringUTFChars(e, name, n);\n" +
        "    (*e)->ReleaseStringUTFChars(e, sig, s);\n" +
        "    if (id == 0) return 0;\n" +
        "    r = (*e)->ToReflectedMethod(e, k, id, JNI_FALSE);\n" +
        "    return r;\n" +
        "}\n" +
        "\n" +
        "/* FromReflectedField: obtain a jfieldID from a java.lang.reflect.Field and\n" +
        "   prove it is usable by reading the field through it. */\n" +
        "JNIEXPORT jint JNICALL Java_JniLookupTest_readViaReflectedField(JNIEnv* e, jclass c, jobject target, jobject f) {\n" +
        "    jfieldID id = (*e)->FromReflectedField(e, f);\n" +
        "    if (id == 0) return -1;\n" +
        "    return (*e)->GetIntField(e, target, id);\n" +
        "}\n" +
        "\n" +
        "/* ToReflectedField: turn a jfieldID (looked up by name+signature) back into a\n" +
        "   java.lang.reflect.Field object. */\n" +
        "JNIEXPORT jobject JNICALL Java_JniLookupTest_toReflectedField(JNIEnv* e, jclass c, jclass k, jstring name, jstring sig) {\n" +
        "    const char* n = (*e)->GetStringUTFChars(e, name, 0);\n" +
        "    const char* s = (*e)->GetStringUTFChars(e, sig, 0);\n" +
        "    jfieldID id;\n" +
        "    jobject r;\n" +
        "    if (n == 0 || s == 0) return 0;\n" +
        "    id = (*e)->GetFieldID(e, k, n, s);\n" +
        "    (*e)->ReleaseStringUTFChars(e, name, n);\n" +
        "    (*e)->ReleaseStringUTFChars(e, sig, s);\n" +
        "    if (id == 0) return 0;\n" +
        "    r = (*e)->ToReflectedField(e, k, id, JNI_FALSE);\n" +
        "    return r;\n" +
        "}\n";

    // ------------------------------------------------------------------
    // Native method declarations (bound by the library above)
    // ------------------------------------------------------------------
    native static Class<?> findClass(String name);
    native static int findClassMissing();
    native static boolean isInstanceOf(Object o, Class<?> k);
    native static int badMethodProbe(Object p);
    native static int callViaReflectedMethod(Object target, Method m);
    native static Method toReflectedMethod(Class<?> k, String name, String sig);
    native static int readViaReflectedField(Object target, Field f);
    native static Field toReflectedField(Class<?> k, String name, String sig);

    // ------------------------------------------------------------------
    // Helper class the native code reaches into via method/field ids
    // ------------------------------------------------------------------
    public static class Point {
        public int x;

        public Point(int x) {
            this.x = x;
        }

        public int sum(int a, int b) {
            return a + b + x;
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
        // library directly (built by `make native` from jnilookup.c).
        String np = System.getProperty("test.nativepath");
        if (np != null) {
            Path pre = Paths.get(np).resolve("libjnilookup.so");
            if (Files.exists(pre)) {
                System.load(pre.toAbsolutePath().toString());
                return;
            }
            throw new AssertionError("test.nativepath set but no prebuilt JNI library found (run: make native)");
        }
        Path dir = Paths.get(System.getProperty("user.dir"));
        Path cFile = dir.resolve("jnilookup.c");
        Path so = dir.resolve("libjnilookup.so");
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

        testFindClass();
        testIsInstanceOf();
        testReflected();

        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testFindClass() {
        check(findClass("java/lang/String") == String.class, "FindClass binary name (slash form)");
        check(findClass("JniLookupTest") == JniLookupTest.class, "FindClass own test class");
        check(findClass("[I") == int[].class, "FindClass array descriptor [I");
        check(findClass("[[Ljava/lang/String;") == String[][].class,
                "FindClass array descriptor [[Ljava/lang/String;");
        check(findClassMissing() == 3, "FindClass missing -> NULL plus pending exception");
        check(badMethodProbe(new Point(1)) == 3, "GetMethodID missing -> NULL plus pending exception");
    }

    static void testIsInstanceOf() {
        Point p = new Point(1);
        check(isInstanceOf(p, Point.class), "IsInstanceOf own class");
        check(isInstanceOf(p, Object.class), "IsInstanceOf superclass");
        check(!isInstanceOf(p, String.class), "IsInstanceOf unrelated class");
        check(isInstanceOf("str", String.class), "IsInstanceOf String");
        check(isInstanceOf("str", Object.class), "IsInstanceOf String as Object");
        check(isInstanceOf(null, String.class), "IsInstanceOf(NULL) is true (null casts to any class)");
    }

    static void testReflected() throws Exception {
        Point p = new Point(7);
        Method sum = Point.class.getMethod("sum", int.class, int.class);
        check(callViaReflectedMethod(p, sum) == 49,
                "FromReflectedMethod yields a callable method id");
        Method viaId = toReflectedMethod(Point.class, "sum", "(II)I");
        check(viaId != null, "ToReflectedMethod returns a Method");
        check("sum".equals(viaId.getName()), "ToReflectedMethod name");
        check(viaId.getDeclaringClass() == Point.class, "ToReflectedMethod declaring class");

        Field xf = Point.class.getField("x");
        check(readViaReflectedField(p, xf) == 7,
                "FromReflectedField yields a readable field id");
        Field viaFid = toReflectedField(Point.class, "x", "I");
        check(viaFid != null, "ToReflectedField returns a Field");
        check("x".equals(viaFid.getName()), "ToReflectedField name");
    }
}
