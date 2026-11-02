/*
 * @test
 * @summary jdk.internal.net.http.ResponseSubscribers.TrustedSubscriber conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http:+open
 * @run main TrustedSubscriberTest
 */
import java.lang.reflect.*;
import java.util.*;
public class TrustedSubscriberTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.ResponseSubscribers$TrustedSubscriber", false, TrustedSubscriberTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.ResponseSubscribers$TrustedSubscriber"), "name");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() == null, "superclass-null");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("java.net.http.HttpResponse$BodySubscriber"), "iface:java.net.http.HttpResponse$BodySubscriber");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("needsExecutor"), "method:needsExecutor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
