/*
 * Canonical native source for JniObjectTest (JNI object & reference matrix).
 *
 * Covers: NewObject (constructor invocation), AllocObject (uninitialized),
 * NewLocalRef / DeleteLocalRef, EnsureLocalCapacity, GetObjectRefType
 * (local / global / weak-global / invalid reference classification).
 *
 * This file is the single source of truth: `make native` compiles it into
 * build/native/<side>-<arch>/libjniobject.so, and JniObjectTest.java embeds an
 * identical copy compiled at run time when no -nativepath is supplied.
 *
 * Manual build command:
 *   gcc -shared -fPIC -I<jdk>/include -I<jdk>/include/linux \
 *       -o libjniobject.so jniobject.c
 */
#include <jni.h>

static jobject g_global = 0;
static jweak g_weak = 0;

/* NewObject: invoke the (I)V constructor of the class. */
JNIEXPORT jobject JNICALL Java_JniObjectTest_newObject(JNIEnv* e, jclass c, jclass k, jint x) {
    jmethodID ctor = (*e)->GetMethodID(e, k, "<init>", "(I)V");
    if (ctor == 0) return 0;
    return (*e)->NewObject(e, k, ctor, x);
}

/* AllocObject: allocate without running any constructor. */
JNIEXPORT jobject JNICALL Java_JniObjectTest_allocObject(JNIEnv* e, jclass c, jclass k) {
    return (*e)->AllocObject(e, k);
}

/* NewLocalRef: create a new local reference (NULL in -> NULL out). */
JNIEXPORT jobject JNICALL Java_JniObjectTest_newLocalRef(JNIEnv* e, jclass c, jobject o) {
    return (*e)->NewLocalRef(e, o);
}

/* DeleteLocalRef: free the local reference. */
JNIEXPORT void JNICALL Java_JniObjectTest_deleteLocalRef(JNIEnv* e, jclass c, jobject o) {
    (*e)->DeleteLocalRef(e, o);
}

/* EnsureLocalCapacity: reserve room for cap local references. */
JNIEXPORT jint JNICALL Java_JniObjectTest_ensureLocalCapacity(JNIEnv* e, jclass c, jint cap) {
    return (*e)->EnsureLocalCapacity(e, cap);
}

/* GetObjectRefType on the argument (a local reference). */
JNIEXPORT jint JNICALL Java_JniObjectTest_localRefType(JNIEnv* e, jclass c, jobject o) {
    return (jint)(*e)->GetObjectRefType(e, o);
}

/* GetObjectRefType on NULL (a null reference is invalid). */
JNIEXPORT jint JNICALL Java_JniObjectTest_nullRefType(JNIEnv* e, jclass c) {
    return (jint)(*e)->GetObjectRefType(e, 0);
}

JNIEXPORT void JNICALL Java_JniObjectTest_keepGlobalRef(JNIEnv* e, jclass c, jobject o) {
    if (g_global != 0) (*e)->DeleteGlobalRef(e, g_global);
    g_global = (*e)->NewGlobalRef(e, o);
}

JNIEXPORT jint JNICALL Java_JniObjectTest_globalRefType(JNIEnv* e, jclass c) {
    return (jint)(*e)->GetObjectRefType(e, g_global);
}

JNIEXPORT void JNICALL Java_JniObjectTest_makeWeakRef(JNIEnv* e, jclass c, jobject o) {
    if (g_weak != 0) (*e)->DeleteWeakGlobalRef(e, g_weak);
    g_weak = (*e)->NewWeakGlobalRef(e, o);
}

JNIEXPORT jint JNICALL Java_JniObjectTest_weakRefType(JNIEnv* e, jclass c) {
    return (jint)(*e)->GetObjectRefType(e, g_weak);
}
