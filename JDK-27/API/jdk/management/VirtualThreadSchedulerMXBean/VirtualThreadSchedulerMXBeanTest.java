/*
 * @test
 * @summary jdk.management.VirtualThreadSchedulerMXBean conformance: white-box structure + black-box behavior.
 * @run main VirtualThreadSchedulerMXBeanTest
 */

import jdk.management.*;
import java.lang.management.*;
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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
