/*
 * @test
 * @summary java.lang.ProcessHandle.Info conformance: interface accessors return Optional.
 * @run main InfoTest
 */
import java.lang.ProcessHandle;

public class InfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        check(ProcessHandle.Info.class.isInterface(), "is interface");
        ProcessHandle.Info info = ProcessHandle.current().info();
        check(info != null, "info non-null");
        check(info.command() != null, "command Optional non-null");
        check(info.commandLine() != null, "commandLine Optional non-null");
        check(info.arguments() != null, "arguments Optional non-null");
        check(info.startInstant() != null, "startInstant Optional non-null");
        check(info.totalCpuDuration() != null, "totalCpuDuration Optional non-null");
        check(info.user() != null, "user Optional non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
