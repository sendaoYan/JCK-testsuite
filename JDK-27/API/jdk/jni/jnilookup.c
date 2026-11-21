/*
 * Canonical native source for JniLookupTest (JNI lookup & reflection matrix).
 *
 * Covers: FindClass (found, and missing -> NULL plus a pending exception),
 * GetMethodID (missing -> NULL plus a pending exception), IsInstanceOf,
 * FromReflectedMethod/ToReflectedMethod, FromReflectedField/ToReflectedField.
 *
 * This file is the single source of truth: `make native` compiles it into
 * build/native/<side>-<arch>/libjnilookup.so, and JniLookupTest.java embeds an
 * identical copy that is compiled at run time when no -nativepath is supplied.
 *
 * Manual build command:
 *   gcc -shared -fPIC -I<jdk>/include -I<jdk>/include/linux \
 *       -o libjnilookup.so jnilookup.c
 */
#include <jni.h>
#include <stdlib.h>

/* FindClass: resolve a binary name or an array descriptor. */
JNIEXPORT jclass JNICALL Java_JniLookupTest_findClass(JNIEnv* e, jclass c, jstring name) {
    const char* n = (*e)->GetStringUTFChars(e, name, 0);
    jclass r;
    if (n == 0) return 0;
    r = (*e)->FindClass(e, n);
    (*e)->ReleaseStringUTFChars(e, name, n);
    return r;
}

/* FindClass of a missing class: NULL result plus a pending exception.
   Returns bit 0 = a pending throwable is present, bit 1 = result is NULL. */
JNIEXPORT jint JNICALL Java_JniLookupTest_findClassMissing(JNIEnv* e, jclass c) {
    jclass r = (*e)->FindClass(e, "no/such/class/JckMissing");
    jthrowable t = (*e)->ExceptionOccurred(e);
    jint bits = 0;
    if (r == 0) bits |= 2;
    if (t != 0) bits |= 1;
    if ((*e)->ExceptionCheck(e)) (*e)->ExceptionClear(e);
    return bits;
}

/* IsInstanceOf(obj, clazz). */
JNIEXPORT jboolean JNICALL Java_JniLookupTest_isInstanceOf(JNIEnv* e, jclass c, jobject o, jclass k) {
    return (*e)->IsInstanceOf(e, o, k);
}

/* GetMethodID of a missing method: NULL plus a pending exception. */
JNIEXPORT jint JNICALL Java_JniLookupTest_badMethodProbe(JNIEnv* e, jclass c, jobject p) {
    jclass k = (*e)->GetObjectClass(e, p);
    jmethodID m = (*e)->GetMethodID(e, k, "noSuchMethodJck", "()V");
    jthrowable t = (*e)->ExceptionOccurred(e);
    jint bits = 0;
    if (m == 0) bits |= 2;
    if (t != 0) bits |= 1;
    if ((*e)->ExceptionCheck(e)) (*e)->ExceptionClear(e);
    return bits;
}

/* FromReflectedMethod: obtain a jmethodID from a java.lang.reflect.Method and
   prove it is usable by invoking the method through it. */
JNIEXPORT jint JNICALL Java_JniLookupTest_callViaReflectedMethod(JNIEnv* e, jclass c, jobject target, jobject m) {
    jmethodID id = (*e)->FromReflectedMethod(e, m);
    if (id == 0) return -1;
    return (*e)->CallIntMethod(e, target, id, 20, 22);
}

/* ToReflectedMethod: turn a jmethodID (looked up by name+signature) back into a
   java.lang.reflect.Method object. */
JNIEXPORT jobject JNICALL Java_JniLookupTest_toReflectedMethod(JNIEnv* e, jclass c, jclass k, jstring name, jstring sig) {
    const char* n = (*e)->GetStringUTFChars(e, name, 0);
    const char* s = (*e)->GetStringUTFChars(e, sig, 0);
    jmethodID id;
    jobject r;
    if (n == 0 || s == 0) return 0;
    id = (*e)->GetMethodID(e, k, n, s);
    (*e)->ReleaseStringUTFChars(e, name, n);
    (*e)->ReleaseStringUTFChars(e, sig, s);
    if (id == 0) return 0;
    r = (*e)->ToReflectedMethod(e, k, id, JNI_FALSE);
    return r;
}

/* FromReflectedField: obtain a jfieldID from a java.lang.reflect.Field and
   prove it is usable by reading the field through it. */
JNIEXPORT jint JNICALL Java_JniLookupTest_readViaReflectedField(JNIEnv* e, jclass c, jobject target, jobject f) {
    jfieldID id = (*e)->FromReflectedField(e, f);
    if (id == 0) return -1;
    return (*e)->GetIntField(e, target, id);
}

/* ToReflectedField: turn a jfieldID (looked up by name+signature) back into a
   java.lang.reflect.Field object. */
JNIEXPORT jobject JNICALL Java_JniLookupTest_toReflectedField(JNIEnv* e, jclass c, jclass k, jstring name, jstring sig) {
    const char* n = (*e)->GetStringUTFChars(e, name, 0);
    const char* s = (*e)->GetStringUTFChars(e, sig, 0);
    jfieldID id;
    jobject r;
    if (n == 0 || s == 0) return 0;
    id = (*e)->GetFieldID(e, k, n, s);
    (*e)->ReleaseStringUTFChars(e, name, n);
    (*e)->ReleaseStringUTFChars(e, sig, s);
    if (id == 0) return 0;
    r = (*e)->ToReflectedField(e, k, id, JNI_FALSE);
    return r;
}
