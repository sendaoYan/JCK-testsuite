/*
 * @test
 * @summary jdk.internal.net.http.RequestPublishers.FileChannelPublisher conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http:+open
 * @run main FileChannelPublisherTest
 */
import java.lang.reflect.*;
import java.util.*;
public class FileChannelPublisherTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.RequestPublishers$FileChannelPublisher", false, FileChannelPublisherTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.RequestPublishers$FileChannelPublisher"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "isFinal");
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
        check(methods.contains("subscribe"), "method:subscribe");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
