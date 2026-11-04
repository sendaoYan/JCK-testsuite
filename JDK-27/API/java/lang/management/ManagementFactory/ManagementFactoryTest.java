/*
 * @test
 * @summary java.lang.management.ManagementFactory conformance: white-box structure + black-box behavior.
 * @run main ManagementFactoryTest
 */
import java.lang.management.*;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Set;
import javax.management.MBeanServer;

public class ManagementFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.management.ManagementFactory");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(Modifier.isPrivate(c.getDeclaredConstructor().getModifiers()), "constructor is private");
        check(c.getMethod("getRuntimeMXBean").getReturnType() == RuntimeMXBean.class, "getRuntimeMXBean returns RuntimeMXBean");
        check(c.getMethod("getMemoryMXBean").getReturnType() == MemoryMXBean.class, "getMemoryMXBean returns MemoryMXBean");
        check(c.getMethod("getThreadMXBean").getReturnType() == ThreadMXBean.class, "getThreadMXBean returns ThreadMXBean");
        check(c.getMethod("getOperatingSystemMXBean").getReturnType() == OperatingSystemMXBean.class,
              "getOperatingSystemMXBean returns OperatingSystemMXBean");
        check(c.getMethod("getClassLoadingMXBean").getReturnType() == ClassLoadingMXBean.class,
              "getClassLoadingMXBean returns ClassLoadingMXBean");
        check(c.getMethod("getCompilationMXBean").getReturnType() == CompilationMXBean.class,
              "getCompilationMXBean returns CompilationMXBean");
        check(c.getMethod("getPlatformMBeanServer").getReturnType() == MBeanServer.class,
              "getPlatformMBeanServer returns MBeanServer");
        check(c.getMethod("getPlatformManagementInterfaces").getReturnType() == Set.class,
              "getPlatformManagementInterfaces returns Set");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(ManagementFactory.getRuntimeMXBean() != null, "runtime MXBean non-null");
        check(ManagementFactory.getMemoryMXBean() != null, "memory MXBean non-null");
        check(ManagementFactory.getThreadMXBean() != null, "thread MXBean non-null");
        check(ManagementFactory.getOperatingSystemMXBean() != null, "os MXBean non-null");
        check(ManagementFactory.getClassLoadingMXBean() != null, "classloading MXBean non-null");
        check(ManagementFactory.getCompilationMXBean() != null, "compilation MXBean non-null");

        check(!ManagementFactory.getMemoryPoolMXBeans().isEmpty(), "memory pools non-empty");
        check(!ManagementFactory.getMemoryManagerMXBeans().isEmpty(), "memory managers non-empty");
        check(!ManagementFactory.getGarbageCollectorMXBeans().isEmpty(), "garbage collectors non-empty");

        MBeanServer server1 = ManagementFactory.getPlatformMBeanServer();
        MBeanServer server2 = ManagementFactory.getPlatformMBeanServer();
        check(server1 != null, "platform MBeanServer non-null");
        check(server1 == server2, "platform MBeanServer is a singleton");

        Set<Class<? extends PlatformManagedObject>> ifaces = ManagementFactory.getPlatformManagementInterfaces();
        check(ifaces.contains(RuntimeMXBean.class), "platform interfaces contain RuntimeMXBean");
        check(ifaces.contains(MemoryMXBean.class), "platform interfaces contain MemoryMXBean");
        check(ifaces.contains(ThreadMXBean.class), "platform interfaces contain ThreadMXBean");
        check(ifaces.contains(OperatingSystemMXBean.class), "platform interfaces contain OperatingSystemMXBean");

        check(ManagementFactory.getPlatformMXBean(RuntimeMXBean.class) != null,
              "getPlatformMXBean(RuntimeMXBean) non-null");
        check(ManagementFactory.getPlatformMXBean(MemoryMXBean.class) != null,
              "getPlatformMXBean(MemoryMXBean) non-null");
        check(ManagementFactory.getPlatformMXBean(ThreadMXBean.class) != null,
              "getPlatformMXBean(ThreadMXBean) non-null");
        List<MemoryPoolMXBean> pools = ManagementFactory.getPlatformMXBeans(MemoryPoolMXBean.class);
        check(!pools.isEmpty(), "getPlatformMXBeans(MemoryPoolMXBean) non-empty");

        // proxy roundtrip over the platform MBeanServer
        RuntimeMXBean proxy = ManagementFactory.newPlatformMXBeanProxy(server1,
                ManagementFactory.RUNTIME_MXBEAN_NAME, RuntimeMXBean.class);
        check(proxy.getVmName() != null && !proxy.getVmName().isEmpty(), "proxy getVmName non-empty");

        check(!"".equals(ManagementFactory.RUNTIME_MXBEAN_NAME), "RUNTIME_MXBEAN_NAME non-empty");
        check(!"".equals(ManagementFactory.MEMORY_MXBEAN_NAME), "MEMORY_MXBEAN_NAME non-empty");
        check(!"".equals(ManagementFactory.THREAD_MXBEAN_NAME), "THREAD_MXBEAN_NAME non-empty");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
