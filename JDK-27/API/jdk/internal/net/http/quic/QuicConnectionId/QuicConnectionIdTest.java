/*
 * @test
 * @summary jdk.internal.net.http.quic.QuicConnectionId conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.quic:+open
 * @run main QuicConnectionIdTest
 */
import java.lang.reflect.*;
import java.util.*;
public class QuicConnectionIdTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.quic.QuicConnectionId", false, QuicConnectionIdTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.quic.QuicConnectionId"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("java.lang.Comparable"), "iface:java.lang.Comparable");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("asReadOnlyBuffer"), "method:asReadOnlyBuffer");
        check(methods.contains("compareBytes"), "method:compareBytes");
        check(methods.contains("compareTo"), "method:compareTo");
        check(methods.contains("equals"), "method:equals");
        check(methods.contains("getBytes"), "method:getBytes");
        check(methods.contains("hashCode"), "method:hashCode");
        check(methods.contains("length"), "method:length");
        check(methods.contains("matches"), "method:matches");
        check(methods.contains("toHexString"), "method:toHexString");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("MAX_CONNECTION_ID_LENGTH"), "field:MAX_CONNECTION_ID_LENGTH");
        check(fields.contains("buf"), "field:buf");
        check(fields.contains("hashCode"), "field:hashCode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
