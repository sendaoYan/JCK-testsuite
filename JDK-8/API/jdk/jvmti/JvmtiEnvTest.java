/*
 * @test
 * @summary JVMTI environment and capability contract: build a native agent
 *          from embedded C with gcc (or load the prebuilt libjvmtienv.so via
 *          -nativepath), launch a child JVM with -agentpath, and assert
 *          GetVersionNumber, GetPhase, GetCapabilities,
 *          GetPotentialCapabilities, AddCapabilities and RelinquishCapabilities
 *          from the on-load phase.
 * @library /lib
 * @run main/othervm JvmtiEnvTest
 */

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class JvmtiEnvTest {
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
    // Embedded C agent (identical to jvmtienv.c in this directory)
    // ------------------------------------------------------------------
    static final String AGENT_SRC =
        "/*\n" +
        " * JVMTI agent: environment, phase and capability observations.\n" +
        " *\n" +
        " * Loaded by a child JVM through -agentpath:<this.so>=<outfile>.  All queries\n" +
        " * run from Agent_OnLoad, where the phase is JVMTI_PHASE_ONLOAD and the\n" +
        " * capability functions are callable.  Observations are written to <outfile> as\n" +
        " * key=value lines (one per call):\n" +
        " *\n" +
        " *   version=<jint>                GetVersionNumber in the on-load phase\n" +
        " *   phase=<jint>                  GetPhase in the on-load phase (expect ONLOAD=1)\n" +
        " *   cap.initial.tag=<0|1>         can_tag_objects reported by GetCapabilities\n" +
        " *   cap.potential.tag=<0|1>       can_tag_objects reported by GetPotentialCapabilities\n" +
        " *   cap.add.rc=<jvmtiError>       AddCapabilities(can_tag_objects) return code\n" +
        " *   cap.after.add.tag=<0|1>       can_tag_objects after AddCapabilities\n" +
        " *   cap.relinquish.rc=<jvmtiError>RelinquishCapabilities(can_tag_objects) return code\n" +
        " *   cap.after.relinquish.tag=<0|1>can_tag_objects after RelinquishCapabilities\n" +
        " *   onload=yes                    Agent_OnLoad entered and returned JNI_OK\n" +
        " *   unload=yes                    Agent_OnUnload entered at VM shutdown\n" +
        " *\n" +
        " * Compiled at run time against the JDK under test's own jvmti.h; see\n" +
        " * JvmtiEnvTest.java for the Java side.  C89/C99-portable, no JVM link\n" +
        " * dependency (jvmti symbols resolve at load time).\n" +
        " */\n" +
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
        "JNIEXPORT jint JNICALL Agent_OnLoad(JavaVM* vm, char* options, void* reserved) {\n" +
        "    jvmtiEnv* jvmti = NULL;\n" +
        "    jvmtiCapabilities caps;\n" +
        "    jvmtiCapabilities potential;\n" +
        "    jvmtiPhase phase = 0;\n" +
        "    jint version = 0;\n" +
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
        "\n" +
        "    emit(\"onload\", \"yes\");\n" +
        "\n" +
        "    if ((*jvmti)->GetVersionNumber(jvmti, &version) == JVMTI_ERROR_NONE) {\n" +
        "        snprintf(number, sizeof number, \"%d\", (int)version);\n" +
        "        emit(\"version\", number);\n" +
        "    }\n" +
        "\n" +
        "    if ((*jvmti)->GetPhase(jvmti, &phase) == JVMTI_ERROR_NONE) {\n" +
        "        snprintf(number, sizeof number, \"%d\", (int)phase);\n" +
        "        emit(\"phase\", number);\n" +
        "    }\n" +
        "\n" +
        "    memset(&caps, 0, sizeof caps);\n" +
        "    if ((*jvmti)->GetCapabilities(jvmti, &caps) == JVMTI_ERROR_NONE) {\n" +
        "        snprintf(number, sizeof number, \"%d\", (int)caps.can_tag_objects);\n" +
        "        emit(\"cap.initial.tag\", number);\n" +
        "    }\n" +
        "\n" +
        "    memset(&potential, 0, sizeof potential);\n" +
        "    if ((*jvmti)->GetPotentialCapabilities(jvmti, &potential) == JVMTI_ERROR_NONE) {\n" +
        "        snprintf(number, sizeof number, \"%d\", (int)potential.can_tag_objects);\n" +
        "        emit(\"cap.potential.tag\", number);\n" +
        "    }\n" +
        "\n" +
        "    memset(&caps, 0, sizeof caps);\n" +
        "    caps.can_tag_objects = 1;\n" +
        "    rc = (*jvmti)->AddCapabilities(jvmti, &caps);\n" +
        "    snprintf(number, sizeof number, \"%d\", (int)rc);\n" +
        "    emit(\"cap.add.rc\", number);\n" +
        "\n" +
        "    memset(&caps, 0, sizeof caps);\n" +
        "    if ((*jvmti)->GetCapabilities(jvmti, &caps) == JVMTI_ERROR_NONE) {\n" +
        "        snprintf(number, sizeof number, \"%d\", (int)caps.can_tag_objects);\n" +
        "        emit(\"cap.after.add.tag\", number);\n" +
        "    }\n" +
        "\n" +
        "    memset(&caps, 0, sizeof caps);\n" +
        "    caps.can_tag_objects = 1;\n" +
        "    rc = (*jvmti)->RelinquishCapabilities(jvmti, &caps);\n" +
        "    snprintf(number, sizeof number, \"%d\", (int)rc);\n" +
        "    emit(\"cap.relinquish.rc\", number);\n" +
        "\n" +
        "    memset(&caps, 0, sizeof caps);\n" +
        "    if ((*jvmti)->GetCapabilities(jvmti, &caps) == JVMTI_ERROR_NONE) {\n" +
        "        snprintf(number, sizeof number, \"%d\", (int)caps.can_tag_objects);\n" +
        "        emit(\"cap.after.relinquish.tag\", number);\n" +
        "    }\n" +
        "\n" +
        "    return JNI_OK;\n" +
        "}\n" +
        "\n" +
        "JNIEXPORT void JNICALL Agent_OnUnload(JavaVM* vm) {\n" +
        "    emit(\"unload\", \"yes\");\n" +
        "}\n";

    static final String PROBE_SRC =
        "public class EnvProbe {\n" +
        "    public static void main(String[] args) {\n" +
        "        System.out.println(\"probe-ok\");\n" +
        "    }\n" +
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
        Path agentSo = buildAgent(dir, "jvmtienv");
        compileProbe(dir, "EnvProbe", PROBE_SRC);

        Path outFile = dir.resolve("agent.out");
        String output = launchChild(dir, agentSo, outFile, "EnvProbe");
        check(output.contains("probe-ok"), "debuggee main executed to completion");

        Map<String, String> props = parse(outFile);
        check("yes".equals(prop(props, "onload")), "Agent_OnLoad invoked");
        check("yes".equals(prop(props, "unload")), "Agent_OnUnload invoked at shutdown");

        int version = intProp(props, "version");
        check(version > 0, "GetVersionNumber non-zero (got " + version + ")");
        check((version & 0xFF000000) == 0x30000000,
                "GetVersionNumber JVMTI kind byte 0x30 (got 0x" + Integer.toHexString(version) + ")");

        int phase = intProp(props, "phase");
        check(phase == 1, "GetPhase returns JVMTI_PHASE_ONLOAD (1) in Agent_OnLoad (got " + phase + ")");

        check(intProp(props, "cap.potential.tag") == 1,
                "GetPotentialCapabilities: can_tag_objects is potentially available");
        check(intProp(props, "cap.add.rc") == 0,
                "AddCapabilities(can_tag_objects) returns JVMTI_ERROR_NONE (0)");
        check(intProp(props, "cap.after.add.tag") == 1,
                "GetCapabilities: can_tag_objects enabled after AddCapabilities");
        check(intProp(props, "cap.relinquish.rc") == 0,
                "RelinquishCapabilities(can_tag_objects) returns JVMTI_ERROR_NONE (0)");
        check(intProp(props, "cap.after.relinquish.tag") == 0,
                "GetCapabilities: can_tag_objects disabled after RelinquishCapabilities");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
