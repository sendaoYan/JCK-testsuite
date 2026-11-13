/*
 * @test
 * @summary JVMTI agent lifecycle and core query contract: build a native agent
 *          from embedded C with gcc (or load the prebuilt libjvmtiagent.so via
 *          -nativepath), launch a child JVM with -agentpath, and assert
 *          Agent_OnLoad/Agent_OnUnload, GetVersionNumber, GetClassSignature /
 *          GetClassStatus at ClassPrepare, and GetThreadInfo.
 * @library /lib
 * @run main/othervm JvmtiAgentTest
 */

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Conformance test for the JVMTI agent contract.  The agent source below is
 * embedded as a string (canonical copy in jvmtiagent.c), written to the scratch
 * dir, built with gcc against the JDK under test's own jvmti.h, and loaded by a
 * child JVM through -agentpath.  The agent writes key=value observations to an
 * output file; all assertions are performed here on the Java side.
 */
public class JvmtiAgentTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean gccAvailable() {
        try {
            Process p = new ProcessBuilder("gcc", "--version").start();
            try { p.getInputStream().close(); } catch (Exception ignore) { }
            try { p.getOutputStream().close(); } catch (Exception ignore) { }
            try { p.getErrorStream().close(); } catch (Exception ignore) { }
            return p.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    // ------------------------------------------------------------------
    // Embedded C agent (identical to jvmtiagent.c in this directory)
    // ------------------------------------------------------------------
    static final String AGENT_SRC =
        "/* JVMTI agent: on-load lifecycle + core function observations. */\n" +
        "#include <jvmti.h>\n" +
        "#include <stdio.h>\n" +
        "#include <string.h>\n" +
        "\n" +
        "static char g_outpath[4096];\n" +
        "\n" +
        "static void emit(const char* key, const char* value) {\n" +
        "    FILE* f = fopen(g_outpath, \"a\");\n" +
        "    if (f == NULL) return;\n" +
        "    fprintf(f, \"%s=%s\\n\", key, value);\n" +
        "    fclose(f);\n" +
        "}\n" +
        "\n" +
        "static void JNICALL onClassPrepare(jvmtiEnv* jvmti, JNIEnv* env,\n" +
        "                                   jthread thread, jclass klass) {\n" +
        "    char* signature = NULL;\n" +
        "    jint status = 0;\n" +
        "    jthread current = NULL;\n" +
        "    jvmtiThreadInfo info;\n" +
        "    char number[64];\n" +
        "\n" +
        "    if ((*jvmti)->GetClassSignature(jvmti, klass, &signature, NULL)\n" +
        "            != JVMTI_ERROR_NONE) return;\n" +
        "    if (signature == NULL || strcmp(signature, \"LAgentProbe;\") != 0) {\n" +
        "        if (signature != NULL) (*jvmti)->Deallocate(jvmti, (unsigned char*)signature);\n" +
        "        return;\n" +
        "    }\n" +
        "    (*jvmti)->Deallocate(jvmti, (unsigned char*)signature);\n" +
        "\n" +
        "    (*jvmti)->GetClassStatus(jvmti, klass, &status);\n" +
        "    snprintf(number, sizeof number, \"%d\", (int)status);\n" +
        "    emit(\"class.signature\", \"LAgentProbe;\");\n" +
        "    emit(\"class.status\", number);\n" +
        "\n" +
        "    if ((*jvmti)->GetCurrentThread(jvmti, &current) == JVMTI_ERROR_NONE\n" +
        "            && current != NULL) {\n" +
        "        memset(&info, 0, sizeof info);\n" +
        "        if ((*jvmti)->GetThreadInfo(jvmti, current, &info) == JVMTI_ERROR_NONE) {\n" +
        "            emit(\"thread.name\", info.name == NULL ? \"<null>\" : info.name);\n" +
        "            snprintf(number, sizeof number, \"%d\", (int)info.priority);\n" +
        "            emit(\"thread.priority\", number);\n" +
        "        }\n" +
        "    }\n" +
        "}\n" +
        "\n" +
        "JNIEXPORT jint JNICALL Agent_OnLoad(JavaVM* vm, char* options, void* reserved) {\n" +
        "    jvmtiEnv* jvmti = NULL;\n" +
        "    jint version = 0;\n" +
        "    jvmtiEventCallbacks callbacks;\n" +
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
        "\n" +
        "    if ((*jvmti)->GetVersionNumber(jvmti, &version) == JVMTI_ERROR_NONE) {\n" +
        "        snprintf(number, sizeof number, \"%d\", (int)version);\n" +
        "        emit(\"version\", number);\n" +
        "    }\n" +
        "    emit(\"onload\", \"yes\");\n" +
        "\n" +
        "    memset(&callbacks, 0, sizeof callbacks);\n" +
        "    callbacks.ClassPrepare = onClassPrepare;\n" +
        "    (*jvmti)->SetEventCallbacks(jvmti, &callbacks, sizeof callbacks);\n" +
        "    (*jvmti)->SetEventNotificationMode(jvmti, JVMTI_ENABLE,\n" +
        "                                       JVMTI_EVENT_CLASS_PREPARE, NULL);\n" +
        "\n" +
        "    return JNI_OK;\n" +
        "}\n" +
        "\n" +
        "JNIEXPORT void JNICALL Agent_OnUnload(JavaVM* vm) {\n" +
        "    emit(\"unload\", \"yes\");\n" +
        "}\n";

    static final String PROBE_SRC =
        "public class AgentProbe {\n" +
        "    public static void main(String[] args) {\n" +
        "        System.out.println(\"probe-ok\");\n" +
        "    }\n" +
        "}\n";

    // ------------------------------------------------------------------
    // Build helpers: locate jvmti.h / javac for the JDK under test
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
            throw new AssertionError("process failed rc=" + p.exitValue()
                    + " output: " + out);
        return out;
    }

    // ------------------------------------------------------------------
    public static void main(String[] args) throws Exception {
        Path dir = Paths.get(System.getProperty("user.dir"));
        Path agentSo;
        String nativepath = System.getProperty("test.nativepath");
        if (nativepath != null) {
            Path pre = Paths.get(nativepath).resolve("libjvmtiagent.so");
            if (!Files.exists(pre))
                throw new AssertionError("test.nativepath set but no libjvmtiagent.so (run: make native)");
            agentSo = pre.toAbsolutePath();
        } else {
            if (!gccAvailable())
                throw new jtreg.SkippedException("no C compiler (gcc) available; JVMTI agent cannot be built");
            Path cFile = dir.resolve("jvmtiagent.c");
            Files.write(cFile, AGENT_SRC.getBytes("UTF-8"));
            agentSo = dir.resolve("libjvmtiagent.so");
            Path inc = jdkHome().resolve("include");
            ProcessBuilder pb = new ProcessBuilder("gcc", "-shared", "-fPIC",
                    "-I" + inc, "-I" + inc.resolve("linux"),
                    "-o", agentSo.toString(), cFile.toString());
            runCollect(pb);
        }

        // Compile the child's probe class with the JDK under test's javac.
        Path probeFile = dir.resolve("AgentProbe.java");
        Files.write(probeFile, PROBE_SRC.getBytes("UTF-8"));
        runCollect(new ProcessBuilder(javacBinary().toString(),
                "-d", dir.toString(), probeFile.toString()));

        // Launch the child with the agent; the agent writes key=value lines here.
        Path outFile = dir.resolve("agent.out");
        Files.deleteIfExists(outFile);
        ProcessBuilder child = new ProcessBuilder(javaBinary().toString(),
                "-agentpath:" + agentSo + "=" + outFile,
                "-cp", dir.toString(), "AgentProbe");
        child.redirectErrorStream(true);
        Process p = child.start();
        byte[] childOut = readAll(p.getInputStream());
        if (!p.waitFor(120, TimeUnit.SECONDS)) {
            p.destroyForcibly();
            throw new AssertionError("child JVM did not finish within 120s");
        }
        String output = new String(childOut, "UTF-8");
        check(p.exitValue() == 0, "child JVM exits cleanly (rc=" + p.exitValue()
                + " output=" + output + ")");

        Map<String, String> props = parse(outFile);
        check(props.size() == 7, "agent recorded all 7 observations (got " + props.size() + ")");
        check("yes".equals(prop(props, "onload")), "Agent_OnLoad invoked");
        check("yes".equals(prop(props, "unload")), "Agent_OnUnload invoked at shutdown");

        int version = intProp(props, "version");
        check(version > 0, "GetVersionNumber non-zero (got " + version + ")");
        check((version & 0xFF000000) == 0x30000000,
                "GetVersionNumber JVMTI kind byte 0x30 (got 0x" + Integer.toHexString(version) + ")");

        check("LAgentProbe;".equals(prop(props, "class.signature")),
                "GetClassSignature returns the JNI descriptor LAgentProbe;");
        int status = intProp(props, "class.status");
        check((status & 0x2) != 0, "GetClassStatus PREPARED bit set at ClassPrepare (status=" + status + ")");
        check((status & 0x4) == 0, "GetClassStatus INITIALIZED bit clear at ClassPrepare (status=" + status + ")");

        check("main".equals(prop(props, "thread.name")), "GetThreadInfo thread name is main");
        check("5".equals(prop(props, "thread.priority")), "GetThreadInfo priority is NORM_PRIORITY=5");
        check(output.contains("probe-ok"), "debuggee main executed to completion");

        System.out.println("PASS (" + passed + " assertions)");
    }

    // ------------------------------------------------------------------
    // key=value parsing helpers
    // ------------------------------------------------------------------
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
}
