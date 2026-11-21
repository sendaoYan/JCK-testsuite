/*
 * @test
 * @summary JVMTI thread/stack query contract: build a native agent from
 *          embedded C with gcc (or load the prebuilt libjvmtistack.so via
 *          -nativepath), launch a child JVM with -agentpath, and assert
 *          GetAllThreads, GetThreadInfo, GetFrameCount and GetStackTrace on the
 *          current thread.
 * @library /lib
 * @run main/othervm JvmtiThreadStackTest
 */

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class JvmtiThreadStackTest {
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
    // Embedded C agent (identical to jvmtistack.c in this directory)
    // ------------------------------------------------------------------
    static final String AGENT_SRC =
        "/*\n" +
        " * JVMTI agent: thread and stack query observations.\n" +
        " *\n" +
        " * Loaded by a child JVM through -agentpath:<this.so>=<outfile>.  The probe\n" +
        " * class StackProbe declares a static native method query() implemented here as\n" +
        " * Java_StackProbe_query; inside that live-phase call (running on the main\n" +
        " * thread, called from StackProbe.leaf) it records:\n" +
        " *\n" +
        " *   threads.count=<jint>    GetAllThreads thread count\n" +
        " *   found.main=<yes|no>     whether a thread named \"main\" is among all threads\n" +
        " *   current.name=<char*>    GetThreadInfo name of the current thread\n" +
        " *   current.priority=<jint> GetThreadInfo priority of the current thread\n" +
        " *   current.daemon=<0|1>    GetThreadInfo is_daemon of the current thread\n" +
        " *   frame.count=<jint>      GetFrameCount on the current thread\n" +
        " *   stack.count=<jint>      GetStackTrace frame count on the current thread\n" +
        " *   stack.frames=<a,b,...>  comma-joined GetMethodName of each stack frame\n" +
        " *\n" +
        " * Compiled at run time against the JDK under test's own jvmti.h; see\n" +
        " * JvmtiThreadStackTest.java for the Java side.\n" +
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
        "    if (options != NULL && options[0] != '\\0') {\n" +
        "        strncpy(g_outpath, options, sizeof g_outpath - 1);\n" +
        "    } else {\n" +
        "        strcpy(g_outpath, \"agent.out\");\n" +
        "    }\n" +
        "    if ((*vm)->GetEnv(vm, (void**)&jvmti, JVMTI_VERSION_1_2) != JNI_OK)\n" +
        "        return JNI_ERR;\n" +
        "    g_jvmti = jvmti;\n" +
        "    emit(\"onload\", \"yes\");\n" +
        "    return JNI_OK;\n" +
        "}\n" +
        "\n" +
        "JNIEXPORT void JNICALL Agent_OnUnload(JavaVM* vm) {\n" +
        "    emit(\"unload\", \"yes\");\n" +
        "}\n" +
        "\n" +
        "JNIEXPORT void JNICALL Java_StackProbe_query(JNIEnv* env, jclass klass) {\n" +
        "    jvmtiEnv* jvmti = g_jvmti;\n" +
        "    jint threads_count = 0;\n" +
        "    jthread* threads = NULL;\n" +
        "    jthread current = NULL;\n" +
        "    jvmtiThreadInfo info;\n" +
        "    jvmtiFrameInfo frames[32];\n" +
        "    jint frame_count = 0;\n" +
        "    jint stack_count = 0;\n" +
        "    char number[64];\n" +
        "    char list[2048];\n" +
        "    int i;\n" +
        "    int pos = 0;\n" +
        "    int found_main = 0;\n" +
        "\n" +
        "    /* Reuse the environment obtained in Agent_OnLoad. */\n" +
        "    if (jvmti == NULL) return;\n" +
        "\n" +
        "    if ((*jvmti)->GetAllThreads(jvmti, &threads_count, &threads) == JVMTI_ERROR_NONE\n" +
        "            && threads != NULL) {\n" +
        "        snprintf(number, sizeof number, \"%d\", (int)threads_count);\n" +
        "        emit(\"threads.count\", number);\n" +
        "        for (i = 0; i < threads_count; i++) {\n" +
        "            memset(&info, 0, sizeof info);\n" +
        "            if ((*jvmti)->GetThreadInfo(jvmti, threads[i], &info) == JVMTI_ERROR_NONE) {\n" +
        "                if (info.name != NULL && strcmp(info.name, \"main\") == 0)\n" +
        "                    found_main = 1;\n" +
        "                if (info.name != NULL)\n" +
        "                    (*jvmti)->Deallocate(jvmti, (unsigned char*)info.name);\n" +
        "            }\n" +
        "        }\n" +
        "        (*jvmti)->Deallocate(jvmti, (unsigned char*)threads);\n" +
        "        emit(\"found.main\", found_main ? \"yes\" : \"no\");\n" +
        "    }\n" +
        "\n" +
        "    if ((*jvmti)->GetCurrentThread(jvmti, &current) == JVMTI_ERROR_NONE\n" +
        "            && current != NULL) {\n" +
        "        memset(&info, 0, sizeof info);\n" +
        "        if ((*jvmti)->GetThreadInfo(jvmti, current, &info) == JVMTI_ERROR_NONE) {\n" +
        "            emit(\"current.name\", info.name == NULL ? \"<null>\" : info.name);\n" +
        "            snprintf(number, sizeof number, \"%d\", (int)info.priority);\n" +
        "            emit(\"current.priority\", number);\n" +
        "            snprintf(number, sizeof number, \"%d\", (int)info.is_daemon);\n" +
        "            emit(\"current.daemon\", number);\n" +
        "            if (info.name != NULL)\n" +
        "                (*jvmti)->Deallocate(jvmti, (unsigned char*)info.name);\n" +
        "        }\n" +
        "\n" +
        "        if ((*jvmti)->GetFrameCount(jvmti, current, &frame_count) == JVMTI_ERROR_NONE) {\n" +
        "            snprintf(number, sizeof number, \"%d\", (int)frame_count);\n" +
        "            emit(\"frame.count\", number);\n" +
        "        }\n" +
        "\n" +
        "        if ((*jvmti)->GetStackTrace(jvmti, current, 0, 32, frames, &stack_count)\n" +
        "                == JVMTI_ERROR_NONE) {\n" +
        "            snprintf(number, sizeof number, \"%d\", (int)stack_count);\n" +
        "            emit(\"stack.count\", number);\n" +
        "            list[0] = '\\0';\n" +
        "            pos = 0;\n" +
        "            for (i = 0; i < stack_count && i < 32; i++) {\n" +
        "                char* name = NULL;\n" +
        "                if ((*jvmti)->GetMethodName(jvmti, frames[i].method, &name, NULL, NULL)\n" +
        "                        == JVMTI_ERROR_NONE && name != NULL) {\n" +
        "                    if (pos > 0 && pos < (int)sizeof list - 1) {\n" +
        "                        list[pos++] = ',';\n" +
        "                        list[pos] = '\\0';\n" +
        "                    }\n" +
        "                    snprintf(list + pos, sizeof list - pos, \"%s\", name);\n" +
        "                    pos += (int)strlen(name);\n" +
        "                    (*jvmti)->Deallocate(jvmti, (unsigned char*)name);\n" +
        "                }\n" +
        "            }\n" +
        "            emit(\"stack.frames\", list);\n" +
        "        }\n" +
        "    }\n" +
        "}\n";

    static final String PROBE_SRC =
        "public class StackProbe {\n" +
        "    public static native void query();\n" +
        "    public static void main(String[] args) {\n" +
        "        leaf();\n" +
        "        System.out.println(\"probe-ok\");\n" +
        "    }\n" +
        "    static void leaf() { query(); }\n" +
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
        Path agentSo = buildAgent(dir, "jvmtistack");
        compileProbe(dir, "StackProbe", PROBE_SRC);

        Path outFile = dir.resolve("agent.out");
        String output = launchChild(dir, agentSo, outFile, "StackProbe");
        check(output.contains("probe-ok"), "debuggee main executed to completion");

        Map<String, String> props = parse(outFile);
        check("yes".equals(prop(props, "onload")), "Agent_OnLoad invoked");
        check("yes".equals(prop(props, "unload")), "Agent_OnUnload invoked at shutdown");

        check(intProp(props, "threads.count") >= 1, "GetAllThreads returns at least one thread");
        check("yes".equals(prop(props, "found.main")), "GetAllThreads includes a thread named main");

        check("main".equals(prop(props, "current.name")), "GetThreadInfo current thread name is main");
        check(intProp(props, "current.priority") == 5, "GetThreadInfo current thread priority is 5");
        check(intProp(props, "current.daemon") == 0, "GetThreadInfo current thread is not a daemon");

        check(intProp(props, "frame.count") >= 2, "GetFrameCount reports at least leaf+main frames");
        check(intProp(props, "stack.count") >= 2, "GetStackTrace reports at least leaf+main frames");

        String frames = prop(props, "stack.frames");
        check(frames.contains("leaf"), "GetStackTrace includes the leaf frame (got " + frames + ")");
        check(frames.contains("main"), "GetStackTrace includes the main frame (got " + frames + ")");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
