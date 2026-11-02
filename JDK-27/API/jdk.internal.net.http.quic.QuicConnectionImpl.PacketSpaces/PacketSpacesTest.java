/*
 * @test
 * @summary jdk.internal.net.http.quic.QuicConnectionImpl.PacketSpaces conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.quic:+open
 * @run main PacketSpacesTest
 */
import java.lang.reflect.*;
import java.util.*;
public class PacketSpacesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.quic.QuicConnectionImpl$PacketSpaces", false, PacketSpacesTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.quic.QuicConnectionImpl$PacketSpaces"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Record"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("app"), "method:app");
        check(methods.contains("close"), "method:close");
        check(methods.contains("equals"), "method:equals");
        check(methods.contains("forConnection"), "method:forConnection");
        check(methods.contains("get"), "method:get");
        check(methods.contains("handshake"), "method:handshake");
        check(methods.contains("hashCode"), "method:hashCode");
        check(methods.contains("initial"), "method:initial");
        check(methods.contains("toString"), "method:toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
