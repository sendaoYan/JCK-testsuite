/*
 * @test
 * @summary java.lang.management.MonitorInfo conformance: locked stack depth/frame.
 * @run main MonitorInfoTest
 */

import java.lang.management.LockInfo;
import java.lang.management.MonitorInfo;

public class MonitorInfoTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testMonitorInfo() {
        MonitorInfo mi = new MonitorInfo("java.lang.Object", 123, 5,
                new StackTraceElement("C", "m", "F", 1));
        check("java.lang.Object".equals(mi.getClassName()), "getClassName");
        check(mi.getIdentityHashCode() == 123, "getIdentityHashCode");
        check(mi.getLockedStackDepth() == 5, "getLockedStackDepth");
        check("C".equals(mi.getLockedStackFrame().getClassName()), "getLockedStackFrame");
        check(mi instanceof LockInfo, "extends LockInfo");
    }

    public static void main(String[] args) {
        testMonitorInfo();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
