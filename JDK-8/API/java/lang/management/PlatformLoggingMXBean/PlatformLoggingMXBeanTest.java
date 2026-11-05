/*
 * @test
 * @summary java.lang.management.PlatformLoggingMXBean conformance: logger names.
 * @run main PlatformLoggingMXBeanTest
 */

import java.lang.management.ManagementFactory;
import java.lang.management.PlatformLoggingMXBean;
import java.util.List;

public class PlatformLoggingMXBeanTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testPlatformLoggingMXBean() {
        List<PlatformLoggingMXBean> beans = ManagementFactory.getPlatformMXBeans(
                PlatformLoggingMXBean.class);
        check(beans.size() > 0, "has logging MXBean");
        PlatformLoggingMXBean p = beans.get(0);
        check(p.getLoggerNames() != null, "getLoggerNames");
        check(p.getObjectName() != null, "getObjectName");
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
        check(ManagementFactory.getPlatformMXBean(PlatformLoggingMXBean.class)
                == ManagementFactory.getPlatformMXBean(PlatformLoggingMXBean.class),
              "logging MXBean is a per-JVM singleton");
    }

    static void testDeepLevelRoundtripCrossApi() {
        String name = "jck.deep.logging.Target";
        java.util.logging.Logger logger = java.util.logging.Logger.getLogger(name);
        PlatformLoggingMXBean b = ManagementFactory.getPlatformMXBean(PlatformLoggingMXBean.class);
        String[] levels = {"SEVERE", "WARNING", "INFO", "CONFIG", "FINE", "FINER", "FINEST"};
        for (String level : levels) {
            b.setLoggerLevel(name, level);
            check(level.equals(b.getLoggerLevel(name)), "round-trip level " + level);
            // real closed loop: the platform bean drives java.util.logging directly
            check(java.util.logging.Level.parse(level).equals(logger.getLevel()),
                  "java.util.logging sees the same level " + level);
        }
        // root logger is always present under the empty name
        check(b.getLoggerNames().contains(""), "logger names include the root logger (empty name)");
        check(b.getLoggerNames().contains(name), "logger names include the created logger");
    }

    static void testDeepNullLevelInheritsParent() {
        String name = "jck.deep.logging.Inherit";
        java.util.logging.Logger logger = java.util.logging.Logger.getLogger(name);
        java.util.logging.Logger.getLogger("jck.deep"); // ensure the parent exists
        PlatformLoggingMXBean b = ManagementFactory.getPlatformMXBean(PlatformLoggingMXBean.class);
        b.setLoggerLevel(name, "INFO");
        check("INFO".equals(b.getLoggerLevel(name)), "pre-state INFO");
        b.setLoggerLevel(name, null); // null = inherit from parent (setLevel(null))
        check("".equals(b.getLoggerLevel(name)),
              "null level reports as empty string (inherited), got '" + b.getLoggerLevel(name) + "'");
        check(logger.getLevel() == null, "java.util.logging level is null (inherit)");
        check("jck.deep".equals(b.getParentLoggerName(name)), "parent name resolves");
        check("".equals(b.getParentLoggerName("jck.deep")),
              "top-level logger's parent is the root (empty name)");
        check("".equals(b.getParentLoggerName("")), "root's parent is the root (empty name)");
        check(b.getParentLoggerName("no.such.logger.zzz") == null,
              "parent of an unknown logger is null");
        check(b.getLoggerLevel("no.such.logger.zzz") == null, "level of an unknown logger is null");
    }

    static void testDeepNegativeMatrix() {
        final PlatformLoggingMXBean b = ManagementFactory.getPlatformMXBean(PlatformLoggingMXBean.class);
        expectIAE(new Runnable() { public void run() { b.setLoggerLevel("jck.deep.logging.Target", "BOGUSLEVEL"); } },
                  "setLoggerLevel(BOGUSLEVEL)", "BOGUSLEVEL");
        expectNPE(new Runnable() { public void run() { b.setLoggerLevel(null, "INFO"); } },
                  "setLoggerLevel(null, ..)", "loggerName");
        expectNPE(new Runnable() { public void run() { b.getParentLoggerName(null); } },
                  "getParentLoggerName(null)");
    }

    static void testDeepObjectNameExact() {
        PlatformLoggingMXBean b = ManagementFactory.getPlatformMXBean(PlatformLoggingMXBean.class);
        check("java.util.logging:type=Logging".equals(b.getObjectName().getCanonicalName()),
              "LoggingMXBean ObjectName exact, got " + b.getObjectName());
    }

    public static void main(String[] args) throws Exception {
        testPlatformLoggingMXBean();
        testDeepSingletonIdentity();
        testDeepLevelRoundtripCrossApi();
        testDeepNullLevelInheritsParent();
        testDeepNegativeMatrix();
        testDeepObjectNameExact();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
