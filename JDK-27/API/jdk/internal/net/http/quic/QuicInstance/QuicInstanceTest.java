/*
 * @test
 * @summary jdk.internal.net.http.quic.QuicInstance conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.quic:+open
 * @run main QuicInstanceTest
 */
import java.lang.reflect.*;
import java.util.*;
public class QuicInstanceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.quic.QuicInstance", false, QuicInstanceTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.quic.QuicInstance"), "name");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() == null, "superclass-null");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("appErrorToString"), "method:appErrorToString");
        check(methods.contains("executor"), "method:executor");
        check(methods.contains("getAvailableVersions"), "method:getAvailableVersions");
        check(methods.contains("getEndpoint"), "method:getEndpoint");
        check(methods.contains("getQuicTLSContext"), "method:getQuicTLSContext");
        check(methods.contains("getReceiveBufferSize"), "method:getReceiveBufferSize");
        check(methods.contains("getSSLParameters"), "method:getSSLParameters");
        check(methods.contains("getSendBufferSize"), "method:getSendBufferSize");
        check(methods.contains("getTransportParameters"), "method:getTransportParameters");
        check(methods.contains("instanceId"), "method:instanceId");
        check(methods.contains("isVersionAvailable"), "method:isVersionAvailable");
        check(methods.contains("name"), "method:name");
        check(methods.contains("unmatchedQuicPacket"), "method:unmatchedQuicPacket");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
