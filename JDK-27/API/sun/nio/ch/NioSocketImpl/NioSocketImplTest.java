/*
 * @test
 * @summary sun.nio.ch.NioSocketImpl conformance: internal package reflection.
 * @modules java.base/sun.nio.ch:+open
 * @run main NioSocketImplTest
 */
public class NioSocketImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.ch.NioSocketImpl", false, NioSocketImplTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.net.SocketImpl"), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "sun.net.PlatformSocketImpl"), "interface sun.net.PlatformSocketImpl");
        check(hasMethod(c, "accept"), "method accept");
        check(hasMethod(c, "available"), "method available");
        check(hasMethod(c, "bind"), "method bind");
        check(hasMethod(c, "close"), "method close");
        check(hasMethod(c, "connect"), "method connect");
        check(hasMethod(c, "create"), "method create");
        check(hasMethod(c, "getInputStream"), "method getInputStream");
        check(hasMethod(c, "getOption"), "method getOption");
        check(hasMethod(c, "getOutputStream"), "method getOutputStream");
        check(hasMethod(c, "listen"), "method listen");
        check(hasMethod(c, "sendUrgentData"), "method sendUrgentData");
        check(hasMethod(c, "setOption"), "method setOption");
        check(hasMethod(c, "shutdownInput"), "method shutdownInput");
        check(hasMethod(c, "shutdownOutput"), "method shutdownOutput");
        check(hasMethod(c, "supportedOptions"), "method supportedOptions");
        check(hasMethod(c, "supportsUrgentData"), "method supportsUrgentData");
        check(hasField(c, "$assertionsDisabled"), "field $assertionsDisabled");
        check(hasField(c, "JIOFDA"), "field JIOFDA");
        check(hasField(c, "ST_CLOSED"), "field ST_CLOSED");
        check(hasField(c, "ST_CLOSING"), "field ST_CLOSING");
        check(hasField(c, "ST_CONNECTED"), "field ST_CONNECTED");
        check(hasField(c, "ST_CONNECTING"), "field ST_CONNECTING");
        check(hasField(c, "ST_NEW"), "field ST_NEW");
        check(hasField(c, "ST_UNCONNECTED"), "field ST_UNCONNECTED");
        check(hasField(c, "cleaner"), "field cleaner");
        check(hasField(c, "clientSocketOptions"), "field clientSocketOptions");
        check(hasField(c, "connectionReset"), "field connectionReset");
        check(hasField(c, "isInputClosed"), "field isInputClosed");
        check(hasField(c, "isOutputClosed"), "field isOutputClosed");
        check(hasField(c, "isReuseAddress"), "field isReuseAddress");
        check(hasField(c, "nd"), "field nd");
        check(hasField(c, "nonBlocking"), "field nonBlocking");
        check(hasField(c, "readEOF"), "field readEOF");
        check(hasField(c, "readLock"), "field readLock");
        check(hasField(c, "readerThread"), "field readerThread");
        check(hasField(c, "server"), "field server");
        check(hasField(c, "serverSocketOptions"), "field serverSocketOptions");
        check(hasField(c, "state"), "field state");
        check(hasField(c, "stateLock"), "field stateLock");
        check(hasField(c, "timeout"), "field timeout");
        check(hasField(c, "writeLock"), "field writeLock");
        check(hasField(c, "writerThread"), "field writerThread");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
