/*
 * JVMTI agent: class and method query observations.
 *
 * Loaded by a child JVM through -agentpath:<this.so>=<outfile>.  The probe
 * class ClassMethodProbe declares a static native method query() which this
 * agent implements as Java_ClassMethodProbe_query; inside that live-phase
 * call it records:
 *
 *   signature=<char*>     GetClassSignature on the probe class
 *   modifiers=<jint>      GetClassModifiers on the probe class
 *   source.file=<char*>   GetSourceFileName on the probe class
 *   loaded.count=<jint>   GetLoadedClasses class count
 *   loaded.found=<yes|no> whether the probe class is among the loaded classes
 *   method.name=<char*>   GetMethodName on ClassMethodProbe.sampleMethod
 *
 * Compiled at run time against the JDK under test's own jvmti.h; see
 * JvmtiClassMethodTest.java for the Java side.
 */
#include <jvmti.h>
#include <stdio.h>
#include <string.h>

static char g_outpath[4096];
static jvmtiEnv* g_jvmti;

static void emit(const char* key, const char* value) {
    FILE* f = fopen(g_outpath, "a");
    if (f == NULL) return;
    fprintf(f, "%s=%s\n", key, value);
    fclose(f);
}

JNIEXPORT jint JNICALL Agent_OnLoad(JavaVM* vm, char* options, void* reserved) {
    jvmtiEnv* jvmti = NULL;
    jvmtiCapabilities caps;
    jint rc;
    char number[64];

    if (options != NULL && options[0] != '\0') {
        strncpy(g_outpath, options, sizeof g_outpath - 1);
    } else {
        strcpy(g_outpath, "agent.out");
    }
    if ((*vm)->GetEnv(vm, (void**)&jvmti, JVMTI_VERSION_1_2) != JNI_OK)
        return JNI_ERR;
    g_jvmti = jvmti;
    emit("onload", "yes");

    /* GetSourceFileName requires the can_get_source_file_name capability. */
    memset(&caps, 0, sizeof caps);
    caps.can_get_source_file_name = 1;
    rc = (*jvmti)->AddCapabilities(jvmti, &caps);
    snprintf(number, sizeof number, "%d", (int)rc);
    emit("addcaps.rc", number);

    return JNI_OK;
}

JNIEXPORT void JNICALL Agent_OnUnload(JavaVM* vm) {
    emit("unload", "yes");
}

JNIEXPORT void JNICALL Java_ClassMethodProbe_query(JNIEnv* env, jclass klass) {
    jvmtiEnv* jvmti = g_jvmti;
    jint loaded_count = 0;
    jclass* loaded = NULL;
    char* signature = NULL;
    char* source = NULL;
    char* method_name = NULL;
    jint modifiers = 0;
    jmethodID mid = NULL;
    char number[64];
    int i;
    int found = 0;

    /* Reuse the environment obtained in Agent_OnLoad: calling GetEnv again in
     * the live phase would mint a fresh, capability-less environment. */
    if (jvmti == NULL) return;

    if ((*jvmti)->GetClassSignature(jvmti, klass, &signature, NULL) == JVMTI_ERROR_NONE
            && signature != NULL) {
        emit("signature", signature);
        (*jvmti)->Deallocate(jvmti, (unsigned char*)signature);
    }

    if ((*jvmti)->GetClassModifiers(jvmti, klass, &modifiers) == JVMTI_ERROR_NONE) {
        snprintf(number, sizeof number, "%d", (int)modifiers);
        emit("modifiers", number);
    }

    if ((*jvmti)->GetSourceFileName(jvmti, klass, &source) == JVMTI_ERROR_NONE
            && source != NULL) {
        emit("source.file", source);
        (*jvmti)->Deallocate(jvmti, (unsigned char*)source);
    }

    if ((*jvmti)->GetLoadedClasses(jvmti, &loaded_count, &loaded) == JVMTI_ERROR_NONE
            && loaded != NULL) {
        snprintf(number, sizeof number, "%d", (int)loaded_count);
        emit("loaded.count", number);
        for (i = 0; i < loaded_count; i++) {
            char* s = NULL;
            if ((*jvmti)->GetClassSignature(jvmti, loaded[i], &s, NULL) == JVMTI_ERROR_NONE
                    && s != NULL) {
                if (strcmp(s, "LClassMethodProbe;") == 0) found = 1;
                (*jvmti)->Deallocate(jvmti, (unsigned char*)s);
            }
        }
        (*jvmti)->Deallocate(jvmti, (unsigned char*)loaded);
        emit("loaded.found", found ? "yes" : "no");
    }

    mid = (*env)->GetMethodID(env, klass, "sampleMethod", "()I");
    if (mid != NULL
            && (*jvmti)->GetMethodName(jvmti, mid, &method_name, NULL, NULL) == JVMTI_ERROR_NONE
            && method_name != NULL) {
        emit("method.name", method_name);
        (*jvmti)->Deallocate(jvmti, (unsigned char*)method_name);
    }
}
