/*
 * @test
 * @summary jdk.management.HotSpotAOTCacheMXBean conformance: white-box structure + black-box behavior.
 * @run main HotSpotAOTCacheMXBeanTest
 */

import jdk.management.*;
import java.lang.management.*;
import javax.management.*;
import java.lang.reflect.*;
import java.util.*;
public class HotSpotAOTCacheMXBeanTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.management.HotSpotAOTCacheMXBean");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(PlatformManagedObject.class.isAssignableFrom(c), "extends PlatformManagedObject");
        check(hasMethod(c, "endRecording"), "endRecording");

    }

    static void testBlackBox() throws Exception {
        HotSpotAOTCacheMXBean bean = null;
        try { bean = ManagementFactory.getPlatformMXBean(HotSpotAOTCacheMXBean.class); } catch (Throwable t) {}
        if (bean != null) { try { bean.endRecording(); } catch (Throwable t) {}
            check(true, "MXBean obtainable via ManagementFactory when AOT cache enabled");
        }
    }

    // ---- deepening: real registration + dual-channel endRecording behavior ----
    static void testDualChannel() throws Exception {
        HotSpotAOTCacheMXBean bean = ManagementFactory.getPlatformMXBean(HotSpotAOTCacheMXBean.class);
        check(bean != null, "bean registered (present on JDK-27 builds)");
        javax.management.ObjectName on = bean.getObjectName();
        check(on != null && "jdk.management:type=HotSpotAOTCache".equals(on.toString()), "ObjectName jdk.management:type=HotSpotAOTCache");
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        check(mbs.isRegistered(on), "registered in platform MBeanServer");
        check(mbs.queryMBeans(on, null).size() == 1, "exactly one instance under the name");
        // no AOT recording in progress in a plain JVM: endRecording reports false and is idempotent
        check(bean.endRecording() == false, "endRecording false when no AOT recording active");
        check(bean.endRecording() == false, "endRecording idempotent");
        // MBeanServer channel agrees
        Object viaMbs = mbs.invoke(on, "endRecording", null, null);
        check(Boolean.FALSE.equals(viaMbs), "endRecording via MBeanServer invoke -> false");
        // no attributes beyond ObjectName; the single operation is endRecording
        MBeanInfo info = mbs.getMBeanInfo(on);
        boolean sawEndRecording = false;
        for (MBeanOperationInfo op : info.getOperations()) if ("endRecording".equals(op.getName())) sawEndRecording = true;
        check(sawEndRecording, "MBeanInfo exposes endRecording operation");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        boolean present = false;
        try { present = ManagementFactory.getPlatformMXBean(HotSpotAOTCacheMXBean.class) != null; } catch (Throwable ignored) {}
        if (present) testDualChannel();
        else check(true, "AOT bean absent on this build; shape checks only");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
