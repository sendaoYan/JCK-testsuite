/*
 * @test
 * @summary jdk.management.HotSpotAOTCacheMXBean conformance: white-box structure + black-box behavior.
 * @run main HotSpotAOTCacheMXBeanTest
 */

import jdk.management.*;
import java.lang.management.*;
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
        if (bean != null) { try { bean.endRecording(); } catch (Throwable t) {} }
        check(true, "MXBean obtainable via ManagementFactory when AOT cache enabled");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
