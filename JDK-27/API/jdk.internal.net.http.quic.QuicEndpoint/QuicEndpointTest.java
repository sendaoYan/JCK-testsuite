/*
 * @test
 * @summary jdk.internal.net.http.quic.QuicEndpoint conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.quic:+open
 * @run main QuicEndpointTest
 */
import java.lang.reflect.*;
import java.util.*;
public class QuicEndpointTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.quic.QuicEndpoint", false, QuicEndpointTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.quic.QuicEndpoint"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("java.lang.AutoCloseable"), "iface:java.lang.AutoCloseable");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("abort"), "method:abort");
        check(methods.contains("addConnectionId"), "method:addConnectionId");
        check(methods.contains("buffer"), "method:buffer");
        check(methods.contains("buffered"), "method:buffered");
        check(methods.contains("channel"), "method:channel");
        check(methods.contains("channelType"), "method:channelType");
        check(methods.contains("checkStatelessReset"), "method:checkStatelessReset");
        check(methods.contains("close"), "method:close");
        check(methods.contains("closing"), "method:closing");
        check(methods.contains("connectionCount"), "method:connectionCount");
        check(methods.contains("detach"), "method:detach");
        check(methods.contains("draining"), "method:draining");
        check(methods.contains("getLocalAddress"), "method:getLocalAddress");
        check(methods.contains("getLocalAddressString"), "method:getLocalAddressString");
        check(methods.contains("idFactory"), "method:idFactory");
        check(methods.contains("isChannelClosed"), "method:isChannelClosed");
        check(methods.contains("name"), "method:name");
        check(methods.contains("pushClosedDatagram"), "method:pushClosedDatagram");
        check(methods.contains("pushClosingDatagram"), "method:pushClosingDatagram");
        check(methods.contains("pushDatagram"), "method:pushDatagram");
        check(methods.contains("registerNewConnection"), "method:registerNewConnection");
        check(methods.contains("registerWithSelector"), "method:registerWithSelector");
        check(methods.contains("removeConnectionId"), "method:removeConnectionId");
        check(methods.contains("timeSource"), "method:timeSource");
        check(methods.contains("timer"), "method:timer");
        check(methods.contains("toString"), "method:toString");
        check(methods.contains("unbuffer"), "method:unbuffer");
        check(methods.contains("unmatchedQuicPacket"), "method:unmatchedQuicPacket");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("CONFIGURED_CHANNEL_TYPE"), "field:CONFIGURED_CHANNEL_TYPE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
