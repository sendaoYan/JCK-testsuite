/*
 * @test
 * @summary java.lang.ProcessHandle.Info conformance: white-box structure + black-box behavior.
 * @run main InfoTest
 */
import java.lang.ProcessHandle;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Optional;

public class InfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = ProcessHandle.Info.class;
        check(c.isInterface(), "Info is an interface");
        check(c.getMethods().length >= 6, "at least six accessors");
        for (String n : new String[]{"command", "commandLine", "arguments",
                                     "startInstant", "totalCpuDuration", "user"}) {
            Method m = c.getMethod(n);
            check(m.getReturnType() == Optional.class, n + "() returns Optional");
            check(m.getParameterCount() == 0, n + "() takes no arguments");
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testCurrentProcessInfo() {
        ProcessHandle.Info info = ProcessHandle.current().info();
        check(info != null, "info non-null");
        check(info.command().isPresent(), "command present for the current process");
        check(info.command().get().contains("java"), "command is the java executable");
        // commandLine()/arguments() are optional: the Windows implementation
        // exposes only the image name, so the POSIX build is where the process
        // arguments are always readable.  Where they are present they must be
        // consistent with command().
        if (info.commandLine().isPresent()) {
            check(info.commandLine().get().contains(info.command().get()),
                  "commandLine starts with command: " + info.commandLine().get());
        } else if (java.io.File.separatorChar == '/') {
            throw new AssertionError("commandLine present on this platform");
        } else {
            passed++; // Windows exposes no command line: absent is allowed
        }
        check(info.user().isPresent() && !info.user().get().isEmpty(), "user present and non-empty");
        check(info.startInstant().isPresent(), "startInstant present");
        check(!info.startInstant().get().isAfter(Instant.now()), "process started no later than now");
        check(info.totalCpuDuration().isPresent(), "totalCpuDuration present for a running process");
        check(!info.totalCpuDuration().get().isNegative(), "cpu duration is not negative");
        // arguments() may legitimately be unavailable; the Optional itself must exist.
        check(info.arguments() != null, "arguments Optional object non-null");
    }

    static void testSnapshotConsistency() {
        ProcessHandle.Info a = ProcessHandle.current().info();
        ProcessHandle.Info b = ProcessHandle.current().info();
        check(a.command().equals(b.command()), "two snapshots agree on command");
        check(a.user().equals(b.user()), "two snapshots agree on user");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testCurrentProcessInfo();
        testSnapshotConsistency();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
