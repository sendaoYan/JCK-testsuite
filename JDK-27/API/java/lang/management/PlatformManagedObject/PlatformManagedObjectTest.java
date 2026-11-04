/*
 * @test
 * @summary java.lang.management.PlatformManagedObject conformance: white-box structure + black-box behavior.
 * @run main PlatformManagedObjectTest
 */
import java.lang.management.*;
import javax.management.ObjectName;

public class PlatformManagedObjectTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.management.PlatformManagedObject");
        check(c.isInterface(), "interface");
        check(c.getMethod("getObjectName").getReturnType() == ObjectName.class, "getObjectName returns ObjectName");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // anonymous implementation roundtrip
        ObjectName expected;
        try {
            expected = new ObjectName("java.lang:type=Runtime");
        } catch (Exception e) { throw new AssertionError(e); }
        PlatformManagedObject pmo = new PlatformManagedObject() {
            public ObjectName getObjectName() { return expected; }
        };
        check(expected.equals(pmo.getObjectName()), "anonymous getObjectName roundtrip");

        // real platform MXBeans implement this interface and return a valid ObjectName
        check(ManagementFactory.getRuntimeMXBean() instanceof PlatformManagedObject,
              "RuntimeMXBean is a PlatformManagedObject");
        check(ManagementFactory.getRuntimeMXBean().getObjectName() != null,
              "RuntimeMXBean objectName non-null");
        check(ManagementFactory.getMemoryMXBean().getObjectName() != null,
              "MemoryMXBean objectName non-null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
