/*
 * @test
 * @summary sun.nio.ch.DatagramSocketAdaptor conformance: internal package reflection.
 * @modules java.base/sun.nio.ch:+open
 * @run main DatagramSocketAdaptorTest
 */
public class DatagramSocketAdaptorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.ch.DatagramSocketAdaptor", false, DatagramSocketAdaptorTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.net.MulticastSocket"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "bind"), "method bind");
        check(hasMethod(c, "close"), "method close");
        check(hasMethod(c, "connect"), "method connect");
        check(hasMethod(c, "disconnect"), "method disconnect");
        check(hasMethod(c, "getBroadcast"), "method getBroadcast");
        check(hasMethod(c, "getChannel"), "method getChannel");
        check(hasMethod(c, "getInetAddress"), "method getInetAddress");
        check(hasMethod(c, "getInterface"), "method getInterface");
        check(hasMethod(c, "getLocalAddress"), "method getLocalAddress");
        check(hasMethod(c, "getLocalPort"), "method getLocalPort");
        check(hasMethod(c, "getLocalSocketAddress"), "method getLocalSocketAddress");
        check(hasMethod(c, "getLoopbackMode"), "method getLoopbackMode");
        check(hasMethod(c, "getNetworkInterface"), "method getNetworkInterface");
        check(hasMethod(c, "getOption"), "method getOption");
        check(hasMethod(c, "getPort"), "method getPort");
        check(hasMethod(c, "getReceiveBufferSize"), "method getReceiveBufferSize");
        check(hasMethod(c, "getRemoteSocketAddress"), "method getRemoteSocketAddress");
        check(hasMethod(c, "getReuseAddress"), "method getReuseAddress");
        check(hasMethod(c, "getSendBufferSize"), "method getSendBufferSize");
        check(hasMethod(c, "getSoTimeout"), "method getSoTimeout");
        check(hasMethod(c, "getTimeToLive"), "method getTimeToLive");
        check(hasMethod(c, "getTrafficClass"), "method getTrafficClass");
        check(hasMethod(c, "isBound"), "method isBound");
        check(hasMethod(c, "isClosed"), "method isClosed");
        check(hasMethod(c, "isConnected"), "method isConnected");
        check(hasMethod(c, "joinGroup"), "method joinGroup");
        check(hasMethod(c, "leaveGroup"), "method leaveGroup");
        check(hasMethod(c, "receive"), "method receive");
        check(hasMethod(c, "send"), "method send");
        check(hasMethod(c, "setBroadcast"), "method setBroadcast");
        check(hasMethod(c, "setInterface"), "method setInterface");
        check(hasMethod(c, "setLoopbackMode"), "method setLoopbackMode");
        check(hasMethod(c, "setNetworkInterface"), "method setNetworkInterface");
        check(hasMethod(c, "setOption"), "method setOption");
        check(hasMethod(c, "setReceiveBufferSize"), "method setReceiveBufferSize");
        check(hasMethod(c, "setReuseAddress"), "method setReuseAddress");
        check(hasMethod(c, "setSendBufferSize"), "method setSendBufferSize");
        check(hasMethod(c, "setSoTimeout"), "method setSoTimeout");
        check(hasMethod(c, "setTimeToLive"), "method setTimeToLive");
        check(hasMethod(c, "setTrafficClass"), "method setTrafficClass");
        check(hasMethod(c, "supportedOptions"), "method supportedOptions");
        check(hasField(c, "dc"), "field dc");
        check(hasField(c, "outgoingInetAddress"), "field outgoingInetAddress");
        check(hasField(c, "outgoingInterfaceLock"), "field outgoingInterfaceLock");
        check(hasField(c, "outgoingNetworkInterface"), "field outgoingNetworkInterface");
        check(hasField(c, "timeout"), "field timeout");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
