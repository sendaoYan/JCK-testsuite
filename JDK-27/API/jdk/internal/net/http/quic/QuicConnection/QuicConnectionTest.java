/*
 * @test
 * @summary jdk.internal.net.http.quic.QuicConnection conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.quic:+open
 * @run main QuicConnectionTest
 */
import java.lang.reflect.*;
import java.util.*;
public class QuicConnectionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.quic.QuicConnection", false, QuicConnectionTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.quic.QuicConnection"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("addRemoteStreamListener"), "method:addRemoteStreamListener");
        check(methods.contains("connectionTerminator"), "method:connectionTerminator");
        check(methods.contains("dbgTag"), "method:dbgTag");
        check(methods.contains("getTLSEngine"), "method:getTLSEngine");
        check(methods.contains("handshakeReachedPeer"), "method:handshakeReachedPeer");
        check(methods.contains("isOpen"), "method:isOpen");
        check(methods.contains("localAddress"), "method:localAddress");
        check(methods.contains("localConnectionId"), "method:localConnectionId");
        check(methods.contains("logTag"), "method:logTag");
        check(methods.contains("openNewLocalBidiStream"), "method:openNewLocalBidiStream");
        check(methods.contains("openNewLocalUniStream"), "method:openNewLocalUniStream");
        check(methods.contains("peerAddress"), "method:peerAddress");
        check(methods.contains("quicStreams"), "method:quicStreams");
        check(methods.contains("removeRemoteStreamListener"), "method:removeRemoteStreamListener");
        check(methods.contains("requestSendPing"), "method:requestSendPing");
        check(methods.contains("startHandshake"), "method:startHandshake");
        check(methods.contains("terminationCause"), "method:terminationCause");
        check(methods.contains("uniqueId"), "method:uniqueId");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
