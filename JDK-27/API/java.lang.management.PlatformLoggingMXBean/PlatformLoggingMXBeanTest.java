/*
 * @test
 * @summary java.lang.management.PlatformLoggingMXBean conformance: white-box structure + black-box behavior.
 * @run main PlatformLoggingMXBeanTest
 */
import java.lang.management.*;
import java.util.List;
import java.util.logging.Logger;

public class PlatformLoggingMXBeanTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.management.PlatformLoggingMXBean");
        check(c.isInterface(), "interface");
        check(PlatformManagedObject.class.isAssignableFrom(c), "extends PlatformManagedObject");
        check(c.getMethod("getLoggerNames").getReturnType() == List.class, "getLoggerNames returns List");
        check(c.getMethod("getLoggerLevel", String.class).getReturnType() == String.class, "getLoggerLevel returns String");
        check(c.getMethod("setLoggerLevel", String.class, String.class).getReturnType() == void.class,
              "setLoggerLevel returns void");
        check(c.getMethod("getParentLoggerName", String.class).getReturnType() == String.class,
              "getParentLoggerName returns String");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        PlatformLoggingMXBean b = ManagementFactory.getPlatformMXBean(PlatformLoggingMXBean.class);
        check(b != null, "non-null");
        check(b.getLoggerNames() != null, "logger names non-null");

        String parent = "jck.platform.logging";
        String name = parent + ".TestLogger";
        Logger.getLogger(parent);   // ensure parent logger exists
        Logger.getLogger(name);     // ensure target logger exists

        check(b.getLoggerNames().contains(name), "logger names contain created logger");
        b.setLoggerLevel(name, "INFO");
        check("INFO".equals(b.getLoggerLevel(name)), "setLoggerLevel/getLoggerLevel roundtrip");
        check(parent.equals(b.getParentLoggerName(name)), "getParentLoggerName resolves parent");
        b.setLoggerLevel(name, null);
        String reset = b.getLoggerLevel(name);
        check(reset == null || "".equals(reset), "level reset clears the level");
        check(b.getObjectName() != null, "objectName non-null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
