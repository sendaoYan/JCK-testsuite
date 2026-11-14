/*
 * @test
 * @summary java.lang.Process conformance: white-box structure (abstract, six
 *          abstract methods, concrete waitFor(timeout)/destroyForcibly/isAlive)
 *          + black-box behavior via an in-memory mock and one trivial real
 *          process (stdout, exit status, isAlive, destroyForcibly).
 * @run main ProcessTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
        public InputStream getInputStream() { return new ByteArrayInputStream(new byte[]{'i','n'}); }
        public InputStream getErrorStream() { return new ByteArrayInputStream(new byte[]{'e','r','r'}); }
        public int waitFor() { return exit; }
        public int exitValue() { return exit; }
        public void destroy() { destroyed = true; exit = 0; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.Process");
        check(Modifier.isAbstract(c.getModifiers()), "Process is abstract");
        check(!c.isInterface(), "Process is a class");
        check(Modifier.isAbstract(c.getMethod("getInputStream").getModifiers()), "getInputStream abstract");
        check(Modifier.isAbstract(c.getMethod("getOutputStream").getModifiers()), "getOutputStream abstract");
        check(Modifier.isAbstract(c.getMethod("getErrorStream").getModifiers()), "getErrorStream abstract");
        check(Modifier.isAbstract(c.getMethod("waitFor").getModifiers()), "waitFor abstract");
        check(Modifier.isAbstract(c.getMethod("exitValue").getModifiers()), "exitValue abstract");
        check(Modifier.isAbstract(c.getMethod("destroy").getModifiers()), "destroy abstract");
        check(!Modifier.isAbstract(c.getMethod("isAlive").getModifiers()), "isAlive concrete");
        check(!Modifier.isAbstract(c.getMethod("destroyForcibly").getModifiers()),
              "destroyForcibly concrete");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testMockStreams() throws Exception {
        MockProcess p = new MockProcess(0);
        String in = new String(readAll(p.getInputStream()), "UTF-8");
        String err = new String(readAll(p.getErrorStream()), "UTF-8");
        check("in".equals(in), "getInputStream returns input bytes");
        check("err".equals(err), "getErrorStream returns error bytes");
        p.getOutputStream().write("hello".getBytes("UTF-8"));
        p.getOutputStream().flush();
        check("hello".equals(p.captured.toString("UTF-8")), "getOutputStream captures writes");
    }

    static void testMockExitAndAlive() throws Exception {
        MockProcess p = new MockProcess(7);
        check(p.waitFor() == 7, "waitFor returns exit value");
        check(p.exitValue() == 7, "exitValue returns exit value");
        check(!p.isAlive(), "isAlive false after exit");
        check(p.waitFor(1, java.util.concurrent.TimeUnit.MILLISECONDS), "waitFor(timeout) true after exit");
    }

    static void testDestroyForcibly() {
        MockProcess pf = new MockProcess(9);
        check(pf.destroyForcibly() == pf, "destroyForcibly returns this");
        check(pf.destroyed, "destroyForcibly invokes destroy()");
    }

    static void testRealProcess() throws Exception {
        Process p = new ProcessBuilder("sh", "-c", "echo hello").start();
        String out = new String(readAll(p.getInputStream()), "UTF-8").trim();
        check(p.waitFor() == 0, "real process exit 0");
        check("hello".equals(out), "real process stdout");
        check(p.exitValue() == 0, "exitValue after waitFor");
        check(!p.isAlive(), "isAlive false after exit");
        p.destroyForcibly();
        passed++;
    }

    static byte[] readAll(InputStream in) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        byte[] buf = new byte[64];
        int n;
        while ((n = in.read(buf)) != -1) bos.write(buf, 0, n);
        return bos.toByteArray();
    }

    // ==== 契约深化（2026-11）新增：真子进程基础设施 ====
    // 子进程一律用 System.getProperty("java.home")+"/bin/java"（JDK-8 指向 <jdk>/jre），避免跨 JDK 混淆。
    static final String LC_JAVA_BIN =
            new File(new File(System.getProperty("java.home"), "bin"), "java").getAbsolutePath();
    static File lcDir; // /tmp 独立子目录，main 末尾 finally 递归清理

    static String lcJavacBin() {
        File home = new File(System.getProperty("java.home"));
        File jc = new File(new File(home, "bin"), "javac");
        if (!jc.exists()) jc = new File(new File(home.getParentFile(), "bin"), "javac");
        return jc.getAbsolutePath();
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

    static void expectNoMethod(String name) throws Exception {
        try {
            Process.class.getMethod(name);
            throw new AssertionError(name + " should be absent in JDK-8 (it is a JDK-9+/17+ API)");
        } catch (NoSuchMethodException expected) {
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
        lcDir = new File("/tmp", "lcG-p8-" + System.nanoTime());
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
        String cjo = new String(readAll(cj.getInputStream()), "UTF-8");
        check(cj.waitFor() == 0, "javac compiles helper sources: " + cjo.trim());
        check(new File(lcDir, "Sleeper.class").exists(), "Sleeper.class produced");
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
        check(p.getOutputStream() != null, "getOutputStream non-null");
        check(p.getInputStream() != null, "getInputStream non-null");
        check(p.getErrorStream() != null, "getErrorStream non-null");
        String out = new String(readAll(p.getInputStream()), "UTF-8");
        check(p.waitFor() == 0, "java -version exit 0");
        check(p.exitValue() == 0, "exitValue 0 after waitFor");
        check(out.length() > 0, "merged output non-empty");
        check(out.contains("version"), "output contains 'version'");
        check(out.trim().startsWith("openjdk"), "Temurin banner starts with openjdk");
        check(out.contains("Runtime Environment"), "banner mentions Runtime Environment");
        check(!p.isAlive(), "not alive after waitFor");
        check(p.waitFor() == 0, "second waitFor stable");
        check(p.waitFor(1, TimeUnit.MILLISECONDS), "waitFor(timeout) true after exit");
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
        check(p.isAlive(), "sleeper alive right after start");
        expectITSE(() -> p.exitValue(), "exitValue while still running");
        try {
            p.exitValue();
            throw new AssertionError("unreachable");
        } catch (IllegalThreadStateException e) {
            // Windows JDK-8 的 ProcessImpl 报 "process has not exited"，POSIX 用 "hasn't"；
            // 只锁消息非空且提到 exited，不锁具体措辞。
            String m = e.getMessage();
            check(m != null && !m.isEmpty() && m.contains("exit"),
                  "ITSE message describes the exit state: " + m);
        }
        check(!p.waitFor(200, TimeUnit.MILLISECONDS), "waitFor(200ms) false while running");
        check(!p.waitFor(0, TimeUnit.MILLISECONDS), "waitFor(0) false while running");
        check(!p.waitFor(-5, TimeUnit.MILLISECONDS), "waitFor(negative) false while running");
        expectNPE(() -> p.waitFor(1, null), "waitFor(1, null unit)");
        check(p.getInputStream() != null, "input stream non-null while running");
        check(p.getOutputStream() != null, "output stream non-null while running");
        check(p.getErrorStream() != null, "error stream non-null while running");
        p.destroy();
        int code = p.waitFor();
        check(code != 0, "destroyed sleeper exits non-zero (Linux SIGTERM observed as 143)");
        check(!p.isAlive(), "not alive after waitFor");
        check(p.exitValue() == code, "exitValue equals waitFor result");
        check(p.waitFor() == code, "waitFor stable across calls");
        p.destroy(); // second destroy must not throw
        check(!p.isAlive(), "still not alive after second destroy");
        check(p.destroyForcibly() == p, "destroyForcibly after exit returns this");
        check(p.waitFor(1, TimeUnit.MILLISECONDS), "waitFor(timeout) true after exit");
    }

    static void testDestroyForciblyLive() throws Exception {
        Process p = lcStart("Sleeper");
        check(p.isAlive(), "second sleeper alive");
        check(p.destroyForcibly() == p, "destroyForcibly returns this while running");
        int code = p.waitFor();
        check(code != 0, "forcibly destroyed sleeper exits non-zero (Linux SIGKILL observed as 137)");
        check(!p.isAlive(), "not alive after forced destruction");
        check(p.exitValue() == code, "exitValue equals forced exit code");
    }

    static void testStdinEcho() throws Exception {
        Process p = lcStart("Cat");
        OutputStream os = p.getOutputStream();
        check(os != null, "getOutputStream non-null for Cat");
        os.write("lcgCat42\n".getBytes("UTF-8"));
        os.flush();
        os.close();
        String out = new String(readAll(p.getInputStream()), "UTF-8");
        check("lcgCat42".equals(out.trim()), "stdin line echoed back exactly");
        check(out.endsWith("\n"), "echo terminated by platform newline");
        check(p.waitFor() == 0, "Cat exits 0");
        check(p.exitValue() == 0, "Cat exitValue 0");
        check(!p.isAlive(), "Cat not alive");
    }

    static void testRedirectErrorStreamMutex() throws Exception {
        Process p = new ProcessBuilder(LC_JAVA_BIN, "-version").redirectErrorStream(true).start();
        check(p.getErrorStream() != null, "getErrorStream non-null with redirectErrorStream(true)");
        String err = new String(readAll(p.getErrorStream()), "UTF-8");
        check(err.length() == 0, "getErrorStream reads EOF when stderr merged into stdout");
        String out = new String(readAll(p.getInputStream()), "UTF-8");
        check(out.contains("version"), "merged stdout carries the version banner");
        check(p.waitFor() == 0, "merged-capture process exits 0");
    }

    static void testConcurrentSleepers() throws Exception {
        Process a = lcStart("Sleeper");
        Process b = lcStart("Sleeper");
        check(a.isAlive(), "sleeper A alive");
        check(b.isAlive(), "sleeper B alive");
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

    static void testWhiteBoxApiSurface() throws Exception {
        check(!Modifier.isAbstract(Process.class.getMethod("waitFor", long.class,
              java.util.concurrent.TimeUnit.class).getModifiers()), "waitFor(long,TimeUnit) concrete in JDK-8");
        check(!Modifier.isAbstract(Process.class.getMethod("destroyForcibly").getModifiers()),
              "destroyForcibly concrete in JDK-8");
        check(!Modifier.isAbstract(Process.class.getMethod("isAlive").getModifiers()), "isAlive concrete in JDK-8");
        expectNoMethod("pid");                     // JDK-9+
        expectNoMethod("onExit");                  // JDK-9+
        expectNoMethod("toHandle");                // JDK-9+
        expectNoMethod("info");                    // JDK-9+
        expectNoMethod("children");                // JDK-9+
        expectNoMethod("descendants");             // JDK-9+
        expectNoMethod("supportsNormalTermination"); // JDK-9+
        expectNoMethod("close");                   // JDK-9+ (Closeable)
        expectNoMethod("inputReader");             // JDK-17+
        // waitFor(Duration) 是 JDK-19+ 重载，8 侧不存在（java.time.Duration 本身在 8 存在）
        try {
            Process.class.getMethod("waitFor", java.time.Duration.class);
            throw new AssertionError("waitFor(Duration) should be absent in JDK-8");
        } catch (NoSuchMethodException expected) {
            passed++;
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testMockStreams();
        testMockExitAndAlive();
        testDestroyForcibly();
        testRealProcess();
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
}
