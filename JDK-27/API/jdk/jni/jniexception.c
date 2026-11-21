/*
 * Canonical native source for JniExceptionTest (JNI exception matrix).
 *
 * Covers: ExceptionOccurred (null when clean, pending throwable otherwise),
 * Throw (delivers an existing throwable, and ExceptionOccurred returns that
 * exact object), ThrowNew (re-checked for the occurred/check/clear state
 * machine), and ExceptionDescribe (prints and clears the pending exception).
 *
 * This file is the single source of truth: `make native` compiles it into
 * build/native/<side>-<arch>/libjniexception.so, and JniExceptionTest.java
 * embeds an identical copy compiled at run time when no -nativepath is given.
 *
 * Manual build command:
 *   gcc -shared -fPIC -I<jdk>/include -I<jdk>/include/linux \
 *       -o libjniexception.so jniexception.c
 */
#include <jni.h>

/* ExceptionOccurred returns NULL when no exception is pending. */
JNIEXPORT jint JNICALL Java_JniExceptionTest_occurredClean(JNIEnv* e, jclass c) {
    return (*e)->ExceptionOccurred(e) == 0 ? 0 : 1;
}

/* ThrowNew + ExceptionOccurred/ExceptionCheck/ExceptionClear state machine.
   Returns bit 0 = occurred non-null, bit 1 = check true, bit 2 = clean after. */
JNIEXPORT jint JNICALL Java_JniExceptionTest_occurredAfterThrowNew(JNIEnv* e, jclass c) {
    jclass k = (*e)->FindClass(e, "java/lang/IllegalStateException");
    jthrowable t;
    jint r = 0;
    if (k == 0) return -1;
    (*e)->ThrowNew(e, k, "jck-msg");
    t = (*e)->ExceptionOccurred(e);
    if (t != 0) r |= 1;
    if ((*e)->ExceptionCheck(e)) r |= 2;
    (*e)->ExceptionClear(e);
    if ((*e)->ExceptionOccurred(e) == 0) r |= 4;
    return r;
}

/* Throw an existing throwable object; ExceptionOccurred must return that exact
   object (IsSameObject).  Returns 1 on success. */
JNIEXPORT jint JNICALL Java_JniExceptionTest_throwAndOccurred(JNIEnv* e, jclass c, jstring msg) {
    jclass k = (*e)->FindClass(e, "java/lang/IllegalArgumentException");
    jmethodID ctor;
    jthrowable t;
    jthrowable occurred;
    jint r = 0;
    if (k == 0) return -1;
    ctor = (*e)->GetMethodID(e, k, "<init>", "(Ljava/lang/String;)V");
    if (ctor == 0) return -2;
    t = (jthrowable)(*e)->NewObject(e, k, ctor, msg);
    (*e)->Throw(e, t);
    occurred = (*e)->ExceptionOccurred(e);
    if ((*e)->IsSameObject(e, t, occurred)) r |= 1;
    (*e)->ExceptionClear(e);
    return r;
}

/* Throw an existing throwable that propagates out to the Java caller. */
JNIEXPORT void JNICALL Java_JniExceptionTest_throwCustom(JNIEnv* e, jclass c, jstring msg) {
    jclass k = (*e)->FindClass(e, "java/lang/IllegalArgumentException");
    jmethodID ctor = (*e)->GetMethodID(e, k, "<init>", "(Ljava/lang/String;)V");
    jthrowable t = (jthrowable)(*e)->NewObject(e, k, ctor, msg);
    (*e)->Throw(e, t);
}

/* ExceptionDescribe prints the pending exception to stderr and, on the
   reference implementation, clears the pending exception as a side effect.
   Returns bit 0 = pending before the call, bit 1 = cleared after the call. */
JNIEXPORT jint JNICALL Java_JniExceptionTest_describe(JNIEnv* e, jclass c) {
    jclass k = (*e)->FindClass(e, "java/lang/IllegalStateException");
    jint r = 0;
    if (k == 0) return -1;
    (*e)->ThrowNew(e, k, "describe-me");
    if ((*e)->ExceptionCheck(e)) r |= 1;
    (*e)->ExceptionDescribe(e);
    if (!(*e)->ExceptionCheck(e)) r |= 2;
    if ((*e)->ExceptionCheck(e)) (*e)->ExceptionClear(e);
    return r;
}
