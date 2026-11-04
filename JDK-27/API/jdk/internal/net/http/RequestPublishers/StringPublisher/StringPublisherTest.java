/*
 * @test
 * @summary jdk.internal.net.http.RequestPublishers.StringPublisher conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http:+open
 * @run main StringPublisherTest
 */
import java.lang.reflect.*;
import java.util.*;
public class StringPublisherTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.RequestPublishers$StringPublisher", false, StringPublisherTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.RequestPublishers$StringPublisher"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("jdk.internal.net.http.RequestPublishers$ByteArrayPublisher"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
