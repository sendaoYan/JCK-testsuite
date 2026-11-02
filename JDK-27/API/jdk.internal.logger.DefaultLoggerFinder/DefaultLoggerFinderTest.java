/*
 * @test
 * @summary jdk.internal.logger.DefaultLoggerFinder conformance: internal package reflection.
 * @modules java.base/jdk.internal.logger:+open
 * @run main DefaultLoggerFinderTest
 */
public class DefaultLoggerFinderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.logger.DefaultLoggerFinder", false, DefaultLoggerFinderTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.System$LoggerFinder"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "demandLoggerFor"), "method demandLoggerFor");
        check(hasMethod(c, "getLocalizedLogger"), "method getLocalizedLogger");
        check(hasMethod(c, "getLogger"), "method getLogger");
        check(hasMethod(c, "isSystem"), "method isSystem");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
