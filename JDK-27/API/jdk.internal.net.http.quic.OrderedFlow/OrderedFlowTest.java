/*
 * @test
 * @summary jdk.internal.net.http.quic.OrderedFlow conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.quic:+open
 * @run main OrderedFlowTest
 */
import java.lang.reflect.*;
import java.util.*;
public class OrderedFlowTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.quic.OrderedFlow", false, OrderedFlowTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.quic.OrderedFlow"), "name");
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
        check(methods.contains("buffered"), "method:buffered");
        check(methods.contains("clear"), "method:clear");
        check(methods.contains("isEmpty"), "method:isEmpty");
        check(methods.contains("offset"), "method:offset");
        check(methods.contains("poll"), "method:poll");
        check(methods.contains("receive"), "method:receive");
        check(methods.contains("size"), "method:size");
        check(methods.contains("slice"), "method:slice");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
