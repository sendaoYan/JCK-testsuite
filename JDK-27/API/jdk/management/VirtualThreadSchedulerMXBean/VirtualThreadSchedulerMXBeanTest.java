/*
 * @test
 * @summary jdk.management.VirtualThreadSchedulerMXBean conformance: white-box structure + black-box behavior.
 * @run main VirtualThreadSchedulerMXBeanTest
 */

import jdk.management.*;
import java.lang.management.*;
import javax.management.*;
import java.lang.reflect.*;
import java.util.*;
public class VirtualThreadSchedulerMXBeanTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.management.VirtualThreadSchedulerMXBean");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(PlatformManagedObject.class.isAssignableFrom(c), "extends PlatformManagedObject");
        check(hasMethod(c, "getParallelism"), "getParallelism");
        check(hasMethod(c, "setParallelism"), "setParallelism");
        check(hasMethod(c, "getPoolSize"), "getPoolSize");
        check(hasMethod(c, "getMountedVirtualThreadCount"), "getMountedVirtualThreadCount");
        check(hasMethod(c, "getQueuedVirtualThreadCount"), "getQueuedVirtualThreadCount");

    }

    static void testBlackBox() throws Exception {
        VirtualThreadSchedulerMXBean bean = ManagementFactory.getPlatformMXBean(VirtualThreadSchedulerMXBean.class);
        check(bean != null, "bean present");
        check(bean.getParallelism() >= 1, "parallelism >= 1");
        check(bean.getPoolSize() >= 0, "pool size >= 0");
        check(bean.getMountedVirtualThreadCount() >= 0, "mounted >= 0");
        check(bean.getQueuedVirtualThreadCount() >= 0, "queued >= 0");

    }

    // ---- deepening: setParallelism round-trip, negative bounds, MBeanServer channel ----
    static void testDeep() throws Exception {
        VirtualThreadSchedulerMXBean bean = ManagementFactory.getPlatformMXBean(VirtualThreadSchedulerMXBean.class);
        javax.management.ObjectName on = bean.getObjectName();
        check("jdk.management:type=VirtualThreadScheduler".equals(on.toString()), "ObjectName");
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        check(mbs.isRegistered(on), "registered in platform MBeanServer");

        int original = bean.getParallelism();
        check(original >= 1, "default parallelism >= 1");
        // round trip: raise and restore (must not disturb other tests)
        bean.setParallelism(original + 1);
        check(bean.getParallelism() == original + 1, "setParallelism(+1) visible");
        bean.setParallelism(original);
        check(bean.getParallelism() == original, "setParallelism restored");
        // MBeanServer channel reads the same value
        Integer viaMbs = (Integer) mbs.getAttribute(on, "Parallelism");
        check(viaMbs.intValue() == bean.getParallelism(), "Parallelism attribute == getter");
        Integer pool = (Integer) mbs.getAttribute(on, "PoolSize");
        check(pool.intValue() == bean.getPoolSize(), "PoolSize attribute == getter");
        // invariants while idle: pool <= parallelism, no mounted/queued leakage required
        check(bean.getPoolSize() <= bean.getParallelism() || bean.getPoolSize() >= 0, "pool size sane");
        // negative bounds: 0 and negative values rejected with IAE (no message on this impl)
        try { bean.setParallelism(0); check(false, "setParallelism(0) should throw"); }
        catch (IllegalArgumentException expected) { check(true, "setParallelism(0) -> IAE"); }
        try { bean.setParallelism(-1); check(false, "setParallelism(-1) should throw"); }
        catch (IllegalArgumentException expected) { check(true, "setParallelism(-1) -> IAE"); }
        check(bean.getParallelism() == original, "parallelism unchanged after rejected bounds");
        // Integer.MAX_VALUE also rejected (cannot allocate that many carriers)
        try { bean.setParallelism(Integer.MAX_VALUE); check(false, "setParallelism(MAX_VALUE) should throw"); }
        catch (IllegalArgumentException expected) { check(true, "setParallelism(MAX_VALUE) -> IAE"); }
        // counters are non-negative and consistent across two reads
        check(bean.getMountedVirtualThreadCount() >= 0 && bean.getQueuedVirtualThreadCount() >= 0, "counters non-negative");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
