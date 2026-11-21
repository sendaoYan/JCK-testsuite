/*
 * @test
 * @summary JVMTI local variable / line number query contract: build a native
 *          agent from embedded C with gcc (or load the prebuilt libjvmtilocals.so
 *          via -nativepath), launch a child JVM with -agentpath, and assert
 *          GetLocalVariableTable and GetLineNumberTable for a known method
 *          compiled with javac -g.
 * @library /lib
 * @run main/othervm JvmtiLocalVarLineTest
 */

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class JvmtiLocalVarLineTest {
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
    // Embedded C agent (identical to jvmtilocals.c in this directory)
    // ------------------------------------------------------------------
    static final String AGENT_SRC =
        "/*\n" +
        " * JVMTI agent: local variable table and line number table observations.\n" +
        " *\n" +
        " * Loaded by a child JVM through -agentpath:<this.so>=<outfile>.  The probe\n" +
        " * class LocalVarProbe declares a static native method query() implemented here\n" +
        " * as Java_LocalVarProbe_query; inside that live-phase call it records, for the\n" +
        " * LocalVarProbe.instrumented method:\n" +
        " *\n" +
        " *   lvt.count=<jint>        GetLocalVariableTable entry count\n" +
        " *   lvt.entries=<name:sig,...> comma-joined \"name:signature\" per entry\n" +
        " *   lnt.count=<jint>        GetLineNumberTable entry count\n" +
        " *   lnt.first=<jint>        line number of the first table entry\n" +
        " *   lnt.last=<jint>         line number of the last table entry\n" +
        " *\n" +
        " * LocalVarProbe is compiled with javac -g so the LocalVariableTable is present.\n" +
        " * Compiled at run time against the JDK under test's own jvmti.h; see\n" +
        " * JvmtiLocalVarLineTest.java for the Java side.\n" +
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
        "    /* GetLocalVariableTable / GetLineNumberTable require the\n" +
        "     * can_access_local_variables / can_get_line_numbers capabilities. */\n" +
        "    memset(&caps, 0, sizeof caps);\n" +
        "    caps.can_access_local_variables = 1;\n" +
        "    caps.can_get_line_numbers = 1;\n" +
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
        "JNIEXPORT void JNICALL Java_LocalVarProbe_query(JNIEnv* env, jclass klass) {\n" +
        "    jvmtiEnv* jvmti = g_jvmti;\n" +
        "    jmethodID mid = NULL;\n" +
        "    jint lvt_count = 0;\n" +
        "    jvmtiLocalVariableEntry* lvt = NULL;\n" +
        "    jint lnt_count = 0;\n" +
        "    jvmtiLineNumberEntry* lnt = NULL;\n" +
        "    char number[64];\n" +
        "    char list[2048];\n" +
        "    int i;\n" +
        "    int pos = 0;\n" +
        "\n" +
        "    /* Reuse the environment obtained in Agent_OnLoad. */\n" +
        "    if (jvmti == NULL) return;\n" +
        "\n" +
        "    mid = (*env)->GetStaticMethodID(env, klass, \"instrumented\", \"()I\");\n" +
        "    if (mid == NULL) {\n" +
        "        emit(\"error\", \"no instrumented method\");\n" +
        "        return;\n" +
        "    }\n" +
        "\n" +
        "    if ((*jvmti)->GetLocalVariableTable(jvmti, mid, &lvt_count, &lvt) == JVMTI_ERROR_NONE\n" +
        "            && lvt != NULL) {\n" +
        "        snprintf(number, sizeof number, \"%d\", (int)lvt_count);\n" +
        "        emit(\"lvt.count\", number);\n" +
        "        list[0] = '\\0';\n" +
        "        pos = 0;\n" +
        "        for (i = 0; i < lvt_count; i++) {\n" +
        "            if (lvt[i].name == NULL || lvt[i].signature == NULL) continue;\n" +
        "            if (pos > 0 && pos < (int)sizeof list - 1) {\n" +
        "                list[pos++] = ',';\n" +
        "                list[pos] = '\\0';\n" +
        "            }\n" +
        "            snprintf(list + pos, sizeof list - pos, \"%s:%s\", lvt[i].name, lvt[i].signature);\n" +
        "            pos += (int)strlen(lvt[i].name) + 1 + (int)strlen(lvt[i].signature);\n" +
        "            if (pos >= (int)sizeof list) pos = (int)sizeof list - 1;\n" +
        "        }\n" +
        "        emit(\"lvt.entries\", list);\n" +
        "        for (i = 0; i < lvt_count; i++) {\n" +
        "            if (lvt[i].name != NULL)\n" +
        "                (*jvmti)->Deallocate(jvmti, (unsigned char*)lvt[i].name);\n" +
        "            if (lvt[i].signature != NULL)\n" +
        "                (*jvmti)->Deallocate(jvmti, (unsigned char*)lvt[i].signature);\n" +
        "            if (lvt[i].generic_signature != NULL)\n" +
        "                (*jvmti)->Deallocate(jvmti, (unsigned char*)lvt[i].generic_signature);\n" +
        "        }\n" +
        "        (*jvmti)->Deallocate(jvmti, (unsigned char*)lvt);\n" +
        "    }\n" +
        "\n" +
        "    if ((*jvmti)->GetLineNumberTable(jvmti, mid, &lnt_count, &lnt) == JVMTI_ERROR_NONE\n" +
        "            && lnt != NULL) {\n" +
        "        snprintf(number, sizeof number, \"%d\", (int)lnt_count);\n" +
        "        emit(\"lnt.count\", number);\n" +
        "        if (lnt_count > 0) {\n" +
        "            snprintf(number, sizeof number, \"%d\", (int)lnt[0].line_number);\n" +
        "            emit(\"lnt.first\", number);\n" +
        "            snprintf(number, sizeof number, \"%d\", (int)lnt[lnt_count - 1].line_number);\n" +
        "            emit(\"lnt.last\", number);\n" +
        "        }\n" +
        "        (*jvmti)->Deallocate(jvmti, (unsigned char*)lnt);\n" +
        "    }\n" +
        "}\n";

    static final String PROBE_SRC =
        "public class LocalVarProbe {\n" +
        "    public static native void query();\n" +
        "    public static void main(String[] args) {\n" +
        "        query();\n" +
        "        instrumented();\n" +
        "        System.out.println(\"probe-ok\");\n" +
        "    }\n" +
        "    static int instrumented() {\n" +
        "        int a = 1;\n" +
        "        long b = 2L;\n" +
        "        double c = 3.0;\n" +
        "        String s = \"hi\";\n" +
        "        return a + (int) b + (int) c + s.length();\n" +
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
        Path agentSo = buildAgent(dir, "jvmtilocals");
        compileProbe(dir, "LocalVarProbe", PROBE_SRC, "-g");

        Path outFile = dir.resolve("agent.out");
        String output = launchChild(dir, agentSo, outFile, "LocalVarProbe");
        check(output.contains("probe-ok"), "debuggee main executed to completion");

        Map<String, String> props = parse(outFile);
        check("yes".equals(prop(props, "onload")), "Agent_OnLoad invoked");
        check("yes".equals(prop(props, "unload")), "Agent_OnUnload invoked at shutdown");
        check(intProp(props, "addcaps.rc") == 0,
                "AddCapabilities(local variables + line numbers) returns JVMTI_ERROR_NONE (0)");

        check(intProp(props, "lvt.count") >= 4,
                "GetLocalVariableTable reports at least 4 entries (a,b,c,s)");
        String entries = prop(props, "lvt.entries");
        check(entries.contains("a:I"), "local variable a has signature I (got " + entries + ")");
        check(entries.contains("b:J"), "local variable b has signature J (got " + entries + ")");
        check(entries.contains("c:D"), "local variable c has signature D (got " + entries + ")");
        check(entries.contains("s:Ljava/lang/String;"),
                "local variable s has signature Ljava/lang/String; (got " + entries + ")");

        check(intProp(props, "lnt.count") >= 1,
                "GetLineNumberTable reports at least one entry");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
