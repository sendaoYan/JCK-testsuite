/*
 * @test
 * @summary java.lang.ProcessHandle conformance: white-box structure + black-box behavior.
 * @run main ProcessHandleTest
 */

import java.io.IOException;
import java.lang.ProcessHandle;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class ProcessHandleTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() {
        check(ProcessHandle.class.isInterface(), "ProcessHandle is an interface");
        check(Comparable.class.isAssignableFrom(ProcessHandle.class),
              "ProcessHandle extends Comparable");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testCurrent() {
        ProcessHandle me = ProcessHandle.current();
        check(me.pid() > 0, "pid positive");
        check(me.isAlive(), "isAlive");
        check(me.compareTo(me) == 0, "compareTo self == 0");
        check(me.equals(me), "equals self");
        check(me.hashCode() == me.hashCode(), "hashCode stable");
        check(me.parent() != null, "parent Optional non-null");
        check(me.children() != null, "children stream non-null");
        check(me.descendants() != null, "descendants stream non-null");
        check(ProcessHandle.allProcesses() != null, "allProcesses non-null");
        check(ProcessHandle.of(me.pid()).isPresent(), "of(pid) present");
        check(!ProcessHandle.of(0L).isPresent(), "of(0) empty (no pid 0 process)");
        me.supportsNormalTermination();
        passed++;
    }

    static void testInfo() {
        ProcessHandle.Info info = ProcessHandle.current().info();
        check(info != null, "info non-null");
        check(info.command() != null, "command Optional non-null");
        check(info.commandLine() != null, "commandLine Optional non-null");
        check(info.arguments() != null, "arguments Optional non-null");
        check(info.startInstant() != null, "startInstant Optional non-null");
        check(info.totalCpuDuration() != null, "totalCpuDuration Optional non-null");
        check(info.user() != null, "user Optional non-null");
    }

    // ---- Part 2b: black-box for destroy/destroyForcibly/onExit ----
    static Process startSleeper(Path source) throws IOException {
        String javaBin = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        return new ProcessBuilder(javaBin, source.toString()).start();
    }

    static void testBlackBox() throws Exception {
        // onExit() on the current process is not permitted (throws IllegalStateException).
        try {
            ProcessHandle.current().onExit();
            throw new AssertionError("onExit on current process: expected IllegalStateException");
        } catch (IllegalStateException expected) {
            passed++;
        }

        Path dir = Files.createTempDirectory("jck-ph");
        Path src = dir.resolve("Sleeper.java");
        Files.writeString(src,
            "public class Sleeper { public static void main(String[] a) throws Exception { Thread.sleep(60000); } }");
        Process c1 = null, c2 = null;
        try {
            c1 = startSleeper(src);
            ProcessHandle h1 = c1.toHandle();
            check(h1.isAlive(), "child1 alive");
            check(h1.destroy(), "destroy() returns true");
            h1.onExit().get(20, TimeUnit.SECONDS);
            check(!h1.isAlive(), "child1 terminated after destroy()");

            c2 = startSleeper(src);
            ProcessHandle h2 = c2.toHandle();
            check(h2.isAlive(), "child2 alive");
            check(h2.destroyForcibly(), "destroyForcibly() returns true");
            h2.onExit().get(20, TimeUnit.SECONDS);
            check(!h2.isAlive(), "child2 terminated after destroyForcibly()");
        } finally {
            if (c1 != null) { c1.destroyForcibly(); try { c1.waitFor(5, TimeUnit.SECONDS); } catch (InterruptedException ignore) { } }
            if (c2 != null) { c2.destroyForcibly(); try { c2.waitFor(5, TimeUnit.SECONDS); } catch (InterruptedException ignore) { } }
            try { Files.deleteIfExists(src); Files.deleteIfExists(dir); } catch (IOException ignore) { }
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testCurrent();
        testInfo();
        testBlackBox();
        try {
            lcSetupHelpers();
            testCurrentContract();
            testOfLookups();
            testAllProcessesContract();
            testChildrenDescendantsContract();
            testChildLifecycleViaHandle();
            testInfoContract();
            testComparableContract();
            testProcessToHandleBridge();
            testWhiteBoxMore();
        } finally {
            lcCleanup();
        }
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ==== 契约深化（2026-11）新增：真子进程基础设施 ====
    // 子进程一律用 System.getProperty("java.home")+"/bin/java"，避免跨 JDK 混淆。
    static final String LC_JAVA_BIN = Path.of(System.getProperty("java.home"), "bin", "java").toString();
    static Path lcDir; // 临时目录下的独立子目录，main 末尾 finally 递归清理

    /** True when destroy() can ask the process to terminate (a catchable signal). */
    static boolean normalTerminationSupported() { return java.io.File.separatorChar == '/'; }

    /** True when the executable name matches after the OS's own resolution rules. */
    static boolean sameExecutable(String reported, String expected) {
        return reported.equals(expected)
            // Windows appends the .exe suffix when it resolves a bare program name.
            || (java.io.File.separatorChar == '\\' && reported.equalsIgnoreCase(expected + ".exe"));
    }

    /** True when the OS-reported account names the user running the JVM. */
    static boolean sameUser(String reported) {
        String me = System.getProperty("user.name");
        return me.equals(reported)
            // Windows reports accounts qualified with a domain or machine prefix.
            || (java.io.File.separatorChar == '\\'
                && (reported.equalsIgnoreCase(me)
                    || reported.toLowerCase().endsWith("\\" + me.toLowerCase())));
    }

    static void expectNPE(Callable<?> c, String msg) throws Exception {
        try {
            c.call();
            throw new AssertionError(msg + ": expected NullPointerException");
        } catch (NullPointerException expected) {
            passed++;
        }
    }

    static void lcSetupHelpers() throws Exception {
        lcDir = Files.createTempDirectory("lcG-ph");
        Path src = lcDir.resolve("Sleeper.java");
        Files.writeString(src,
            "public class Sleeper { public static void main(String[] a) throws Exception { Thread.sleep(10000); } }");
        String javac = Path.of(System.getProperty("java.home"), "bin", "javac").toString();
        Process cj = new ProcessBuilder(javac, "-d", lcDir.toString(), src.toString())
                .redirectErrorStream(true).start();
        String out = new String(cj.getInputStream().readAllBytes(), "UTF-8");
        check(cj.waitFor() == 0, "javac compiles Sleeper: " + out.trim());
        check(Files.exists(lcDir.resolve("Sleeper.class")), "Sleeper.class produced");
    }

    static Process lcSleeper() throws IOException {
        return new ProcessBuilder(LC_JAVA_BIN, "-cp", lcDir.toString(), "Sleeper").start();
    }

    static void lcReap(Process p) {
        if (p == null) return;
        p.destroyForcibly();
        try { p.waitFor(5, TimeUnit.SECONDS); } catch (InterruptedException ignore) { }
    }

    static void lcCleanup() {
        if (lcDir == null) return;
        try (var s = Files.walk(lcDir)) {
            s.sorted(Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
        } catch (IOException ignore) { }
    }

    // ---- Part 3: 契约深化（2026-11）----
    static void testCurrentContract() {
        ProcessHandle me = ProcessHandle.current();
        check(me != null, "current() non-null");
        long pid = me.pid();
        check(pid > 0, "current pid positive");
        check(ProcessHandle.current().pid() == pid, "current pid stable across calls");
        check(ProcessHandle.current().equals(me), "current() equal across calls");
        check(me.isAlive(), "current is alive");
        Optional<ProcessHandle> lookup = ProcessHandle.of(pid);
        check(lookup != null, "of() returns Optional (non-null)");
        check(lookup.isPresent(), "of(currentPid) present");
        check(lookup.get().equals(me), "of(currentPid) equals current()");
        check(lookup.get().pid() == pid, "lookup pid round-trip");
        check(lookup.get().hashCode() == me.hashCode(), "equal handles share hashCode");
        check(lookup.get().compareTo(me) == 0, "lookup compareTo current 0");
        check(me.compareTo(me) == 0, "compareTo self 0");
        check(me.equals(me), "equals self");
        check(!me.equals(null), "equals(null) false");
        check(!me.equals(new Object()), "equals(non-ProcessHandle) false");
        check(me.supportsNormalTermination() == normalTerminationSupported(),
              "supportsNormalTermination follows destroy() on this platform");
        check(me.toString() != null, "toString non-null");
        check(me.toString().equals(Long.toString(pid)), "toString is exactly the pid string");
        Optional<ProcessHandle> parent = me.parent();
        check(parent != null, "parent Optional non-null (presence itself not locked)");
        if (parent.isPresent()) {
            check(parent.get().isAlive(), "parent alive when present");
            check(!parent.get().equals(me), "parent is a distinct handle");
            check(me.parent().equals(parent), "parent() stable across calls");
        }
        check(me.info() != null, "current info non-null");
        check(me.children() != null, "children stream non-null");
        check(me.descendants() != null, "descendants stream non-null");
    }

    static void testOfLookups() {
        check(!ProcessHandle.of(0L).isPresent(), "of(0) empty");
        check(!ProcessHandle.of(-1L).isPresent(), "of(-1) empty without exception");
        check(!ProcessHandle.of(999999999L).isPresent(), "of(unused large pid) empty");
        Optional<ProcessHandle> init = ProcessHandle.of(1L);
        check(init != null, "of(1) returns an Optional");
        if (java.io.File.separatorChar == '/') {
            // pid 1 is init/systemd on POSIX; Windows has no such well-known pid.
            check(init.isPresent(), "of(1) present (init always exists on POSIX)");
        }
        long pid = ProcessHandle.current().pid();
        Optional<ProcessHandle> a = ProcessHandle.of(pid);
        Optional<ProcessHandle> b = ProcessHandle.of(pid);
        check(a.isPresent() && b.isPresent(), "two lookups present");
        check(a.get().equals(b.get()), "two lookups equal");
        check(a.get().isAlive(), "lookup of live pid isAlive");
        check(a.get().toString().equals(Long.toString(pid)), "lookup toString is pid string");
    }

    static void testAllProcessesContract() {
        check(ProcessHandle.allProcesses() != null, "allProcesses stream non-null");
        long pid = ProcessHandle.current().pid();
        check(ProcessHandle.allProcesses().anyMatch(h -> h.pid() == pid),
              "allProcesses sees the current process");
        // Windows 的 System Idle Process 以 pid 0 出现在进程表里，所以只要求非负；
        // 真正的约定是每个句柄都带一个可读的 pid。
        check(ProcessHandle.allProcesses().allMatch(h -> h.pid() >= 0), "all reported pids non-negative");
        check(ProcessHandle.allProcesses().count() >= 1, "allProcesses non-empty");
        // 单快照内统计（两次 allProcesses() 是两个独立快照，忙机器上进程增减会导致计数漂移）
        List<Long> pids = ProcessHandle.allProcesses()
                .map(ProcessHandle::pid)
                .collect(java.util.stream.Collectors.toList());
        check(pids.size() == pids.stream().distinct().count(), "allProcesses pids distinct (single snapshot)");
    }

    static void testChildrenDescendantsContract() throws Exception {
        ProcessHandle me = ProcessHandle.current();
        Process child = lcSleeper();
        try {
            ProcessHandle h = child.toHandle();
            check(me.children() != null, "children stream non-null");
            check(me.children().anyMatch(c -> c.pid() == child.pid()), "children contains live child");
            check(me.descendants().anyMatch(c -> c.pid() == child.pid()), "descendants contains live child");
            check(h.children() != null && h.descendants() != null, "child handle streams non-null");
            check(h.children().count() == 0, "sleeper has no children of its own");
            check(h.descendants().count() == 0, "sleeper has no descendants");
            Optional<ProcessHandle> par = h.parent();
            check(par.isPresent(), "child parent present");
            check(par.get().equals(me), "child parent is current handle");
            check(par.get().pid() == me.pid(), "child parent pid matches current pid");
        } finally {
            lcReap(child);
        }
        check(!me.children().anyMatch(c -> c.pid() == child.pid()),
              "children no longer contains the exited child");
    }

    static void testChildLifecycleViaHandle() throws Exception {
        Process child = lcSleeper();
        try {
            ProcessHandle h = child.toHandle();
            check(h != null, "toHandle non-null");
            check(h.pid() == child.pid(), "handle pid equals process pid");
            check(child.pid() > 0, "child pid positive");
            check(h.isAlive() && child.isAlive(), "handle and process alive views agree");
            check(h.supportsNormalTermination() == normalTerminationSupported(),
                  "child supportsNormalTermination follows destroy() on this platform");
            check(h.destroy(), "destroy() on live child returns true");
            int code = child.waitFor();
            check(code != 0, "destroyed child exits non-zero (signal or kill status)");
            check(!h.isAlive(), "handle not alive after exit");
            check(!child.isAlive(), "process not alive after exit");
            check(!h.destroy(), "destroy() after exit returns false");
            check(!h.destroyForcibly(), "destroyForcibly() after exit returns false");
            check(!h.destroy(), "second destroy() still false");
            ProcessHandle.Info dead = h.info();
            check(dead != null, "info of exited process non-null");
            check(!dead.command().isPresent(), "command empty after exit");
            check(!dead.user().isPresent(), "user empty after exit");
            check(!dead.startInstant().isPresent(), "startInstant empty after exit");
            check(!ProcessHandle.of(child.pid()).isPresent(), "of(exited pid) empty");
        } finally {
            lcReap(child);
        }
        Process c2 = lcSleeper();
        try {
            ProcessHandle h2 = c2.toHandle();
            check(h2.destroyForcibly(), "destroyForcibly on live child returns true");
            check(c2.waitFor() != 0, "forcibly destroyed child exits non-zero (kill status)");
            check(!h2.isAlive(), "handle not alive after forced kill");
        } finally {
            lcReap(c2);
        }
    }

    static void testInfoContract() throws Exception {
        Process child = lcSleeper();
        try {
            ProcessHandle.Info info = child.toHandle().info();
            check(info != null, "child info non-null");
            check(info.command().isPresent() && sameExecutable(info.command().get(), LC_JAVA_BIN),
                  "child command is the java binary we launched: " + info.command());
            // The Windows implementation exposes only the image name, so the
            // argument vector is readable on POSIX only.
            if (java.io.File.separatorChar == '/') {
                check(info.commandLine().isPresent(), "child commandLine present");
                check(info.commandLine().get().contains("Sleeper"), "child commandLine contains Sleeper");
                check(info.arguments().isPresent(), "child arguments present");
                String[] argv = info.arguments().get();
                check(argv.length == 3 && "-cp".equals(argv[0]) && "Sleeper".equals(argv[2]),
                      "child arguments exact [-cp, <dir>, Sleeper]");
            } else {
                check(info.commandLine() != null && info.arguments() != null,
                      "commandLine/arguments Optionals exist where the platform omits them");
            }
            check(info.user().isPresent(), "child user present");
            check(sameUser(info.user().get()), "child user names user.name: " + info.user());
            check(info.startInstant().isPresent(), "child startInstant present");
            check(info.totalCpuDuration().isPresent(), "child totalCpuDuration present");
        } finally {
            lcReap(child);
        }
        ProcessHandle.Info mine = ProcessHandle.current().info();
        check(mine.command().isPresent(), "current command present");
        check(mine.command().get().contains("java"), "current command is a java executable");
        if (java.io.File.separatorChar == '/') {
            check(mine.commandLine().isPresent(), "current commandLine present");
        } else {
            check(mine.commandLine() != null, "current commandLine Optional exists on Windows");
        }
        check(mine.user().isPresent() && sameUser(mine.user().get()),
              "current user names user.name: " + mine.user());
        check(mine.startInstant().isPresent(), "current startInstant present");
    }

    static void testComparableContract() throws Exception {
        ProcessHandle me = ProcessHandle.current();
        Process child = lcSleeper();
        try {
            ProcessHandle h = child.toHandle();
            check(me.compareTo(me) == 0, "current self compareTo 0");
            check(h.compareTo(h) == 0, "child self compareTo 0");
            int meChild = me.compareTo(h);
            int childMe = h.compareTo(me);
            check(meChild != 0, "distinct handles compare non-zero");
            check(meChild == -childMe, "compareTo antisymmetric");
            check(Integer.signum(meChild) == Integer.signum(Long.compare(me.pid(), h.pid())),
                  "order follows pid order (impl stable on Linux)");
            check(!me.equals(h) && !h.equals(me), "distinct pids not equal");
            expectNPE(() -> me.compareTo(null), "compareTo(null)");
        } finally {
            lcReap(child);
        }
    }

    static void testProcessToHandleBridge() throws Exception {
        Process child = lcSleeper();
        try {
            ProcessHandle h1 = child.toHandle();
            ProcessHandle h2 = child.toHandle();
            check(h1 == h2, "toHandle returns the same instance per Process");
            check(h1.pid() == child.pid(), "bridge handle pid matches");
            check(ProcessHandle.of(child.pid()).get().equals(h1), "of(pid) equals toHandle()");
            check(h1.isAlive() == child.isAlive(), "alive views consistent");
            check(h1.destroy(), "destroy via handle returns true");
            check(child.waitFor() != 0, "process exits non-zero after handle destroy");
            check(!h1.isAlive() && !child.isAlive(), "both views report dead");
            check(h1.toString().equals(Long.toString(child.pid())), "bridge handle toString is pid");
        } finally {
            lcReap(child);
        }
    }

    static void testWhiteBoxMore() throws Exception {
        check(ProcessHandle.Info.class.isInterface(), "ProcessHandle.Info is an interface");
        check(!java.io.Serializable.class.isAssignableFrom(ProcessHandle.class),
              "ProcessHandle not Serializable");
        check(ProcessHandle.class.getMethod("current") != null, "static current() present");
        check(ProcessHandle.class.getMethod("of", long.class) != null, "static of(long) present");
        check(ProcessHandle.class.getMethod("allProcesses") != null, "static allProcesses() present");
    }
}
