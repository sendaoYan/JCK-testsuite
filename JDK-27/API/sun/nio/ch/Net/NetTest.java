/*
 * @test
 * @summary sun.nio.ch.Net conformance: internal package reflection.
 * @modules java.base/sun.nio.ch:+open
 * @run main NetTest
 */
public class NetTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.ch.Net", false, NetTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "accept"), "method accept");
        check(hasMethod(c, "bind"), "method bind");
        check(hasMethod(c, "checkAddress"), "method checkAddress");
        check(hasMethod(c, "localAddress"), "method localAddress");
        check(hasMethod(c, "pollConnect"), "method pollConnect");
        check(hasField(c, "$assertionsDisabled"), "field $assertionsDisabled");
        check(hasField(c, "ANY_LOCAL_INET4ADDRESS"), "field ANY_LOCAL_INET4ADDRESS");
        check(hasField(c, "ANY_LOCAL_INET6ADDRESS"), "field ANY_LOCAL_INET6ADDRESS");
        check(hasField(c, "EXCLUSIVE_BIND"), "field EXCLUSIVE_BIND");
        check(hasField(c, "EXTENDED_OPTIONS"), "field EXTENDED_OPTIONS");
        check(hasField(c, "FAST_LOOPBACK"), "field FAST_LOOPBACK");
        check(hasField(c, "INET4_LOOPBACK_ADDRESS"), "field INET4_LOOPBACK_ADDRESS");
        check(hasField(c, "INET6_LOOPBACK_ADDRESS"), "field INET6_LOOPBACK_ADDRESS");
        check(hasField(c, "IPV6_AVAILABLE"), "field IPV6_AVAILABLE");
        check(hasField(c, "POLLCONN"), "field POLLCONN");
        check(hasField(c, "POLLERR"), "field POLLERR");
        check(hasField(c, "POLLHUP"), "field POLLHUP");
        check(hasField(c, "POLLIN"), "field POLLIN");
        check(hasField(c, "POLLNVAL"), "field POLLNVAL");
        check(hasField(c, "POLLOUT"), "field POLLOUT");
        check(hasField(c, "SHUTDOWN_WRITE_BEFORE_CLOSE"), "field SHUTDOWN_WRITE_BEFORE_CLOSE");
        check(hasField(c, "SHUT_RD"), "field SHUT_RD");
        check(hasField(c, "SHUT_RDWR"), "field SHUT_RDWR");
        check(hasField(c, "SHUT_WR"), "field SHUT_WR");
        check(hasField(c, "SO_REUSEPORT_AVAILABLE"), "field SO_REUSEPORT_AVAILABLE");
        check(hasField(c, "UNSPEC"), "field UNSPEC");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
