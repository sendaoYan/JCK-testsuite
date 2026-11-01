/*
 * @test
 * @summary java.lang.management.PlatformManagedObject conformance: getObjectName.
 * @run main PlatformManagedObjectTest
 */

import java.lang.management.ManagementFactory;
import java.lang.management.PlatformManagedObject;

public class PlatformManagedObjectTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testPlatformManagedObject() {
        PlatformManagedObject pmo = ManagementFactory.getRuntimeMXBean();
        check(pmo.getObjectName() != null, "getObjectName");
    }

    public static void main(String[] args) {
        testPlatformManagedObject();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
