/*
 * @test
 * @summary java.lang.ProcessHandle conformance: white-box structure + black-box behavior.
 * @run main ProcessHandleTest
 */

import java.io.IOException;
import java.lang.ProcessHandle;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
