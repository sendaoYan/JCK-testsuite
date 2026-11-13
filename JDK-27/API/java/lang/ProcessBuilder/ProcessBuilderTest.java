/*
 * @test
 * @summary java.lang.ProcessBuilder conformance: white-box structure + black-box behavior.
 * @run main ProcessBuilderTest
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ProcessBuilderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectNPE(Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected NullPointerException");
        } catch (NullPointerException expected) {
            passed++;
        }
    }

    static void expectIAE(Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            passed++;
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.ProcessBuilder");
        check(Modifier.isFinal(c.getModifiers()), "ProcessBuilder is final");
        check(!c.isInterface(), "ProcessBuilder is a class");
        check(c.getConstructor(List.class) != null, "(List) constructor present");
        check(c.getConstructor(String[].class) != null, "(String...) constructor present");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testCommand() {
        ProcessBuilder pb = new ProcessBuilder("a", "b", "c");
        check(pb.command().equals(Arrays.asList("a", "b", "c")), "command() reflects varargs");

        pb.command("x", "y");
        check(pb.command().equals(Arrays.asList("x", "y")), "command(String...) sets and returns");

        pb.command(Arrays.asList("m", "n", "o"));
        check(pb.command().equals(Arrays.asList("m", "n", "o")), "command(List) sets and returns");
        check(pb.command() == pb.command(), "command() returns a fresh/defensive view (callable)");
    }

    static void testEnvironment() {
        ProcessBuilder pb = new ProcessBuilder("cmd");
        Map<String, String> env = pb.environment();
        check(env != null, "environment() non-null");
        check(env.containsKey("PATH") || env.containsKey("Path") || env.containsKey("path"),
              "environment contains a path variable (case varies by OS)");
        env.put("JCK_PB_TEST_VAR", "value");
        check("value".equals(env.get("JCK_PB_TEST_VAR")), "environment map is mutable");
        env.remove("JCK_PB_TEST_VAR");
        check(!env.containsKey("JCK_PB_TEST_VAR"), "environment map removal");
    }

    static void testDirectoryAndRedirect() throws Exception {
        ProcessBuilder pb = new ProcessBuilder("cmd");
        File dir = java.nio.file.Files.createTempDirectory("jck-pb").toFile();
        try {
            pb.directory(dir);
            check(pb.directory() != null && pb.directory().getAbsolutePath().equals(dir.getAbsolutePath()),
                  "directory()/directory(File) roundtrip");
        } finally {
            java.nio.file.Files.deleteIfExists(dir.toPath());
        }
        check(!pb.redirectErrorStream(), "redirectErrorStream default false");
        pb.redirectErrorStream(true);
        check(pb.redirectErrorStream(), "redirectErrorStream(true)");
        check(pb.redirectInput() == ProcessBuilder.Redirect.PIPE, "redirectInput default PIPE");
        check(pb.redirectOutput() == ProcessBuilder.Redirect.PIPE, "redirectOutput default PIPE");
        check(pb.redirectError() == ProcessBuilder.Redirect.PIPE, "redirectError default PIPE");
        pb.inheritIO(); // must not throw
        passed++;
    }

    static void testStartMissingCommand() throws Exception {
        ProcessBuilder pb = new ProcessBuilder("jck-no-such-command-xyz-12345");
        try {
            pb.start();
            throw new AssertionError("start() of missing command: expected IOException");
        } catch (java.io.IOException expected) {
            passed++;
        }
    }

    // ---- Part 2b: black-box for the (List) constructor and startPipeline ----
    static void testBlackBox() throws Exception {
        // (List) constructor
        ProcessBuilder pb = new ProcessBuilder(List.of("a", "b", "c"));
        check(pb.command().equals(List.of("a", "b", "c")), "ProcessBuilder(List) command roundtrip");

        // startPipeline(empty) -> empty list, no process started.
        List<Process> empty = ProcessBuilder.startPipeline(List.of());
        check(empty.isEmpty(), "startPipeline(empty) returns empty list");

        // startPipeline with a single missing command -> IOException (nothing left running).
        try {
            ProcessBuilder.startPipeline(List.of(new ProcessBuilder("jck-no-such-cmd-xyz")));
            throw new AssertionError("startPipeline(missing command): expected IOException");
        } catch (java.io.IOException expected) {
            passed++;
        }

        // A non-last stage must have redirectOutput == PIPE; violation throws before any start.
        ProcessBuilder a = new ProcessBuilder("jck-cmd-a").redirectOutput(ProcessBuilder.Redirect.INHERIT);
        ProcessBuilder b = new ProcessBuilder("jck-cmd-b");
        try {
            ProcessBuilder.startPipeline(List.of(a, b));
            throw new AssertionError("startPipeline(non-last redirectOutput != PIPE): expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            passed++;
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testCommand();
        testEnvironment();
        testDirectoryAndRedirect();
        testStartMissingCommand();
        testBlackBox();
        try {
            lcSetupHelpers();
            testCommandContract();
            testCommandListAffectsStart();
            testDirectoryContract();
            testEnvironmentContract();
            testEnvironmentVisibleToChild();
            testRedirectErrorStreamContract();
            testRedirectChainContract();
            testRedirectFactoryContract();
            testRedirectTypeContract();
            testDiscardContract();
            testRedirectRealFileRoundTrip();
            testRealJavaVersion();
            testNegativeStart();
            testStartPipelineReal();
            testToStringContract();
            testWhiteBoxMore();
        } finally {
            lcDeleteRec(lcDir);
        }
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ==== 契约深化（2026-11）新增：真子进程基础设施 ====
    // 子进程一律用 System.getProperty("java.home")+"/bin/java"，避免跨 JDK 混淆。
    static final String LC_JAVA_BIN =
            new File(new File(System.getProperty("java.home"), "bin"), "java").getAbsolutePath();
    static File lcDir; // /tmp 独立子目录，main 末尾 finally 递归清理

    static String lcJavacBin() {
        return new File(new File(System.getProperty("java.home"), "bin"), "javac").getAbsolutePath();
    }

    static void lcWrite(File f, String s) throws Exception {
        Writer w = new OutputStreamWriter(new FileOutputStream(f), "UTF-8");
        try { w.write(s); } finally { w.close(); }
    }

    static String lcReadFile(File f) throws Exception {
        BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        try { while ((line = r.readLine()) != null) sb.append(line).append('\n'); } finally { r.close(); }
        return sb.toString();
    }

    static int lcCount(String s, String t) {
        int c = 0, i = 0;
        while ((i = s.indexOf(t, i)) != -1) { c++; i += t.length(); }
        return c;
    }

    static void lcDeleteRec(File f) {
        if (f == null) return;
        if (f.isDirectory()) { File[] ch = f.listFiles(); if (ch != null) for (File c : ch) lcDeleteRec(c); }
        f.delete();
    }

    static byte[] lcReadAll(InputStream in) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        byte[] buf = new byte[64];
        int n;
        while ((n = in.read(buf)) != -1) bos.write(buf, 0, n);
        return bos.toByteArray();
    }

    // 编译真子进程样本类：Cat（stdin 回显）、Out（打印标记）、Env（打印注入环境变量与 cwd）。
    static void lcSetupHelpers() throws Exception {
        lcDir = new File(System.getProperty("java.io.tmpdir"), "lcG-pb27-" + System.nanoTime());
        check(lcDir.mkdirs(), "helper dir created");
        lcWrite(new File(lcDir, "Cat.java"),
            "public class Cat { public static void main(String[] a) throws Exception {"
            + " java.io.BufferedReader r = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));"
            + " System.out.println(r.readLine()); } }");
        lcWrite(new File(lcDir, "Out.java"),
            "public class Out { public static void main(String[] a) { System.out.println(\"LCG_MARKER_7\"); } }");
        lcWrite(new File(lcDir, "Env.java"),
            "public class Env { public static void main(String[] a) throws Exception {"
            + " System.out.println(\"ENV=\" + System.getenv(\"LCG_VAR\"));"
            + " System.out.println(\"PWD=\" + new java.io.File(\".\").getCanonicalPath()); } }");
        Process cj = new ProcessBuilder(lcJavacBin(), "-d", lcDir.getAbsolutePath(),
                new File(lcDir, "Cat.java").getAbsolutePath(),
                new File(lcDir, "Out.java").getAbsolutePath(),
                new File(lcDir, "Env.java").getAbsolutePath())
            .redirectErrorStream(true).start();
        String cjo = new String(lcReadAll(cj.getInputStream()), "UTF-8");
        check(cj.waitFor() == 0, "javac compiles helper sources: " + cjo.trim());
        check(new File(lcDir, "Out.class").exists(), "Out.class produced");
    }

    // ---- Part 3: 契约深化（2026-11）----
    static void testCommandContract() {
        ProcessBuilder pb = new ProcessBuilder("a", "b");
        check(pb.command("c", "d") == pb, "command(String...) returns this");
        List<String> l = pb.command();
        check(l.size() == 2, "command() size 2");
        check("c".equals(l.get(0)), "command().get(0)");
        check("d".equals(l.get(1)), "command().get(1)");
        check(l.equals(Arrays.asList("c", "d")), "command() equals literal list");
        pb.command("solo");
        check(pb.command().size() == 1, "single vararg replaces command");
        check(pb.command(Arrays.asList("x", "y")) == pb, "command(List) returns this");
        check(pb.command().equals(Arrays.asList("x", "y")), "command(List) replaces");
        ProcessBuilder arr = new ProcessBuilder();
        arr.command("m", "n");
        ProcessBuilder lst = new ProcessBuilder();
        lst.command(Arrays.asList("m", "n"));
        check(arr.command().equals(lst.command()), "array form equals List form");
        // 实测（8 与 27 一致）：command(List)/List 构造器按引用保存调用方列表，
        // command() 返回该引用；varargs 形态则返回可变内部列表。
        List<String> mine = new java.util.ArrayList<>(Arrays.asList("p", "q"));
        check(new ProcessBuilder(mine).command() == mine, "List constructor stores caller list by reference");
        check(pb.command(mine) == pb && pb.command() == mine, "command(List) stores caller list by reference");
        pb.command("x", "y");
        List<String> live = pb.command();
        check(pb.command() == live, "command() returns same live list across calls");
        live.set(0, "z");
        check("z".equals(pb.command().get(0)), "mutations to returned list are visible");
        check(pb.command().size() == 2, "set keeps size");
        live.add("extra");
        check(pb.command().size() == 3, "add visible in builder");
        live.remove("extra");
        check(pb.command().size() == 2, "remove visible in builder");
        pb.command(new String[0]);
        check(pb.command().isEmpty(), "empty varargs clears command");
        expectNPE(() -> pb.command((String[]) null), "command((String[]) null)");
        expectNPE(() -> new ProcessBuilder((List<String>) null), "ProcessBuilder((List) null)");
    }

    static void testCommandListAffectsStart() throws Exception {
        ProcessBuilder pb = new ProcessBuilder("LCG_PLACEHOLDER", "-version");
        List<String> l = pb.command();
        l.set(0, LC_JAVA_BIN);
        Process p = pb.start();
        check(p.waitFor() == 0, "mutated command list drives real start (java -version)");
        check(pb.command().get(0).equals(LC_JAVA_BIN), "builder list keeps mutated value");
        check(p.exitValue() == 0, "exitValue 0 after waitFor");
        check(p.getInputStream() != null, "getInputStream non-null");
    }

    static void testDirectoryContract() {
        ProcessBuilder pb = new ProcessBuilder("cmd");
        check(pb.directory() == null, "fresh builder directory() is null");
        check(pb.directory(lcDir) == pb, "directory(File) returns this");
        check(lcDir.equals(pb.directory()), "directory round-trip");
        File other = new File(lcDir, "no-such-child");
        pb.directory(other);
        check(other.equals(pb.directory()), "second directory() wins");
        pb.directory(null);
        check(pb.directory() == null, "directory(null) resets to inherit");
    }

    static void testEnvironmentContract() {
        ProcessBuilder pb = new ProcessBuilder("cmd");
        Map<String, String> env = pb.environment();
        check(env != null, "environment() non-null");
        if (System.getenv("PATH") != null) {
            check(env.containsKey("PATH"), "environment contains PATH");
            check(System.getenv("PATH").equals(env.get("PATH")), "PATH matches System.getenv");
        }
        check(pb.environment() == env, "environment() returns same live map");
        check(env.put("LCG_PB27_VAR", "v1") == null, "first put returns null");
        check("v1".equals(pb.environment().get("LCG_PB27_VAR")), "put visible via fresh environment()");
        env.put("LCG_PB27_VAR", "v2");
        check("v2".equals(env.get("LCG_PB27_VAR")), "overwrite visible");
        check("v2".equals(env.remove("LCG_PB27_VAR")), "remove returns last value");
        check(!env.containsKey("LCG_PB27_VAR"), "removed key gone");
        pb.environment().put("LCG_PB27_IND", "x");
        ProcessBuilder pb2 = new ProcessBuilder("cmd");
        check(!pb2.environment().containsKey("LCG_PB27_IND"), "two builders keep independent environments");
        pb.environment().remove("LCG_PB27_IND");
        expectNPE(() -> env.put(null, "v"), "environment().put(null, v)");
        expectNPE(() -> env.put("LCG_PB27_VAR", null), "environment().put(k, null)");
        check(!env.containsKey("LCG_PB27_VAR"), "environment clean after negatives");
    }

    static void testEnvironmentVisibleToChild() throws Exception {
        ProcessBuilder pb = new ProcessBuilder(LC_JAVA_BIN, "-cp", lcDir.getAbsolutePath(), "Env");
        pb.environment().put("LCG_VAR", "lcgValue9");
        pb.directory(lcDir);
        Process p = pb.start();
        String out = new String(lcReadAll(p.getInputStream()), "UTF-8");
        check(p.waitFor() == 0, "Env child exits 0");
        check(out.contains("ENV=lcgValue9"), "child sees variable injected via environment()");
        check(out.contains("PWD=" + lcDir.getCanonicalPath()), "child cwd equals directory() target");
        pb.environment().remove("LCG_VAR");
        check(!pb.environment().containsKey("LCG_VAR"), "environment restored after child");
    }

    static void testRedirectErrorStreamContract() {
        ProcessBuilder pb = new ProcessBuilder("cmd");
        check(!pb.redirectErrorStream(), "redirectErrorStream fresh default false");
        check(pb.redirectErrorStream(true) == pb, "redirectErrorStream(true) returns this");
        check(pb.redirectErrorStream(), "redirectErrorStream true round-trip");
        check(pb.redirectErrorStream(false) == pb, "redirectErrorStream(false) returns this");
        check(!pb.redirectErrorStream(), "redirectErrorStream back to false");
    }

    static void testRedirectChainContract() {
        ProcessBuilder pb = new ProcessBuilder("cmd");
        check(pb.redirectInput() == ProcessBuilder.Redirect.PIPE, "fresh redirectInput PIPE");
        check(pb.redirectOutput() == ProcessBuilder.Redirect.PIPE, "fresh redirectOutput PIPE");
        check(pb.redirectError() == ProcessBuilder.Redirect.PIPE, "fresh redirectError PIPE");
        check(pb.redirectInput(ProcessBuilder.Redirect.INHERIT) == pb, "redirectInput returns this");
        check(pb.redirectInput().type() == ProcessBuilder.Redirect.Type.INHERIT, "input type INHERIT");
        check(pb.redirectOutput(ProcessBuilder.Redirect.INHERIT) == pb, "redirectOutput returns this");
        check(pb.redirectOutput().type() == ProcessBuilder.Redirect.Type.INHERIT, "output type INHERIT");
        check(pb.redirectError(ProcessBuilder.Redirect.INHERIT) == pb, "redirectError returns this");
        check(pb.redirectError().type() == ProcessBuilder.Redirect.Type.INHERIT, "error type INHERIT");
        File f = new File(lcDir, "chain.txt");
        pb.redirectOutput(f);
        check(pb.redirectOutput().type() == ProcessBuilder.Redirect.Type.WRITE, "redirectOutput(File) type WRITE");
        check(f.equals(pb.redirectOutput().file()), "redirectOutput(File) file round-trip");
        check(pb.redirectInput() == ProcessBuilder.Redirect.INHERIT, "changing output leaves input");
        check(pb.redirectError() == ProcessBuilder.Redirect.INHERIT, "changing output leaves error");
        pb.redirectInput(f);
        check(pb.redirectInput().type() == ProcessBuilder.Redirect.Type.READ, "redirectInput(File) type READ");
        check(f.equals(pb.redirectInput().file()), "redirectInput(File) file round-trip");
        pb.redirectError(f);
        check(pb.redirectError().type() == ProcessBuilder.Redirect.Type.WRITE, "redirectError(File) type WRITE");
        check(f.equals(pb.redirectError().file()), "redirectError(File) file round-trip");
        expectNPE(() -> pb.redirectInput((ProcessBuilder.Redirect) null), "redirectInput(null)");
        expectNPE(() -> pb.redirectOutput((ProcessBuilder.Redirect) null), "redirectOutput(null)");
        expectNPE(() -> pb.redirectError((ProcessBuilder.Redirect) null), "redirectError(null)");
        expectNPE(() -> pb.redirectOutput((File) null), "redirectOutput((File) null)");
    }

    static void testRedirectFactoryContract() {
        File f = new File(lcDir, "rf.txt");
        File g = new File(lcDir, "rg.txt");
        check(ProcessBuilder.Redirect.PIPE.type() == ProcessBuilder.Redirect.Type.PIPE, "PIPE type");
        check(ProcessBuilder.Redirect.INHERIT.type() == ProcessBuilder.Redirect.Type.INHERIT, "INHERIT type");
        check(ProcessBuilder.Redirect.PIPE.file() == null, "PIPE file() null");
        check(ProcessBuilder.Redirect.INHERIT.file() == null, "INHERIT file() null");
        check(ProcessBuilder.Redirect.from(f).type() == ProcessBuilder.Redirect.Type.READ, "Redirect.from type READ");
        check(f.equals(ProcessBuilder.Redirect.from(f).file()), "Redirect.from file round-trip");
        check(ProcessBuilder.Redirect.to(f).type() == ProcessBuilder.Redirect.Type.WRITE, "Redirect.to type WRITE");
        check(f.equals(ProcessBuilder.Redirect.to(f).file()), "Redirect.to file round-trip");
        check(ProcessBuilder.Redirect.appendTo(f).type() == ProcessBuilder.Redirect.Type.APPEND,
              "Redirect.appendTo type APPEND (not WRITE)");
        check(f.equals(ProcessBuilder.Redirect.appendTo(f).file()), "Redirect.appendTo file round-trip");
        check(!ProcessBuilder.Redirect.to(f).equals(ProcessBuilder.Redirect.appendTo(f)), "to != appendTo");
        check(ProcessBuilder.Redirect.appendTo(f).equals(ProcessBuilder.Redirect.appendTo(f)), "appendTo equals");
        check(ProcessBuilder.Redirect.appendTo(f).hashCode() == ProcessBuilder.Redirect.appendTo(f).hashCode(),
              "appendTo hashCode consistent");
        check(!ProcessBuilder.Redirect.from(f).equals(ProcessBuilder.Redirect.from(g)), "different files differ");
        check("PIPE".equals(ProcessBuilder.Redirect.PIPE.toString()), "PIPE toString is PIPE");
        check("INHERIT".equals(ProcessBuilder.Redirect.INHERIT.toString()), "INHERIT toString is INHERIT");
        check(ProcessBuilder.Redirect.to(f).toString().contains("write to file"), "to toString describes write");
        check(ProcessBuilder.Redirect.appendTo(f).toString().contains("append"), "appendTo toString describes append");
        expectNPE(() -> ProcessBuilder.Redirect.from(null), "Redirect.from(null)");
        expectNPE(() -> ProcessBuilder.Redirect.to(null), "Redirect.to(null)");
        expectNPE(() -> ProcessBuilder.Redirect.appendTo(null), "Redirect.appendTo(null)");
    }

    static void testRedirectTypeContract() {
        ProcessBuilder.Redirect.Type[] vals = ProcessBuilder.Redirect.Type.values();
        check(vals.length == 5, "Redirect.Type has 5 constants");
        check(Arrays.asList(vals).equals(Arrays.asList(
                  ProcessBuilder.Redirect.Type.PIPE, ProcessBuilder.Redirect.Type.INHERIT,
                  ProcessBuilder.Redirect.Type.READ, ProcessBuilder.Redirect.Type.WRITE,
                  ProcessBuilder.Redirect.Type.APPEND)),
              "Type order PIPE,INHERIT,READ,WRITE,APPEND");
        check(ProcessBuilder.Redirect.Type.valueOf("PIPE") == ProcessBuilder.Redirect.Type.PIPE, "valueOf PIPE");
        check(ProcessBuilder.Redirect.Type.valueOf("INHERIT") == ProcessBuilder.Redirect.Type.INHERIT, "valueOf INHERIT");
        check(ProcessBuilder.Redirect.Type.valueOf("READ") == ProcessBuilder.Redirect.Type.READ, "valueOf READ");
        check(ProcessBuilder.Redirect.Type.valueOf("WRITE") == ProcessBuilder.Redirect.Type.WRITE, "valueOf WRITE");
        check(ProcessBuilder.Redirect.Type.valueOf("APPEND") == ProcessBuilder.Redirect.Type.APPEND, "valueOf APPEND");
        check(ProcessBuilder.Redirect.Type.PIPE.ordinal() == 0
              && ProcessBuilder.Redirect.Type.INHERIT.ordinal() == 1
              && ProcessBuilder.Redirect.Type.READ.ordinal() == 2
              && ProcessBuilder.Redirect.Type.WRITE.ordinal() == 3
              && ProcessBuilder.Redirect.Type.APPEND.ordinal() == 4, "Type ordinals 0..4");
        expectIAE(() -> ProcessBuilder.Redirect.Type.valueOf("NO_SUCH_TYPE"), "Type.valueOf(bogus)");
        check("PIPE".equals(ProcessBuilder.Redirect.Type.PIPE.name())
              && "PIPE".equals(ProcessBuilder.Redirect.Type.PIPE.toString()), "Type name/toString");
        check(ProcessBuilder.Redirect.Type.class.isEnum(), "Redirect.Type is enum");
    }

    // DISCARD 是 JDK-9+ 新增常量（JDK-8 无）：输出被丢弃，file() 是该平台的空设备。
    static void testDiscardContract() throws Exception {
        check(ProcessBuilder.Redirect.DISCARD == ProcessBuilder.Redirect.DISCARD, "DISCARD singleton identity");
        check(ProcessBuilder.Redirect.DISCARD.type() == ProcessBuilder.Redirect.Type.WRITE, "DISCARD type WRITE");
        File nullDevice = new File(File.separatorChar == '\\' ? "NUL" : "/dev/null");
        File discardFile = ProcessBuilder.Redirect.DISCARD.file();
        check(discardFile != null && discardFile.equals(nullDevice),
              "DISCARD file() is the platform null device " + nullDevice + ": " + discardFile);
        check("WRITE".equals(ProcessBuilder.Redirect.DISCARD.toString()), "DISCARD toString is WRITE");
        ProcessBuilder pb = new ProcessBuilder("cmd");
        check(pb.redirectOutput(ProcessBuilder.Redirect.DISCARD).redirectOutput()
              == ProcessBuilder.Redirect.DISCARD, "redirectOutput(DISCARD) keeps identity");
        check(pb.redirectError(ProcessBuilder.Redirect.DISCARD).redirectError()
              == ProcessBuilder.Redirect.DISCARD, "redirectError(DISCARD) keeps identity");
        Process p = new ProcessBuilder(LC_JAVA_BIN, "-version")
                .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                .redirectError(ProcessBuilder.Redirect.DISCARD)
                .start();
        check(p.waitFor() == 0, "java -version with DISCARD output+error exits 0");
    }

    static void testRedirectRealFileRoundTrip() throws Exception {
        File rf = new File(lcDir, "redirect-out.txt");
        Process o1 = new ProcessBuilder(LC_JAVA_BIN, "-cp", lcDir.getAbsolutePath(), "Out")
                .redirectOutput(ProcessBuilder.Redirect.to(rf)).start();
        check(o1.waitFor() == 0, "redirectOutput(to(file)) child exit 0");
        check(rf.exists(), "redirect target file created");
        check("LCG_MARKER_7".equals(lcReadFile(rf).trim()), "file holds stdout exactly");
        Process o2 = new ProcessBuilder(LC_JAVA_BIN, "-cp", lcDir.getAbsolutePath(), "Out")
                .redirectOutput(ProcessBuilder.Redirect.appendTo(rf)).start();
        check(o2.waitFor() == 0, "appendTo run exit 0");
        check(lcCount(lcReadFile(rf), "LCG_MARKER_7") == 2, "appendTo appends (2 markers)");
        File inf = new File(lcDir, "redirect-in.txt");
        lcWrite(inf, "lcgInput99\n");
        Process c1 = new ProcessBuilder(LC_JAVA_BIN, "-cp", lcDir.getAbsolutePath(), "Cat")
                .redirectInput(ProcessBuilder.Redirect.from(inf)).start();
        String cout = new String(lcReadAll(c1.getInputStream()), "UTF-8");
        check(c1.waitFor() == 0, "redirectInput(from(file)) child exit 0");
        check("lcgInput99".equals(cout.trim()), "child reads redirected stdin file");
        File ef = new File(lcDir, "redirect-err.txt");
        Process v1 = new ProcessBuilder(LC_JAVA_BIN, "-version")
                .redirectError(ProcessBuilder.Redirect.to(ef)).start();
        check(v1.waitFor() == 0, "redirectError(to(file)) child exit 0");
        check(lcReadFile(ef).contains("version"), "stderr captured in redirect file");
    }

    static void testRealJavaVersion() throws Exception {
        ProcessBuilder pb = new ProcessBuilder(LC_JAVA_BIN, "-version").redirectErrorStream(true);
        check(pb.redirectErrorStream(), "redirectErrorStream set for merged capture");
        Process p = pb.start();
        String out = new String(lcReadAll(p.getInputStream()), "UTF-8");
        check(p.getInputStream() != null, "getInputStream non-null");
        check(p.getErrorStream() != null, "getErrorStream non-null");
        check(p.getOutputStream() != null, "getOutputStream non-null");
        check(p.waitFor() == 0, "java -version exit 0");
        check(p.exitValue() == 0, "exitValue 0");
        check(out.contains("version"), "merged output contains 'version'");
        check(out.trim().startsWith("openjdk"), "Temurin banner starts with openjdk");
        check(out.contains("Runtime Environment"), "banner mentions Runtime Environment");
        check(out.length() > 0, "output non-empty");
        check(!p.isAlive(), "not alive after waitFor");
        check(p.waitFor() == 0, "second waitFor returns same exit code");
        check(pb.command().get(0).equals(LC_JAVA_BIN), "builder command untouched by start()");
    }

    static void testNegativeStart() throws Exception {
        try {
            new ProcessBuilder("lcgNoSuchCmd12345").start();
            throw new AssertionError("start() of missing command: expected IOException");
        } catch (java.io.IOException e) {
            passed++;
            check(e.getMessage().startsWith("Cannot run program \"lcgNoSuchCmd12345\""),
                  "IOException message locks command name prefix");
        }
        try {
            new ProcessBuilder().start();
            throw new AssertionError("start() with empty command: expected failure");
        } catch (ArrayIndexOutOfBoundsException expected) {
            // 两侧均为 AIOOBE；消息文本不同（8: "[0]"，27: "[Index 0 out of bounds for length 0]"），只锁类型
            passed++;
        }
        try {
            new ProcessBuilder("").start();
            throw new AssertionError("start() with empty program name: expected IOException");
        } catch (java.io.IOException expected) {
            passed++;
        }
        File noDir = new File(System.getProperty("java.io.tmpdir"), "lcG-no-such-dir-xyz");
        lcDeleteRec(noDir); // a directory created by an earlier round must not mask the failure
        try {
            new ProcessBuilder(LC_JAVA_BIN, "-version").directory(noDir).start();
            throw new AssertionError("start() with missing working directory: expected IOException");
        } catch (java.io.IOException e) {
            passed++;
            check(e.getMessage().contains("(in directory \"" + noDir.getPath() + "\")"),
                  "IOException message locks directory: " + e.getMessage());
        }
    }

    // 真实两级管道：Out | Cat —— 末级 stdout 即首级输出（既有负例外，此处补正例闭环）。
    static void testStartPipelineReal() throws Exception {
        List<Process> pipe = ProcessBuilder.startPipeline(java.util.List.of(
                new ProcessBuilder(LC_JAVA_BIN, "-cp", lcDir.getAbsolutePath(), "Out"),
                new ProcessBuilder(LC_JAVA_BIN, "-cp", lcDir.getAbsolutePath(), "Cat")));
        check(pipe.size() == 2, "startPipeline returns 2 processes");
        String last = new String(lcReadAll(pipe.get(1).getInputStream()), "UTF-8").trim();
        check("LCG_MARKER_7".equals(last), "pipeline last stage receives first stage stdout");
        check(pipe.get(0).waitFor() == 0, "first stage exit 0");
        check(pipe.get(1).waitFor() == 0, "last stage exit 0");
        check(pipe.get(0).exitValue() == 0 && pipe.get(1).exitValue() == 0, "both exitValues 0");
        check(!pipe.get(0).isAlive() && !pipe.get(1).isAlive(), "neither stage alive");
    }

    static void testToStringContract() {
        String s = new ProcessBuilder("progA", "argB").toString();
        check(s != null, "ProcessBuilder.toString() non-null");
        check(s.startsWith("java.lang.ProcessBuilder@"), "toString default Object form (no command info)");
    }

    static void testWhiteBoxMore() throws Exception {
        Class<?> red = Class.forName("java.lang.ProcessBuilder$Redirect");
        check(Modifier.isAbstract(red.getModifiers()), "ProcessBuilder.Redirect is abstract");
        check(red.getConstructors().length == 0, "Redirect has no public constructors");
        check(!java.io.Serializable.class.isAssignableFrom(red), "Redirect not Serializable");
        Class<?> type = Class.forName("java.lang.ProcessBuilder$Redirect$Type");
        check(type.isEnum() && Modifier.isFinal(type.getModifiers()), "Redirect.Type is a final enum");
        check(Class.forName("java.lang.ProcessBuilder").getConstructors().length == 2,
              "exactly two public constructors");
        Field pipe = red.getField("PIPE");
        check(Modifier.isPublic(pipe.getModifiers()) && Modifier.isStatic(pipe.getModifiers())
              && Modifier.isFinal(pipe.getModifiers()), "PIPE is public static final");
        check(pipe.get(null) == ProcessBuilder.Redirect.PIPE, "PIPE field is the same constant");
        check(Class.forName("java.lang.ProcessBuilder").getMethod(
                  "startPipeline", java.util.List.class) != null, "startPipeline present in JDK-27");
    }
}
