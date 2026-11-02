/*
 * @test
 * @summary jdk.internal.net.http.ResponseSubscribers.HttpResponseInputStream conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http:+open
 * @run main HttpResponseInputStreamTest
 */
import java.lang.reflect.*;
import java.util.*;
public class HttpResponseInputStreamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.ResponseSubscribers$HttpResponseInputStream", false, HttpResponseInputStreamTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.ResponseSubscribers$HttpResponseInputStream"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("java.io.InputStream"), "superclass");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("jdk.internal.net.http.ResponseSubscribers$TrustedSubscriber"), "iface:jdk.internal.net.http.ResponseSubscribers$TrustedSubscriber");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("available"), "method:available");
        check(methods.contains("close"), "method:close");
        check(methods.contains("getBody"), "method:getBody");
        check(methods.contains("onComplete"), "method:onComplete");
        check(methods.contains("onError"), "method:onError");
        check(methods.contains("onNext"), "method:onNext");
        check(methods.contains("onSubscribe"), "method:onSubscribe");
        check(methods.contains("read"), "method:read");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
