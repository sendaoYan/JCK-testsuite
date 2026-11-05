/*
 * @test
 * @summary java.lang.management.RuntimeMXBean conformance: JVM/name/start/uptime.
 * @run main RuntimeMXBeanTest
 */

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

public class RuntimeMXBeanTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testRuntimeMXBean() {
        RuntimeMXBean rmx = ManagementFactory.getRuntimeMXBean();
        check(rmx.getName() != null, "getName");
        check(rmx.getVmName() != null, "getVmName");
        check(rmx.getVmVersion() != null, "getVmVersion");
        check(rmx.getVmVendor() != null, "getVmVendor");
        check(rmx.getSpecName() != null, "getSpecName");
        check(rmx.getSpecVersion() != null, "getSpecVersion");
        check(rmx.getStartTime() > 0, "getStartTime");
        check(rmx.getUptime() >= 0, "getUptime");
        check(rmx.getInputArguments() != null, "getInputArguments");
        check(rmx.getClassPath() != null, "getClassPath");
        check(rmx.getSystemProperties() != null, "getSystemProperties");
        check(rmx.getLibraryPath() != null, "getLibraryPath");
        check(rmx.getObjectName() != null, "getObjectName");
        check(rmx.getManagementSpecVersion() != null, "getManagementSpecVersion");
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
        check("1.8".equals(b.getSpecVersion()), "JDK 8 reports spec version 1.8, got " + b.getSpecVersion());
        check("1.2".equals(b.getManagementSpecVersion()),
              "JDK 8 management spec version 1.2, got " + b.getManagementSpecVersion());
        check(b.isBootClassPathSupported(), "JDK 8 supports the boot class path");
        check(b.getBootClassPath() != null, "JDK 8 getBootClassPath returns a non-null value");
        try {
            b.getClass().getMethod("getPid");
            check(false, "JDK 8 RuntimeMXBean has no getPid");
        } catch (NoSuchMethodException expected) {
            check(true, "JDK 8 RuntimeMXBean has no getPid");
        }
        check("java.lang:type=Runtime".equals(b.getObjectName().getCanonicalName()), "ObjectName exact");
    }

    public static void main(String[] args) throws Exception {
        testRuntimeMXBean();
        testDeepNameFormatAndMonotonic();
        testDeepSystemPropertiesMirror();
        testDeepVersionDifferences();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
