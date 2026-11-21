/*
 * @test
 * @summary JVMTI event callback contract: build a native agent from embedded C
 *          with gcc (or load the prebuilt libjvmtievents.so via -nativepath),
 *          launch a child JVM with -agentpath, and assert ClassLoad,
 *          ClassPrepare, MethodEntry/MethodExit, ThreadStart/ThreadEnd and
 *          VMInit/VMDeath all fire at the expected points.
 * @library /lib
 * @run main/othervm JvmtiEventTest
 */

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class JvmtiEventTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean gccAvailable() {
        try {
            Process p = new ProcessBuilder("gcc", "--version").start();
            p.getInputStream().close();
            p.getOutputStream().close();
            p.getErrorStream().close();
            return p.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    // ------------------------------------------------------------------
    // Embedded C agent (identical to jvmtievents.c in this directory)
    // ------------------------------------------------------------------
    static final String AGENT_SRC =
        "/*\n" +
        " * JVMTI agent: event callback observations.\n" +
        " *\n" +
        " * Loaded by a child JVM through -agentpath:<this.so>=<outfile>.  The agent\n" +
        " * enables VMInit/VMDeath, ClassLoad/ClassPrepare, ThreadStart/ThreadEnd and\n" +
        " * MethodEntry/MethodExit and records, as key=value lines, whether each fires\n" +
        " * for the probe class EventProbe and its \"probe-worker\" thread:\n" +
        " *\n" +
        " *   event.vminit=yes             VMInit fired; phase.vminit reports LIVE (4)\n" +
        " *   event.vmdeath=yes            VMDeath fired; phase.vmdeath still reports LIVE (4)\n" +
        " *   event.classload=yes          ClassLoad fired for LEventProbe;\n" +
        " *   event.classprepare=yes       ClassPrepare fired for LEventProbe;\n" +
        " *   event.threadstart.probe=yes  ThreadStart fired for the probe-worker thread\n" +
        " *   event.threadend.probe=yes    ThreadEnd fired for the probe-worker thread\n" +
        " *   event.methodentry=yes        MethodEntry fired for EventProbe.sampleMethod\n" +
        " *   event.methodexit=yes         MethodExit fired for EventProbe.sampleMethod\n" +
        " *   onload=yes / unload=yes      Agent_OnLoad / Agent_OnUnload entered\n" +
        " *\n" +
        " * MethodEntry/MethodExit require the can_generate_method_entry_events /\n" +
        " * can_generate_method_exit_events capabilities; the agent acquires them in\n" +
        " * Agent_OnLoad via AddCapabilities.  A raw monitor serializes the concurrent\n" +
        " * emits that the various callback threads produce.  See JvmtiEventTest.java.\n" +
        " */\n" +
        "#include <jvmti.h>\n" +
        "#include <stdio.h>\n" +
        "#include <string.h>\n" +
        "\n" +
        "static char g_outpath[4096];\n" +
        "static jvmtiEnv* g_jvmti;\n" +
        "static jrawMonitorID g_lock;\n" +
        "static jthread g_worker; /* global ref of the probe-worker thread */\n" +
        "\n" +
        "static void emit(const char* key, const char* value) {\n" +
        "    jvmtiPhase phase = JVMTI_PHASE_LIVE;\n" +
        "    int locked = 0;\n" +
        "    if (g_lock != NULL && g_jvmti != NULL\n" +
        "            && (*g_jvmti)->GetPhase(g_jvmti, &phase) == JVMTI_ERROR_NONE\n" +
        "            && phase == JVMTI_PHASE_LIVE) {\n" +
        "        (*g_jvmti)->RawMonitorEnter(g_jvmti, g_lock);\n" +
        "        locked = 1;\n" +
        "    }\n" +
        "    {\n" +
        "        FILE* f = fopen(g_outpath, \"a\");\n" +
        "        if (f != NULL) {\n" +
        "            fprintf(f, \"%s=%s\\n\", key, value);\n" +
        "            fclose(f);\n" +
        "        }\n" +
        "    }\n" +
        "    if (locked) (*g_jvmti)->RawMonitorExit(g_jvmti, g_lock);\n" +
        "}\n" +
        "\n" +
        "static int is_probe_method(jvmtiEnv* jvmti, jmethodID method) {\n" +
        "    char* name = NULL;\n" +
        "    jclass declaring = NULL;\n" +
        "    char* sig = NULL;\n" +
        "    int result = 0;\n" +
        "\n" +
        "    if ((*jvmti)->GetMethodName(jvmti, method, &name, NULL, NULL) != JVMTI_ERROR_NONE)\n" +
        "        return 0;\n" +
        "    if (name == NULL || strcmp(name, \"sampleMethod\") != 0) {\n" +
        "        if (name != NULL) (*jvmti)->Deallocate(jvmti, (unsigned char*)name);\n" +
        "        return 0;\n" +
        "    }\n" +
        "    (*jvmti)->Deallocate(jvmti, (unsigned char*)name);\n" +
        "\n" +
        "    if ((*jvmti)->GetMethodDeclaringClass(jvmti, method, &declaring) != JVMTI_ERROR_NONE)\n" +
        "        return 0;\n" +
        "    if ((*jvmti)->GetClassSignature(jvmti, declaring, &sig, NULL) != JVMTI_ERROR_NONE)\n" +
        "        return 0;\n" +
        "    if (sig != NULL && strcmp(sig, \"LEventProbe;\") == 0)\n" +
        "        result = 1;\n" +
        "    if (sig != NULL) (*jvmti)->Deallocate(jvmti, (unsigned char*)sig);\n" +
        "    return result;\n" +
        "}\n" +
        "\n" +
        "static void JNICALL onVMInit(jvmtiEnv* jvmti, JNIEnv* env, jthread thread) {\n" +
        "    jvmtiPhase phase = 0;\n" +
        "    char number[64];\n" +
        "    emit(\"event.vminit\", \"yes\");\n" +
        "    if ((*jvmti)->GetPhase(jvmti, &phase) == JVMTI_ERROR_NONE) {\n" +
        "        snprintf(number, sizeof number, \"%d\", (int)phase);\n" +
        "        emit(\"phase.vminit\", number);\n" +
        "    }\n" +
        "    /* Defer MethodEntry/MethodExit enablement until the VM is live so the\n" +
        "     * startup class-loading flood is not observed. */\n" +
        "    (*jvmti)->SetEventNotificationMode(jvmti, JVMTI_ENABLE, JVMTI_EVENT_METHOD_ENTRY, NULL);\n" +
        "    (*jvmti)->SetEventNotificationMode(jvmti, JVMTI_ENABLE, JVMTI_EVENT_METHOD_EXIT, NULL);\n" +
        "}\n" +
        "\n" +
        "static void JNICALL onVMDeath(jvmtiEnv* jvmti, JNIEnv* env) {\n" +
        "    jvmtiPhase phase = 0;\n" +
        "    char number[64];\n" +
        "    emit(\"event.vmdeath\", \"yes\");\n" +
        "    if ((*jvmti)->GetPhase(jvmti, &phase) == JVMTI_ERROR_NONE) {\n" +
        "        snprintf(number, sizeof number, \"%d\", (int)phase);\n" +
        "        emit(\"phase.vmdeath\", number);\n" +
        "    }\n" +
        "}\n" +
        "\n" +
        "static void JNICALL onClassLoad(jvmtiEnv* jvmti, JNIEnv* env, jthread thread, jclass klass) {\n" +
        "    char* sig = NULL;\n" +
        "    if ((*jvmti)->GetClassSignature(jvmti, klass, &sig, NULL) != JVMTI_ERROR_NONE) return;\n" +
        "    if (sig == NULL) return;\n" +
        "    if (strcmp(sig, \"LEventProbe;\") == 0) emit(\"event.classload\", \"yes\");\n" +
        "    (*jvmti)->Deallocate(jvmti, (unsigned char*)sig);\n" +
        "}\n" +
        "\n" +
        "static void JNICALL onClassPrepare(jvmtiEnv* jvmti, JNIEnv* env, jthread thread, jclass klass) {\n" +
        "    char* sig = NULL;\n" +
        "    if ((*jvmti)->GetClassSignature(jvmti, klass, &sig, NULL) != JVMTI_ERROR_NONE) return;\n" +
        "    if (sig == NULL) return;\n" +
        "    if (strcmp(sig, \"LEventProbe;\") == 0) emit(\"event.classprepare\", \"yes\");\n" +
        "    (*jvmti)->Deallocate(jvmti, (unsigned char*)sig);\n" +
        "}\n" +
        "\n" +
        "static void JNICALL onThreadStart(jvmtiEnv* jvmti, JNIEnv* env, jthread thread) {\n" +
        "    jvmtiThreadInfo info;\n" +
        "    memset(&info, 0, sizeof info);\n" +
        "    if ((*jvmti)->GetThreadInfo(jvmti, thread, &info) == JVMTI_ERROR_NONE && info.name != NULL) {\n" +
        "        if (strcmp(info.name, \"probe-worker\") == 0) {\n" +
        "            if (g_worker == NULL)\n" +
        "                g_worker = (*env)->NewGlobalRef(env, thread);\n" +
        "            emit(\"event.threadstart.probe\", \"yes\");\n" +
        "        }\n" +
        "        (*jvmti)->Deallocate(jvmti, (unsigned char*)info.name);\n" +
        "    }\n" +
        "}\n" +
        "\n" +
        "static void JNICALL onThreadEnd(jvmtiEnv* jvmti, JNIEnv* env, jthread thread) {\n" +
        "    if (g_worker != NULL && (*env)->IsSameObject(env, thread, g_worker))\n" +
        "        emit(\"event.threadend.probe\", \"yes\");\n" +
        "}\n" +
        "\n" +
        "static void JNICALL onMethodEntry(jvmtiEnv* jvmti, JNIEnv* env, jthread thread, jmethodID method) {\n" +
        "    if (is_probe_method(jvmti, method)) emit(\"event.methodentry\", \"yes\");\n" +
        "}\n" +
        "\n" +
        "static void JNICALL onMethodExit(jvmtiEnv* jvmti, JNIEnv* env, jthread thread, jmethodID method,\n" +
        "                                 jboolean was_popped_by_exception, jvalue return_value) {\n" +
        "    if (is_probe_method(jvmti, method)) emit(\"event.methodexit\", \"yes\");\n" +
        "}\n" +
        "\n" +
        "JNIEXPORT jint JNICALL Agent_OnLoad(JavaVM* vm, char* options, void* reserved) {\n" +
        "    jvmtiEnv* jvmti = NULL;\n" +
        "    jvmtiCapabilities caps;\n" +
        "    jvmtiEventCallbacks callbacks;\n" +
        "    jint rc;\n" +
        "    char number[64];\n" +
        "\n" +
        "    if (options != NULL && options[0] != '\\0') {\n" +
        "        strncpy(g_outpath, options, sizeof g_outpath - 1);\n" +
        "    } else {\n" +
        "        strcpy(g_outpath, \"agent.out\");\n" +
        "    }\n" +
        "\n" +
        "    if ((*vm)->GetEnv(vm, (void**)&jvmti, JVMTI_VERSION_1_2) != JNI_OK)\n" +
        "        return JNI_ERR;\n" +
        "    g_jvmti = jvmti;\n" +
        "\n" +
        "    (*jvmti)->CreateRawMonitor(jvmti, \"jvmti-event-emit-lock\", &g_lock);\n" +
        "\n" +
        "    memset(&caps, 0, sizeof caps);\n" +
        "    caps.can_generate_method_entry_events = 1;\n" +
        "    caps.can_generate_method_exit_events = 1;\n" +
        "    rc = (*jvmti)->AddCapabilities(jvmti, &caps);\n" +
        "    snprintf(number, sizeof number, \"%d\", (int)rc);\n" +
        "    emit(\"addcaps.rc\", number);\n" +
        "\n" +
        "    memset(&callbacks, 0, sizeof callbacks);\n" +
        "    callbacks.VMInit = onVMInit;\n" +
        "    callbacks.VMDeath = onVMDeath;\n" +
        "    callbacks.ClassLoad = onClassLoad;\n" +
        "    callbacks.ClassPrepare = onClassPrepare;\n" +
        "    callbacks.ThreadStart = onThreadStart;\n" +
        "    callbacks.ThreadEnd = onThreadEnd;\n" +
        "    callbacks.MethodEntry = onMethodEntry;\n" +
        "    callbacks.MethodExit = onMethodExit;\n" +
        "    (*jvmti)->SetEventCallbacks(jvmti, &callbacks, sizeof callbacks);\n" +
        "\n" +
        "    (*jvmti)->SetEventNotificationMode(jvmti, JVMTI_ENABLE, JVMTI_EVENT_VM_INIT, NULL);\n" +
        "    (*jvmti)->SetEventNotificationMode(jvmti, JVMTI_ENABLE, JVMTI_EVENT_VM_DEATH, NULL);\n" +
        "    (*jvmti)->SetEventNotificationMode(jvmti, JVMTI_ENABLE, JVMTI_EVENT_CLASS_LOAD, NULL);\n" +
        "    (*jvmti)->SetEventNotificationMode(jvmti, JVMTI_ENABLE, JVMTI_EVENT_CLASS_PREPARE, NULL);\n" +
        "    (*jvmti)->SetEventNotificationMode(jvmti, JVMTI_ENABLE, JVMTI_EVENT_THREAD_START, NULL);\n" +
        "    (*jvmti)->SetEventNotificationMode(jvmti, JVMTI_ENABLE, JVMTI_EVENT_THREAD_END, NULL);\n" +
        "\n" +
        "    emit(\"onload\", \"yes\");\n" +
        "    return JNI_OK;\n" +
        "}\n" +
        "\n" +
        "JNIEXPORT void JNICALL Agent_OnUnload(JavaVM* vm) {\n" +
        "    emit(\"unload\", \"yes\");\n" +
        "    if (g_worker != NULL && g_jvmti != NULL) {\n" +
        "        /* DeleteGlobalRef needs a JNIEnv; the VM is already dead here, so just\n" +
        "         * drop the reference without cleanup -- a one-shot test process. */\n" +
        "    }\n" +
        "}\n";

    static final String PROBE_SRC =
        "public class EventProbe {\n" +
        "    public static void main(String[] args) throws Exception {\n" +
        "        Thread t = new Thread(new Runnable() {\n" +
        "            public void run() { sampleMethod(); }\n" +
        "        }, \"probe-worker\");\n" +
        "        t.start();\n" +
        "        t.join();\n" +
        "        System.out.println(\"probe-ok\");\n" +
        "    }\n" +
        "    public static void sampleMethod() { }\n" +
        "}\n";

    // ------------------------------------------------------------------
    // Build / launch helpers
    // ------------------------------------------------------------------
    static Path jdkHome() {
        Path p = Paths.get(System.getProperty("java.home"));
        return p.endsWith("jre") ? p.getParent() : p;
    }

    static Path javaBinary() {
        return Paths.get(System.getProperty("java.home")).resolve("bin").resolve("java");
    }

    static Path javacBinary() {
        return jdkHome().resolve("bin").resolve("javac");
    }

    static byte[] readAll(java.io.InputStream in) throws java.io.IOException {
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
        return out.toByteArray();
    }

    static String runCollect(ProcessBuilder pb) throws Exception {
        pb.redirectErrorStream(true);
        Process p = pb.start();
        byte[] all = readAll(p.getInputStream());
        if (!p.waitFor(120, TimeUnit.SECONDS)) {
            p.destroyForcibly();
            throw new AssertionError("process did not finish within 120s");
        }
        String out = new String(all, "UTF-8");
        if (p.exitValue() != 0)
            throw new AssertionError("process failed rc=" + p.exitValue() + " output: " + out);
        return out;
    }

    static Path buildAgent(Path dir, String name) throws Exception {
        String nativepath = System.getProperty("test.nativepath");
        if (nativepath != null) {
            Path pre = Paths.get(nativepath).resolve(System.mapLibraryName(name));
            if (!Files.exists(pre))
                throw new AssertionError("test.nativepath set but no " + System.mapLibraryName(name)
                        + " (run: make native)");
            return pre.toAbsolutePath();
        }
        if (!gccAvailable())
            throw new jtreg.SkippedException("no C compiler (gcc) available; JVMTI agent cannot be built");
        Path cFile = dir.resolve(name + ".c");
        Files.write(cFile, AGENT_SRC.getBytes("UTF-8"));
        Path so = dir.resolve(System.mapLibraryName(name));
        Path inc = jdkHome().resolve("include");
        runCollect(new ProcessBuilder("gcc", "-shared", "-fPIC",
                "-I" + inc, "-I" + inc.resolve("linux"),
                "-o", so.toString(), cFile.toString()));
        return so;
    }

    static void compileProbe(Path dir, String className, String source, String... extra) throws Exception {
        Path src = dir.resolve(className + ".java");
        Files.write(src, source.getBytes("UTF-8"));
        List<String> cmd = new ArrayList<>();
        cmd.add(javacBinary().toString());
        cmd.add("-d");
        cmd.add(dir.toString());
        for (String a : extra) cmd.add(a);
        cmd.add(src.toString());
        runCollect(new ProcessBuilder(cmd));
    }

    static String launchChild(Path dir, Path agentSo, Path outFile, String mainClass) throws Exception {
        Files.deleteIfExists(outFile);
        ProcessBuilder child = new ProcessBuilder(javaBinary().toString(),
                "-agentpath:" + agentSo + "=" + outFile,
                "-cp", dir.toString(), mainClass);
        child.redirectErrorStream(true);
        Process p = child.start();
        byte[] out = readAll(p.getInputStream());
        if (!p.waitFor(120, TimeUnit.SECONDS)) {
            p.destroyForcibly();
            throw new AssertionError("child JVM did not finish within 120s");
        }
        return new String(out, "UTF-8");
    }

    static String prop(Map<String, String> m, String k) {
        return m.getOrDefault(k, "");
    }

    static int intProp(Map<String, String> m, String k) {
        try { return Integer.parseInt(m.get(k)); } catch (Exception e) { return -1; }
    }

    static Map<String, String> parse(Path file) throws Exception {
        Map<String, String> m = new LinkedHashMap<>();
        if (!Files.exists(file)) return m;
        for (String line : Files.readAllLines(file)) {
            int eq = line.indexOf('=');
            if (eq < 0) continue;
            m.put(line.substring(0, eq), line.substring(eq + 1));
        }
        return m;
    }

    // ------------------------------------------------------------------
    public static void main(String[] args) throws Exception {
        Path dir = Paths.get(System.getProperty("user.dir"));
        Path agentSo = buildAgent(dir, "jvmtievents");
        compileProbe(dir, "EventProbe", PROBE_SRC);

        Path outFile = dir.resolve("agent.out");
        String output = launchChild(dir, agentSo, outFile, "EventProbe");
        check(output.contains("probe-ok"), "debuggee main executed to completion");

        Map<String, String> props = parse(outFile);
        check("yes".equals(prop(props, "onload")), "Agent_OnLoad invoked");
        check("yes".equals(prop(props, "unload")), "Agent_OnUnload invoked at shutdown");
        check(intProp(props, "addcaps.rc") == 0,
                "AddCapabilities(method entry/exit events) returns JVMTI_ERROR_NONE (0)");

        check("yes".equals(prop(props, "event.vminit")), "VMInit event fired");
        check(intProp(props, "phase.vminit") == 4, "GetPhase is JVMTI_PHASE_LIVE (4) at VMInit");
        check("yes".equals(prop(props, "event.vmdeath")), "VMDeath event fired");
        check(intProp(props, "phase.vmdeath") == 4,
                "GetPhase is still JVMTI_PHASE_LIVE (4) inside the VMDeath callback");

        check("yes".equals(prop(props, "event.classload")), "ClassLoad event fired for LEventProbe;");
        check("yes".equals(prop(props, "event.classprepare")), "ClassPrepare event fired for LEventProbe;");
        check("yes".equals(prop(props, "event.threadstart.probe")), "ThreadStart event fired for probe-worker");
        check("yes".equals(prop(props, "event.threadend.probe")), "ThreadEnd event fired for probe-worker");
        check("yes".equals(prop(props, "event.methodentry")), "MethodEntry event fired for sampleMethod");
        check("yes".equals(prop(props, "event.methodexit")), "MethodExit event fired for sampleMethod");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
