/*
 * @test
 * @summary JNI environment semantics: a dedicated native library, compiled at
 *          run time from an embedded C source, exercises the JNIEnv function
 *          table mainline - primitive round trips, modified UTF-8 strings,
 *          int-array regions/elements, instance and static field and method
 *          access (including a non-virtual invocation), ThrowNew exception
 *          delivery with ExceptionCheck/ExceptionClear, global and weak global
 *          references, local frames, JNI_OnLoad version negotiation,
 *          RegisterNatives/UnregisterNatives, and GetJavaVM.
 * @run main/othervm --enable-native-access=ALL-UNNAMED JniSemanticsTest
 */

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

/**
 * Conformance test for the JNIEnv contract against a real native library.
 *
 * The C source below is embedded as a string constant and written to the jtreg
 * scratch directory (user.dir) at run time, compiled with gcc -shared -fPIC
 * against the JDK under test's own jni.h, and loaded with System.load.  The
 * library is therefore built per JDK under test; no binary is kept in the
 * repository.  All assertions are behavioural: they go through the JNIEnv
 * function table, never through reflection on the test class itself.
 */
public class JniSemanticsTest {
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
        "\n" +
        "static JavaVM* g_vm = 0;\n" +
        "static jint g_onload = 0;\n" +
        "static jobject g_global = 0;\n" +
        "static jweak g_weak = 0;\n" +
        "\n" +
        "static jint regMul(JNIEnv* env, jclass clz, jint a, jint b) {\n" +
        "    return a * b;\n" +
        "}\n" +
        "\n" +
        "/* Called by the VM during System.load; negotiates JNI 1.8 and\n" +
        "   registers one method dynamically, without Java_ name lookup. */\n" +
        "JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved) {\n" +
        "    JNIEnv* env;\n" +
        "    JNINativeMethod m[1];\n" +
        "    jclass clz;\n" +
        "    g_vm = vm;\n" +
        "    if ((*vm)->GetEnv(vm, (void**)&env, JNI_VERSION_1_8) != JNI_OK) return JNI_ERR;\n" +
        "    clz = (*env)->FindClass(env, \"JniSemanticsTest\");\n" +
        "    if (clz == 0) return JNI_ERR;\n" +
        "    m[0].name = \"registeredMul\";\n" +
        "    m[0].signature = \"(II)I\";\n" +
        "    m[0].fnPtr = (void*)regMul;\n" +
        "    if ((*env)->RegisterNatives(env, clz, m, 1) != JNI_OK) return JNI_ERR;\n" +
        "    g_onload = JNI_VERSION_1_8;\n" +
        "    return JNI_VERSION_1_8;\n" +
        "}\n" +
        "\n" +
        "/* ---- primitive round trips ---- */\n" +
        "JNIEXPORT jint JNICALL Java_JniSemanticsTest_primAdd(JNIEnv* e, jclass c, jint a, jint b) { return a + b; }\n" +
        "JNIEXPORT jlong JNICALL Java_JniSemanticsTest_primLong(JNIEnv* e, jclass c, jlong a) { return a; }\n" +
        "JNIEXPORT jdouble JNICALL Java_JniSemanticsTest_primDouble(JNIEnv* e, jclass c, jdouble a) { return a * 2.0; }\n" +
        "JNIEXPORT jboolean JNICALL Java_JniSemanticsTest_primBool(JNIEnv* e, jclass c, jboolean b) { return b == JNI_TRUE ? JNI_FALSE : JNI_TRUE; }\n" +
        "JNIEXPORT jbyte JNICALL Java_JniSemanticsTest_primByte(JNIEnv* e, jclass c, jbyte b) { return b; }\n" +
        "JNIEXPORT jshort JNICALL Java_JniSemanticsTest_primShort(JNIEnv* e, jclass c, jshort s) { return s; }\n" +
        "JNIEXPORT jchar JNICALL Java_JniSemanticsTest_primChar(JNIEnv* e, jclass c, jchar ch) { return ch; }\n" +
        "JNIEXPORT jfloat JNICALL Java_JniSemanticsTest_primFloat(JNIEnv* e, jclass c, jfloat f) { return f * 0.5f; }\n" +
        "\n" +
        "/* ---- strings (modified UTF-8) ---- */\n" +
        "JNIEXPORT jint JNICALL Java_JniSemanticsTest_utfLength(JNIEnv* e, jclass c, jstring s) {\n" +
        "    return (*e)->GetStringUTFLength(e, s);\n" +
        "}\n" +
        "JNIEXPORT jint JNICALL Java_JniSemanticsTest_utf16Length(JNIEnv* e, jclass c, jstring s) {\n" +
        "    return (*e)->GetStringLength(e, s);\n" +
        "}\n" +
        "JNIEXPORT jstring JNICALL Java_JniSemanticsTest_echoUtf(JNIEnv* e, jclass c, jstring s) {\n" +
        "    const char* u;\n" +
        "    jstring r;\n" +
        "    if (s == 0) return 0;\n" +
        "    u = (*e)->GetStringUTFChars(e, s, 0);\n" +
        "    if (u == 0) return 0;\n" +
        "    r = (*e)->NewStringUTF(e, u);\n" +
        "    (*e)->ReleaseStringUTFChars(e, s, u);\n" +
        "    return r;\n" +
        "}\n" +
        "\n" +
        "/* ---- arrays ---- */\n" +
        "JNIEXPORT jintArray JNICALL Java_JniSemanticsTest_copyInts(JNIEnv* e, jclass c, jintArray a) {\n" +
        "    jsize n = (*e)->GetArrayLength(e, a);\n" +
        "    jintArray r = (*e)->NewIntArray(e, n);\n" +
        "    jint* tmp;\n" +
        "    if (r == 0) return 0;\n" +
        "    if (n == 0) return r;\n" +
        "    tmp = (jint*)malloc((size_t)n * sizeof(jint));\n" +
        "    if (tmp == 0) return 0;\n" +
        "    (*e)->GetIntArrayRegion(e, a, 0, n, tmp);\n" +
        "    (*e)->SetIntArrayRegion(e, r, 0, n, tmp);\n" +
        "    free(tmp);\n" +
        "    return r;\n" +
        "}\n" +
        "JNIEXPORT void JNICALL Java_JniSemanticsTest_bumpInts(JNIEnv* e, jclass c, jintArray a) {\n" +
        "    jsize i;\n" +
        "    jsize n = (*e)->GetArrayLength(e, a);\n" +
        "    jint* p = (*e)->GetIntArrayElements(e, a, 0);\n" +
        "    if (p == 0) return;\n" +
        "    for (i = 0; i < n; i++) p[i] = p[i] + 1;\n" +
        "    (*e)->ReleaseIntArrayElements(e, a, p, 0);\n" +
        "}\n" +
        "JNIEXPORT void JNICALL Java_JniSemanticsTest_setRange(JNIEnv* e, jclass c, jintArray a, jint start, jint len, jint val) {\n" +
        "    jint buf[16];\n" +
        "    jsize i;\n" +
        "    if (len > 16) return;\n" +
        "    for (i = 0; i < len; i++) buf[i] = val;\n" +
        "    (*e)->SetIntArrayRegion(e, a, start, len, buf);\n" +
        "}\n" +
        "JNIEXPORT jint JNICALL Java_JniSemanticsTest_arrayLen(JNIEnv* e, jclass c, jobject a) {\n" +
        "    return (*e)->GetArrayLength(e, a);\n" +
        "}\n" +
        "\n" +
        "/* ---- instance and static fields ---- */\n" +
        "JNIEXPORT jint JNICALL Java_JniSemanticsTest_readX(JNIEnv* e, jclass c, jobject p) {\n" +
        "    jclass k = (*e)->GetObjectClass(e, p);\n" +
        "    jfieldID f = (*e)->GetFieldID(e, k, \"x\", \"I\");\n" +
        "    return (*e)->GetIntField(e, p, f);\n" +
        "}\n" +
        "JNIEXPORT void JNICALL Java_JniSemanticsTest_writeX(JNIEnv* e, jclass c, jobject p, jint v) {\n" +
        "    jclass k = (*e)->GetObjectClass(e, p);\n" +
        "    jfieldID f = (*e)->GetFieldID(e, k, \"x\", \"I\");\n" +
        "    (*e)->SetIntField(e, p, f, v);\n" +
        "}\n" +
        "JNIEXPORT jlong JNICALL Java_JniSemanticsTest_readStamp(JNIEnv* e, jclass c, jobject p) {\n" +
        "    jclass k = (*e)->GetObjectClass(e, p);\n" +
        "    jfieldID f = (*e)->GetFieldID(e, k, \"stamp\", \"J\");\n" +
        "    return (*e)->GetLongField(e, p, f);\n" +
        "}\n" +
        "JNIEXPORT void JNICALL Java_JniSemanticsTest_writeStamp(JNIEnv* e, jclass c, jobject p, jlong v) {\n" +
        "    jclass k = (*e)->GetObjectClass(e, p);\n" +
        "    jfieldID f = (*e)->GetFieldID(e, k, \"stamp\", \"J\");\n" +
        "    (*e)->SetLongField(e, p, f, v);\n" +
        "}\n" +
        "JNIEXPORT jboolean JNICALL Java_JniSemanticsTest_readSeen(JNIEnv* e, jclass c, jobject p) {\n" +
        "    jclass k = (*e)->GetObjectClass(e, p);\n" +
        "    jfieldID f = (*e)->GetFieldID(e, k, \"seen\", \"Z\");\n" +
        "    return (*e)->GetBooleanField(e, p, f);\n" +
        "}\n" +
        "JNIEXPORT jdouble JNICALL Java_JniSemanticsTest_readWeight(JNIEnv* e, jclass c, jobject p) {\n" +
        "    jclass k = (*e)->GetObjectClass(e, p);\n" +
        "    jfieldID f = (*e)->GetFieldID(e, k, \"weight\", \"D\");\n" +
        "    return (*e)->GetDoubleField(e, p, f);\n" +
        "}\n" +
        "JNIEXPORT jobject JNICALL Java_JniSemanticsTest_readTag(JNIEnv* e, jclass c, jobject p) {\n" +
        "    jclass k = (*e)->GetObjectClass(e, p);\n" +
        "    jfieldID f = (*e)->GetFieldID(e, k, \"tag\", \"Ljava/lang/String;\");\n" +
        "    return (*e)->GetObjectField(e, p, f);\n" +
        "}\n" +
        "JNIEXPORT void JNICALL Java_JniSemanticsTest_writeTag(JNIEnv* e, jclass c, jobject p, jobject v) {\n" +
        "    jclass k = (*e)->GetObjectClass(e, p);\n" +
        "    jfieldID f = (*e)->GetFieldID(e, k, \"tag\", \"Ljava/lang/String;\");\n" +
        "    (*e)->SetObjectField(e, p, f, v);\n" +
        "}\n" +
        "JNIEXPORT jint JNICALL Java_JniSemanticsTest_readCount(JNIEnv* e, jclass c, jclass k) {\n" +
        "    jfieldID f = (*e)->GetStaticFieldID(e, k, \"count\", \"I\");\n" +
        "    return (*e)->GetStaticIntField(e, k, f);\n" +
        "}\n" +
        "JNIEXPORT void JNICALL Java_JniSemanticsTest_writeCount(JNIEnv* e, jclass c, jclass k, jint v) {\n" +
        "    jfieldID f = (*e)->GetStaticFieldID(e, k, \"count\", \"I\");\n" +
        "    (*e)->SetStaticIntField(e, k, f, v);\n" +
        "}\n" +
        "JNIEXPORT jobject JNICALL Java_JniSemanticsTest_readNote(JNIEnv* e, jclass c, jclass k) {\n" +
        "    jfieldID f = (*e)->GetStaticFieldID(e, k, \"note\", \"Ljava/lang/String;\");\n" +
        "    return (*e)->GetStaticObjectField(e, k, f);\n" +
        "}\n" +
        "/* GetFieldID failure: returns 0x3 when the field is missing, the pending\n" +
        "   NoSuchFieldError is observed via ExceptionCheck and cleared. */\n" +
        "JNIEXPORT jint JNICALL Java_JniSemanticsTest_badFieldProbe(JNIEnv* e, jclass c, jobject p) {\n" +
        "    jclass k = (*e)->GetObjectClass(e, p);\n" +
        "    jfieldID f = (*e)->GetFieldID(e, k, \"noSuchFieldJck\", \"I\");\n" +
        "    jint pending = (*e)->ExceptionCheck(e) ? 1 : 0;\n" +
        "    if ((*e)->ExceptionCheck(e)) (*e)->ExceptionClear(e);\n" +
        "    return (f == 0 ? 2 : 0) | pending;\n" +
        "}\n" +
        "\n" +
        "/* ---- methods ---- */\n" +
        "JNIEXPORT jint JNICALL Java_JniSemanticsTest_callSum(JNIEnv* e, jclass c, jobject p, jint a, jint b) {\n" +
        "    jclass k = (*e)->GetObjectClass(e, p);\n" +
        "    jmethodID m = (*e)->GetMethodID(e, k, \"sum\", \"(II)I\");\n" +
        "    return (*e)->CallIntMethod(e, p, m, a, b);\n" +
        "}\n" +
        "JNIEXPORT jstring JNICALL Java_JniSemanticsTest_callName(JNIEnv* e, jclass c, jobject p) {\n" +
        "    jclass k = (*e)->GetObjectClass(e, p);\n" +
        "    jmethodID m = (*e)->GetMethodID(e, k, \"name\", \"()Ljava/lang/String;\");\n" +
        "    return (jstring)(*e)->CallObjectMethod(e, p, m);\n" +
        "}\n" +
        "JNIEXPORT jint JNICALL Java_JniSemanticsTest_callStaticMul(JNIEnv* e, jclass c, jclass k, jint a, jint b) {\n" +
        "    jmethodID m = (*e)->GetStaticMethodID(e, k, \"mul\", \"(II)I\");\n" +
        "    return (*e)->CallStaticIntMethod(e, k, m, a, b);\n" +
        "}\n" +
        "JNIEXPORT jint JNICALL Java_JniSemanticsTest_callNonvirtualSum(JNIEnv* e, jclass c, jobject p, jclass k, jint a, jint b) {\n" +
        "    jmethodID m = (*e)->GetMethodID(e, k, \"sum\", \"(II)I\");\n" +
        "    return (*e)->CallNonvirtualIntMethod(e, p, k, m, a, b);\n" +
        "}\n" +
        "JNIEXPORT jclass JNICALL Java_JniSemanticsTest_superclass(JNIEnv* e, jclass c, jclass k) {\n" +
        "    return (*e)->GetSuperclass(e, k);\n" +
        "}\n" +
        "/* JNI IsAssignableFrom(clazz1, clazz2) asks whether clazz1 objects can be\n" +
        "   stored in clazz2 variables, so the arguments are passed swapped with\n" +
        "   respect to the Java-facing (to, from) order. */\n" +
        "JNIEXPORT jboolean JNICALL Java_JniSemanticsTest_assignable(JNIEnv* e, jclass c, jclass to, jclass from) {\n" +
        "    return (*e)->IsAssignableFrom(e, from, to);\n" +
        "}\n" +
        "\n" +
        "/* ---- exceptions ---- */\n" +
        "JNIEXPORT void JNICALL Java_JniSemanticsTest_throwIae(JNIEnv* e, jclass c, jstring msg) {\n" +
        "    jclass k = (*e)->FindClass(e, \"java/lang/IllegalArgumentException\");\n" +
        "    const char* m;\n" +
        "    if (k == 0) return;\n" +
        "    m = (*e)->GetStringUTFChars(e, msg, 0);\n" +
        "    (*e)->ThrowNew(e, k, m);\n" +
        "    (*e)->ReleaseStringUTFChars(e, msg, m);\n" +
        "}\n" +
        "/* Returns 0x3 when ExceptionCheck sees the pending ThrowNew exception and\n" +
        "   reports no exception again after ExceptionClear. */\n" +
        "JNIEXPORT jint JNICALL Java_JniSemanticsTest_exceptionFlow(JNIEnv* e, jclass c) {\n" +
        "    jclass k = (*e)->FindClass(e, \"java/lang/IllegalStateException\");\n" +
        "    jint r;\n" +
        "    if (k == 0) return -1;\n" +
        "    (*e)->ThrowNew(e, k, \"jck\");\n" +
        "    r = ((*e)->ExceptionCheck(e) ? 2 : 0);\n" +
        "    (*e)->ExceptionClear(e);\n" +
        "    if (!(*e)->ExceptionCheck(e)) r |= 1;\n" +
        "    return r;\n" +
        "}\n" +
        "JNIEXPORT jstring JNICALL Java_JniSemanticsTest_throwClearContinue(JNIEnv* e, jclass c) {\n" +
        "    jclass k = (*e)->FindClass(e, \"java/lang/UnsupportedOperationException\");\n" +
        "    if (k == 0) return 0;\n" +
        "    (*e)->ThrowNew(e, k, \"cleared\");\n" +
        "    (*e)->ExceptionClear(e);\n" +
        "    return (*e)->NewStringUTF(e, \"still-alive\");\n" +
        "}\n" +
        "\n" +
        "/* ---- references ---- */\n" +
        "JNIEXPORT void JNICALL Java_JniSemanticsTest_keepGlobal(JNIEnv* e, jclass c, jobject o) {\n" +
        "    if (g_global != 0) (*e)->DeleteGlobalRef(e, g_global);\n" +
        "    g_global = (*e)->NewGlobalRef(e, o);\n" +
        "}\n" +
        "JNIEXPORT jobject JNICALL Java_JniSemanticsTest_readGlobal(JNIEnv* e, jclass c) {\n" +
        "    return g_global;\n" +
        "}\n" +
        "JNIEXPORT void JNICALL Java_JniSemanticsTest_makeWeak(JNIEnv* e, jclass c, jobject o) {\n" +
        "    if (g_weak != 0) (*e)->DeleteWeakGlobalRef(e, g_weak);\n" +
        "    g_weak = (*e)->NewWeakGlobalRef(e, o);\n" +
        "}\n" +
        "JNIEXPORT jobject JNICALL Java_JniSemanticsTest_readWeak(JNIEnv* e, jclass c) {\n" +
        "    return g_weak;\n" +
        "}\n" +
        "JNIEXPORT jint JNICALL Java_JniSemanticsTest_pushPopFrame(JNIEnv* e, jclass c) {\n" +
        "    if ((*e)->PushLocalFrame(e, 16) != 0) return -1;\n" +
        "    (*e)->NewStringUTF(e, \"frame-local\");\n" +
        "    return ((*e)->PopLocalFrame(e, 0) == 0) ? 0 : 1;\n" +
        "}\n" +
        "JNIEXPORT jobject JNICALL Java_JniSemanticsTest_frameCarry(JNIEnv* e, jclass c, jobject o) {\n" +
        "    if ((*e)->PushLocalFrame(e, 16) != 0) return 0;\n" +
        "    return (*e)->PopLocalFrame(e, o);\n" +
        "}\n" +
        "\n" +
        "/* ---- VM and registration management ---- */\n" +
        "JNIEXPORT jint JNICALL Java_JniSemanticsTest_onloadVersion(JNIEnv* e, jclass c) {\n" +
        "    return g_onload;\n" +
        "}\n" +
        "JNIEXPORT jint JNICALL Java_JniSemanticsTest_envVersion(JNIEnv* e, jclass c) {\n" +
        "    return (*e)->GetVersion(e);\n" +
        "}\n" +
        "JNIEXPORT jint JNICALL Java_JniSemanticsTest_getVm(JNIEnv* e, jclass c, jlongArray out) {\n" +
        "    JavaVM* vm = 0;\n" +
        "    jint rc = (*e)->GetJavaVM(e, &vm);\n" +
        "    if (rc == 0 && out != 0 && (*e)->GetArrayLength(e, out) >= 1)\n" +
        "        (*e)->SetLongArrayRegion(e, out, 0, 1, (const jlong*)&vm);\n" +
        "    return rc;\n" +
        "}\n" +
        "JNIEXPORT void JNICALL Java_JniSemanticsTest_unregisterAll(JNIEnv* e, jclass c) {\n" +
        "    (*e)->UnregisterNatives(e, c);\n" +
        "}\n";

    // ------------------------------------------------------------------
    // Native method declarations (bound by the library above)
    // ------------------------------------------------------------------
    native static int primAdd(int a, int b);
    native static long primLong(long a);
    native static double primDouble(double a);
    native static boolean primBool(boolean b);
    native static byte primByte(byte b);
    native static short primShort(short s);
    native static char primChar(char c);
    native static float primFloat(float f);

    native static int utfLength(String s);
    native static int utf16Length(String s);
    native static String echoUtf(String s);

    native static int[] copyInts(int[] a);
    native static void bumpInts(int[] a);
    native static void setRange(int[] a, int start, int len, int val);
    native static int arrayLen(Object a);

    native static int readX(Object p);
    native static void writeX(Object p, int v);
    native static long readStamp(Object p);
    native static void writeStamp(Object p, long v);
    native static boolean readSeen(Object p);
    native static double readWeight(Object p);
    native static Object readTag(Object p);
    native static void writeTag(Object p, Object v);
    native static int readCount(Class<?> k);
    native static void writeCount(Class<?> k, int v);
    native static Object readNote(Class<?> k);
    native static int badFieldProbe(Object p);

    native static int callSum(Object p, int a, int b);
    native static String callName(Object p);
    native static int callStaticMul(Class<?> k, int a, int b);
    native static int callNonvirtualSum(Object p, Class<?> k, int a, int b);
    native static Class<?> superclass(Class<?> k);
    native static boolean assignable(Class<?> to, Class<?> from);

    native static void throwIae(String msg);
    native static int exceptionFlow();
    native static String throwClearContinue();

    native static void keepGlobal(Object o);
    native static Object readGlobal();
    native static void makeWeak(Object o);
    native static Object readWeak();
    native static int pushPopFrame();
    native static Object frameCarry(Object o);

    native static int onloadVersion();
    native static int envVersion();
    native static int getVm(long[] out);
    native static void unregisterAll();
    native static int registeredMul(int a, int b);

    // ------------------------------------------------------------------
    // Helper classes the native code reaches into by field and method ids
    // ------------------------------------------------------------------
    public static class Point {
        int x;
        long stamp;
        boolean seen;
        double weight;
        String tag;
        static int count;
        static long total;
        static String note = "note0";

        public Point(int x) {
            this.x = x;
            this.stamp = 100L + x;
            this.seen = x % 2 == 1;
            this.weight = x + 0.25;
            this.tag = "tag" + x;
        }

        public int sum(int a, int b) { return a + b + x; }

        public String name() { return "point-" + x; }

        public static int mul(int a, int b) { return a * b; }
    }

    public static class PointSub extends Point {
        public PointSub(int x) { super(x); }

        @Override public int sum(int a, int b) { return a * 100 + b; }

        @Override public String name() { return "sub-" + x; }
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
        // test.nativepath) load the prebuilt library directly and never
        // invoke the host gcc; the build system names it after the C source
        // (libjnisemantics.so), the historical in-test name is libjckjni.so.
        String np = System.getProperty("test.nativepath");
        if (np != null) {
            for (String cand : new String[] {"libjckjni.so", "libjnisemantics.so"}) {
                Path pre = Paths.get(np).resolve(cand);
                if (Files.exists(pre)) {
                    System.load(pre.toAbsolutePath().toString());
                    return;
                }
            }
            throw new AssertionError("test.nativepath set but no prebuilt JNI library found (run: make native)");
        }
        Path dir = Paths.get(System.getProperty("user.dir"));
        Path cFile = dir.resolve("jnisemantics.c");
        Path so = dir.resolve("libjckjni.so");
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
            System.out.println("SKIP: no C compiler (gcc) available; native library cannot be built");
            return;
        }
        compileAndLoad();

        testPrimitives();
        testStrings();
        testArrays();
        testFields();
        testMethods();
        testExceptions();
        testReferences();
        testRegistrationAndVm();

        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testPrimitives() {
        check(primAdd(20, 22) == 42, "primAdd 20+22");
        check(primAdd(0, 0) == 0, "primAdd zeros");
        check(primAdd(-1, 1) == 0, "primAdd -1+1");
        check(primAdd(Integer.MIN_VALUE, 0) == Integer.MIN_VALUE, "primAdd MIN+0");
        check(primAdd(Integer.MAX_VALUE, 0) == Integer.MAX_VALUE, "primAdd MAX+0");
        check(primLong(Long.MIN_VALUE) == Long.MIN_VALUE, "primLong MIN identity");
        check(primLong(Long.MAX_VALUE) == Long.MAX_VALUE, "primLong MAX identity");
        check(primLong(0x123456789ABCDEF0L) == 0x123456789ABCDEF0L, "primLong payload identity");
        check(primDouble(0.25) == 0.5, "primDouble 0.25*2");
        check(primDouble(-8.5) == -17.0, "primDouble -8.5*2");
        check(primDouble(0.0) == 0.0 && 1 / primDouble(0.0) > 0, "primDouble +0.0*2 keeps +0");
        check(primDouble(Double.MAX_VALUE) == Double.POSITIVE_INFINITY,
                "primDouble MAX_VALUE*2 overflows to +Inf per IEEE 754");
        check(Double.isNaN(primDouble(Double.NaN)), "primDouble NaN propagates");
        check(primDouble(Double.POSITIVE_INFINITY) == Double.POSITIVE_INFINITY,
                "primDouble +Inf propagates");
        check(primBool(true) == false, "primBool true -> false");
        check(primBool(false) == true, "primBool false -> true");
        check(primByte((byte) -128) == (byte) -128, "primByte MIN identity");
        check(primByte((byte) 127) == (byte) 127, "primByte MAX identity");
        check(primShort((short) -32768) == (short) -32768, "primShort MIN identity");
        check(primShort((short) 32767) == (short) 32767, "primShort MAX identity");
        check(primChar('\u0000') == '\u0000', "primChar NUL identity");
        check(primChar('A') == 'A', "primChar A identity");
        check(primChar('中') == '中', "primChar CJK identity");
        check(primFloat(0.5f) == 0.25f, "primFloat 0.5*0.5f");
        check(primFloat(-3.0f) == -1.5f, "primFloat -3*0.5f");
    }

    static void testStrings() {
        check(utfLength("abc") == 3, "GetStringUTFLength ascii");
        check(utfLength("") == 0, "GetStringUTFLength empty");
        // modified UTF-8 encodes U+4E2D and U+6587 as 3 bytes each
        check(utfLength("中文") == 6, "GetStringUTFLength CJK 2*3 bytes");
        // supplementary characters are encoded as their surrogate pair,
        // two 3-byte sequences, not a 4-byte standard UTF-8 sequence
        check(utfLength("😀") == 6, "GetStringUTFLength surrogate pair 3+3");
        check(utfLength("a\u0000b") == 4, "GetStringUTFLength NUL is 2 bytes (0xC0 0x80)");
        check(utf16Length("abc") == 3, "GetStringLength ascii = UTF-16 units");
        check(utf16Length("中文") == 2, "GetStringLength CJK = 2 UTF-16 units");
        check(utf16Length("😀") == 2, "GetStringLength surrogate pair = 2 units");
        check("".equals(echoUtf("")), "echoUtf empty");
        check("hello".equals(echoUtf("hello")), "echoUtf ascii");
        String cjk = "中文Abc";
        check(cjk.equals(echoUtf(cjk)), "echoUtf CJK round trip");
        String nulStr = "a\u0000b";
        String echoed = echoUtf(nulStr);
        check(echoed.length() == 3 && echoed.charAt(1) == '\u0000',
                "echoUtf preserves embedded NUL through modified UTF-8");
        String all = "😀 中文";
        check(all.equals(echoUtf(all)), "echoUtf surrogate pair round trip");
    }

    static void testArrays() {
        int[] src = {3, 1, 4, 1, 5, 9, 2, 6};
        int[] copy = copyInts(src);
        check(copy != src, "copyInts returns a new array");
        check(java.util.Arrays.equals(src, copy), "copyInts contents equal");
        int[] empty = {};
        check(copyInts(empty).length == 0, "copyInts of empty array");
        int[] big = new int[257];
        for (int i = 0; i < big.length; i++) big[i] = i;
        check(java.util.Arrays.equals(big, copyInts(big)), "copyInts 257 elements");
        int[] ext = {0, 0, 0};
        bumpInts(ext);
        check(ext[0] == 1 && ext[1] == 1 && ext[2] == 1, "GetIntArrayElements + Release(mode 0) copies back");
        bumpInts(ext);
        check(ext[0] == 2, "elements modification visible after second pass");
        int[] patch = new int[4];
        setRange(patch, 1, 2, 7);
        check(patch[0] == 0 && patch[1] == 7 && patch[2] == 7 && patch[3] == 0,
                "SetIntArrayRegion sets only the requested range");
        setRange(patch, 4, 0, 9);
        check(patch[3] == 0, "SetIntArrayRegion start==length with len 0 is a no-op");
        check(arrayLen(src) == src.length, "GetArrayLength int[]");
        check(arrayLen(new String[3]) == 3, "GetArrayLength String[]");
        check(arrayLen(new long[7]) == 7, "GetArrayLength long[]");
        check(arrayLen(empty) == 0, "GetArrayLength empty");
        expectAioobe(() -> setRange(patch, 4, 1, 9), "region start==length len==1");
        expectAioobe(() -> setRange(patch, -1, 1, 9), "region negative start");
        expectAioobe(() -> setRange(patch, 0, 5, 9), "region end beyond length");
        expectAioobe(() -> setRange(patch, 0, -1, 9), "region negative length");
    }

    static void expectAioobe(Runnable r, String what) {
        try {
            r.run();
            throw new AssertionError("expected ArrayIndexOutOfBoundsException: " + what);
        } catch (ArrayIndexOutOfBoundsException expected) {
            check(true, what + " -> ArrayIndexOutOfBoundsException from the native frame");
        }
    }

    static void testFields() {
        Point p = new Point(7);
        check(readX(p) == 7, "GetIntField");
        writeX(p, 19);
        check(p.x == 19, "SetIntField visible in Java");
        check(readStamp(p) == 107L, "GetLongField");
        writeStamp(p, Long.MIN_VALUE);
        check(p.stamp == Long.MIN_VALUE, "SetLongField MIN_VALUE");
        check(readSeen(p) == true, "GetBooleanField (odd x)");
        check(readWeight(p) == 7.25, "GetDoubleField");
        check("tag7".equals(readTag(p)), "GetObjectField");
        String replacement = "replaced";
        writeTag(p, replacement);
        check(p.tag == replacement, "SetObjectField stores the same reference");
        writeCount(Point.class, 404);
        check(Point.count == 404, "SetStaticIntField");
        check(readCount(Point.class) == 404, "GetStaticIntField");
        check(readNote(Point.class) == Point.note, "GetStaticObjectField returns the same reference");
        check(badFieldProbe(p) == 3,
                "missing field: GetFieldID returns NULL with a pending error that ExceptionClear removes");
    }

    static void testMethods() {
        Point p = new Point(7);
        check(callSum(p, 20, 22) == 49, "CallIntMethod dispatches to sum");
        check("point-7".equals(callName(p)), "CallObjectMethod returns String");
        check(callStaticMul(Point.class, 6, 7) == 42, "CallStaticIntMethod");
        PointSub sub = new PointSub(7);
        check(callSum(sub, 1, 1) == 101, "CallIntMethod is virtual (subclass impl)");
        check(callNonvirtualSum(sub, Point.class, 1, 1) == 9,
                "CallNonvirtualIntMethod selects the declaring class implementation");
        check(superclass(PointSub.class) == Point.class, "GetSuperclass direct");
        check(superclass(Point.class) == Object.class, "GetSuperclass of Object subclass");
        check(superclass(Object.class) == null, "GetSuperclass of java.lang.Object is null");
        check(superclass(Runnable.class) == null, "GetSuperclass of an interface is null");
        check(assignable(Point.class, PointSub.class), "IsAssignableFrom super <- sub");
        check(!assignable(PointSub.class, Point.class), "IsAssignableFrom rejects reverse");
        check(assignable(Point.class, Point.class), "IsAssignableFrom reflexive");
        check(!assignable(String.class, Integer.class), "IsAssignableFrom unrelated");
    }

    static void testExceptions() {
        try {
            throwIae("jck-message");
            check(false, "ThrowNew must deliver an exception to the Java caller");
        } catch (IllegalArgumentException e) {
            check(true, "ThrowNew delivers the exact exception class");
            check("jck-message".equals(e.getMessage()), "ThrowNew preserves the message");
        }
        check(exceptionFlow() == 3,
                "ExceptionCheck true while pending, false after ExceptionClear");
        check("still-alive".equals(throwClearContinue()),
                "native allocation works again after ExceptionClear");
    }

    static void testReferences() {
        Object o = new Object();
        keepGlobal(o);
        check(readGlobal() == o, "NewGlobalRef keeps the same object across JNI calls");
        // Replace and read back to prove the old global ref was deleted without
        // leaking and that the new one is a live reference again.
        Object o2 = new Object();
        keepGlobal(o2);
        check(readGlobal() == o2, "DeleteGlobalRef + NewGlobalRef rebinding");
        check(pushPopFrame() == 0, "PushLocalFrame/PopLocalFrame pair succeeds");
        Object carried = frameCarry(o2);
        check(carried == o2, "PopLocalFrame passes the reference through the frame");

        Object weakTarget = new Object();
        makeWeak(weakTarget);
        check(readWeak() == weakTarget, "weak global ref returns the same object while reachable");
        weakTarget = null;
        // Bounded GC retry: a weak global reference is cleared once the referent
        // is collected.  In an extremely GC-hostile environment this may need
        // more cycles; the bound keeps the test deterministic in wall time.
        for (int i = 0; i < 10 && readWeak() != null; i++) {
            System.gc();
        }
        check(readWeak() == null, "NewWeakGlobalRef does not prevent reclamation");
    }

    static void testRegistrationAndVm() {
        check(onloadVersion() == 0x10008, "JNI_OnLoad ran at System.load and its version was cached");
        // JDK 8 reports JNI_VERSION_1_8 (0x00010008); newer releases report the
        // modern scheme (e.g. 0x00180000), so compare numerically.
        check(envVersion() >= 0x10008, "GetVersion reports at least JNI 1.8 (got 0x"
                + Integer.toHexString(envVersion()) + ")");
        check(registeredMul(6, 7) == 42, "RegisterNatives binds without Java_ naming");
        check(registeredMul(-3, 5) == -15, "registered method honours negative operands");
        long[] vmOut = new long[1];
        check(getVm(vmOut) == 0, "GetJavaVM returns JNI_OK (0)");
        check(vmOut[0] != 0, "GetJavaVM writes a non-null JavaVM*");

        unregisterAll();
        try {
            registeredMul(2, 3);
            check(false, "UnregisterNatives must invalidate the dynamic registration");
        } catch (UnsatisfiedLinkError e) {
            String msg = String.valueOf(e.getMessage());
            check(msg.contains("registeredMul"),
                    "post-unregister ULE names the method (got: " + msg + ")");
        }
        // The class fell back to its pre-linking state; the JVM resolves the
        // Java_-named implementations again on the next call.
        check(primAdd(1, 2) == 3, "Java_-named natives re-bind after UnregisterNatives");
    }
}
