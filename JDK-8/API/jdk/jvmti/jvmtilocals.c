/*
 * JVMTI agent: local variable table and line number table observations.
 *
 * Loaded by a child JVM through -agentpath:<this.so>=<outfile>.  The probe
 * class LocalVarProbe declares a static native method query() implemented here
 * as Java_LocalVarProbe_query; inside that live-phase call it records, for the
 * LocalVarProbe.instrumented method:
 *
 *   lvt.count=<jint>        GetLocalVariableTable entry count
 *   lvt.entries=<name:sig,...> comma-joined "name:signature" per entry
 *   lnt.count=<jint>        GetLineNumberTable entry count
 *   lnt.first=<jint>        line number of the first table entry
 *   lnt.last=<jint>         line number of the last table entry
 *
 * LocalVarProbe is compiled with javac -g so the LocalVariableTable is present.
 * Compiled at run time against the JDK under test's own jvmti.h; see
 * JvmtiLocalVarLineTest.java for the Java side.
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

    /* GetLocalVariableTable / GetLineNumberTable require the
     * can_access_local_variables / can_get_line_numbers capabilities. */
    memset(&caps, 0, sizeof caps);
    caps.can_access_local_variables = 1;
    caps.can_get_line_numbers = 1;
    rc = (*jvmti)->AddCapabilities(jvmti, &caps);
    snprintf(number, sizeof number, "%d", (int)rc);
    emit("addcaps.rc", number);

    return JNI_OK;
}

JNIEXPORT void JNICALL Agent_OnUnload(JavaVM* vm) {
    emit("unload", "yes");
}

JNIEXPORT void JNICALL Java_LocalVarProbe_query(JNIEnv* env, jclass klass) {
    jvmtiEnv* jvmti = g_jvmti;
    jmethodID mid = NULL;
    jint lvt_count = 0;
    jvmtiLocalVariableEntry* lvt = NULL;
    jint lnt_count = 0;
    jvmtiLineNumberEntry* lnt = NULL;
    char number[64];
    char list[2048];
    int i;
    int pos = 0;

    /* Reuse the environment obtained in Agent_OnLoad. */
    if (jvmti == NULL) return;

    mid = (*env)->GetStaticMethodID(env, klass, "instrumented", "()I");
    if (mid == NULL) {
        emit("error", "no instrumented method");
        return;
    }

    if ((*jvmti)->GetLocalVariableTable(jvmti, mid, &lvt_count, &lvt) == JVMTI_ERROR_NONE
            && lvt != NULL) {
        snprintf(number, sizeof number, "%d", (int)lvt_count);
        emit("lvt.count", number);
        list[0] = '\0';
        pos = 0;
        for (i = 0; i < lvt_count; i++) {
            if (lvt[i].name == NULL || lvt[i].signature == NULL) continue;
            if (pos > 0 && pos < (int)sizeof list - 1) {
                list[pos++] = ',';
                list[pos] = '\0';
            }
            snprintf(list + pos, sizeof list - pos, "%s:%s", lvt[i].name, lvt[i].signature);
            pos += (int)strlen(lvt[i].name) + 1 + (int)strlen(lvt[i].signature);
            if (pos >= (int)sizeof list) pos = (int)sizeof list - 1;
        }
        emit("lvt.entries", list);
        for (i = 0; i < lvt_count; i++) {
            if (lvt[i].name != NULL)
                (*jvmti)->Deallocate(jvmti, (unsigned char*)lvt[i].name);
            if (lvt[i].signature != NULL)
                (*jvmti)->Deallocate(jvmti, (unsigned char*)lvt[i].signature);
            if (lvt[i].generic_signature != NULL)
                (*jvmti)->Deallocate(jvmti, (unsigned char*)lvt[i].generic_signature);
        }
        (*jvmti)->Deallocate(jvmti, (unsigned char*)lvt);
    }

    if ((*jvmti)->GetLineNumberTable(jvmti, mid, &lnt_count, &lnt) == JVMTI_ERROR_NONE
            && lnt != NULL) {
        snprintf(number, sizeof number, "%d", (int)lnt_count);
        emit("lnt.count", number);
        if (lnt_count > 0) {
            snprintf(number, sizeof number, "%d", (int)lnt[0].line_number);
            emit("lnt.first", number);
            snprintf(number, sizeof number, "%d", (int)lnt[lnt_count - 1].line_number);
            emit("lnt.last", number);
        }
        (*jvmti)->Deallocate(jvmti, (unsigned char*)lnt);
    }
}
