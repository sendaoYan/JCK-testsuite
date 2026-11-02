/*
 * @test
 * @summary java.lang.ProcessHandle conformance: current/pid/isAlive/compareTo/children/allProcesses.
 * @run main ProcessHandleTest
 */
import java.lang.ProcessHandle;
import java.util.Optional;

public class ProcessHandleTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        check(ProcessHandle.class.isInterface(), "is interface");
        ProcessHandle me = ProcessHandle.current();
        check(me.pid() > 0, "pid positive");
        check(me.isAlive(), "isAlive");
        check(me.compareTo(me) == 0, "compareTo self");
        check(me.parent() != null, "parent Optional non-null");
        check(me.children() != null, "children stream non-null");
        check(me.descendants() != null, "descendants stream non-null");
        check(ProcessHandle.allProcesses() != null, "allProcesses non-null");
        check(ProcessHandle.of(me.pid()).isPresent(), "of(pid) present");
        me.supportsNormalTermination();
        passed++;
        System.out.println("PASS (" + passed + " assertions)");
    }
}
