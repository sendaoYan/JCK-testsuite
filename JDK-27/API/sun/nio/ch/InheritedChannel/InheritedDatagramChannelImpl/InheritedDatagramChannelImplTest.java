/*
 * @test
 * @requires os.family == "linux"
 * @summary sun.nio.ch.InheritedChannel.InheritedDatagramChannelImpl conformance: internal package reflection.
 * @modules java.base/sun.nio.ch:+open
 * @run main InheritedDatagramChannelImplTest
 */
public class InheritedDatagramChannelImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.ch.InheritedChannel$InheritedDatagramChannelImpl", false, InheritedDatagramChannelImplTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.nio.ch.DatagramChannelImpl"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "bind"), "method bind");
        check(hasMethod(c, "connect"), "method connect");
        check(hasMethod(c, "disconnect"), "method disconnect");
        check(hasMethod(c, "getFD"), "method getFD");
        check(hasMethod(c, "getFDVal"), "method getFDVal");
        check(hasMethod(c, "getLocalAddress"), "method getLocalAddress");
        check(hasMethod(c, "getOption"), "method getOption");
        check(hasMethod(c, "getRemoteAddress"), "method getRemoteAddress");
        check(hasMethod(c, "implCloseSelectableChannel"), "method implCloseSelectableChannel");
        check(hasMethod(c, "isConnected"), "method isConnected");
        check(hasMethod(c, "join"), "method join");
        check(hasMethod(c, "kill"), "method kill");
        check(hasMethod(c, "park"), "method park");
        check(hasMethod(c, "read"), "method read");
        check(hasMethod(c, "receive"), "method receive");
        check(hasMethod(c, "send"), "method send");
        check(hasMethod(c, "setOption"), "method setOption");
        check(hasMethod(c, "socket"), "method socket");
        check(hasMethod(c, "translateAndSetReadyOps"), "method translateAndSetReadyOps");
        check(hasMethod(c, "translateAndUpdateReadyOps"), "method translateAndUpdateReadyOps");
        check(hasMethod(c, "translateInterestOps"), "method translateInterestOps");
        check(hasMethod(c, "translateReadyOps"), "method translateReadyOps");
        check(hasMethod(c, "write"), "method write");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
