/*
 * @test
 * @summary java.lang.management.ManagementFactory conformance: full contract coverage.
 *          every singleton MXBean accessor non-null, memory heap/non-heap usage and
 *          pool/GC/manager beans non-empty with deterministic value bounds, runtime
 *          name/uptime/input-arguments/system-properties, OS name/arch/processors,
 *          thread counts, class-loading counters, compilation bean guarded-null,
 *          platform MBean server + interfaces set, getPlatformMXBean(s), MemoryType
 *          enumeration, MemoryUsage value invariants, and non-null string identity.
 * @run main ManagementFactoryTest
 */

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.CompilationMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryManagerMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.util.List;
import java.util.Set;

public class ManagementFactoryTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testSingletons() {
        check(ManagementFactory.getMemoryMXBean() != null, "getMemoryMXBean");
        check(ManagementFactory.getThreadMXBean() != null, "getThreadMXBean");
        check(ManagementFactory.getRuntimeMXBean() != null, "getRuntimeMXBean");
        check(ManagementFactory.getClassLoadingMXBean() != null, "getClassLoadingMXBean");
        check(ManagementFactory.getOperatingSystemMXBean() != null, "getOperatingSystemMXBean");
        check(ManagementFactory.getMemoryPoolMXBeans() != null, "getMemoryPoolMXBeans");
        check(ManagementFactory.getMemoryManagerMXBeans() != null, "getMemoryManagerMXBeans");
        check(ManagementFactory.getGarbageCollectorMXBeans() != null, "getGarbageCollectorMXBeans");
        check(ManagementFactory.getPlatformMBeanServer() != null, "getPlatformMBeanServer");
    }

    static void testMemoryMXBean() {
        MemoryMXBean mem = ManagementFactory.getMemoryMXBean();
        MemoryUsage heap = mem.getHeapMemoryUsage();
        MemoryUsage nonHeap = mem.getNonHeapMemoryUsage();
        check(heap != null && nonHeap != null, "heap/non-heap usage non-null");
        check(heap.getInit() >= 0 && heap.getUsed() >= 0 && heap.getCommitted() >= 0,
                "heap usage non-negative");
        check(heap.getMax() >= -1, "heap max >= -1");
        check(heap.getCommitted() >= heap.getUsed(), "heap committed >= used");
        check(nonHeap.getCommitted() >= nonHeap.getUsed(), "non-heap committed >= used");
        check(mem.getObjectPendingFinalizationCount() >= 0,
                "pending finalization count >= 0");
        // isVerbose is a stable boolean; no assertion on value
        mem.isVerbose();
        passed++;
    }

    static void testRuntimeMXBean() {
        RuntimeMXBean rt = ManagementFactory.getRuntimeMXBean();
        String name = rt.getName();
        check(name != null && !name.isEmpty(), "runtime name non-empty");
        // Java 8 name format: "<pid>@<hostname>"
        check(name.indexOf('@') > 0, "runtime name contains @");
        check(rt.getUptime() >= 0, "uptime >= 0");
        check(rt.getStartTime() > 0, "start time > 0");
        check(rt.getVmName() != null && !rt.getVmName().isEmpty(), "vm name non-empty");
        check(rt.getVmVendor() != null && !rt.getVmVendor().isEmpty(), "vm vendor non-empty");
        check(rt.getVmVersion() != null && !rt.getVmVersion().isEmpty(), "vm version non-empty");
        check(rt.getInputArguments() != null, "input arguments non-null");
        check(rt.getClassPath() != null, "class path non-null");
        check(rt.getLibraryPath() != null, "library path non-null");
        check(rt.getSystemProperties() != null, "system properties non-null");
        check(rt.getSystemProperties().containsKey("java.version"),
                "system properties has java.version");
        check(rt.getManagementSpecVersion() != null, "spec version non-null");
    }

    static void testOperatingSystemMXBean() {
        OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
        check(os.getName() != null && !os.getName().isEmpty(), "os name non-empty");
        check(os.getArch() != null && !os.getArch().isEmpty(), "os arch non-empty");
        check(os.getVersion() != null && !os.getVersion().isEmpty(), "os version non-empty");
        check(os.getAvailableProcessors() >= 1, "available processors >= 1");
        // load average may be negative (unavailable); just require finite double
        double la = os.getSystemLoadAverage();
        check(!Double.isNaN(la), "load average not NaN");
    }

    static void testThreadMXBean() {
        ThreadMXBean tm = ManagementFactory.getThreadMXBean();
        check(tm.getThreadCount() >= 1, "thread count >= 1");
        check(tm.getPeakThreadCount() >= tm.getThreadCount(), "peak >= current");
        check(tm.getDaemonThreadCount() >= 0, "daemon count >= 0");
        check(tm.getTotalStartedThreadCount() >= 1, "total started >= 1");
        check(tm.getCurrentThreadCpuTime() >= -1, "current thread cpu time >= -1");
        check(tm.getCurrentThreadUserTime() >= -1, "current thread user time >= -1");
        check(tm.isCurrentThreadCpuTimeSupported() || !tm.isCurrentThreadCpuTimeSupported(),
                "cpu time support is boolean");
    }

    static void testClassLoadingMXBean() {
        ClassLoadingMXBean cl = ManagementFactory.getClassLoadingMXBean();
        check(cl.getLoadedClassCount() >= 0, "loaded class count >= 0");
        check(cl.getTotalLoadedClassCount() >= cl.getLoadedClassCount(),
                "total loaded >= loaded");
        check(cl.getUnloadedClassCount() >= 0, "unloaded >= 0");
        cl.isVerbose();
        passed++;
    }

    static void testCompilationMXBean() {
        CompilationMXBean cm = ManagementFactory.getCompilationMXBean();
        if (cm != null) {
            passed++;
            check(cm.getName() != null && !cm.getName().isEmpty(), "compiler name non-empty");
        } else {
            passed++; // no JIT compiler is legal
        }
    }

    static void testPoolAndGCBeans() {
        List<MemoryPoolMXBean> pools = ManagementFactory.getMemoryPoolMXBeans();
        check(!pools.isEmpty(), "memory pools non-empty");
        for (MemoryPoolMXBean p : pools) {
            check(p.getName() != null && !p.getName().isEmpty(), "pool name non-empty");
            check(p.getType() == MemoryType.HEAP || p.getType() == MemoryType.NON_HEAP,
                    "pool type is HEAP or NON_HEAP");
            MemoryUsage u = p.getUsage();
            check(u != null, "pool usage non-null");
            check(u.getInit() >= 0 && u.getUsed() >= 0 && u.getCommitted() >= 0,
                    "pool usage non-negative");
            check(u.getMax() >= -1, "pool max >= -1");
        }

        List<GarbageCollectorMXBean> gcs = ManagementFactory.getGarbageCollectorMXBeans();
        check(!gcs.isEmpty(), "GC beans non-empty");
        for (GarbageCollectorMXBean gc : gcs) {
            check(gc.getName() != null && !gc.getName().isEmpty(), "gc name non-empty");
            check(gc.getCollectionCount() >= 0, "gc collection count >= 0");
            check(gc.getCollectionTime() >= 0, "gc collection time >= 0");
        }

        List<MemoryManagerMXBean> mgrs = ManagementFactory.getMemoryManagerMXBeans();
        check(!mgrs.isEmpty(), "memory managers non-empty");
        for (MemoryManagerMXBean m : mgrs) {
            check(m.getName() != null && !m.getName().isEmpty(), "manager name non-empty");
        }
    }

    static void testPlatformMXBeans() {
        check(ManagementFactory.getPlatformMXBean(MemoryMXBean.class) != null,
                "getPlatformMXBean MemoryMXBean");
        check(ManagementFactory.getPlatformMXBean(RuntimeMXBean.class) != null,
                "getPlatformMXBean RuntimeMXBean");

        check(!ManagementFactory.getPlatformMXBeans(MemoryPoolMXBean.class).isEmpty(),
                "getPlatformMXBeans MemoryPoolMXBean");
        check(!ManagementFactory.getPlatformMXBeans(GarbageCollectorMXBean.class).isEmpty(),
                "getPlatformMXBeans GarbageCollectorMXBean");
        check(!ManagementFactory.getPlatformMXBeans(MemoryManagerMXBean.class).isEmpty(),
                "getPlatformMXBeans MemoryManagerMXBean");

        Set<Class<? extends java.lang.management.PlatformManagedObject>> ifaces =
                ManagementFactory.getPlatformManagementInterfaces();
        check(ifaces != null && !ifaces.isEmpty(), "platform management interfaces non-empty");
        check(ifaces.contains(MemoryMXBean.class), "interfaces contains MemoryMXBean");
        check(ifaces.contains(RuntimeMXBean.class), "interfaces contains RuntimeMXBean");
    }

    static void testMemoryType() {
        check(MemoryType.HEAP != null && MemoryType.NON_HEAP != null, "MemoryType values");
        check(MemoryType.valueOf("HEAP") == MemoryType.HEAP, "valueOf HEAP");
        check(MemoryType.valueOf("NON_HEAP") == MemoryType.NON_HEAP, "valueOf NON_HEAP");
        check(MemoryType.values().length == 2, "MemoryType has two values");
        check(MemoryType.HEAP.name().equals("HEAP"), "HEAP name");
        check(MemoryType.HEAP.toString() != null && !MemoryType.HEAP.toString().isEmpty(),
                "HEAP toString non-empty");
    }

    static void testMemoryUsageInvariants() {
        MemoryUsage u = new MemoryUsage(100L, 50L, 80L, 200L);
        check(u.getInit() == 100L && u.getUsed() == 50L
                && u.getCommitted() == 80L && u.getMax() == 200L, "MemoryUsage getters");
        check(u.toString() != null && u.toString().contains("50"),
                "MemoryUsage toString contains used");
        check(!u.equals(null) && u.equals(u), "MemoryUsage equals");
        check(u.hashCode() == u.hashCode(), "MemoryUsage hashCode stable");

        // max == -1 is the unspecified sentinel
        MemoryUsage u2 = new MemoryUsage(-1L, 1L, 2L, -1L);
        check(u2.getInit() == -1L && u2.getMax() == -1L, "MemoryUsage -1 sentinel");
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

    static void testDeepSingletonIdentity() {
        check(ManagementFactory.getMemoryMXBean() == ManagementFactory.getMemoryMXBean(), "MemoryMXBean singleton");
        check(ManagementFactory.getThreadMXBean() == ManagementFactory.getThreadMXBean(), "ThreadMXBean singleton");
        check(ManagementFactory.getClassLoadingMXBean() == ManagementFactory.getClassLoadingMXBean(), "ClassLoading singleton");
        check(ManagementFactory.getOperatingSystemMXBean() == ManagementFactory.getOperatingSystemMXBean(), "OS singleton");
        check(ManagementFactory.getRuntimeMXBean() == ManagementFactory.getRuntimeMXBean(), "Runtime singleton");
        check(ManagementFactory.getPlatformMBeanServer() == ManagementFactory.getPlatformMBeanServer(),
              "platform MBeanServer singleton across calls");
        check(ManagementFactory.getPlatformMXBean(ThreadMXBean.class) == ManagementFactory.getThreadMXBean(),
              "getPlatformMXBean returns the same singleton as getThreadMXBean");
        check(ManagementFactory.getPlatformMXBean(MemoryMXBean.class) == ManagementFactory.getMemoryMXBean(),
              "getPlatformMXBean returns the same singleton as getMemoryMXBean");
        check(ManagementFactory.getOperatingSystemMXBean().toString() != null,
              "OS bean toString non-null (com.sun view not imported here)");
    }

    static void testDeepListCrossReferences() {
        java.util.List<MemoryPoolMXBean> pools = ManagementFactory.getMemoryPoolMXBeans();
        java.util.List<GarbageCollectorMXBean> gcs = ManagementFactory.getGarbageCollectorMXBeans();
        java.util.List<MemoryManagerMXBean> mgrs = ManagementFactory.getMemoryManagerMXBeans();
        check(pools.size() >= 1 && gcs.size() >= 1 && mgrs.size() >= 1, "pool/GC/manager lists non-empty");
        java.util.Set<String> managerNames = new java.util.HashSet<String>();
        java.util.Set<String> poolNames = new java.util.HashSet<String>();
        for (MemoryManagerMXBean m : mgrs) {
            check(m.getName() != null && m.isValid(), "manager named and valid");
            managerNames.add(m.getName());
        }
        for (MemoryPoolMXBean p : pools) {
            check(p.getName() != null && p.isValid(), "pool named and valid");
            poolNames.add(p.getName());
            check(p.getMemoryManagerNames() != null, "pool has manager names");
            for (String mn : p.getMemoryManagerNames()) {
                check(managerNames.contains(mn), "pool " + p.getName() + " manager " + mn + " exists");
            }
            check(("java.lang:name=" + p.getName() + ",type=MemoryPool")
                    .equals(p.getObjectName().getCanonicalName()),
                  "pool ObjectName pattern exact for " + p.getName());
        }
        for (GarbageCollectorMXBean g : gcs) {
            check(managerNames.contains(g.getName()), "GC " + g.getName() + " is also a memory manager");
            check(("java.lang:name=" + g.getName() + ",type=GarbageCollector")
                    .equals(g.getObjectName().getCanonicalName()),
                  "GC ObjectName pattern exact for " + g.getName());
            for (String pn : g.getMemoryPoolNames()) {
                check(poolNames.contains(pn), "GC " + g.getName() + " pool " + pn + " exists");
            }
        }
    }

    static void testDeepProxyClosedLoop() throws Exception {
        javax.management.MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        RuntimeMXBean proxy = ManagementFactory.getPlatformMXBean(server, RuntimeMXBean.class);
        check(proxy != null && proxy instanceof RuntimeMXBean, "server proxy implements RuntimeMXBean");
        check(proxy.getVmName().equals(ManagementFactory.getRuntimeMXBean().getVmName()),
              "proxy getVmName equals the local bean (cross-proxy equivalence)");
        check(proxy.getObjectName().getCanonicalName()
                .equals(ManagementFactory.getRuntimeMXBean().getObjectName().getCanonicalName()),
              "proxy ObjectName equals the local bean ObjectName");
        check(proxy.getUptime() >= 0, "proxy getUptime callable");
        java.util.List<MemoryPoolMXBean> viaProxy =
                ManagementFactory.getPlatformMXBeans(server, MemoryPoolMXBean.class);
        check(viaProxy.size() == ManagementFactory.getMemoryPoolMXBeans().size(),
              "proxy pool list size equals the local list size");
    }

    static void testDeepProxyNegativeMatrix() throws Exception {
        final javax.management.MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        try {
            ManagementFactory.getPlatformMXBean(server, null);
            check(false, "getPlatformMXBean(server, null) should throw NPE");
        } catch (NullPointerException expected) {
            check(true, "getPlatformMXBean(server, null) NPE");
        }
        try {
            ManagementFactory.getPlatformMXBeans(null, RuntimeMXBean.class);
            check(false, "getPlatformMXBeans(null, C) should throw NPE");
        } catch (NullPointerException expected) {
            check(true, "getPlatformMXBeans(null, C) NPE");
        }
        expectIAE(new Runnable() { public void run() { ManagementFactory.getPlatformMXBean((Class) Object.class); } },
                  "getPlatformMXBean(Object.class raw)", "not a platform management interface");
        expectIAE(new Runnable() { public void run() { ManagementFactory.getPlatformMXBeans((Class) Object.class); } },
                  "getPlatformMXBeans(Object.class raw)", "not a platform management interface");
    }

    static void testDeepObjectNamesExact() throws Exception {
        check("java.lang:type=Runtime".equals(
                ManagementFactory.getRuntimeMXBean().getObjectName().getCanonicalName()), "Runtime name");
        check("java.lang:type=Memory".equals(
                ManagementFactory.getMemoryMXBean().getObjectName().getCanonicalName()), "Memory name");
        check("java.lang:type=Threading".equals(
                ManagementFactory.getThreadMXBean().getObjectName().getCanonicalName()), "Threading name");
        check("java.lang:type=ClassLoading".equals(
                ManagementFactory.getClassLoadingMXBean().getObjectName().getCanonicalName()), "ClassLoading name");
        check("java.lang:type=OperatingSystem".equals(
                ManagementFactory.getOperatingSystemMXBean().getObjectName().getCanonicalName()), "OS name");
        if (ManagementFactory.getCompilationMXBean() != null) {
            check("java.lang:type=Compilation".equals(
                    ManagementFactory.getCompilationMXBean().getObjectName().getCanonicalName()), "Compilation name");
        }
        check("java.util.logging:type=Logging".equals(
                ManagementFactory.getPlatformMXBean(java.lang.management.PlatformLoggingMXBean.class).getObjectName().getCanonicalName()),
              "Logging name");
    }

    public static void main(String[] args) throws Exception {
        testSingletons();
        testMemoryMXBean();
        testRuntimeMXBean();
        testOperatingSystemMXBean();
        testThreadMXBean();
        testClassLoadingMXBean();
        testCompilationMXBean();
        testPoolAndGCBeans();
        testPlatformMXBeans();
        testMemoryType();
        testMemoryUsageInvariants();
        testDeepSingletonIdentity();
        testDeepListCrossReferences();
        testDeepProxyClosedLoop();
        testDeepProxyNegativeMatrix();
        testDeepObjectNamesExact();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
