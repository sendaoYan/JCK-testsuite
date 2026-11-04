/*
 * @test
 * @summary java.util.logging.LoggingMXBean conformance: white-box structure + black-box behavior.
 * @run main LoggingMXBeanTest
 */

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.LoggingMXBean;

public class LoggingMXBeanTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void expectThrows(Class<? extends Throwable> type, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + ": expected " + type.getSimpleName() +
                    " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + type.getSimpleName() + " but none thrown");
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.logging.LoggingMXBean");
        check(c.isInterface(), "LoggingMXBean is an interface");
        check(Modifier.isPublic(c.getModifiers()), "public interface");
        check(c.isAnnotationPresent(Deprecated.class), "deprecated since 9");
        check(c.getMethod("getLoggerNames") != null, "getLoggerNames()");
        check(c.getMethod("getLoggerLevel", String.class) != null, "getLoggerLevel(String)");
        check(c.getMethod("setLoggerLevel", String.class, String.class) != null, "setLoggerLevel(String,String)");
        check(c.getMethod("getParentLoggerName", String.class) != null, "getParentLoggerName(String)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        LoggingMXBean mx = LogManager.getLoggingMXBean();
        check(mx != null, "LogManager.getLoggingMXBean non-null");

        List<String> names = mx.getLoggerNames();
        check(names != null, "getLoggerNames non-null");
        check(!names.isEmpty(), "getLoggerNames non-empty");
        check(names.contains("global"), "getLoggerNames contains global");

        // read level of an existing named logger (create parent first so the
        // parent-child hierarchy is deterministic)
        Logger.getLogger("jck.mxbean");
        Logger lg = Logger.getLogger("jck.mxbean.test");
        lg.setLevel(Level.INFO);
        check("INFO".equals(mx.getLoggerLevel("jck.mxbean.test")), "getLoggerLevel INFO");

        // set level via MXBean and observe the logger
        mx.setLoggerLevel("jck.mxbean.test", "WARNING");
        check("WARNING".equals(mx.getLoggerLevel("jck.mxbean.test")), "setLoggerLevel round-trip");
        check(Logger.getLogger("jck.mxbean.test").getLevel() == Level.WARNING,
                "MXBean set level visible on Logger");

        // set level to null -> inherit (empty string level name)
        mx.setLoggerLevel("jck.mxbean.test", null);
        check("".equals(mx.getLoggerLevel("jck.mxbean.test")), "null level -> empty string");
        check(Logger.getLogger("jck.mxbean.test").getLevel() == null, "null level -> Logger level null");

        // parent logger name
        check("jck.mxbean".equals(mx.getParentLoggerName("jck.mxbean.test")),
                "getParentLoggerName returns parent");

        // non-existent logger -> null
        check(mx.getLoggerLevel("jck.mxbean.does.not.exist") == null, "unknown logger level null");
        check(mx.getParentLoggerName("jck.mxbean.does.not.exist") == null,
                "unknown logger parent null");

        // error conditions
        expectThrows(NullPointerException.class,
                () -> mx.setLoggerLevel(null, "INFO"), "setLoggerLevel(null name) NPE");
        expectThrows(IllegalArgumentException.class,
                () -> mx.setLoggerLevel("jck.mxbean.does.not.exist", "INFO"),
                "setLoggerLevel unknown logger IAE");
        expectThrows(IllegalArgumentException.class,
                () -> mx.setLoggerLevel("jck.mxbean.test", "BOGUS_LEVEL"),
                "setLoggerLevel unknown level IAE");
    }


    // ===================== 契约深化：接口契约面（getLoggerLevel/setLoggerLevel/getParentLoggerName）反射锁定 =====================
    static void testContracts() throws Exception {
        Class<?> c = Class.forName("java.util.logging.LoggingMXBean");
        check(c.isInterface(), "LoggingMXBean is interface");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getLoggerLevel") && names.contains("setLoggerLevel")
              && names.contains("getParentLoggerName"), "core management ops present");
        // 行为（经反射拿到的实例或直接构建代理均需平台支持；此处验证方法签名返回类型）
        for (java.lang.reflect.Method m : c.getMethods()) {
            if (m.getName().equals("getLoggerLevel")) check(m.getReturnType() == String.class, "getLoggerLevel -> String");
            if (m.getName().equals("getParentLoggerName")) check(m.getReturnType() == String.class, "getParentLoggerName -> String");
            if (m.getName().equals("setLoggerLevel")) check(m.getReturnType() == void.class, "setLoggerLevel -> void");
        }
    }

    public static void main(String[] args) throws Exception {
        try { testContracts(); } catch (Exception e) { throw new RuntimeException(e); }
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
