/*
 * @test
 * @summary jdk.internal.net.http.RequestPublishers.FilePublisher conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http:+open
 * @run main FilePublisherTest
 */
import java.lang.reflect.*;
import java.util.*;
public class FilePublisherTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.RequestPublishers$FilePublisher", false, FilePublisherTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.RequestPublishers$FilePublisher"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("java.net.http.HttpRequest$BodyPublisher"), "iface:java.net.http.HttpRequest$BodyPublisher");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("contentLength"), "method:contentLength");
        check(methods.contains("create"), "method:create");
        check(methods.contains("subscribe"), "method:subscribe");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
