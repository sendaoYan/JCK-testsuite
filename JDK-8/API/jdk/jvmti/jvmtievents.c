/*
 * JVMTI agent: event callback observations.
 *
 * Loaded by a child JVM through -agentpath:<this.so>=<outfile>.  The agent
 * enables VMInit/VMDeath, ClassLoad/ClassPrepare, ThreadStart/ThreadEnd and
 * MethodEntry/MethodExit and records, as key=value lines, whether each fires
 * for the probe class EventProbe and its "probe-worker" thread:
 *
 *   event.vminit=yes             VMInit fired; phase.vminit reports LIVE (4)
 *   event.vmdeath=yes            VMDeath fired; phase.vmdeath still reports LIVE (4)
 *   event.classload=yes          ClassLoad fired for LEventProbe;
 *   event.classprepare=yes       ClassPrepare fired for LEventProbe;
 *   event.threadstart.probe=yes  ThreadStart fired for the probe-worker thread
 *   event.threadend.probe=yes    ThreadEnd fired for the probe-worker thread
 *   event.methodentry=yes        MethodEntry fired for EventProbe.sampleMethod
 *   event.methodexit=yes         MethodExit fired for EventProbe.sampleMethod
 *   onload=yes / unload=yes      Agent_OnLoad / Agent_OnUnload entered
 *
 * MethodEntry/MethodExit require the can_generate_method_entry_events /
 * can_generate_method_exit_events capabilities; the agent acquires them in
 * Agent_OnLoad via AddCapabilities.  A raw monitor serializes the concurrent
 * emits that the various callback threads produce.  See JvmtiEventTest.java.
 */
#include <jvmti.h>
#include <stdio.h>
#include <string.h>

static char g_outpath[4096];
static jvmtiEnv* g_jvmti;
static jrawMonitorID g_lock;
static jthread g_worker; /* global ref of the probe-worker thread */

static void emit(const char* key, const char* value) {
    jvmtiPhase phase = JVMTI_PHASE_LIVE;
    int locked = 0;
    if (g_lock != NULL && g_jvmti != NULL
            && (*g_jvmti)->GetPhase(g_jvmti, &phase) == JVMTI_ERROR_NONE
            && phase == JVMTI_PHASE_LIVE) {
        (*g_jvmti)->RawMonitorEnter(g_jvmti, g_lock);
        locked = 1;
    }
    {
        FILE* f = fopen(g_outpath, "a");
        if (f != NULL) {
            fprintf(f, "%s=%s\n", key, value);
            fclose(f);
        }
    }
    if (locked) (*g_jvmti)->RawMonitorExit(g_jvmti, g_lock);
}

static int is_probe_method(jvmtiEnv* jvmti, jmethodID method) {
    char* name = NULL;
    jclass declaring = NULL;
    char* sig = NULL;
    int result = 0;

    if ((*jvmti)->GetMethodName(jvmti, method, &name, NULL, NULL) != JVMTI_ERROR_NONE)
        return 0;
    if (name == NULL || strcmp(name, "sampleMethod") != 0) {
        if (name != NULL) (*jvmti)->Deallocate(jvmti, (unsigned char*)name);
        return 0;
    }
    (*jvmti)->Deallocate(jvmti, (unsigned char*)name);

    if ((*jvmti)->GetMethodDeclaringClass(jvmti, method, &declaring) != JVMTI_ERROR_NONE)
        return 0;
    if ((*jvmti)->GetClassSignature(jvmti, declaring, &sig, NULL) != JVMTI_ERROR_NONE)
        return 0;
    if (sig != NULL && strcmp(sig, "LEventProbe;") == 0)
        result = 1;
    if (sig != NULL) (*jvmti)->Deallocate(jvmti, (unsigned char*)sig);
    return result;
}

static void JNICALL onVMInit(jvmtiEnv* jvmti, JNIEnv* env, jthread thread) {
    jvmtiPhase phase = 0;
    char number[64];
    emit("event.vminit", "yes");
    if ((*jvmti)->GetPhase(jvmti, &phase) == JVMTI_ERROR_NONE) {
        snprintf(number, sizeof number, "%d", (int)phase);
        emit("phase.vminit", number);
    }
    /* Defer MethodEntry/MethodExit enablement until the VM is live so the
     * startup class-loading flood is not observed. */
    (*jvmti)->SetEventNotificationMode(jvmti, JVMTI_ENABLE, JVMTI_EVENT_METHOD_ENTRY, NULL);
    (*jvmti)->SetEventNotificationMode(jvmti, JVMTI_ENABLE, JVMTI_EVENT_METHOD_EXIT, NULL);
}

static void JNICALL onVMDeath(jvmtiEnv* jvmti, JNIEnv* env) {
    jvmtiPhase phase = 0;
    char number[64];
    emit("event.vmdeath", "yes");
    if ((*jvmti)->GetPhase(jvmti, &phase) == JVMTI_ERROR_NONE) {
        snprintf(number, sizeof number, "%d", (int)phase);
        emit("phase.vmdeath", number);
    }
}

static void JNICALL onClassLoad(jvmtiEnv* jvmti, JNIEnv* env, jthread thread, jclass klass) {
    char* sig = NULL;
    if ((*jvmti)->GetClassSignature(jvmti, klass, &sig, NULL) != JVMTI_ERROR_NONE) return;
    if (sig == NULL) return;
    if (strcmp(sig, "LEventProbe;") == 0) emit("event.classload", "yes");
    (*jvmti)->Deallocate(jvmti, (unsigned char*)sig);
}

static void JNICALL onClassPrepare(jvmtiEnv* jvmti, JNIEnv* env, jthread thread, jclass klass) {
    char* sig = NULL;
    if ((*jvmti)->GetClassSignature(jvmti, klass, &sig, NULL) != JVMTI_ERROR_NONE) return;
    if (sig == NULL) return;
    if (strcmp(sig, "LEventProbe;") == 0) emit("event.classprepare", "yes");
    (*jvmti)->Deallocate(jvmti, (unsigned char*)sig);
}

static void JNICALL onThreadStart(jvmtiEnv* jvmti, JNIEnv* env, jthread thread) {
    jvmtiThreadInfo info;
    memset(&info, 0, sizeof info);
    if ((*jvmti)->GetThreadInfo(jvmti, thread, &info) == JVMTI_ERROR_NONE && info.name != NULL) {
        if (strcmp(info.name, "probe-worker") == 0) {
            if (g_worker == NULL)
                g_worker = (*env)->NewGlobalRef(env, thread);
            emit("event.threadstart.probe", "yes");
        }
        (*jvmti)->Deallocate(jvmti, (unsigned char*)info.name);
    }
}

static void JNICALL onThreadEnd(jvmtiEnv* jvmti, JNIEnv* env, jthread thread) {
    if (g_worker != NULL && (*env)->IsSameObject(env, thread, g_worker))
        emit("event.threadend.probe", "yes");
}

static void JNICALL onMethodEntry(jvmtiEnv* jvmti, JNIEnv* env, jthread thread, jmethodID method) {
    if (is_probe_method(jvmti, method)) emit("event.methodentry", "yes");
}

static void JNICALL onMethodExit(jvmtiEnv* jvmti, JNIEnv* env, jthread thread, jmethodID method,
                                 jboolean was_popped_by_exception, jvalue return_value) {
    if (is_probe_method(jvmti, method)) emit("event.methodexit", "yes");
}

JNIEXPORT jint JNICALL Agent_OnLoad(JavaVM* vm, char* options, void* reserved) {
    jvmtiEnv* jvmti = NULL;
    jvmtiCapabilities caps;
    jvmtiEventCallbacks callbacks;
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

    (*jvmti)->CreateRawMonitor(jvmti, "jvmti-event-emit-lock", &g_lock);

    memset(&caps, 0, sizeof caps);
    caps.can_generate_method_entry_events = 1;
    caps.can_generate_method_exit_events = 1;
    rc = (*jvmti)->AddCapabilities(jvmti, &caps);
    snprintf(number, sizeof number, "%d", (int)rc);
    emit("addcaps.rc", number);

    memset(&callbacks, 0, sizeof callbacks);
    callbacks.VMInit = onVMInit;
    callbacks.VMDeath = onVMDeath;
    callbacks.ClassLoad = onClassLoad;
    callbacks.ClassPrepare = onClassPrepare;
    callbacks.ThreadStart = onThreadStart;
    callbacks.ThreadEnd = onThreadEnd;
    callbacks.MethodEntry = onMethodEntry;
    callbacks.MethodExit = onMethodExit;
    (*jvmti)->SetEventCallbacks(jvmti, &callbacks, sizeof callbacks);

    (*jvmti)->SetEventNotificationMode(jvmti, JVMTI_ENABLE, JVMTI_EVENT_VM_INIT, NULL);
    (*jvmti)->SetEventNotificationMode(jvmti, JVMTI_ENABLE, JVMTI_EVENT_VM_DEATH, NULL);
    (*jvmti)->SetEventNotificationMode(jvmti, JVMTI_ENABLE, JVMTI_EVENT_CLASS_LOAD, NULL);
    (*jvmti)->SetEventNotificationMode(jvmti, JVMTI_ENABLE, JVMTI_EVENT_CLASS_PREPARE, NULL);
    (*jvmti)->SetEventNotificationMode(jvmti, JVMTI_ENABLE, JVMTI_EVENT_THREAD_START, NULL);
    (*jvmti)->SetEventNotificationMode(jvmti, JVMTI_ENABLE, JVMTI_EVENT_THREAD_END, NULL);

    emit("onload", "yes");
    return JNI_OK;
}

JNIEXPORT void JNICALL Agent_OnUnload(JavaVM* vm) {
    emit("unload", "yes");
    if (g_worker != NULL && g_jvmti != NULL) {
        /* DeleteGlobalRef needs a JNIEnv; the VM is already dead here, so just
         * drop the reference without cleanup -- a one-shot test process. */
    }
}
