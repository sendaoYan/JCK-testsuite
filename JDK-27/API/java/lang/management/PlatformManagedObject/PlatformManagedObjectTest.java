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


    static void testDeepCanonicalNamesExact() throws Exception {
        check("java.lang:type=Runtime".equals(
                ManagementFactory.getRuntimeMXBean().getObjectName().getCanonicalName()), "Runtime");
        check("java.lang:type=Memory".equals(
                ManagementFactory.getMemoryMXBean().getObjectName().getCanonicalName()), "Memory");
        check("java.lang:type=Threading".equals(
                ManagementFactory.getThreadMXBean().getObjectName().getCanonicalName()), "Threading");
        check("java.lang:type=ClassLoading".equals(
                ManagementFactory.getClassLoadingMXBean().getObjectName().getCanonicalName()), "ClassLoading");
        check("java.lang:type=OperatingSystem".equals(
                ManagementFactory.getOperatingSystemMXBean().getObjectName().getCanonicalName()), "OperatingSystem");
        if (ManagementFactory.getCompilationMXBean() != null) {
            check("java.lang:type=Compilation".equals(
                    ManagementFactory.getCompilationMXBean().getObjectName().getCanonicalName()), "Compilation");
        }
        check("java.util.logging:type=Logging".equals(
                ManagementFactory.getPlatformMXBean(java.lang.management.PlatformLoggingMXBean.class)
                        .getObjectName().getCanonicalName()), "Logging");
    }

    static void testDeepBufferPoolNamesPattern() {
        for (java.lang.management.BufferPoolMXBean p : ManagementFactory.getPlatformMXBeans(java.lang.management.BufferPoolMXBean.class)) {
            check(p instanceof PlatformManagedObject, "BufferPoolMXBean is a PlatformManagedObject");
            check(p.getObjectName().getDomain().equals("java.nio"), "buffer pool domain is java.nio");
            check(p.getObjectName().getKeyProperty("type").equals("BufferPool"), "buffer pool type key");
            check(p.getObjectName().getKeyProperty("name").equals(p.getName()), "buffer pool name key");
        }
    }

    static void testDeepCustomImplementation() throws Exception {
        final javax.management.ObjectName on =
                new javax.management.ObjectName("jck.domain:type=Fake,name=Test");
        PlatformManagedObject custom = new PlatformManagedObject() {
            public javax.management.ObjectName getObjectName() {
                return on;
            }
        };
        check(custom.getObjectName() == on, "getObjectName round-trips the given ObjectName");
        check(custom.getObjectName().equals(new javax.management.ObjectName("jck.domain:type=Fake,name=Test")),
              "value-equal ObjectName");
        check(PlatformManagedObject.class.isAssignableFrom(java.lang.management.RuntimeMXBean.class),
              "every platform MXBean interface extends PlatformManagedObject");
        check(PlatformManagedObject.class.isAssignableFrom(java.lang.management.BufferPoolMXBean.class),
              "BufferPoolMXBean extends PlatformManagedObject");
    }

    static void testDeepSingletonUniqueness() {
        java.util.Set<String> names = new java.util.HashSet<String>();
        names.add(ManagementFactory.getRuntimeMXBean().getObjectName().getCanonicalName());
        names.add(ManagementFactory.getMemoryMXBean().getObjectName().getCanonicalName());
        names.add(ManagementFactory.getThreadMXBean().getObjectName().getCanonicalName());
        names.add(ManagementFactory.getClassLoadingMXBean().getObjectName().getCanonicalName());
        names.add(ManagementFactory.getOperatingSystemMXBean().getObjectName().getCanonicalName());
        if (ManagementFactory.getCompilationMXBean() != null) {
            names.add(ManagementFactory.getCompilationMXBean().getObjectName().getCanonicalName());
        }
        names.add(ManagementFactory.getPlatformMXBean(java.lang.management.PlatformLoggingMXBean.class)
                .getObjectName().getCanonicalName());
        check(names.size() == 7, "all singleton ObjectNames are distinct, got " + names.size());
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepCanonicalNamesExact();
        testDeepBufferPoolNamesPattern();
        testDeepCustomImplementation();
        testDeepSingletonUniqueness();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
