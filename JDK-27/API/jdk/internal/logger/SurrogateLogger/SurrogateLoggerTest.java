/*
 * @test
 * @summary jdk.internal.logger.SurrogateLogger conformance: internal package reflection.
 * @modules java.base/jdk.internal.logger:+open
 * @run main SurrogateLoggerTest
 */
public class SurrogateLoggerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.logger.SurrogateLogger", false, SurrogateLoggerTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("jdk.internal.logger.SimpleConsoleLogger"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "getSimpleFormat"), "method getSimpleFormat");
        check(hasMethod(c, "isFilteredFrame"), "method isFilteredFrame");
        check(hasMethod(c, "makeSurrogateLogger"), "method makeSurrogateLogger");
        check(hasField(c, "JUL_DEFAULT_LEVEL"), "field JUL_DEFAULT_LEVEL");
        check(hasField(c, "simpleFormatString"), "field simpleFormatString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
