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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
