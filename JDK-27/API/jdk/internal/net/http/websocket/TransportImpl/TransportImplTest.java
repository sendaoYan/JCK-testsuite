/*
 * @test
 * @summary jdk.internal.net.http.websocket.TransportImpl conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.websocket:+open
 * @run main TransportImplTest
 */
import java.lang.reflect.*;
import java.util.*;
public class TransportImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.websocket.TransportImpl", false, TransportImplTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.websocket.TransportImpl"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("jdk.internal.net.http.websocket.Transport"), "iface:jdk.internal.net.http.websocket.Transport");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("acknowledgeReception"), "method:acknowledgeReception");
        check(methods.contains("closeInput"), "method:closeInput");
        check(methods.contains("closeOutput"), "method:closeOutput");
        check(methods.contains("request"), "method:request");
        check(methods.contains("sendBinary"), "method:sendBinary");
        check(methods.contains("sendClose"), "method:sendClose");
        check(methods.contains("sendPing"), "method:sendPing");
        check(methods.contains("sendPong"), "method:sendPong");
        check(methods.contains("sendText"), "method:sendText");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
