/*
 * @test
 * @summary sun.util.logging.PlatformLogger.Bridge conformance: internal package reflection.
 * @modules java.base/sun.util.logging:+open
 * @run main BridgeTest
 */
public class BridgeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.logging.PlatformLogger$Bridge", false, BridgeTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "superclass null");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "convert"), "method convert");
        check(hasMethod(c, "getName"), "method getName");
        check(hasMethod(c, "isEnabled"), "method isEnabled");
        check(hasMethod(c, "isLoggable"), "method isLoggable");
        check(hasMethod(c, "log"), "method log");
        check(hasMethod(c, "logp"), "method logp");
        check(hasMethod(c, "logrb"), "method logrb");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
