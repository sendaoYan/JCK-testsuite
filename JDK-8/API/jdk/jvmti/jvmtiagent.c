/*
 * JVMTI agent: on-load lifecycle + core function observations.
 *
 * Loaded by a child JVM through -agentpath:<this.so>=<outfile>.  The agent
 * writes its observations to <outfile> as key=value lines (one per call):
 *
 *   version=<jint>            GetVersionNumber in the on-load phase
 *   onload=yes                Agent_OnLoad was entered and returned JNI_OK
 *   class.signature=...       GetClassSignature at the AgentProbe ClassPrepare
 *   class.status=<jint>       GetClassStatus at the AgentProbe ClassPrepare
 *   thread.name=...           GetThreadInfo on the current thread (live phase)
 *   thread.priority=<jint>    GetThreadInfo priority
 *   unload=yes                Agent_OnUnload was entered at VM shutdown
 *
 * GetCurrentThread / GetThreadInfo are deliberately called from the
 * ClassPrepare callback rather than Agent_OnLoad: the OnLoad phase runs
 * before the VM is "live", where GetCurrentThread reports WRONG_PHASE.
 *
 * Compiled at run time against the JDK under test's own jvmti.h; see
 * JvmtiAgentTest.java for the Java side.  C89/C99-portable, no JVM link
 * dependency (jvmti symbols resolve at load time).
 */
#include <jvmti.h>
#include <stdio.h>
#include <string.h>

static char g_outpath[4096];

static void emit(const char* key, const char* value) {
    FILE* f = fopen(g_outpath, "a");
    if (f == NULL) return;
    fprintf(f, "%s=%s\n", key, value);
    fclose(f);
}

static void JNICALL onClassPrepare(jvmtiEnv* jvmti, JNIEnv* env,
                                   jthread thread, jclass klass) {
    char* signature = NULL;
    jint status = 0;
    jthread current = NULL;
    jvmtiThreadInfo info;
    char number[64];

    if ((*jvmti)->GetClassSignature(jvmti, klass, &signature, NULL)
            != JVMTI_ERROR_NONE) return;
    if (signature == NULL || strcmp(signature, "LAgentProbe;") != 0) {
        if (signature != NULL) (*jvmti)->Deallocate(jvmti, (unsigned char*)signature);
        return;
    }
    (*jvmti)->Deallocate(jvmti, (unsigned char*)signature);

    (*jvmti)->GetClassStatus(jvmti, klass, &status);
    snprintf(number, sizeof number, "%d", (int)status);
    emit("class.signature", "LAgentProbe;");
    emit("class.status", number);

    if ((*jvmti)->GetCurrentThread(jvmti, &current) == JVMTI_ERROR_NONE
            && current != NULL) {
        memset(&info, 0, sizeof info);
        if ((*jvmti)->GetThreadInfo(jvmti, current, &info) == JVMTI_ERROR_NONE) {
            emit("thread.name", info.name == NULL ? "<null>" : info.name);
            snprintf(number, sizeof number, "%d", (int)info.priority);
            emit("thread.priority", number);
        }
    }
}

JNIEXPORT jint JNICALL Agent_OnLoad(JavaVM* vm, char* options, void* reserved) {
    jvmtiEnv* jvmti = NULL;
    jint version = 0;
    jvmtiEventCallbacks callbacks;
    char number[64];

    if (options != NULL && options[0] != '\0') {
        strncpy(g_outpath, options, sizeof g_outpath - 1);
    } else {
        strcpy(g_outpath, "agent.out");
    }

    if ((*vm)->GetEnv(vm, (void**)&jvmti, JVMTI_VERSION_1_2) != JNI_OK)
        return JNI_ERR;

    if ((*jvmti)->GetVersionNumber(jvmti, &version) == JVMTI_ERROR_NONE) {
        snprintf(number, sizeof number, "%d", (int)version);
        emit("version", number);
    }
    emit("onload", "yes");

    memset(&callbacks, 0, sizeof callbacks);
    callbacks.ClassPrepare = onClassPrepare;
    (*jvmti)->SetEventCallbacks(jvmti, &callbacks, sizeof callbacks);
    (*jvmti)->SetEventNotificationMode(jvmti, JVMTI_ENABLE,
                                       JVMTI_EVENT_CLASS_PREPARE, NULL);

    return JNI_OK;
}

JNIEXPORT void JNICALL Agent_OnUnload(JavaVM* vm) {
    emit("unload", "yes");
}
