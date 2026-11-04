/*
 * @test
 * @summary jdk.internal.logger.SimpleConsoleLogger conformance: internal package reflection.
 * @modules java.base/jdk.internal.logger:+open
 * @run main SimpleConsoleLoggerTest
 */
public class SimpleConsoleLoggerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.logger.SimpleConsoleLogger", false, SimpleConsoleLoggerTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.util.logging.PlatformLogger$ConfigurableBridge$LoggerConfiguration"), "superclass");
        check(c.getInterfaces().length == 3, "interface count");
        check(hasInterface(c, "java.lang.System$Logger"), "interface java.lang.System$Logger");
        check(hasInterface(c, "sun.util.logging.PlatformLogger$Bridge"), "interface sun.util.logging.PlatformLogger$Bridge");
        check(hasInterface(c, "sun.util.logging.PlatformLogger$ConfigurableBridge"), "interface sun.util.logging.PlatformLogger$ConfigurableBridge");
        check(hasMethod(c, "getLoggerConfiguration"), "method getLoggerConfiguration");
        check(hasMethod(c, "getName"), "method getName");
        check(hasMethod(c, "getPlatformLevel"), "method getPlatformLevel");
        check(hasMethod(c, "isEnabled"), "method isEnabled");
        check(hasMethod(c, "isLoggable"), "method isLoggable");
        check(hasMethod(c, "log"), "method log");
        check(hasMethod(c, "logp"), "method logp");
        check(hasMethod(c, "logrb"), "method logrb");
        check(hasMethod(c, "makeSimpleLogger"), "method makeSimpleLogger");
        check(hasMethod(c, "setPlatformLevel"), "method setPlatformLevel");
        check(hasField(c, "DEFAULT_LEVEL"), "field DEFAULT_LEVEL");
        check(hasField(c, "DEFAULT_PLATFORM_LEVEL"), "field DEFAULT_PLATFORM_LEVEL");
        check(hasField(c, "level"), "field level");
        check(hasField(c, "name"), "field name");
        check(hasField(c, "usePlatformLevel"), "field usePlatformLevel");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
