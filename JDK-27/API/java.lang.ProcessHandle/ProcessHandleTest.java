/*
 * @test
 * @summary java.lang.ProcessHandle conformance: white-box structure + black-box behavior.
 * @run main ProcessHandleTest
 */

import java.lang.ProcessHandle;
import java.util.Optional;

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

    public static void main(String[] args) {
        testWhiteBox();
        testCurrent();
        testInfo();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
