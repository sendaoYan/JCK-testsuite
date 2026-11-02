/*
 * @test
 * @summary jdk.internal.net.http.websocket.WebSocketImpl conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.websocket:+open
 * @run main WebSocketImplTest
 */
import java.lang.reflect.*;
import java.util.*;
public class WebSocketImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.websocket.WebSocketImpl", false, WebSocketImplTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.websocket.WebSocketImpl"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("java.net.http.WebSocket"), "iface:java.net.http.WebSocket");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("abort"), "method:abort");
        check(methods.contains("getSubprotocol"), "method:getSubprotocol");
        check(methods.contains("isInputClosed"), "method:isInputClosed");
        check(methods.contains("isOutputClosed"), "method:isOutputClosed");
        check(methods.contains("newInstanceAsync"), "method:newInstanceAsync");
        check(methods.contains("request"), "method:request");
        check(methods.contains("sendBinary"), "method:sendBinary");
        check(methods.contains("sendClose"), "method:sendClose");
        check(methods.contains("sendPing"), "method:sendPing");
        check(methods.contains("sendPong"), "method:sendPong");
        check(methods.contains("sendText"), "method:sendText");
        check(methods.contains("toString"), "method:toString");
        check(methods.contains("transport"), "method:transport");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
