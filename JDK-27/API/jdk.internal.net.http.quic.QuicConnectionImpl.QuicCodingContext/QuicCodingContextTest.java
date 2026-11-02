/*
 * @test
 * @summary jdk.internal.net.http.quic.QuicConnectionImpl.QuicCodingContext conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.quic:+open
 * @run main QuicCodingContextTest
 */
import java.lang.reflect.*;
import java.util.*;
public class QuicCodingContextTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.quic.QuicConnectionImpl$QuicCodingContext", false, QuicCodingContextTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.quic.QuicConnectionImpl$QuicCodingContext"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("jdk.internal.net.http.quic.CodingContext"), "iface:jdk.internal.net.http.quic.CodingContext");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("connectionIdLength"), "method:connectionIdLength");
        check(methods.contains("getTLSEngine"), "method:getTLSEngine");
        check(methods.contains("largestAckedPN"), "method:largestAckedPN");
        check(methods.contains("largestProcessedPN"), "method:largestProcessedPN");
        check(methods.contains("originalServerConnId"), "method:originalServerConnId");
        check(methods.contains("parsePacket"), "method:parsePacket");
        check(methods.contains("verifyToken"), "method:verifyToken");
        check(methods.contains("writePacket"), "method:writePacket");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
