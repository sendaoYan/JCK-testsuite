/*
 * @test
 * @summary sun.nio.ch.ThreadPool conformance: internal package reflection.
 * @modules java.base/sun.nio.ch:+open
 * @run main ThreadPoolTest
 */
public class ThreadPoolTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.ch.ThreadPool", false, ThreadPoolTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "wrap"), "method wrap");
        check(hasField(c, "DEFAULT_THREAD_POOL_INITIAL_SIZE"), "field DEFAULT_THREAD_POOL_INITIAL_SIZE");
        check(hasField(c, "DEFAULT_THREAD_POOL_THREAD_FACTORY"), "field DEFAULT_THREAD_POOL_THREAD_FACTORY");
        check(hasField(c, "executor"), "field executor");
        check(hasField(c, "isFixed"), "field isFixed");
        check(hasField(c, "poolSize"), "field poolSize");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
