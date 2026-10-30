/*
 * @test
 * @summary JVMS 6.5: monitorenter and monitorexit acquire and release an
 *          object's monitor, with re-entrant acquisition supported.
 * @compile MonitorOps.jasm
 * @run main MonitorOpsTest
 */

public class MonitorOpsTest {
    public static void main(String[] args) {
        Object lock = new Object();
        if (MonitorOps.lockUnlock(lock) != 42) throw new AssertionError("lockUnlock");
        if (MonitorOps.reentrant(lock) != 1) throw new AssertionError("reentrant");

        System.out.println("PASS");
    }
}
