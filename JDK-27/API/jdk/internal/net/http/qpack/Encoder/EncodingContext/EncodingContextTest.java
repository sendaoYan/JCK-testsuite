/*
 * @test
 * @summary jdk.internal.net.http.qpack.Encoder.EncodingContext conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.qpack:+open
 * @run main EncodingContextTest
 */
import java.lang.reflect.*;
import java.util.*;
public class EncodingContextTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.qpack.Encoder$EncodingContext", false, EncodingContextTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.qpack.Encoder$EncodingContext"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("java.lang.AutoCloseable"), "iface:java.lang.AutoCloseable");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("close"), "method:close");
        check(methods.contains("evictionLimit"), "method:evictionLimit");
        check(methods.contains("referenceEntry"), "method:referenceEntry");
        check(methods.contains("registerSessionReference"), "method:registerSessionReference");
        check(methods.contains("sectionPrefix"), "method:sectionPrefix");
        check(methods.contains("tryInsertEntry"), "method:tryInsertEntry");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
