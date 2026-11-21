/*
 * Canonical native source for JniArrayTest (JNI primitive-array matrix).
 *
 * Covers: NewLongArray / NewDoubleArray / NewByteArray, Get/SetLongArrayRegion,
 * Get/SetDoubleArrayRegion, GetLongArrayElements / ReleaseLongArrayElements,
 * GetByteArrayElements / ReleaseByteArrayElements, and
 * GetPrimitiveArrayCritical / ReleasePrimitiveArrayCritical.
 *
 * This file is the single source of truth: `make native` compiles it into
 * build/native/<side>-<arch>/libjniarray.so, and JniArrayTest.java embeds an
 * identical copy compiled at run time when no -nativepath is supplied.
 *
 * Manual build command:
 *   gcc -shared -fPIC -I<jdk>/include -I<jdk>/include/linux \
 *       -o libjniarray.so jniarray.c
 */
#include <jni.h>
#include <stdlib.h>

JNIEXPORT jlongArray JNICALL Java_JniArrayTest_copyLongs(JNIEnv* e, jclass c, jlongArray a) {
    jsize n = (*e)->GetArrayLength(e, a);
    jlongArray r = (*e)->NewLongArray(e, n);
    jlong* tmp;
    if (r == 0) return 0;
    if (n == 0) return r;
    tmp = (jlong*)malloc((size_t)n * sizeof(jlong));
    if (tmp == 0) return 0;
    (*e)->GetLongArrayRegion(e, a, 0, n, tmp);
    (*e)->SetLongArrayRegion(e, r, 0, n, tmp);
    free(tmp);
    return r;
}

JNIEXPORT void JNICALL Java_JniArrayTest_bumpLongs(JNIEnv* e, jclass c, jlongArray a) {
    jsize n = (*e)->GetArrayLength(e, a);
    jlong* p = (*e)->GetLongArrayElements(e, a, 0);
    jsize i;
    if (p == 0) return;
    for (i = 0; i < n; i++) p[i] += 1;
    (*e)->ReleaseLongArrayElements(e, a, p, 0);
}

JNIEXPORT jdoubleArray JNICALL Java_JniArrayTest_copyDoubles(JNIEnv* e, jclass c, jdoubleArray a) {
    jsize n = (*e)->GetArrayLength(e, a);
    jdoubleArray r = (*e)->NewDoubleArray(e, n);
    jdouble* tmp;
    if (r == 0) return 0;
    if (n == 0) return r;
    tmp = (jdouble*)malloc((size_t)n * sizeof(jdouble));
    if (tmp == 0) return 0;
    (*e)->GetDoubleArrayRegion(e, a, 0, n, tmp);
    (*e)->SetDoubleArrayRegion(e, r, 0, n, tmp);
    free(tmp);
    return r;
}

JNIEXPORT jbyteArray JNICALL Java_JniArrayTest_copyBytes(JNIEnv* e, jclass c, jbyteArray a) {
    jsize n = (*e)->GetArrayLength(e, a);
    jbyteArray r = (*e)->NewByteArray(e, n);
    jbyte* p = (*e)->GetByteArrayElements(e, a, 0);
    if (r == 0) return 0;
    if (n == 0) return r;
    if (p == 0) return 0;
    (*e)->SetByteArrayRegion(e, r, 0, n, p);
    (*e)->ReleaseByteArrayElements(e, a, p, JNI_ABORT);
    return r;
}

/* NewLongArray of a given length; the length is read back via GetArrayLength. */
JNIEXPORT jint JNICALL Java_JniArrayTest_newLongArrayLen(JNIEnv* e, jclass c, jint n) {
    jlongArray a = (*e)->NewLongArray(e, n);
    if (a == 0) return -1;
    return (*e)->GetArrayLength(e, a);
}

/* GetPrimitiveArrayCritical / ReleasePrimitiveArrayCritical.  GetArrayLength is
   read BEFORE the critical region; no JNI calls are made inside it. */
JNIEXPORT jint JNICALL Java_JniArrayTest_criticalSum(JNIEnv* e, jclass c, jintArray a) {
    jsize n = (*e)->GetArrayLength(e, a);
    jint* p = (*e)->GetPrimitiveArrayCritical(e, a, 0);
    jint sum = 0;
    jsize i;
    if (p == 0) return -1;
    for (i = 0; i < n; i++) sum += p[i];
    (*e)->ReleasePrimitiveArrayCritical(e, a, p, 0);
    return sum;
}

JNIEXPORT void JNICALL Java_JniArrayTest_criticalBump(JNIEnv* e, jclass c, jintArray a) {
    jsize n = (*e)->GetArrayLength(e, a);
    jint* p = (*e)->GetPrimitiveArrayCritical(e, a, 0);
    jsize i;
    if (p == 0) return;
    for (i = 0; i < n; i++) p[i] += 1;
    (*e)->ReleasePrimitiveArrayCritical(e, a, p, 0);
}
