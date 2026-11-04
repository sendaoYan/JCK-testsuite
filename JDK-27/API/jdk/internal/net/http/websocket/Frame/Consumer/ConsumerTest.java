/*
 * @test
 * @summary jdk.internal.net.http.websocket.Frame.Consumer conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.websocket:+open
 * @run main ConsumerTest
 */
import java.lang.reflect.*;
import java.util.*;
public class ConsumerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.websocket.Frame$Consumer", false, ConsumerTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.websocket.Frame$Consumer"), "name");
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
        check(methods.contains("endFrame"), "method:endFrame");
        check(methods.contains("fin"), "method:fin");
        check(methods.contains("mask"), "method:mask");
        check(methods.contains("maskingKey"), "method:maskingKey");
        check(methods.contains("opcode"), "method:opcode");
        check(methods.contains("payloadData"), "method:payloadData");
        check(methods.contains("payloadLen"), "method:payloadLen");
        check(methods.contains("rsv1"), "method:rsv1");
        check(methods.contains("rsv2"), "method:rsv2");
        check(methods.contains("rsv3"), "method:rsv3");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
