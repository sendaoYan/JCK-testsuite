#include <jni.h>
JNIEXPORT jint JNICALL Java_SystemLoadNativeTest_jckAdd(JNIEnv* e, jclass c, jint a, jint b) {
    return a + b;
}
