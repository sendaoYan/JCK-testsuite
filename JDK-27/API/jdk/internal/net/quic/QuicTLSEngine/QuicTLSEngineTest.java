/*
 * @test
 * @summary jdk.internal.net.quic.QuicTLSEngine conformance: internal package reflection.
 * @modules java.base/jdk.internal.net.quic:+open
 * @run main QuicTLSEngineTest
 */
public class QuicTLSEngineTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.quic.QuicTLSEngine", false, QuicTLSEngineTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "superclass null");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "computeHeaderProtectionMask"), "method computeHeaderProtectionMask");
        check(hasMethod(c, "consumeHandshakeBytes"), "method consumeHandshakeBytes");
        check(hasMethod(c, "decryptPacket"), "method decryptPacket");
        check(hasMethod(c, "deriveInitialKeys"), "method deriveInitialKeys");
        check(hasMethod(c, "discardKeys"), "method discardKeys");
        check(hasMethod(c, "encryptPacket"), "method encryptPacket");
        check(hasMethod(c, "getApplicationProtocol"), "method getApplicationProtocol");
        check(hasMethod(c, "getAuthTagSize"), "method getAuthTagSize");
        check(hasMethod(c, "getCurrentSendKeySpace"), "method getCurrentSendKeySpace");
        check(hasMethod(c, "getDelegatedTask"), "method getDelegatedTask");
        check(hasMethod(c, "getHandshakeBytes"), "method getHandshakeBytes");
        check(hasMethod(c, "getHandshakeSession"), "method getHandshakeSession");
        check(hasMethod(c, "getHandshakeState"), "method getHandshakeState");
        check(hasMethod(c, "getHeaderProtectionSampleSize"), "method getHeaderProtectionSampleSize");
        check(hasMethod(c, "getSSLParameters"), "method getSSLParameters");
        check(hasMethod(c, "getSession"), "method getSession");
        check(hasMethod(c, "getSupportedQuicVersions"), "method getSupportedQuicVersions");
        check(hasMethod(c, "getUseClientMode"), "method getUseClientMode");
        check(hasMethod(c, "isTLSHandshakeComplete"), "method isTLSHandshakeComplete");
        check(hasMethod(c, "keysAvailable"), "method keysAvailable");
        check(hasMethod(c, "restartHandshake"), "method restartHandshake");
        check(hasMethod(c, "setLocalQuicTransportParameters"), "method setLocalQuicTransportParameters");
        check(hasMethod(c, "setOneRttContext"), "method setOneRttContext");
        check(hasMethod(c, "setRemoteQuicTransportParametersConsumer"), "method setRemoteQuicTransportParametersConsumer");
        check(hasMethod(c, "setSSLParameters"), "method setSSLParameters");
        check(hasMethod(c, "setUseClientMode"), "method setUseClientMode");
        check(hasMethod(c, "signRetryPacket"), "method signRetryPacket");
        check(hasMethod(c, "tryMarkHandshakeDone"), "method tryMarkHandshakeDone");
        check(hasMethod(c, "tryReceiveHandshakeDone"), "method tryReceiveHandshakeDone");
        check(hasMethod(c, "verifyRetryPacket"), "method verifyRetryPacket");
        check(hasMethod(c, "versionNegotiated"), "method versionNegotiated");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
