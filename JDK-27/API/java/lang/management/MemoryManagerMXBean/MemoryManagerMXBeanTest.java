/*
 * @test
 * @summary java.lang.management.MemoryManagerMXBean conformance: white-box structure + black-box behavior.
 * @run main MemoryManagerMXBeanTest
 */
import java.lang.management.*;
import java.util.*;

public class MemoryManagerMXBeanTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.management.MemoryManagerMXBean");
        check(c.isInterface(), "interface");
        check(PlatformManagedObject.class.isAssignableFrom(c), "extends PlatformManagedObject");
        check(c.getMethod("getName").getReturnType() == String.class, "getName returns String");
        check(c.getMethod("isValid").getReturnType() == boolean.class, "isValid returns boolean");
        check(c.getMethod("getMemoryPoolNames").getReturnType() == String[].class, "getMemoryPoolNames returns String[]");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        List<MemoryManagerMXBean> mgrs = ManagementFactory.getMemoryManagerMXBeans();
        check(!mgrs.isEmpty(), "memory managers present");
        List<String> names = new ArrayList<>();
        for (MemoryManagerMXBean m : mgrs) {
            check(m.getName() != null && !m.getName().isEmpty(), "manager name non-empty");
            check(m.isValid(), "manager valid");
            check(m.getMemoryPoolNames() != null, "pool names non-null");
            check(m.getObjectName() != null, "objectName non-null");
            names.add(m.getName());
        }
        Collections.sort(names);
        for (int i = 1; i < names.size(); i++) check(!names.get(i).equals(names.get(i - 1)), "manager names distinct");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
