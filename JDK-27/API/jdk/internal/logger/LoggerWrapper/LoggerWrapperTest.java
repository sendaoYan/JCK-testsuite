/*
 * @test
 * @summary jdk.internal.logger.LoggerWrapper conformance: internal package reflection.
 * @modules java.base/jdk.internal.logger:+open
 * @run main LoggerWrapperTest
 */
public class LoggerWrapperTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.logger.LoggerWrapper", false, LoggerWrapperTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("jdk.internal.logger.AbstractLoggerWrapper"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "getLoggerConfiguration"), "method getLoggerConfiguration");
        check(hasMethod(c, "isEnabled"), "method isEnabled");
        check(hasMethod(c, "isLoggable"), "method isLoggable");
        check(hasMethod(c, "log"), "method log");
        check(hasMethod(c, "logp"), "method logp");
        check(hasMethod(c, "logrb"), "method logrb");
        check(hasMethod(c, "platformProxy"), "method platformProxy");
        check(hasMethod(c, "wrapped"), "method wrapped");
        check(hasField(c, "platformProxy"), "field platformProxy");
        check(hasField(c, "wrapped"), "field wrapped");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
