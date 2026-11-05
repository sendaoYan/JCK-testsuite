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
        testPlatformManagedObject();
        testDeepCanonicalNamesExact();
        testDeepBufferPoolNamesPattern();
        testDeepCustomImplementation();
        testDeepSingletonUniqueness();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
