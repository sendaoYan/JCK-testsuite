/*
 * @test
 * @summary jdk.internal.net.http.quic.QuicClient conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.quic:+open
 * @run main QuicClientTest
 */
import java.lang.reflect.*;
import java.util.*;
public class QuicClientTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.quic.QuicClient", false, QuicClientTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.quic.QuicClient"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("java.lang.AutoCloseable"), "iface:java.lang.AutoCloseable");
        check(ifaces.contains("jdk.internal.net.http.quic.QuicInstance"), "iface:jdk.internal.net.http.quic.QuicInstance");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("abort"), "method:abort");
        check(methods.contains("appErrorToString"), "method:appErrorToString");
        check(methods.contains("bindAddress"), "method:bindAddress");
        check(methods.contains("clientId"), "method:clientId");
        check(methods.contains("close"), "method:close");
        check(methods.contains("createConnectionFor"), "method:createConnectionFor");
        check(methods.contains("executor"), "method:executor");
        check(methods.contains("getAvailableVersions"), "method:getAvailableVersions");
        check(methods.contains("getEndpoint"), "method:getEndpoint");
        check(methods.contains("getQuicTLSContext"), "method:getQuicTLSContext");
        check(methods.contains("getSSLParameters"), "method:getSSLParameters");
        check(methods.contains("getTransportParameters"), "method:getTransportParameters");
        check(methods.contains("instanceId"), "method:instanceId");
        check(methods.contains("isVersionAvailable"), "method:isVersionAvailable");
        check(methods.contains("name"), "method:name");
        check(methods.contains("unmatchedQuicPacket"), "method:unmatchedQuicPacket");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
