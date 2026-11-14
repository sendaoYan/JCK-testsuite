/*
 * @test
 * @summary java.lang.Process conformance: white-box structure + black-box behavior.
 * @run main ProcessTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Modifier;
import java.util.concurrent.TimeUnit;

public class ProcessTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // Minimal in-memory Process implementation (no real external command).
    static class MockProcess extends Process {
        final ByteArrayOutputStream captured = new ByteArrayOutputStream();
        int exit;
        boolean destroyed;
        MockProcess(int exit) { this.exit = exit; }
        public OutputStream getOutputStream() { return captured; }
        public InputStream getInputStream() { return new ByteArrayInputStream("in".getBytes(java.nio.charset.StandardCharsets.UTF_8)); }
        public InputStream getErrorStream() { return new ByteArrayInputStream("err".getBytes(java.nio.charset.StandardCharsets.UTF_8)); }
        public int waitFor() { return exit; }
        public int exitValue() { return exit; }
        public void destroy() { destroyed = true; exit = 0; }
        public void close() throws IOException { destroy(); }
        @Override public long pid() { return 12345L; }
        @Override public boolean supportsNormalTermination() { return true; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.Process");
        check(Modifier.isAbstract(c.getModifiers()), "Process is abstract");
        check(!c.isInterface(), "Process is a class");
        check(java.io.Closeable.class.isAssignableFrom(c), "Process implements Closeable");
        check(Modifier.isAbstract(c.getMethod("getInputStream").getModifiers()), "getInputStream abstract");
        check(Modifier.isAbstract(c.getMethod("getOutputStream").getModifiers()), "getOutputStream abstract");
        check(Modifier.isAbstract(c.getMethod("waitFor").getModifiers()), "waitFor abstract");
        check(Modifier.isAbstract(c.getMethod("exitValue").getModifiers()), "exitValue abstract");
    }

    // ---- Part 2: black-box (behavioral) ----
    /** The charset inputReader()/errorReader() pick when no charset is given. */
    static java.nio.charset.Charset nativeCharset() {
        try {
            return java.nio.charset.Charset.forName(System.getProperty("native.encoding"));
        } catch (RuntimeException unnamedOrUnsupported) {
            return java.nio.charset.Charset.defaultCharset();
        }
    }

    static void testReaders() throws Exception {
        MockProcess p = new MockProcess(0);
        check(p.inputReader().readLine().equals("in"), "inputReader reads getInputStream");
        check(p.errorReader().readLine().equals("err"), "errorReader reads getErrorStream");

        // The no-argument form decodes with the native charset, and both forms
        // hand back one cached reader per stream.
        check(p.inputReader() == p.inputReader(), "inputReader() is cached");
        check(p.inputReader(nativeCharset()) == p.inputReader(),
              "inputReader(native charset) is the default reader");
        check(p.errorReader(nativeCharset()) == p.errorReader(),
              "errorReader(native charset) is the default reader");

        // A reader buffers the stream it decodes, so a second reader with a
        // different charset cannot be served: asking for one is rejected.
        java.nio.charset.Charset other =
                nativeCharset().equals(java.nio.charset.StandardCharsets.UTF_8)
                        ? java.nio.charset.StandardCharsets.ISO_8859_1
                        : java.nio.charset.StandardCharsets.UTF_8;
        try {
            p.inputReader(other);
            throw new AssertionError("inputReader with a conflicting charset: expected IllegalStateException");
        } catch (IllegalStateException expected) {
            passed++;
        }
        try {
            p.errorReader(other);
            throw new AssertionError("errorReader with a conflicting charset: expected IllegalStateException");
        } catch (IllegalStateException expected) {
            passed++;
        }

        // A fresh process is free to pick any charset for its single reader.
        // 无参 inputReader()/errorReader() 用 native.encoding（zh_CN Windows 上是 GBK），
        // 与已按 UTF-8 建好的 reader 冲突，所以缓存语义只在同一 charset 下比较。
        MockProcess q = new MockProcess(0);
        check(q.inputReader(java.nio.charset.StandardCharsets.UTF_8) != null,
              "inputReader(Charset) non-null");
        check(q.inputReader(java.nio.charset.StandardCharsets.UTF_8)
                == q.inputReader(java.nio.charset.StandardCharsets.UTF_8),
              "same charset returns the same reader");
        check(q.errorReader(java.nio.charset.StandardCharsets.UTF_8) != null,
              "errorReader(Charset) non-null");
        check(q.errorReader(java.nio.charset.StandardCharsets.UTF_8)
                == q.errorReader(java.nio.charset.StandardCharsets.UTF_8),
              "same charset returns the same error reader");
        try {
            new MockProcess(0).inputReader(null);
            throw new AssertionError("inputReader(null): expected NullPointerException");
        } catch (NullPointerException expected) {
            passed++;
        }
        try {
            new MockProcess(0).errorReader(null);
            throw new AssertionError("errorReader(null): expected NullPointerException");
        } catch (NullPointerException expected) {
            passed++;
        }
    }

    static void testWriters() throws Exception {
        MockProcess p = new MockProcess(0);
        p.outputWriter().write("hello");
        p.outputWriter().flush();
        p.outputWriter().close();
        String written = p.captured.toString(java.nio.charset.StandardCharsets.UTF_8.name());
        check(written.equals("hello"), "outputWriter writes to getOutputStream");
    }

    static void testExitAndAlive() throws Exception {
        MockProcess p = new MockProcess(7);
        check(p.waitFor() == 7, "waitFor returns exit value");
        check(p.exitValue() == 7, "exitValue returns exit value");
        check(!p.isAlive(), "isAlive false after exit");
        p.destroy();
        passed++;
    }

    static void testWaitForDuration() throws Exception {
        MockProcess p = new MockProcess(0);
        check(p.waitFor(java.time.Duration.ofMillis(100)), "waitFor(Duration) returns true when already exited");
    }

    // ---- Part 2b: black-box for the previously uncovered Process methods ----
    static void testBlackBox() throws Exception {
        // getErrorStream: reads the error bytes directly.
        MockProcess p = new MockProcess(0);
        String err = new String(p.getErrorStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        check("err".equals(err), "getErrorStream returns the error bytes");

        // destroyForcibly: delegates to destroy() and returns this.
        MockProcess pf = new MockProcess(9);
        check(pf.destroyForcibly() == pf, "destroyForcibly returns this");
        check(pf.destroyed, "destroyForcibly invokes destroy()");

        // pid / supportsNormalTermination (concrete members, overridden by the mock).
        check(p.pid() == 12345L, "pid returns the mock pid");
        check(p.supportsNormalTermination(), "supportsNormalTermination true");

        // onExit: completes with this Process once waitFor() returns.
        check(p.onExit().get() == p, "onExit completes with this Process");

        // toHandle/info/children/descendants default to throwing UnsupportedOperationException
        // for a subclass that does not provide a real process handle.
        try { p.toHandle(); throw new AssertionError("toHandle default: expected UnsupportedOperationException"); }
        catch (UnsupportedOperationException expected) { passed++; }
        try { p.info(); throw new AssertionError("info default: expected UnsupportedOperationException"); }
        catch (UnsupportedOperationException expected) { passed++; }
        try { p.children(); throw new AssertionError("children default: expected UnsupportedOperationException"); }
        catch (UnsupportedOperationException expected) { passed++; }
        try { p.descendants(); throw new AssertionError("descendants default: expected UnsupportedOperationException"); }
        catch (UnsupportedOperationException expected) { passed++; }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testReaders();
        testWriters();
        testExitAndAlive();
        testWaitForDuration();
        testBlackBox();
        try {
            lcSetupHelpers();
            testRealJavaVersionProcess();
            testExitCodeTable();
            testSleeperLifecycle();
            testDestroyForciblyLive();
            testStdinEcho();
            testRedirectErrorStreamMutex();
            testConcurrentSleepers();
            testMockGate();
            testMockProcessMore();
            testWhiteBoxApiSurface();
        } finally {
            lcDeleteRec(lcDir);
        }
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ==== 契约深化（2026-11）新增：真子进程基础设施 ====
    // 子进程一律用 System.getProperty("java.home")+"/bin/java"，避免跨 JDK 混淆。
    static final String LC_JAVA_BIN =
            new File(new File(System.getProperty("java.home"), "bin"), "java").getAbsolutePath();
    static File lcDir; // 临时目录下的独立子目录，main 末尾 finally 递归清理

    /** True when the executable name resolved the way the OS resolves it. */
    static boolean sameExecutable(String reported, String expected) {
        // Windows appends the .exe suffix when it resolves a bare program name.
        return reported.equals(expected)
            || (File.separatorChar == '\\' && reported.equalsIgnoreCase(expected + ".exe"));
    }

    /** True when the OS-reported account names the user running the JVM. */
    static boolean sameUser(String reported) {
        String me = System.getProperty("user.name");
        return me.equals(reported)
            // Windows reports accounts qualified with a domain or machine prefix.
            || (File.separatorChar == '\\'
                && reported.toLowerCase().endsWith("\\" + me.toLowerCase()));
    }

    static String lcJavacBin() {
        return new File(new File(System.getProperty("java.home"), "bin"), "javac").getAbsolutePath();
    }

    interface LCAction { void run() throws Exception; }

    static void expectNPE(LCAction a, String msg) throws Exception {
        try {
            a.run();
            throw new AssertionError(msg + ": expected NullPointerException");
        } catch (NullPointerException expected) {
            passed++;
        }
    }

    static void expectITSE(LCAction a, String msg) throws Exception {
        try {
            a.run();
            throw new AssertionError(msg + ": expected IllegalThreadStateException");
        } catch (IllegalThreadStateException expected) {
            passed++;
        }
    }

    static void lcWrite(File f, String s) throws Exception {
        Writer w = new OutputStreamWriter(new FileOutputStream(f), "UTF-8");
        try { w.write(s); } finally { w.close(); }
    }

    static void lcDeleteRec(File f) {
        if (f == null) return;
        if (f.isDirectory()) { File[] ch = f.listFiles(); if (ch != null) for (File c : ch) lcDeleteRec(c); }
        f.delete();
    }

    // 编译真子进程样本类：Sleeper（长驻 10s）、ExitN（按参数退出）、Cat（stdin 一行回显）。
    static void lcSetupHelpers() throws Exception {
        lcDir = new File(System.getProperty("java.io.tmpdir"), "lcG-p27-" + System.nanoTime());
        check(lcDir.mkdirs(), "helper dir created");
        lcWrite(new File(lcDir, "Sleeper.java"),
            "public class Sleeper { public static void main(String[] a) throws Exception { Thread.sleep(10000); } }");
        lcWrite(new File(lcDir, "ExitN.java"),
            "public class ExitN { public static void main(String[] a) { System.exit(Integer.parseInt(a[0])); } }");
        lcWrite(new File(lcDir, "Cat.java"),
            "public class Cat { public static void main(String[] a) throws Exception {"
            + " java.io.BufferedReader r = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));"
            + " System.out.println(r.readLine()); } }");
        Process cj = new ProcessBuilder(lcJavacBin(), "-d", lcDir.getAbsolutePath(),
                new File(lcDir, "Sleeper.java").getAbsolutePath(),
                new File(lcDir, "ExitN.java").getAbsolutePath(),
                new File(lcDir, "Cat.java").getAbsolutePath())
            .redirectErrorStream(true).start();
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        byte[] buf = new byte[64];
        int n;
        try (InputStream in = cj.getInputStream()) {
            while ((n = in.read(buf)) != -1) bos.write(buf, 0, n);
        }
        check(cj.waitFor() == 0, "javac compiles helper sources: " + bos.toString("UTF-8").trim());
        check(new File(lcDir, "Sleeper.class").exists(), "Sleeper.class produced");
    }

    static byte[] lcReadAll(InputStream in) throws IOException {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        byte[] buf = new byte[64];
        int n;
        while ((n = in.read(buf)) != -1) bos.write(buf, 0, n);
        return bos.toByteArray();
    }

    static Process lcStart(String cls, String... extra) throws Exception {
        String[] c = new String[4 + extra.length];
        c[0] = LC_JAVA_BIN;
        c[1] = "-cp";
        c[2] = lcDir.getAbsolutePath();
        c[3] = cls;
        System.arraycopy(extra, 0, c, 4, extra.length);
        return new ProcessBuilder(c).start();
    }

    // exitValue 未退出前抛 ITSE 的 Process（无真进程），用于断言具体 isAlive/waitFor 契约。
    static class GateProcess extends Process {
        boolean done;
        final ByteArrayOutputStream captured = new ByteArrayOutputStream();
        public OutputStream getOutputStream() { return captured; }
        public InputStream getInputStream() { return new ByteArrayInputStream(new byte[0]); }
        public InputStream getErrorStream() { return new ByteArrayInputStream(new byte[0]); }
        public int waitFor() { done = true; return 5; }
        public int exitValue() {
            if (!done) throw new IllegalThreadStateException("process hasn't exited");
            return 5;
        }
        public void destroy() { done = true; }
    }

    // ---- Part 3: 契约深化（2026-11）----
    static void testRealJavaVersionProcess() throws Exception {
        Process p = new ProcessBuilder(LC_JAVA_BIN, "-version").redirectErrorStream(true).start();
        long pid = p.pid();
        check(pid > 0, "pid() positive (JDK-9+)");
        check(p.pid() == pid, "pid() stable across calls");
        ProcessHandle h = p.toHandle();
        check(h != null, "toHandle() non-null");
        check(h.pid() == pid, "toHandle().pid() equals process pid()");
        check(p.toHandle() == h, "toHandle() returns same handle instance");
        // destroy() can ask the process to terminate only where the platform has
        // a catchable signal; Windows kills outright, so it reports false.
        check(p.supportsNormalTermination() == (File.separatorChar == '/'),
              "supportsNormalTermination follows destroy() on this platform");
        check(p.info() != null, "info() non-null");
        check(p.getOutputStream() != null, "getOutputStream non-null");
        check(p.getInputStream() != null, "getInputStream non-null");
        check(p.getErrorStream() != null, "getErrorStream non-null");
        String out = new String(lcReadAll(p.getInputStream()), java.nio.charset.StandardCharsets.UTF_8);
        check(p.waitFor() == 0, "java -version exit 0");
        check(p.exitValue() == 0, "exitValue 0 after waitFor");
        check(out.length() > 0, "merged output non-empty");
        check(out.contains("version"), "output contains 'version'");
        check(out.trim().startsWith("openjdk"), "Temurin banner starts with openjdk");
        check(out.contains("Runtime Environment"), "banner mentions Runtime Environment");
        check(!p.isAlive(), "not alive after waitFor");
        check(p.waitFor() == 0, "second waitFor stable");
        check(p.waitFor(1, TimeUnit.MILLISECONDS), "waitFor(timeout) true after exit");
        p.close(); // 已退出进程 close 不抛异常（委托 destroy）
        passed++;
    }

    static void testExitCodeTable() throws Exception {
        int[] codes = {0, 3, 7, 200, 255};
        Process last = null;
        for (int code : codes) {
            Process p = lcStart("ExitN", String.valueOf(code));
            check(p.waitFor() == code, "ExitN " + code + " waitFor precise");
            check(p.exitValue() == code, "ExitN " + code + " exitValue precise");
            check(!p.isAlive(), "ExitN " + code + " not alive");
            last = p;
        }
        check(last.toString() != null, "toString non-null on real process");
    }

    static void testSleeperLifecycle() throws Exception {
        Process p = lcStart("Sleeper");
        check(p.pid() > 0, "sleeper pid positive");
        check(p.isAlive(), "sleeper alive right after start");
        expectITSE(() -> p.exitValue(), "exitValue while still running");
        try {
            p.exitValue();
            throw new AssertionError("unreachable");
        } catch (IllegalThreadStateException e) {
            // Windows ProcessImpl 报 "process has not exited"，POSIX 用 "hasn't"；
            // 只锁消息非空且提到 exited，不锁具体措辞。
            String m = e.getMessage();
            check(m != null && !m.isEmpty() && m.contains("exit"),
                  "ITSE message describes the exit state: " + m);
        }
        // 活跃子进程的 info 契约（长驻进程保证可读）
        ProcessHandle.Info info = p.info();
        check(info != null, "sleeper info non-null");
        check(info.command().isPresent() && sameExecutable(info.command().get(), LC_JAVA_BIN),
              "info.command is exact java binary while alive: " + info.command());
        // The Windows implementation of ProcessHandle.Info reports only the
        // image name; the argument vector is exposed on POSIX only.
        if (File.separatorChar == '/') {
            check(info.commandLine().isPresent() && info.commandLine().get().contains("Sleeper"),
                  "info.commandLine contains class name");
        } else {
            // Windows exposes the image name only; whether a command line is
            // available is provider-defined, so both an absent and a present
            // (and matching) value satisfy the contract.
            if (info.commandLine().isPresent()) {
                check(info.commandLine().get().contains("java"), "info.commandLine names the executable");
            } else {
                check(true, "Windows exposes no command line (absent is allowed)");
            }
        }
        check(info.user().isPresent() && sameUser(info.user().get()),
              "info.user names user.name: " + info.user());
        check(!p.waitFor(200, TimeUnit.MILLISECONDS), "waitFor(200ms) false while running");
        check(!p.waitFor(java.time.Duration.ofMillis(200)), "waitFor(Duration) false while running");
        check(!p.waitFor(0, TimeUnit.MILLISECONDS), "waitFor(0) false while running");
        check(!p.waitFor(-5, TimeUnit.MILLISECONDS), "waitFor(negative) false while running");
        expectNPE(() -> p.waitFor(1, null), "waitFor(1, null unit)");
        check(p.getInputStream() != null, "input stream non-null while running");
        check(p.getOutputStream() != null, "output stream non-null while running");
        check(p.getErrorStream() != null, "error stream non-null while running");
        p.destroy();
        int code = p.waitFor();
        check(code != 0, "destroyed sleeper exits non-zero (signal or kill status)");
        check(!p.isAlive(), "not alive after waitFor");
        check(p.exitValue() == code, "exitValue equals waitFor result");
        check(p.waitFor() == code, "waitFor stable across calls");
        p.destroy(); // second destroy must not throw
        check(!p.isAlive(), "still not alive after second destroy");
        check(p.destroyForcibly() == p, "destroyForcibly after exit returns this");
        check(p.waitFor(1, TimeUnit.MILLISECONDS), "waitFor(timeout) true after exit");
        check(p.waitFor(java.time.Duration.ofMillis(500)), "waitFor(Duration) true after exit");
    }

    static void testDestroyForciblyLive() throws Exception {
        Process p = lcStart("Sleeper");
        check(p.isAlive(), "second sleeper alive");
        check(p.destroyForcibly() == p, "destroyForcibly returns this while running");
        int code = p.waitFor();
        check(code != 0, "forcibly destroyed sleeper exits non-zero (kill status)");
        check(!p.isAlive(), "not alive after forced destruction");
        check(p.exitValue() == code, "exitValue equals forced exit code");
    }

    static void testStdinEcho() throws Exception {
        Process p = lcStart("Cat");
        OutputStream os = p.getOutputStream();
        check(os != null, "getOutputStream non-null for Cat");
        os.write("lcgCat42\n".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        os.flush();
        os.close();
        String out = new String(lcReadAll(p.getInputStream()), java.nio.charset.StandardCharsets.UTF_8);
        check("lcgCat42".equals(out.trim()), "stdin line echoed back exactly");
        check(out.endsWith("\n"), "echo terminated by platform newline");
        check(p.waitFor() == 0, "Cat exits 0");
        check(p.exitValue() == 0, "Cat exitValue 0");
        check(!p.isAlive(), "Cat not alive");
    }

    static void testRedirectErrorStreamMutex() throws Exception {
        Process p = new ProcessBuilder(LC_JAVA_BIN, "-version").redirectErrorStream(true).start();
        check(p.getErrorStream() != null, "getErrorStream non-null with redirectErrorStream(true)");
        String err = new String(lcReadAll(p.getErrorStream()), java.nio.charset.StandardCharsets.UTF_8);
        check(err.length() == 0, "getErrorStream reads EOF when stderr merged into stdout");
        String out = new String(lcReadAll(p.getInputStream()), java.nio.charset.StandardCharsets.UTF_8);
        check(out.contains("version"), "merged stdout carries the version banner");
        check(p.waitFor() == 0, "merged-capture process exits 0");
    }

    static void testConcurrentSleepers() throws Exception {
        Process a = lcStart("Sleeper");
        Process b = lcStart("Sleeper");
        check(a.isAlive(), "sleeper A alive");
        check(b.isAlive(), "sleeper B alive");
        check(a.pid() != b.pid(), "concurrent sleepers have distinct pids");
        a.destroy();
        b.destroyForcibly();
        check(a.waitFor() != 0, "sleeper A terminated non-zero");
        check(b.waitFor() != 0, "sleeper B terminated non-zero");
        check(!a.isAlive(), "sleeper A not alive");
        check(!b.isAlive(), "sleeper B not alive");
        check(a.exitValue() != 0, "sleeper A exitValue non-zero");
        check(b.exitValue() != 0, "sleeper B exitValue non-zero");
    }

    static void testMockGate() throws Exception {
        GateProcess gate = new GateProcess();
        check(gate.isAlive(), "concrete isAlive() derives from exitValue() while running");
        check(!gate.waitFor(50, TimeUnit.MILLISECONDS), "waitFor(timeout) false while gate closed");
        check(gate.waitFor() == 5, "waitFor returns exit code");
        check(!gate.isAlive(), "gate not alive after waitFor");
        check(gate.exitValue() == 5, "exitValue after termination");
        check(gate.waitFor(1, TimeUnit.MILLISECONDS), "waitFor(timeout) true after termination");
        check(gate.destroyForcibly() == gate, "destroyForcibly delegates and returns this");
        check(gate.done, "destroyForcibly invoked destroy()");
    }

    static void testMockProcessMore() throws Exception {
        MockProcess p = new MockProcess(7);
        check(p.exitValue() == 7, "mock exitValue initial");
        p.destroy();
        check(p.destroyed, "mock destroy flag");
        check(p.exitValue() == 0, "mock destroy zeroes exit");
        check(p.waitFor() == 0, "mock waitFor after destroy");
        check(p.waitFor(1, TimeUnit.MILLISECONDS), "mock waitFor(timeout) after destroy");
        check(!p.isAlive(), "mock not alive after destroy");
    }

    // 锁定 JDK-27 的 Process API 面（9+/17+/19+ 成员）。
    static void testWhiteBoxApiSurface() throws Exception {
        check(!Modifier.isAbstract(Process.class.getMethod("pid").getModifiers()), "pid() concrete");
        check(!Modifier.isAbstract(Process.class.getMethod("toHandle").getModifiers()), "toHandle() concrete");
        check(!Modifier.isAbstract(Process.class.getMethod("info").getModifiers()), "info() concrete");
        check(!Modifier.isAbstract(Process.class.getMethod("onExit").getModifiers()), "onExit() concrete");
        check(!Modifier.isAbstract(Process.class.getMethod("children").getModifiers()), "children() concrete");
        check(!Modifier.isAbstract(Process.class.getMethod("descendants").getModifiers()), "descendants() concrete");
        check(!Modifier.isAbstract(Process.class.getMethod(
              "waitFor", long.class, TimeUnit.class).getModifiers()), "waitFor(long,TimeUnit) concrete");
        check(Process.class.getMethod("waitFor", java.time.Duration.class) != null,
              "waitFor(Duration) present (JDK-19+)");
        check(Process.class.getMethod("close") != null, "close() present (Closeable)");
        check(Process.class.getMethod("inputReader") != null, "inputReader() present (JDK-17+)");
        check(Process.class.getMethod("errorReader") != null, "errorReader() present (JDK-17+)");
        check(Process.class.getMethod("outputWriter") != null, "outputWriter() present (JDK-17+)");
    }
}
