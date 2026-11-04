/*
 * @test
 * @summary jdk.internal.net.http.quic.CodingContext conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.quic:+open
 * @run main CodingContextTest
 */
import java.lang.reflect.*;
import java.util.*;
public class CodingContextTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.quic.CodingContext", false, CodingContextTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.quic.CodingContext"), "name");
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
        check(methods.contains("connectionIdLength"), "method:connectionIdLength");
        check(methods.contains("getTLSEngine"), "method:getTLSEngine");
        check(methods.contains("largestAckedPN"), "method:largestAckedPN");
        check(methods.contains("largestProcessedPN"), "method:largestProcessedPN");
        check(methods.contains("minShortPacketPayloadSize"), "method:minShortPacketPayloadSize");
        check(methods.contains("originalServerConnId"), "method:originalServerConnId");
        check(methods.contains("parsePacket"), "method:parsePacket");
        check(methods.contains("verifyToken"), "method:verifyToken");
        check(methods.contains("writePacket"), "method:writePacket");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
