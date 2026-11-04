/*
 * @test
 * @summary java.lang.management.RuntimeMXBean conformance: white-box structure + black-box behavior.
 * @run main RuntimeMXBeanTest
 */
import java.lang.management.*;
import java.util.List;
import java.util.Map;

public class RuntimeMXBeanTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.management.RuntimeMXBean");
        check(c.isInterface(), "interface");
        check(PlatformManagedObject.class.isAssignableFrom(c), "extends PlatformManagedObject");
        check(c.getMethod("getName").getReturnType() == String.class, "getName returns String");
        check(c.getMethod("getVmName").getReturnType() == String.class, "getVmName returns String");
        check(c.getMethod("getSpecVersion").getReturnType() == String.class, "getSpecVersion returns String");
        check(c.getMethod("getUptime").getReturnType() == long.class, "getUptime returns long");
        check(c.getMethod("getStartTime").getReturnType() == long.class, "getStartTime returns long");
        check(c.getMethod("getPid").getReturnType() == long.class, "getPid returns long");
        check(c.getMethod("getSystemProperties").getReturnType() == Map.class, "getSystemProperties returns Map");
        check(c.getMethod("getInputArguments").getReturnType() == List.class, "getInputArguments returns List");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        RuntimeMXBean b = ManagementFactory.getRuntimeMXBean();
        check(b != null, "non-null");
        check(b.getVmName() != null && !b.getVmName().isEmpty(), "vmName non-empty");
        check(b.getSpecVersion() != null && !b.getSpecVersion().isEmpty(), "specVersion non-empty");
        check("Java Virtual Machine Specification".equals(b.getSpecName()), "specName");
        check(b.getVmVersion() != null && !b.getVmVersion().isEmpty(), "vmVersion non-empty");
        check(b.getManagementSpecVersion() != null && !b.getManagementSpecVersion().isEmpty(),
              "managementSpecVersion non-empty");
        check(b.getName() != null && !b.getName().isEmpty(), "name non-empty");
        check(b.getPid() > 0, "pid positive");
        check(b.getUptime() >= 0, "uptime non-negative");
        check(b.getStartTime() > 0, "startTime positive");
        check(b.getInputArguments() != null, "inputArguments non-null");
        check(b.getClassPath() != null, "classPath non-null");
        check(b.getLibraryPath() != null, "libraryPath non-null");
        Map<String, String> props = b.getSystemProperties();
        check(props != null && props.containsKey("java.version"), "systemProperties has java.version");
        check(b.getObjectName() != null, "objectName non-null");
        check(!b.isBootClassPathSupported() || b.getBootClassPath() != null, "bootClassPath non-null when supported");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
