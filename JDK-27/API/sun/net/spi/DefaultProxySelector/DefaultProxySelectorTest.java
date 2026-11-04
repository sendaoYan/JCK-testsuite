/*
 * @test
 * @summary sun.net.spi.DefaultProxySelector conformance: internal package reflection.
 * @modules java.base/sun.net.spi:+open
 * @run main DefaultProxySelectorTest
 */
public class DefaultProxySelectorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.spi.DefaultProxySelector", false, DefaultProxySelectorTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.net.ProxySelector"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "connectFailed"), "method connectFailed");
        check(hasMethod(c, "select"), "method select");
        check(hasMethod(c, "socksProxyVersion"), "method socksProxyVersion");
        check(hasField(c, "NO_PROXY_LIST"), "field NO_PROXY_LIST");
        check(hasField(c, "SOCKS_PROXY_VERSION"), "field SOCKS_PROXY_VERSION");
        check(hasField(c, "hasSystemProxies"), "field hasSystemProxies");
        check(hasField(c, "props"), "field props");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
