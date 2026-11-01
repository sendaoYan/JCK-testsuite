/*
 * @test
 * @summary java.lang.management.MemoryNotificationInfo conformance: pool/usage/count.
 * @run main MemoryNotificationInfoTest
 */

import java.lang.management.MemoryNotificationInfo;
import java.lang.management.MemoryUsage;

public class MemoryNotificationInfoTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testMemoryNotificationInfo() {
        MemoryUsage mu = new MemoryUsage(100, 50, 100, 100);
        MemoryNotificationInfo info = new MemoryNotificationInfo("pool", mu, 7);
        check("pool".equals(info.getPoolName()), "getPoolName");
        check(info.getUsage().getUsed() == 50, "getUsage");
        check(info.getCount() == 7, "getCount");
        check(MemoryNotificationInfo.MEMORY_THRESHOLD_EXCEEDED != null,
              "MEMORY_THRESHOLD_EXCEEDED");
        check(MemoryNotificationInfo.MEMORY_COLLECTION_THRESHOLD_EXCEEDED != null,
              "MEMORY_COLLECTION_THRESHOLD_EXCEEDED");
    }

    public static void main(String[] args) {
        testMemoryNotificationInfo();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
