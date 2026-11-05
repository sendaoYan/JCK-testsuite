/*
 * @test
 * @summary java.lang.ProcessBuilder conformance: white-box structure (final,
 *          two constructors, no startPipeline in JDK-8) + black-box behavior
 *          (command/env/directory/redirect get-set round-trips, inheritIO,
 *          redirectErrorStream, Redirect type/file/round-trip, and one trivial
 *          real process via sh -c echo).
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
        c.getConstructor(List.class);
        c.getConstructor(String[].class);
        passed += 2;
        try {
            c.getMethod("startPipeline", List.class);
            throw new AssertionError("startPipeline should be absent in JDK-8");
        } catch (NoSuchMethodException expected) {
            passed++;
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testCommand() {
        ProcessBuilder pb = new ProcessBuilder("a", "b");
        check(pb.command().equals(Arrays.asList("a", "b")), "varargs ctor sets command list");
        pb.command(Arrays.asList("x", "y"));
        check(pb.command().equals(Arrays.asList("x", "y")), "command(List) replaces");
        pb.command("m", "n");
        check(pb.command().equals(Arrays.asList("m", "n")), "command(String...) replaces");
        expectNPE(() -> pb.command((List<String>) null), "command(null List)");
    }

    static void testEnvironment() {
        ProcessBuilder pb = new ProcessBuilder("true");
        Map<String, String> env = pb.environment();
        check(env != null, "environment non-null");
        check(env.containsKey("PATH") || !env.isEmpty(), "environment has entries");
        env.put("JCK_TEST_VAR", "1");
        check("1".equals(pb.environment().get("JCK_TEST_VAR")), "environment is live");
        pb.environment().remove("JCK_TEST_VAR");
        check(!pb.environment().containsKey("JCK_TEST_VAR"), "environment removal visible");
    }

    static void testDirectory() {
        ProcessBuilder pb = new ProcessBuilder("true");
        check(pb.directory() == null, "directory defaults null (inherit)");
        File dir = new File(System.getProperty("java.io.tmpdir", "."));
        pb.directory(dir);
        check(dir.equals(pb.directory()), "directory get/set round-trip");
    }

    static void testRedirects() {
        ProcessBuilder pb = new ProcessBuilder("true");
        check(pb.redirectInput() == ProcessBuilder.Redirect.PIPE, "redirectInput default PIPE");
        check(pb.redirectOutput() == ProcessBuilder.Redirect.PIPE, "redirectOutput default PIPE");
        check(pb.redirectError() == ProcessBuilder.Redirect.PIPE, "redirectError default PIPE");

        pb.redirectInput(ProcessBuilder.Redirect.INHERIT);
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);
        check(pb.redirectInput() == ProcessBuilder.Redirect.INHERIT, "redirectInput(INHERIT)");
        check(pb.redirectOutput() == ProcessBuilder.Redirect.INHERIT, "redirectOutput(INHERIT)");
        check(pb.redirectError() == ProcessBuilder.Redirect.INHERIT, "redirectError(INHERIT)");

        File out = new File(System.getProperty("java.io.tmpdir", "."), "jck-pb-out-" + System.nanoTime());
        pb.redirectOutput(out);
        check(pb.redirectOutput().type() == ProcessBuilder.Redirect.Type.WRITE, "redirectOutput(File) type WRITE");
        check(out.equals(pb.redirectOutput().file()), "redirectOutput(File) file round-trip");
        out.delete();

        pb.inheritIO();
        check(pb.redirectInput() == ProcessBuilder.Redirect.INHERIT, "inheritIO sets input INHERIT");
        check(pb.redirectOutput() == ProcessBuilder.Redirect.INHERIT, "inheritIO sets output INHERIT");
        check(pb.redirectError() == ProcessBuilder.Redirect.INHERIT, "inheritIO sets error INHERIT");
    }

    static void testRedirectErrorStream() {
        ProcessBuilder pb = new ProcessBuilder("true");
        check(!pb.redirectErrorStream(), "redirectErrorStream defaults false");
        check(pb.redirectErrorStream(true) == pb, "redirectErrorStream(boolean) returns this");
        check(pb.redirectErrorStream(), "redirectErrorStream now true");
    }

    static void testRedirectTypes() {
        check(ProcessBuilder.Redirect.PIPE.type() == ProcessBuilder.Redirect.Type.PIPE, "PIPE type");
        check(ProcessBuilder.Redirect.INHERIT.type() == ProcessBuilder.Redirect.Type.INHERIT, "INHERIT type");
        File f = new File("jck-redirect-from");
        ProcessBuilder.Redirect from = ProcessBuilder.Redirect.from(f);
        check(from.type() == ProcessBuilder.Redirect.Type.READ, "Redirect.from type READ");
        check(f.equals(from.file()), "Redirect.from file round-trip");
        ProcessBuilder.Redirect to = ProcessBuilder.Redirect.to(f);
        check(to.type() == ProcessBuilder.Redirect.Type.WRITE, "Redirect.to type WRITE");
        check(f.equals(to.file()), "Redirect.to file round-trip");
        check(from.equals(ProcessBuilder.Redirect.from(f)), "Redirect.equals same");
        check(from.hashCode() == ProcessBuilder.Redirect.from(f).hashCode(), "Redirect.hashCode same");
        check(!from.equals(ProcessBuilder.Redirect.to(f)), "READ != WRITE");
    }

    static void testRealStart() throws Exception {
        Process p = new ProcessBuilder("sh", "-c", "echo hello").redirectErrorStream(true).start();
        check(p.waitFor() == 0, "start() produces working process exit 0");
        String out = new String(readAll(p.getInputStream()), "UTF-8").trim();
        check("hello".equals(out), "stdout captured");
    }

    static byte[] readAll(java.io.InputStream in) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        byte[] buf = new byte[64];
        int n;
        while ((n = in.read(buf)) != -1) bos.write(buf, 0, n);
        return bos.toByteArray();
    }

    // ==== 契约深化（2026-11）新增：真子进程基础设施 ====
    // 子进程一律用 System.getProperty("java.home")+"/bin/java"，避免跨 JDK 混淆。
    static final String LC_JAVA_BIN =
            new File(new File(System.getProperty("java.home"), "bin"), "java").getAbsolutePath();
    static File lcDir; // /tmp 独立子目录，main 末尾 finally 递归清理

    // JDK-8 的 java.home 指向 <jdk>/jre（javac 在 <jdk>/bin），JDK-9+ 直接在 bin 下。
    static String lcJavacBin() {
        File home = new File(System.getProperty("java.home"));
        File jc = new File(new File(home, "bin"), "javac");
        if (!jc.exists()) jc = new File(new File(home.getParentFile(), "bin"), "javac");
        return jc.getAbsolutePath();
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

    // 编译真子进程样本类：Cat（stdin 回显）、Out（打印标记）、Env（打印注入环境变量与 cwd）。
    static void lcSetupHelpers() throws Exception {
        lcDir = new File("/tmp", "lcG-pb8-" + System.nanoTime());
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
        String cjo = new String(readAll(cj.getInputStream()), "UTF-8");
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
        List<String> mine = new java.util.ArrayList<String>(Arrays.asList("p", "q"));
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
        ProcessBuilder pb = new ProcessBuilder("true");
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
        ProcessBuilder pb = new ProcessBuilder("true");
        Map<String, String> env = pb.environment();
        check(env != null, "environment() non-null");
        if (System.getenv("PATH") != null) {
            check(env.containsKey("PATH"), "environment contains PATH");
            check(System.getenv("PATH").equals(env.get("PATH")), "PATH matches System.getenv");
        }
        check(pb.environment() == env, "environment() returns same live map");
        check(env.put("LCG_PB8_VAR", "v1") == null, "first put returns null");
        check("v1".equals(pb.environment().get("LCG_PB8_VAR")), "put visible via fresh environment()");
        env.put("LCG_PB8_VAR", "v2");
        check("v2".equals(env.get("LCG_PB8_VAR")), "overwrite visible");
        check("v2".equals(env.remove("LCG_PB8_VAR")), "remove returns last value");
        check(!env.containsKey("LCG_PB8_VAR"), "removed key gone");
        pb.environment().put("LCG_PB8_IND", "x");
        ProcessBuilder pb2 = new ProcessBuilder("true");
        check(!pb2.environment().containsKey("LCG_PB8_IND"), "two builders keep independent environments");
        pb.environment().remove("LCG_PB8_IND");
        expectNPE(() -> env.put(null, "v"), "environment().put(null, v)");
        expectNPE(() -> env.put("LCG_PB8_VAR", null), "environment().put(k, null)");
        check(!env.containsKey("LCG_PB8_VAR"), "environment clean after negatives");
    }

    static void testEnvironmentVisibleToChild() throws Exception {
        ProcessBuilder pb = new ProcessBuilder(LC_JAVA_BIN, "-cp", lcDir.getAbsolutePath(), "Env");
        pb.environment().put("LCG_VAR", "lcgValue9");
        pb.directory(lcDir);
        Process p = pb.start();
        String out = new String(readAll(p.getInputStream()), "UTF-8");
        check(p.waitFor() == 0, "Env child exits 0");
        check(out.contains("ENV=lcgValue9"), "child sees variable injected via environment()");
        check(out.contains("PWD=" + lcDir.getCanonicalPath()), "child cwd equals directory() target");
        pb.environment().remove("LCG_VAR");
        check(!pb.environment().containsKey("LCG_VAR"), "environment restored after child");
    }

    static void testRedirectErrorStreamContract() {
        ProcessBuilder pb = new ProcessBuilder("true");
        check(!pb.redirectErrorStream(), "redirectErrorStream fresh default false");
        check(pb.redirectErrorStream(true) == pb, "redirectErrorStream(true) returns this");
        check(pb.redirectErrorStream(), "redirectErrorStream true round-trip");
        check(pb.redirectErrorStream(false) == pb, "redirectErrorStream(false) returns this");
        check(!pb.redirectErrorStream(), "redirectErrorStream back to false");
    }

    static void testRedirectChainContract() {
        ProcessBuilder pb = new ProcessBuilder("true");
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

    static void testRedirectTypeContract() throws Exception {
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
        // JDK-8 无 DISCARD（JDK-9+ 才有）——白盒锁定
        try {
            Class.forName("java.lang.ProcessBuilder$Redirect").getField("DISCARD");
            throw new AssertionError("DISCARD should be absent in JDK-8");
        } catch (NoSuchFieldException expected) {
            passed++;
        }
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
        String cout = new String(readAll(c1.getInputStream()), "UTF-8");
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
        String out = new String(readAll(p.getInputStream()), "UTF-8");
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
        try {
            new ProcessBuilder(LC_JAVA_BIN, "-version")
                    .directory(new File("/tmp/lcG-no-such-dir-xyz")).start();
            throw new AssertionError("start() with missing working directory: expected IOException");
        } catch (java.io.IOException e) {
            passed++;
            check(e.getMessage().contains("(in directory \"/tmp/lcG-no-such-dir-xyz\")"),
                  "IOException message locks directory");
        }
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
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testCommand();
        testEnvironment();
        testDirectory();
        testRedirects();
        testRedirectErrorStream();
        testRedirectTypes();
        testRealStart();
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
            testRedirectRealFileRoundTrip();
            testRealJavaVersion();
            testNegativeStart();
            testToStringContract();
            testWhiteBoxMore();
        } finally {
            lcDeleteRec(lcDir);
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
