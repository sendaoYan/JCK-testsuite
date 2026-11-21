/*
 * Canonical native source for JniStringTest (JNI string matrix).
 *
 * Covers: NewString (build from UTF-16 code units), GetStringChars /
 * ReleaseStringChars, GetStringRegion, GetStringUTFRegion (modified UTF-8
 * bytes), GetStringCritical / ReleaseStringCritical.
 *
 * This file is the single source of truth: `make native` compiles it into
 * build/native/<side>-<arch>/libjnistring.so, and JniStringTest.java embeds an
 * identical copy compiled at run time when no -nativepath is supplied.
 *
 * Manual build command:
 *   gcc -shared -fPIC -I<jdk>/include -I<jdk>/include/linux \
 *       -o libjnistring.so jnistring.c
 */
#include <jni.h>
#include <stdlib.h>

/* NewString: build a jstring from UTF-16 code units. */
JNIEXPORT jstring JNICALL Java_JniStringTest_newStringFromChars(JNIEnv* e, jclass c, jcharArray chars) {
    jsize n = (*e)->GetArrayLength(e, chars);
    jchar* buf = (jchar*)malloc((size_t)(n > 0 ? n : 1) * sizeof(jchar));
    jstring r;
    if (buf == 0) return 0;
    if (n > 0) (*e)->GetCharArrayRegion(e, chars, 0, n, buf);
    r = (*e)->NewString(e, buf, n);
    free(buf);
    return r;
}

/* GetStringChars + NewString + ReleaseStringChars round trip. */
JNIEXPORT jstring JNICALL Java_JniStringTest_echoChars(JNIEnv* e, jclass c, jstring s) {
    const jchar* u = (*e)->GetStringChars(e, s, 0);
    jstring r;
    jsize n;
    if (u == 0) return 0;
    n = (*e)->GetStringLength(e, s);
    r = (*e)->NewString(e, u, n);
    (*e)->ReleaseStringChars(e, s, u);
    return r;
}

/* GetStringRegion: copy len UTF-16 chars starting at start into a new char[]. */
JNIEXPORT jcharArray JNICALL Java_JniStringTest_getStringRegion(JNIEnv* e, jclass c, jstring s, jint start, jint len) {
    jcharArray r = (*e)->NewCharArray(e, len);
    jchar* buf;
    if (r == 0) return 0;
    if (len == 0) return r;
    buf = (jchar*)malloc((size_t)len * sizeof(jchar));
    if (buf == 0) return 0;
    (*e)->GetStringRegion(e, s, start, len, buf);
    (*e)->SetCharArrayRegion(e, r, 0, len, buf);
    free(buf);
    return r;
}

/* GetStringUTFRegion: copy len chars starting at start as modified UTF-8 into a
   byte[].  The whole string is copied when start==0 && len==length, so the
   buffer is sized to GetStringUTFLength (plus one spare byte in case the
   implementation null-terminates; only n bytes are handed back to Java). */
JNIEXPORT jbyteArray JNICALL Java_JniStringTest_getStringUTFRegion(JNIEnv* e, jclass c, jstring s, jint start, jint len) {
    jsize n = (*e)->GetStringUTFLength(e, s);
    jbyteArray r = (*e)->NewByteArray(e, n);
    jbyte* buf;
    if (r == 0) return 0;
    if (n == 0) return r;
    buf = (jbyte*)malloc((size_t)n + 1);
    if (buf == 0) return 0;
    (*e)->GetStringUTFRegion(e, s, start, len, buf);
    (*e)->SetByteArrayRegion(e, r, 0, n, buf);
    free(buf);
    return r;
}

/* GetStringCritical / ReleaseStringCritical: sum the UTF-16 code units of the
   critical copy.  No JNI calls are made inside the critical region. */
JNIEXPORT jint JNICALL Java_JniStringTest_stringCriticalSum(JNIEnv* e, jclass c, jstring s) {
    jsize n = (*e)->GetStringLength(e, s);
    const jchar* p = (*e)->GetStringCritical(e, s, 0);
    jint sum = 0;
    jsize i;
    if (p == 0) return -1;
    for (i = 0; i < n; i++) sum += (jint)p[i];
    (*e)->ReleaseStringCritical(e, s, p);
    return sum;
}
