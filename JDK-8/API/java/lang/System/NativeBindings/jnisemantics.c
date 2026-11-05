#include <jni.h>
#include <stdlib.h>

static JavaVM* g_vm = 0;
static jint g_onload = 0;
static jobject g_global = 0;
static jweak g_weak = 0;

static jint regMul(JNIEnv* env, jclass clz, jint a, jint b) {
    return a * b;
}

/* Called by the VM during System.load; negotiates JNI 1.8 and
   registers one method dynamically, without Java_ name lookup. */
JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved) {
    JNIEnv* env;
    JNINativeMethod m[1];
    jclass clz;
    g_vm = vm;
    if ((*vm)->GetEnv(vm, (void**)&env, JNI_VERSION_1_8) != JNI_OK) return JNI_ERR;
    clz = (*env)->FindClass(env, "JniSemanticsTest");
    if (clz == 0) return JNI_ERR;
    m[0].name = "registeredMul";
    m[0].signature = "(II)I";
    m[0].fnPtr = (void*)regMul;
    if ((*env)->RegisterNatives(env, clz, m, 1) != JNI_OK) return JNI_ERR;
    g_onload = JNI_VERSION_1_8;
    return JNI_VERSION_1_8;
}

/* ---- primitive round trips ---- */
JNIEXPORT jint JNICALL Java_JniSemanticsTest_primAdd(JNIEnv* e, jclass c, jint a, jint b) { return a + b; }
JNIEXPORT jlong JNICALL Java_JniSemanticsTest_primLong(JNIEnv* e, jclass c, jlong a) { return a; }
JNIEXPORT jdouble JNICALL Java_JniSemanticsTest_primDouble(JNIEnv* e, jclass c, jdouble a) { return a * 2.0; }
JNIEXPORT jboolean JNICALL Java_JniSemanticsTest_primBool(JNIEnv* e, jclass c, jboolean b) { return b == JNI_TRUE ? JNI_FALSE : JNI_TRUE; }
JNIEXPORT jbyte JNICALL Java_JniSemanticsTest_primByte(JNIEnv* e, jclass c, jbyte b) { return b; }
JNIEXPORT jshort JNICALL Java_JniSemanticsTest_primShort(JNIEnv* e, jclass c, jshort s) { return s; }
JNIEXPORT jchar JNICALL Java_JniSemanticsTest_primChar(JNIEnv* e, jclass c, jchar ch) { return ch; }
JNIEXPORT jfloat JNICALL Java_JniSemanticsTest_primFloat(JNIEnv* e, jclass c, jfloat f) { return f * 0.5f; }

/* ---- strings (modified UTF-8) ---- */
JNIEXPORT jint JNICALL Java_JniSemanticsTest_utfLength(JNIEnv* e, jclass c, jstring s) {
    return (*e)->GetStringUTFLength(e, s);
}
JNIEXPORT jint JNICALL Java_JniSemanticsTest_utf16Length(JNIEnv* e, jclass c, jstring s) {
    return (*e)->GetStringLength(e, s);
}
JNIEXPORT jstring JNICALL Java_JniSemanticsTest_echoUtf(JNIEnv* e, jclass c, jstring s) {
    const char* u;
    jstring r;
    if (s == 0) return 0;
    u = (*e)->GetStringUTFChars(e, s, 0);
    if (u == 0) return 0;
    r = (*e)->NewStringUTF(e, u);
    (*e)->ReleaseStringUTFChars(e, s, u);
    return r;
}

/* ---- arrays ---- */
JNIEXPORT jintArray JNICALL Java_JniSemanticsTest_copyInts(JNIEnv* e, jclass c, jintArray a) {
    jsize n = (*e)->GetArrayLength(e, a);
    jintArray r = (*e)->NewIntArray(e, n);
    jint* tmp;
    if (r == 0) return 0;
    if (n == 0) return r;
    tmp = (jint*)malloc((size_t)n * sizeof(jint));
    if (tmp == 0) return 0;
    (*e)->GetIntArrayRegion(e, a, 0, n, tmp);
    (*e)->SetIntArrayRegion(e, r, 0, n, tmp);
    free(tmp);
    return r;
}
JNIEXPORT void JNICALL Java_JniSemanticsTest_bumpInts(JNIEnv* e, jclass c, jintArray a) {
    jsize i;
    jsize n = (*e)->GetArrayLength(e, a);
    jint* p = (*e)->GetIntArrayElements(e, a, 0);
    if (p == 0) return;
    for (i = 0; i < n; i++) p[i] = p[i] + 1;
    (*e)->ReleaseIntArrayElements(e, a, p, 0);
}
JNIEXPORT void JNICALL Java_JniSemanticsTest_setRange(JNIEnv* e, jclass c, jintArray a, jint start, jint len, jint val) {
    jint buf[16];
    jsize i;
    if (len > 16) return;
    for (i = 0; i < len; i++) buf[i] = val;
    (*e)->SetIntArrayRegion(e, a, start, len, buf);
}
JNIEXPORT jint JNICALL Java_JniSemanticsTest_arrayLen(JNIEnv* e, jclass c, jobject a) {
    return (*e)->GetArrayLength(e, a);
}

/* ---- instance and static fields ---- */
JNIEXPORT jint JNICALL Java_JniSemanticsTest_readX(JNIEnv* e, jclass c, jobject p) {
    jclass k = (*e)->GetObjectClass(e, p);
    jfieldID f = (*e)->GetFieldID(e, k, "x", "I");
    return (*e)->GetIntField(e, p, f);
}
JNIEXPORT void JNICALL Java_JniSemanticsTest_writeX(JNIEnv* e, jclass c, jobject p, jint v) {
    jclass k = (*e)->GetObjectClass(e, p);
    jfieldID f = (*e)->GetFieldID(e, k, "x", "I");
    (*e)->SetIntField(e, p, f, v);
}
JNIEXPORT jlong JNICALL Java_JniSemanticsTest_readStamp(JNIEnv* e, jclass c, jobject p) {
    jclass k = (*e)->GetObjectClass(e, p);
    jfieldID f = (*e)->GetFieldID(e, k, "stamp", "J");
    return (*e)->GetLongField(e, p, f);
}
JNIEXPORT void JNICALL Java_JniSemanticsTest_writeStamp(JNIEnv* e, jclass c, jobject p, jlong v) {
    jclass k = (*e)->GetObjectClass(e, p);
    jfieldID f = (*e)->GetFieldID(e, k, "stamp", "J");
    (*e)->SetLongField(e, p, f, v);
}
JNIEXPORT jboolean JNICALL Java_JniSemanticsTest_readSeen(JNIEnv* e, jclass c, jobject p) {
    jclass k = (*e)->GetObjectClass(e, p);
    jfieldID f = (*e)->GetFieldID(e, k, "seen", "Z");
    return (*e)->GetBooleanField(e, p, f);
}
JNIEXPORT jdouble JNICALL Java_JniSemanticsTest_readWeight(JNIEnv* e, jclass c, jobject p) {
    jclass k = (*e)->GetObjectClass(e, p);
    jfieldID f = (*e)->GetFieldID(e, k, "weight", "D");
    return (*e)->GetDoubleField(e, p, f);
}
JNIEXPORT jobject JNICALL Java_JniSemanticsTest_readTag(JNIEnv* e, jclass c, jobject p) {
    jclass k = (*e)->GetObjectClass(e, p);
    jfieldID f = (*e)->GetFieldID(e, k, "tag", "Ljava/lang/String;");
    return (*e)->GetObjectField(e, p, f);
}
JNIEXPORT void JNICALL Java_JniSemanticsTest_writeTag(JNIEnv* e, jclass c, jobject p, jobject v) {
    jclass k = (*e)->GetObjectClass(e, p);
    jfieldID f = (*e)->GetFieldID(e, k, "tag", "Ljava/lang/String;");
    (*e)->SetObjectField(e, p, f, v);
}
JNIEXPORT jint JNICALL Java_JniSemanticsTest_readCount(JNIEnv* e, jclass c, jclass k) {
    jfieldID f = (*e)->GetStaticFieldID(e, k, "count", "I");
    return (*e)->GetStaticIntField(e, k, f);
}
JNIEXPORT void JNICALL Java_JniSemanticsTest_writeCount(JNIEnv* e, jclass c, jclass k, jint v) {
    jfieldID f = (*e)->GetStaticFieldID(e, k, "count", "I");
    (*e)->SetStaticIntField(e, k, f, v);
}
JNIEXPORT jobject JNICALL Java_JniSemanticsTest_readNote(JNIEnv* e, jclass c, jclass k) {
    jfieldID f = (*e)->GetStaticFieldID(e, k, "note", "Ljava/lang/String;");
    return (*e)->GetStaticObjectField(e, k, f);
}
/* GetFieldID failure: returns 0x3 when the field is missing, the pending
   NoSuchFieldError is observed via ExceptionCheck and cleared. */
JNIEXPORT jint JNICALL Java_JniSemanticsTest_badFieldProbe(JNIEnv* e, jclass c, jobject p) {
    jclass k = (*e)->GetObjectClass(e, p);
    jfieldID f = (*e)->GetFieldID(e, k, "noSuchFieldJck", "I");
    jint pending = (*e)->ExceptionCheck(e) ? 1 : 0;
    if ((*e)->ExceptionCheck(e)) (*e)->ExceptionClear(e);
    return (f == 0 ? 2 : 0) | pending;
}

/* ---- methods ---- */
JNIEXPORT jint JNICALL Java_JniSemanticsTest_callSum(JNIEnv* e, jclass c, jobject p, jint a, jint b) {
    jclass k = (*e)->GetObjectClass(e, p);
    jmethodID m = (*e)->GetMethodID(e, k, "sum", "(II)I");
    return (*e)->CallIntMethod(e, p, m, a, b);
}
JNIEXPORT jstring JNICALL Java_JniSemanticsTest_callName(JNIEnv* e, jclass c, jobject p) {
    jclass k = (*e)->GetObjectClass(e, p);
    jmethodID m = (*e)->GetMethodID(e, k, "name", "()Ljava/lang/String;");
    return (jstring)(*e)->CallObjectMethod(e, p, m);
}
JNIEXPORT jint JNICALL Java_JniSemanticsTest_callStaticMul(JNIEnv* e, jclass c, jclass k, jint a, jint b) {
    jmethodID m = (*e)->GetStaticMethodID(e, k, "mul", "(II)I");
    return (*e)->CallStaticIntMethod(e, k, m, a, b);
}
JNIEXPORT jint JNICALL Java_JniSemanticsTest_callNonvirtualSum(JNIEnv* e, jclass c, jobject p, jclass k, jint a, jint b) {
    jmethodID m = (*e)->GetMethodID(e, k, "sum", "(II)I");
    return (*e)->CallNonvirtualIntMethod(e, p, k, m, a, b);
}
JNIEXPORT jclass JNICALL Java_JniSemanticsTest_superclass(JNIEnv* e, jclass c, jclass k) {
    return (*e)->GetSuperclass(e, k);
}
/* JNI IsAssignableFrom(clazz1, clazz2) asks whether clazz1 objects can be
   stored in clazz2 variables, so the arguments are passed swapped with
   respect to the Java-facing (to, from) order. */
JNIEXPORT jboolean JNICALL Java_JniSemanticsTest_assignable(JNIEnv* e, jclass c, jclass to, jclass from) {
    return (*e)->IsAssignableFrom(e, from, to);
}

/* ---- exceptions ---- */
JNIEXPORT void JNICALL Java_JniSemanticsTest_throwIae(JNIEnv* e, jclass c, jstring msg) {
    jclass k = (*e)->FindClass(e, "java/lang/IllegalArgumentException");
    const char* m;
    if (k == 0) return;
    m = (*e)->GetStringUTFChars(e, msg, 0);
    (*e)->ThrowNew(e, k, m);
    (*e)->ReleaseStringUTFChars(e, msg, m);
}
/* Returns 0x3 when ExceptionCheck sees the pending ThrowNew exception and
   reports no exception again after ExceptionClear. */
JNIEXPORT jint JNICALL Java_JniSemanticsTest_exceptionFlow(JNIEnv* e, jclass c) {
    jclass k = (*e)->FindClass(e, "java/lang/IllegalStateException");
    jint r;
    if (k == 0) return -1;
    (*e)->ThrowNew(e, k, "jck");
    r = ((*e)->ExceptionCheck(e) ? 2 : 0);
    (*e)->ExceptionClear(e);
    if (!(*e)->ExceptionCheck(e)) r |= 1;
    return r;
}
JNIEXPORT jstring JNICALL Java_JniSemanticsTest_throwClearContinue(JNIEnv* e, jclass c) {
    jclass k = (*e)->FindClass(e, "java/lang/UnsupportedOperationException");
    if (k == 0) return 0;
    (*e)->ThrowNew(e, k, "cleared");
    (*e)->ExceptionClear(e);
    return (*e)->NewStringUTF(e, "still-alive");
}

/* ---- references ---- */
JNIEXPORT void JNICALL Java_JniSemanticsTest_keepGlobal(JNIEnv* e, jclass c, jobject o) {
    if (g_global != 0) (*e)->DeleteGlobalRef(e, g_global);
    g_global = (*e)->NewGlobalRef(e, o);
}
JNIEXPORT jobject JNICALL Java_JniSemanticsTest_readGlobal(JNIEnv* e, jclass c) {
    return g_global;
}
JNIEXPORT void JNICALL Java_JniSemanticsTest_makeWeak(JNIEnv* e, jclass c, jobject o) {
    if (g_weak != 0) (*e)->DeleteWeakGlobalRef(e, g_weak);
    g_weak = (*e)->NewWeakGlobalRef(e, o);
}
JNIEXPORT jobject JNICALL Java_JniSemanticsTest_readWeak(JNIEnv* e, jclass c) {
    return g_weak;
}
JNIEXPORT jint JNICALL Java_JniSemanticsTest_pushPopFrame(JNIEnv* e, jclass c) {
    if ((*e)->PushLocalFrame(e, 16) != 0) return -1;
    (*e)->NewStringUTF(e, "frame-local");
    return ((*e)->PopLocalFrame(e, 0) == 0) ? 0 : 1;
}
JNIEXPORT jobject JNICALL Java_JniSemanticsTest_frameCarry(JNIEnv* e, jclass c, jobject o) {
    if ((*e)->PushLocalFrame(e, 16) != 0) return 0;
    return (*e)->PopLocalFrame(e, o);
}

/* ---- VM and registration management ---- */
JNIEXPORT jint JNICALL Java_JniSemanticsTest_onloadVersion(JNIEnv* e, jclass c) {
    return g_onload;
}
JNIEXPORT jint JNICALL Java_JniSemanticsTest_envVersion(JNIEnv* e, jclass c) {
    return (*e)->GetVersion(e);
}
JNIEXPORT jint JNICALL Java_JniSemanticsTest_getVm(JNIEnv* e, jclass c, jlongArray out) {
    JavaVM* vm = 0;
    jint rc = (*e)->GetJavaVM(e, &vm);
    if (rc == 0 && out != 0 && (*e)->GetArrayLength(e, out) >= 1)
        (*e)->SetLongArrayRegion(e, out, 0, 1, (const jlong*)&vm);
    return rc;
}
JNIEXPORT void JNICALL Java_JniSemanticsTest_unregisterAll(JNIEnv* e, jclass c) {
    (*e)->UnregisterNatives(e, c);
}
