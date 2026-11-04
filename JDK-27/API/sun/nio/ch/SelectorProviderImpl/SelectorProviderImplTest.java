/*
 * @test
 * @summary sun.nio.ch.SelectorProviderImpl conformance: internal package reflection.
 * @modules java.base/sun.nio.ch:+open
 * @run main SelectorProviderImplTest
 */
public class SelectorProviderImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.ch.SelectorProviderImpl", false, SelectorProviderImplTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.nio.channels.spi.SelectorProvider"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "openDatagramChannel"), "method openDatagramChannel");
        check(hasMethod(c, "openPipe"), "method openPipe");
        check(hasMethod(c, "openSelector"), "method openSelector");
        check(hasMethod(c, "openServerSocketChannel"), "method openServerSocketChannel");
        check(hasMethod(c, "openSocketChannel"), "method openSocketChannel");
        check(hasMethod(c, "openUninterruptibleDatagramChannel"), "method openUninterruptibleDatagramChannel");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
