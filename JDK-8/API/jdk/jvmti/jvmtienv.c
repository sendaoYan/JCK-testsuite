/*
 * JVMTI agent: environment, phase and capability observations.
 *
 * Loaded by a child JVM through -agentpath:<this.so>=<outfile>.  All queries
 * run from Agent_OnLoad, where the phase is JVMTI_PHASE_ONLOAD and the
 * capability functions are callable.  Observations are written to <outfile> as
 * key=value lines (one per call):
 *
 *   version=<jint>                GetVersionNumber in the on-load phase
 *   phase=<jint>                  GetPhase in the on-load phase (expect ONLOAD=1)
 *   cap.initial.tag=<0|1>         can_tag_objects reported by GetCapabilities
 *   cap.potential.tag=<0|1>       can_tag_objects reported by GetPotentialCapabilities
 *   cap.add.rc=<jvmtiError>       AddCapabilities(can_tag_objects) return code
 *   cap.after.add.tag=<0|1>       can_tag_objects after AddCapabilities
 *   cap.relinquish.rc=<jvmtiError>RelinquishCapabilities(can_tag_objects) return code
 *   cap.after.relinquish.tag=<0|1>can_tag_objects after RelinquishCapabilities
 *   onload=yes                    Agent_OnLoad entered and returned JNI_OK
 *   unload=yes                    Agent_OnUnload entered at VM shutdown
 *
 * Compiled at run time against the JDK under test's own jvmti.h; see
 * JvmtiEnvTest.java for the Java side.  C89/C99-portable, no JVM link
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

JNIEXPORT jint JNICALL Agent_OnLoad(JavaVM* vm, char* options, void* reserved) {
    jvmtiEnv* jvmti = NULL;
    jvmtiCapabilities caps;
    jvmtiCapabilities potential;
    jvmtiPhase phase = 0;
    jint version = 0;
    jint rc;
    char number[64];

    if (options != NULL && options[0] != '\0') {
        strncpy(g_outpath, options, sizeof g_outpath - 1);
    } else {
        strcpy(g_outpath, "agent.out");
    }

    if ((*vm)->GetEnv(vm, (void**)&jvmti, JVMTI_VERSION_1_2) != JNI_OK)
        return JNI_ERR;

    emit("onload", "yes");

    if ((*jvmti)->GetVersionNumber(jvmti, &version) == JVMTI_ERROR_NONE) {
        snprintf(number, sizeof number, "%d", (int)version);
        emit("version", number);
    }

    if ((*jvmti)->GetPhase(jvmti, &phase) == JVMTI_ERROR_NONE) {
        snprintf(number, sizeof number, "%d", (int)phase);
        emit("phase", number);
    }

    memset(&caps, 0, sizeof caps);
    if ((*jvmti)->GetCapabilities(jvmti, &caps) == JVMTI_ERROR_NONE) {
        snprintf(number, sizeof number, "%d", (int)caps.can_tag_objects);
        emit("cap.initial.tag", number);
    }

    memset(&potential, 0, sizeof potential);
    if ((*jvmti)->GetPotentialCapabilities(jvmti, &potential) == JVMTI_ERROR_NONE) {
        snprintf(number, sizeof number, "%d", (int)potential.can_tag_objects);
        emit("cap.potential.tag", number);
    }

    memset(&caps, 0, sizeof caps);
    caps.can_tag_objects = 1;
    rc = (*jvmti)->AddCapabilities(jvmti, &caps);
    snprintf(number, sizeof number, "%d", (int)rc);
    emit("cap.add.rc", number);

    memset(&caps, 0, sizeof caps);
    if ((*jvmti)->GetCapabilities(jvmti, &caps) == JVMTI_ERROR_NONE) {
        snprintf(number, sizeof number, "%d", (int)caps.can_tag_objects);
        emit("cap.after.add.tag", number);
    }

    memset(&caps, 0, sizeof caps);
    caps.can_tag_objects = 1;
    rc = (*jvmti)->RelinquishCapabilities(jvmti, &caps);
    snprintf(number, sizeof number, "%d", (int)rc);
    emit("cap.relinquish.rc", number);

    memset(&caps, 0, sizeof caps);
    if ((*jvmti)->GetCapabilities(jvmti, &caps) == JVMTI_ERROR_NONE) {
        snprintf(number, sizeof number, "%d", (int)caps.can_tag_objects);
        emit("cap.after.relinquish.tag", number);
    }

    return JNI_OK;
}

JNIEXPORT void JNICALL Agent_OnUnload(JavaVM* vm) {
    emit("unload", "yes");
}
