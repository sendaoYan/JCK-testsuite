/*
 * @test
 * @summary jdk.internal.net.http.websocket.MessageQueue conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.websocket:+open
 * @run main MessageQueueTest
 */
import java.lang.reflect.*;
import java.util.*;
public class MessageQueueTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.websocket.MessageQueue", false, MessageQueueTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.websocket.MessageQueue"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("addBinary"), "method:addBinary");
        check(methods.contains("addClose"), "method:addClose");
        check(methods.contains("addPing"), "method:addPing");
        check(methods.contains("addPong"), "method:addPong");
        check(methods.contains("addText"), "method:addText");
        check(methods.contains("effectiveCapacityOf"), "method:effectiveCapacityOf");
        check(methods.contains("isEmpty"), "method:isEmpty");
        check(methods.contains("peek"), "method:peek");
        check(methods.contains("remove"), "method:remove");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
