/*
 * @test
 * @summary sun.util.logging.PlatformLogger conformance: internal package reflection.
 * @modules java.base/sun.util.logging:+open
 * @run main PlatformLoggerTest
 */
public class PlatformLoggerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.logging.PlatformLogger", false, PlatformLoggerTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "config"), "method config");
        check(hasMethod(c, "fine"), "method fine");
        check(hasMethod(c, "finer"), "method finer");
        check(hasMethod(c, "finest"), "method finest");
        check(hasMethod(c, "getLogger"), "method getLogger");
        check(hasMethod(c, "getName"), "method getName");
        check(hasMethod(c, "info"), "method info");
        check(hasMethod(c, "isEnabled"), "method isEnabled");
        check(hasMethod(c, "isLoggable"), "method isLoggable");
        check(hasMethod(c, "level"), "method level");
        check(hasMethod(c, "setLevel"), "method setLevel");
        check(hasMethod(c, "severe"), "method severe");
        check(hasMethod(c, "toPlatformLevel"), "method toPlatformLevel");
        check(hasMethod(c, "warning"), "method warning");
        check(hasField(c, "$assertionsDisabled"), "field $assertionsDisabled");
        check(hasField(c, "loggerProxy"), "field loggerProxy");
        check(hasField(c, "loggers"), "field loggers");
        check(hasField(c, "spi2platformLevelMapping"), "field spi2platformLevelMapping");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
