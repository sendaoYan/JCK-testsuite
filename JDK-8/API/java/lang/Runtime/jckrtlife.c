#include <jni.h>
JNIEXPORT jint JNICALL Java_RuntimeLoadNativeTest_jckMul(JNIEnv* e, jclass c, jint a, jint b) {
    return a * b;
}
