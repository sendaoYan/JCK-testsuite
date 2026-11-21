/*
 * JVMTI agent: thread and stack query observations.
 *
 * Loaded by a child JVM through -agentpath:<this.so>=<outfile>.  The probe
 * class StackProbe declares a static native method query() implemented here as
 * Java_StackProbe_query; inside that live-phase call (running on the main
 * thread, called from StackProbe.leaf) it records:
 *
 *   threads.count=<jint>    GetAllThreads thread count
 *   found.main=<yes|no>     whether a thread named "main" is among all threads
 *   current.name=<char*>    GetThreadInfo name of the current thread
 *   current.priority=<jint> GetThreadInfo priority of the current thread
 *   current.daemon=<0|1>    GetThreadInfo is_daemon of the current thread
 *   frame.count=<jint>      GetFrameCount on the current thread
 *   stack.count=<jint>      GetStackTrace frame count on the current thread
 *   stack.frames=<a,b,...>  comma-joined GetMethodName of each stack frame
 *
 * Compiled at run time against the JDK under test's own jvmti.h; see
 * JvmtiThreadStackTest.java for the Java side.
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
    if (options != NULL && options[0] != '\0') {
        strncpy(g_outpath, options, sizeof g_outpath - 1);
    } else {
        strcpy(g_outpath, "agent.out");
    }
    if ((*vm)->GetEnv(vm, (void**)&jvmti, JVMTI_VERSION_1_2) != JNI_OK)
        return JNI_ERR;
    g_jvmti = jvmti;
    emit("onload", "yes");
    return JNI_OK;
}

JNIEXPORT void JNICALL Agent_OnUnload(JavaVM* vm) {
    emit("unload", "yes");
}

JNIEXPORT void JNICALL Java_StackProbe_query(JNIEnv* env, jclass klass) {
    jvmtiEnv* jvmti = g_jvmti;
    jint threads_count = 0;
    jthread* threads = NULL;
    jthread current = NULL;
    jvmtiThreadInfo info;
    jvmtiFrameInfo frames[32];
    jint frame_count = 0;
    jint stack_count = 0;
    char number[64];
    char list[2048];
    int i;
    int pos = 0;
    int found_main = 0;

    /* Reuse the environment obtained in Agent_OnLoad. */
    if (jvmti == NULL) return;

    if ((*jvmti)->GetAllThreads(jvmti, &threads_count, &threads) == JVMTI_ERROR_NONE
            && threads != NULL) {
        snprintf(number, sizeof number, "%d", (int)threads_count);
        emit("threads.count", number);
        for (i = 0; i < threads_count; i++) {
            memset(&info, 0, sizeof info);
            if ((*jvmti)->GetThreadInfo(jvmti, threads[i], &info) == JVMTI_ERROR_NONE) {
                if (info.name != NULL && strcmp(info.name, "main") == 0)
                    found_main = 1;
                if (info.name != NULL)
                    (*jvmti)->Deallocate(jvmti, (unsigned char*)info.name);
            }
        }
        (*jvmti)->Deallocate(jvmti, (unsigned char*)threads);
        emit("found.main", found_main ? "yes" : "no");
    }

    if ((*jvmti)->GetCurrentThread(jvmti, &current) == JVMTI_ERROR_NONE
            && current != NULL) {
        memset(&info, 0, sizeof info);
        if ((*jvmti)->GetThreadInfo(jvmti, current, &info) == JVMTI_ERROR_NONE) {
            emit("current.name", info.name == NULL ? "<null>" : info.name);
            snprintf(number, sizeof number, "%d", (int)info.priority);
            emit("current.priority", number);
            snprintf(number, sizeof number, "%d", (int)info.is_daemon);
            emit("current.daemon", number);
            if (info.name != NULL)
                (*jvmti)->Deallocate(jvmti, (unsigned char*)info.name);
        }

        if ((*jvmti)->GetFrameCount(jvmti, current, &frame_count) == JVMTI_ERROR_NONE) {
            snprintf(number, sizeof number, "%d", (int)frame_count);
            emit("frame.count", number);
        }

        if ((*jvmti)->GetStackTrace(jvmti, current, 0, 32, frames, &stack_count)
                == JVMTI_ERROR_NONE) {
            snprintf(number, sizeof number, "%d", (int)stack_count);
            emit("stack.count", number);
            list[0] = '\0';
            pos = 0;
            for (i = 0; i < stack_count && i < 32; i++) {
                char* name = NULL;
                if ((*jvmti)->GetMethodName(jvmti, frames[i].method, &name, NULL, NULL)
                        == JVMTI_ERROR_NONE && name != NULL) {
                    if (pos > 0 && pos < (int)sizeof list - 1) {
                        list[pos++] = ',';
                        list[pos] = '\0';
                    }
                    snprintf(list + pos, sizeof list - pos, "%s", name);
                    pos += (int)strlen(name);
                    (*jvmti)->Deallocate(jvmti, (unsigned char*)name);
                }
            }
            emit("stack.frames", list);
        }
    }
}
