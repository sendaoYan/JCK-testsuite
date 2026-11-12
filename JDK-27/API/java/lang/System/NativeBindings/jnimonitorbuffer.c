#include <jni.h>
#include <stdlib.h>
#include <stdint.h>

/* JNI function matrix: monitor operations, NIO direct-buffer access, and
   object-array handling.  Compiled at run time against the JDK under test's
   own jni.h; see JniMonitorAndBufferTest.java for the Java side.  All
   entries are static natives, so the second argument is the class. */

/* ---- monitor operations (JNI spec "Monitor Operations") ---- */

JNIEXPORT jint JNICALL Java_JniMonitorAndBufferTest_monitorEnterExit(JNIEnv* e, jclass c, jobject o) {
    if ((*e)->MonitorEnter(e, o) != 0) return -1;
    if ((*e)->MonitorExit(e, o) != 0) return -2;
    return 0;
}

JNIEXPORT jint JNICALL Java_JniMonitorAndBufferTest_monitorEnterTwiceExitOnce(JNIEnv* e, jclass c, jobject o) {
    if ((*e)->MonitorEnter(e, o) != 0) return -1;
    if ((*e)->MonitorEnter(e, o) != 0) return -2;
    if ((*e)->MonitorExit(e, o) != 0) return -3;
    return 0; /* recursion count is now 1: the caller's monitor is still held */
}

JNIEXPORT jint JNICALL Java_JniMonitorAndBufferTest_monitorExitOnce(JNIEnv* e, jclass c, jobject o) {
    return (*e)->MonitorExit(e, o);
}

JNIEXPORT jint JNICALL Java_JniMonitorAndBufferTest_monitorEnterNull(JNIEnv* e, jclass c) {
    (*e)->MonitorEnter(e, 0); /* NULL object */
    {
        jint pending = (*e)->ExceptionCheck(e) ? 1 : 0;
        if ((*e)->ExceptionCheck(e)) (*e)->ExceptionClear(e);
        return pending;
    }
}

/* Enters o's monitor, signals the entered latch, blocks until the release
   latch fires (via a callback into Java), then exits the monitor. */
JNIEXPORT void JNICALL Java_JniMonitorAndBufferTest_monitorEnterHoldExit(JNIEnv* e, jclass c, jobject o, jobject entered, jobject release) {
    jclass k = (*e)->FindClass(e, "JniMonitorAndBufferTest");
    jmethodID signal;
    jmethodID await;
    if (k == 0) return;
    signal = (*e)->GetStaticMethodID(e, k, "signalEntered", "(Ljava/util/concurrent/CountDownLatch;)V");
    await  = (*e)->GetStaticMethodID(e, k, "awaitRelease", "(Ljava/util/concurrent/CountDownLatch;)V");
    if (signal == 0 || await == 0) return;
    if ((*e)->MonitorEnter(e, o) != 0) return;
    (*e)->CallStaticVoidMethod(e, k, signal, entered);
    (*e)->CallStaticVoidMethod(e, k, await, release);
    (*e)->MonitorExit(e, o);
}

/* ---- NIO direct buffers (JNI spec "NIO Support") ---- */

JNIEXPORT jlong JNICALL Java_JniMonitorAndBufferTest_directBufferCapacity(JNIEnv* e, jclass c, jobject buf) {
    return (jlong)(*e)->GetDirectBufferCapacity(e, buf);
}

JNIEXPORT jlong JNICALL Java_JniMonitorAndBufferTest_directBufferAddress(JNIEnv* e, jclass c, jobject buf) {
    return (jlong)(intptr_t)(*e)->GetDirectBufferAddress(e, buf);
}

/* NULL is treated as "not a direct buffer": returns -1 with NO exception
   (unlike MonitorEnter(NULL), which throws NPE).  GetDirectBufferAddress is
   deliberately NOT probed with NULL here because that path dereferences the
   handle and crashes the VM - a documented do-not-call. */
JNIEXPORT jlong JNICALL Java_JniMonitorAndBufferTest_directBufferCapacityNull(JNIEnv* e, jclass c) {
    return (jlong)(*e)->GetDirectBufferCapacity(e, 0);
}

/* Wraps freshly malloc'd memory into a direct ByteBuffer with a known byte
   pattern (byte i == i % 251). */
JNIEXPORT jobject JNICALL Java_JniMonitorAndBufferTest_newDirectByteBuffer(JNIEnv* e, jclass c, jint cap) {
    unsigned char* buf = (unsigned char*)malloc((size_t)cap);
    jint i;
    if (buf == 0) return 0;
    for (i = 0; i < cap; i++) buf[i] = (unsigned char)(i % 251);
    return (*e)->NewDirectByteBuffer(e, buf, (jlong)cap);
}

/* ---- object arrays (JNI spec "Operations on Arrays") ---- */

JNIEXPORT jint JNICALL Java_JniMonitorAndBufferTest_objectArrayLength(JNIEnv* e, jclass c, jobjectArray arr) {
    return (*e)->GetArrayLength(e, arr);
}

/* Returns element 0 and overwrites element 1 with a native-built string. */
JNIEXPORT jobject JNICALL Java_JniMonitorAndBufferTest_objectArrayReadWrite(JNIEnv* e, jclass c, jobjectArray arr) {
    jobject first = (*e)->GetObjectArrayElement(e, arr, 0);
    jstring w = (*e)->NewStringUTF(e, "written-by-native");
    (*e)->SetObjectArrayElement(e, arr, 1, w);
    return first;
}

/* Out-of-bounds read must leave a pending ArrayIndexOutOfBoundsException. */
JNIEXPORT jint JNICALL Java_JniMonitorAndBufferTest_objectArrayOOB(JNIEnv* e, jclass c, jobjectArray arr) {
    (*e)->GetObjectArrayElement(e, arr, -1);
    {
        jint pending = (*e)->ExceptionCheck(e) ? 1 : 0;
        if ((*e)->ExceptionCheck(e)) (*e)->ExceptionClear(e);
        return pending;
    }
}

JNIEXPORT jobjectArray JNICALL Java_JniMonitorAndBufferTest_newObjectArray(JNIEnv* e, jclass c, jclass elemCls, jobject fill, jint len) {
    return (*e)->NewObjectArray(e, len, elemCls, fill);
}

JNIEXPORT jboolean JNICALL Java_JniMonitorAndBufferTest_isSameObject(JNIEnv* e, jclass c, jobject a, jobject b) {
    return (*e)->IsSameObject(e, a, b);
}
