/*
 * @test
 * @summary JVMTI class/method query contract: build a native agent from embedded
 *          C with gcc (or load the prebuilt libjvmticlass.so via -nativepath),
 *          launch a child JVM with -agentpath, and assert GetLoadedClasses,
 *          GetClassSignature, GetClassModifiers, GetMethodName and
 *          GetSourceFileName against a known probe class.
 * @library /lib
 * @run main/othervm JvmtiClassMethodTest
 */

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class JvmtiClassMethodTest {
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
    // Embedded C agent (identical to jvmticlass.c in this directory)
    // ------------------------------------------------------------------
    static final String AGENT_SRC =
        "/*\n" +
        " * JVMTI agent: class and method query observations.\n" +
        " *\n" +
        " * Loaded by a child JVM through -agentpath:<this.so>=<outfile>.  The probe\n" +
        " * class ClassMethodProbe declares a static native method query() which this\n" +
        " * agent implements as Java_ClassMethodProbe_query; inside that live-phase\n" +
        " * call it records:\n" +
        " *\n" +
        " *   signature=<char*>     GetClassSignature on the probe class\n" +
        " *   modifiers=<jint>      GetClassModifiers on the probe class\n" +
        " *   source.file=<char*>   GetSourceFileName on the probe class\n" +
        " *   loaded.count=<jint>   GetLoadedClasses class count\n" +
        " *   loaded.found=<yes|no> whether the probe class is among the loaded classes\n" +
        " *   method.name=<char*>   GetMethodName on ClassMethodProbe.sampleMethod\n" +
        " *\n" +
        " * Compiled at run time against the JDK under test's own jvmti.h; see\n" +
        " * JvmtiClassMethodTest.java for the Java side.\n" +
        " */\n" +
        "#include <jvmti.h>\n" +
        "#include <stdio.h>\n" +
        "#include <string.h>\n" +
        "\n" +
        "static char g_outpath[4096];\n" +
        "static jvmtiEnv* g_jvmti;\n" +
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
        "    jint rc;\n" +
        "    char number[64];\n" +
        "\n" +
        "    if (options != NULL && options[0] != '\\0') {\n" +
        "        strncpy(g_outpath, options, sizeof g_outpath - 1);\n" +
        "    } else {\n" +
        "        strcpy(g_outpath, \"agent.out\");\n" +
        "    }\n" +
        "    if ((*vm)->GetEnv(vm, (void**)&jvmti, JVMTI_VERSION_1_2) != JNI_OK)\n" +
        "        return JNI_ERR;\n" +
        "    g_jvmti = jvmti;\n" +
        "    emit(\"onload\", \"yes\");\n" +
        "\n" +
        "    /* GetSourceFileName requires the can_get_source_file_name capability. */\n" +
        "    memset(&caps, 0, sizeof caps);\n" +
        "    caps.can_get_source_file_name = 1;\n" +
        "    rc = (*jvmti)->AddCapabilities(jvmti, &caps);\n" +
        "    snprintf(number, sizeof number, \"%d\", (int)rc);\n" +
        "    emit(\"addcaps.rc\", number);\n" +
        "\n" +
        "    return JNI_OK;\n" +
        "}\n" +
        "\n" +
        "JNIEXPORT void JNICALL Agent_OnUnload(JavaVM* vm) {\n" +
        "    emit(\"unload\", \"yes\");\n" +
        "}\n" +
        "\n" +
        "JNIEXPORT void JNICALL Java_ClassMethodProbe_query(JNIEnv* env, jclass klass) {\n" +
        "    jvmtiEnv* jvmti = g_jvmti;\n" +
        "    jint loaded_count = 0;\n" +
        "    jclass* loaded = NULL;\n" +
        "    char* signature = NULL;\n" +
        "    char* source = NULL;\n" +
        "    char* method_name = NULL;\n" +
        "    jint modifiers = 0;\n" +
        "    jmethodID mid = NULL;\n" +
        "    char number[64];\n" +
        "    int i;\n" +
        "    int found = 0;\n" +
        "\n" +
        "    /* Reuse the environment obtained in Agent_OnLoad: calling GetEnv again in\n" +
        "     * the live phase would mint a fresh, capability-less environment. */\n" +
        "    if (jvmti == NULL) return;\n" +
        "\n" +
        "    if ((*jvmti)->GetClassSignature(jvmti, klass, &signature, NULL) == JVMTI_ERROR_NONE\n" +
        "            && signature != NULL) {\n" +
        "        emit(\"signature\", signature);\n" +
        "        (*jvmti)->Deallocate(jvmti, (unsigned char*)signature);\n" +
        "    }\n" +
        "\n" +
        "    if ((*jvmti)->GetClassModifiers(jvmti, klass, &modifiers) == JVMTI_ERROR_NONE) {\n" +
        "        snprintf(number, sizeof number, \"%d\", (int)modifiers);\n" +
        "        emit(\"modifiers\", number);\n" +
        "    }\n" +
        "\n" +
        "    if ((*jvmti)->GetSourceFileName(jvmti, klass, &source) == JVMTI_ERROR_NONE\n" +
        "            && source != NULL) {\n" +
        "        emit(\"source.file\", source);\n" +
        "        (*jvmti)->Deallocate(jvmti, (unsigned char*)source);\n" +
        "    }\n" +
        "\n" +
        "    if ((*jvmti)->GetLoadedClasses(jvmti, &loaded_count, &loaded) == JVMTI_ERROR_NONE\n" +
        "            && loaded != NULL) {\n" +
        "        snprintf(number, sizeof number, \"%d\", (int)loaded_count);\n" +
        "        emit(\"loaded.count\", number);\n" +
        "        for (i = 0; i < loaded_count; i++) {\n" +
        "            char* s = NULL;\n" +
        "            if ((*jvmti)->GetClassSignature(jvmti, loaded[i], &s, NULL) == JVMTI_ERROR_NONE\n" +
        "                    && s != NULL) {\n" +
        "                if (strcmp(s, \"LClassMethodProbe;\") == 0) found = 1;\n" +
        "                (*jvmti)->Deallocate(jvmti, (unsigned char*)s);\n" +
        "            }\n" +
        "        }\n" +
        "        (*jvmti)->Deallocate(jvmti, (unsigned char*)loaded);\n" +
        "        emit(\"loaded.found\", found ? \"yes\" : \"no\");\n" +
        "    }\n" +
        "\n" +
        "    mid = (*env)->GetMethodID(env, klass, \"sampleMethod\", \"()I\");\n" +
        "    if (mid != NULL\n" +
        "            && (*jvmti)->GetMethodName(jvmti, mid, &method_name, NULL, NULL) == JVMTI_ERROR_NONE\n" +
        "            && method_name != NULL) {\n" +
        "        emit(\"method.name\", method_name);\n" +
        "        (*jvmti)->Deallocate(jvmti, (unsigned char*)method_name);\n" +
        "    }\n" +
        "}\n";

    static final String PROBE_SRC =
        "public class ClassMethodProbe {\n" +
        "    public static native void query();\n" +
        "    public static void main(String[] args) {\n" +
        "        query();\n" +
        "        System.out.println(\"probe-ok\");\n" +
        "    }\n" +
        "    public int sampleMethod() { return 42; }\n" +
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
        Path agentSo = buildAgent(dir, "jvmticlass");
        compileProbe(dir, "ClassMethodProbe", PROBE_SRC);

        Path outFile = dir.resolve("agent.out");
        String output = launchChild(dir, agentSo, outFile, "ClassMethodProbe");
        check(output.contains("probe-ok"), "debuggee main executed to completion");

        Map<String, String> props = parse(outFile);
        check("yes".equals(prop(props, "onload")), "Agent_OnLoad invoked");
        check("yes".equals(prop(props, "unload")), "Agent_OnUnload invoked at shutdown");
        check(intProp(props, "addcaps.rc") == 0,
                "AddCapabilities(can_get_source_file_name) returns JVMTI_ERROR_NONE (0)");

        check("LClassMethodProbe;".equals(prop(props, "signature")),
                "GetClassSignature returns the JNI descriptor LClassMethodProbe;");
        int modifiers = intProp(props, "modifiers");
        check((modifiers & 0x1) != 0, "GetClassModifiers has ACC_PUBLIC bit set (modifiers=" + modifiers + ")");
        check("ClassMethodProbe.java".equals(prop(props, "source.file")),
                "GetSourceFileName returns ClassMethodProbe.java");

        check(intProp(props, "loaded.count") > 0,
                "GetLoadedClasses returns a non-zero class count");
        check("yes".equals(prop(props, "loaded.found")),
                "GetLoadedClasses includes the probe class");
        check("sampleMethod".equals(prop(props, "method.name")),
                "GetMethodName returns sampleMethod");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
