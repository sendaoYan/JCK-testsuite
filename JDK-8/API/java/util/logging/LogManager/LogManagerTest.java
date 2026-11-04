/*
 * @test
 * @summary java.util.logging.LogManager conformance: white-box structure + black-box behavior.
 * @run main LogManagerTest
 */

import java.io.ByteArrayInputStream;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class LogManagerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.logging.LogManager");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        check(!Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(Modifier.isProtected(c.getDeclaredConstructor().getModifiers()), "protected constructor");
        check(Modifier.isStatic(c.getMethod("getLogManager").getModifiers()), "getLogManager static");
        check(Modifier.isStatic(c.getMethod("getLoggingMXBean").getModifiers()), "getLoggingMXBean static");
        String[] methods = {"getLogger","getLoggerNames","addLogger","readConfiguration",
                "reset","getProperty","checkAccess"};
        for (String m : methods) check(hasMethod(c, m), "has method " + m);
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getMethods()) if (m.getName().equals(name)) return true;
        return false;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // global singleton (read-only; never reset)
        LogManager global = LogManager.getLogManager();
        check(global != null, "getLogManager non-null");
        check(LogManager.getLogManager() == global, "getLogManager same singleton");
        check(global.getLoggerNames() != null, "global getLoggerNames non-null");
        check(LogManager.getLoggingMXBean() != null, "getLoggingMXBean non-null");
        global.checkAccess(); // no-op without a SecurityManager

        // isolated LogManager instance (no global pollution)
        LogManager iso = new TestLogManager();

        // a named logger is created via the global manager, then adopted by iso.
        Logger lg = Logger.getLogger("jck.isolated.logger");
        check(iso.addLogger(lg), "addLogger new logger returns true");
        check(!iso.addLogger(lg), "addLogger existing logger returns false");

        // read an isolated InputStream configuration into this instance only
        String cfg = ".level=FINE\njck.isolated.logger.level=WARNING\n";
        iso.readConfiguration(new ByteArrayInputStream(cfg.getBytes(StandardCharsets.UTF_8)));

        check("FINE".equals(iso.getProperty(".level")), "getProperty .level");
        check("WARNING".equals(iso.getProperty("jck.isolated.logger.level")),
                "getProperty logger level");
        check(iso.getLogger("jck.isolated.logger") == lg, "getLogger returns configured logger");
        check(lg.getLevel() == Level.WARNING, "configured level applied to logger");
        check(iso.getLogger("jck.no.such.logger") == null, "unknown logger null");

        // getLoggerNames contains the adopted logger
        boolean found = false;
        Enumeration<String> names = iso.getLoggerNames();
        while (names.hasMoreElements()) {
            if ("jck.isolated.logger".equals(names.nextElement())) { found = true; break; }
        }
        check(found, "getLoggerNames contains logger");

        // reset clears properties and level, but keeps the registered logger
        iso.reset();
        check(iso.getProperty(".level") == null, "reset clears properties");
        check(iso.getLogger("jck.isolated.logger") == lg, "reset keeps registered logger");
        check(lg.getLevel() == null, "reset clears logger level");
    }

    // expose the protected LogManager constructor for an isolated instance
    static final class TestLogManager extends LogManager {
    }


    // ===================== 契约深化：readConfiguration 属性生效与重置/addLogger 唯一性/getProperty 未知 null（含恢复） =====================
    static void testContracts() throws Exception {
        java.util.logging.LogManager lm = java.util.logging.LogManager.getLogManager();
        // 命名空间层次："." 分隔父子（父 logger 先创建，parent 才会解析到最近已注册祖先）
        java.util.logging.Logger p = java.util.logging.Logger.getLogger("jck.logmanager.p");
        java.util.logging.Logger deep = java.util.logging.Logger.getLogger("jck.logmanager.p.q");
        check(deep.getParent() == p, "dot namespace parent linkage");
        String isolate = "jck.logmanager.test.level";
        // 属性经 readConfiguration 生效；空配置重置为默认（null）
        try {
            lm.readConfiguration(new java.io.ByteArrayInputStream(
                    (isolate + "=FINE\n").getBytes("UTF-8")));
            check("FINE".equals(lm.getProperty(isolate)), "readConfiguration applies property");
            lm.readConfiguration(new java.io.ByteArrayInputStream(new byte[0]));
            check(lm.getProperty(isolate) == null, "empty readConfiguration resets property");
            // null 配置 NPE（锁定，不解码消息）
            try {
                lm.readConfiguration(null);
                check(false, "readConfiguration(null) NPE");
            } catch (NullPointerException e) { check(true, "readConfiguration(null) NPE"); }
        } finally {
            // 恢复默认配置，避免污染同 JVM 的其他用例
            lm.readConfiguration(new java.io.ByteArrayInputStream(new byte[0]));
        }
        check(lm.getProperty("no.such.jck.key") == null, "unknown property null");
        // addLogger：同名第二次返回 false 且 getLogger 返回首个
        java.util.logging.Logger a = java.util.logging.Logger.getLogger("jck.logmanager.add");
        check(!lm.addLogger(a), "addLogger existing returns false");
        java.util.logging.Logger b = new java.util.logging.Logger("jck.logmanager.add2", null) {};
        check(lm.addLogger(b), "addLogger new returns true");
        check(!lm.addLogger(b), "re-add same instance false");
        check(lm.getLogger("jck.logmanager.add2") == b, "getLogger from manager");
        check(lm.getLogger("jck.logmanager.absent") == null, "absent logger null");
    }

    public static void main(String[] args) throws Exception {
        try { testContracts(); } catch (Exception e) { throw new RuntimeException(e); }
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
