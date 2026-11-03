/*
 * @test
 * @summary java.lang.System.LoggerFinder conformance: white-box structure + black-box behavior.
 * @run main LoggerFinderTest
 */
import java.lang.System.LoggerFinder;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class LoggerFinderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = LoggerFinder.class;
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        for (Constructor<?> ctor : c.getDeclaredConstructors()) {
            check(Modifier.isProtected(ctor.getModifiers()), "constructors are protected");
        }
        Method getLogger = c.getMethod("getLogger", String.class, Module.class);
        check(Modifier.isAbstract(getLogger.getModifiers()), "getLogger(String, Module) is abstract");
        check(Modifier.isPublic(getLogger.getModifiers()), "getLogger is public");
        Method getLocalized = c.getMethod("getLocalizedLogger", String.class, ResourceBundle.class, Module.class);
        check(!Modifier.isAbstract(getLocalized.getModifiers()), "getLocalizedLogger is concrete");
        Method finder = c.getMethod("getLoggerFinder");
        check(Modifier.isStatic(finder.getModifiers()) && Modifier.isPublic(finder.getModifiers()),
              "getLoggerFinder() is public static");
    }

    // ---- Part 2: black-box (behavioral) ----
    static class RecordingFinder extends System.LoggerFinder {
        String lastName;
        Module lastModule;
        @Override
        public System.Logger getLogger(String name, Module module) {
            lastName = name;
            lastModule = module;
            return System.getLogger(name);
        }
    }

    static void testSubclass() {
        RecordingFinder finder = new RecordingFinder();
        Module m = LoggerFinderTest.class.getModule();
        System.Logger logger = finder.getLogger("jck-finder", m);
        check(logger != null, "getLogger returns a logger");
        check("jck-finder".equals(finder.lastName), "getLogger received the name");
        check(finder.lastModule == m, "getLogger received the module");
        logger.log(System.Logger.Level.INFO, "hello via subclass finder");
        passed++;
    }

    static void testLocalized() {
        RecordingFinder finder = new RecordingFinder();
        System.Logger localized = finder.getLocalizedLogger("jck-loc", null, LoggerFinderTest.class.getModule());
        check(localized != null, "getLocalizedLogger wraps getLogger by default");
        check("jck-loc".equals(finder.lastName), "getLocalizedLogger delegates to getLogger");
        localized.log(System.Logger.Level.INFO, "localized message");
        passed++;
    }

    static void testDefaultFinder() {
        System.LoggerFinder def = System.LoggerFinder.getLoggerFinder();
        check(def != null, "getLoggerFinder() returns the installed provider");
        System.Logger logger = def.getLogger("jck-default", LoggerFinderTest.class.getModule());
        check(logger != null, "default provider creates loggers");
        check(logger.getName().equals("jck-default"), "logger carries its name");
        // The JDK default is the JUL bridge; logging through it must not throw.
        logger.log(System.Logger.Level.DEBUG, "msg {0}", "arg");
        logger.log(System.Logger.Level.ERROR, "boom", new RuntimeException("logged-only"));
        passed += 2;
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testSubclass();
        testLocalized();
        testDefaultFinder();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
