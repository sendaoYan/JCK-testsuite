/*
 * @test
 * @summary jdk.internal.net.http.websocket.Transport conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.websocket:+open
 * @run main TransportTest
 */
import java.lang.reflect.*;
import java.util.*;
public class TransportTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.websocket.Transport", false, TransportTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.websocket.Transport"), "name");
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
