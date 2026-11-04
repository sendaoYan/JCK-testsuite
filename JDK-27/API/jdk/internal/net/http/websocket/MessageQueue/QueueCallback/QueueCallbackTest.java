/*
 * @test
 * @summary jdk.internal.net.http.websocket.MessageQueue.QueueCallback conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.websocket:+open
 * @run main QueueCallbackTest
 */
import java.lang.reflect.*;
import java.util.*;
public class QueueCallbackTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.websocket.MessageQueue$QueueCallback", false, QueueCallbackTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.websocket.MessageQueue$QueueCallback"), "name");
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
        check(methods.contains("onBinary"), "method:onBinary");
        check(methods.contains("onClose"), "method:onClose");
        check(methods.contains("onEmpty"), "method:onEmpty");
        check(methods.contains("onPing"), "method:onPing");
        check(methods.contains("onPong"), "method:onPong");
        check(methods.contains("onText"), "method:onText");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
