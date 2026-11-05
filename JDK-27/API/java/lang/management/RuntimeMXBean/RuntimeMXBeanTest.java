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


    static void expectIAE(Runnable op, String what, String... fragments) {
        try {
            op.run();
            check(false, what + " should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            check(true, what + " throws IllegalArgumentException");
            for (String f : fragments) {
                check(e.getMessage() != null && e.getMessage().contains(f),
                      what + " message contains '" + f + "' (got: " + e.getMessage() + ")");
            }
        }
    }

    static void expectNPE(Runnable op, String what, String... fragments) {
        try {
            op.run();
            check(false, what + " should throw NullPointerException");
        } catch (NullPointerException e) {
            check(true, what + " throws NullPointerException");
            for (String f : fragments) {
                check(e.getMessage() == null || e.getMessage().contains(f),
                      what + " message contains '" + f + "' or is null (got: " + e.getMessage() + ")");
            }
        }
    }

    static void expectUOE(Runnable op, String what, String... fragments) {
        try {
            op.run();
            check(false, what + " should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            check(true, what + " throws UnsupportedOperationException");
            for (String f : fragments) {
                check(e.getMessage() != null && e.getMessage().contains(f),
                      what + " message contains '" + f + "' (got: " + e.getMessage() + ")");
            }
        }
    }

    static void testDeepNameFormatAndMonotonic() {
        RuntimeMXBean b = ManagementFactory.getRuntimeMXBean();
        String name = b.getName();
        int at = name.indexOf('@');
        check(at > 0, "name has the <pid>@<host> form, got " + name);
        check(name.substring(0, at).matches("[0-9]+"), "name prefix is the numeric pid");
        check(name.length() > at + 1, "name suffix is the host");
        long u1 = b.getUptime();
        long u2 = b.getUptime();
        check(u1 >= 0 && u2 >= u1, "uptime non-negative and non-decreasing");
        long start = b.getStartTime();
        check(start > 0, "start time > 0");
        check(start <= System.currentTimeMillis() + 1000,
              "start time is not in the future (beyond clock skew tolerance)");
    }

    static void testDeepSystemPropertiesMirror() {
        RuntimeMXBean b = ManagementFactory.getRuntimeMXBean();
        java.util.Map<String, String> props = b.getSystemProperties();
        check(props != null, "system properties non-null");
        String v = props.get("java.version");
        check(v != null && v.equals(System.getProperty("java.version")),
              "java.version mirrors System.getProperty exactly");
        check(props.containsKey("java.vm.version"), "contains java.vm.version");
        check(b.getClassPath().equals(System.getProperty("java.class.path")), "class path mirrors property");
        check(b.getLibraryPath().equals(System.getProperty("java.library.path")), "library path mirrors property");
        check(b.getInputArguments() != null, "input arguments non-null list");
        check("Java Virtual Machine Specification".equals(b.getSpecName()), "specName exact");
        check(b.getVmName() != null && b.getVmName().equals(b.getVmName()), "vmName stable");
        check(b.getVmVendor() != null && b.getVmVendor().length() > 0, "vmVendor non-empty");
        check(b.getVmVersion() != null && b.getVmVersion().length() > 0, "vmVersion non-empty");
    }

    static void testDeepVersionDifferences() throws Exception {
        RuntimeMXBean b = ManagementFactory.getRuntimeMXBean();
        check("27".equals(b.getSpecVersion()), "JDK 27 reports spec version 27, got " + b.getSpecVersion());
        check("5.0".equals(b.getManagementSpecVersion()),
              "JDK 27 management spec version 5.0, got " + b.getManagementSpecVersion());
        check(!b.isBootClassPathSupported(), "JDK 27 does not support the boot class path");
        expectUOE(new Runnable() { public void run() { b.getBootClassPath(); } },
                  "getBootClassPath", "Boot class path");
        String name = b.getName();
        check(b.getPid() == Long.parseLong(name.substring(0, name.indexOf('@'))),
              "getPid matches the pid prefix of getName()");
        check(b.getPid() > 0, "pid positive");
        check("java.lang:type=Runtime".equals(b.getObjectName().getCanonicalName()), "ObjectName exact");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepNameFormatAndMonotonic();
        testDeepSystemPropertiesMirror();
        testDeepVersionDifferences();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
