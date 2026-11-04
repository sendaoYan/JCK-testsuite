/*
 * @test
 * @summary sun.nio.ch.SelChImpl conformance: internal package reflection.
 * @modules java.base/sun.nio.ch:+open
 * @run main SelChImplTest
 */
public class SelChImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.ch.SelChImpl", false, SelChImplTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "superclass null");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "java.nio.channels.Channel"), "interface java.nio.channels.Channel");
        check(hasMethod(c, "getFD"), "method getFD");
        check(hasMethod(c, "getFDVal"), "method getFDVal");
        check(hasMethod(c, "kill"), "method kill");
        check(hasMethod(c, "park"), "method park");
        check(hasMethod(c, "translateAndSetReadyOps"), "method translateAndSetReadyOps");
        check(hasMethod(c, "translateAndUpdateReadyOps"), "method translateAndUpdateReadyOps");
        check(hasMethod(c, "translateInterestOps"), "method translateInterestOps");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
