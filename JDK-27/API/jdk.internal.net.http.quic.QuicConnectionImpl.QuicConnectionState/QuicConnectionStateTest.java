/*
 * @test
 * @summary jdk.internal.net.http.quic.QuicConnectionImpl.QuicConnectionState conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.quic:+open
 * @run main QuicConnectionStateTest
 */
import java.lang.reflect.*;
import java.util.*;
public class QuicConnectionStateTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.quic.QuicConnectionImpl$QuicConnectionState", false, QuicConnectionStateTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.quic.QuicConnectionImpl$QuicConnectionState"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("closing"), "method:closing");
        check(methods.contains("draining"), "method:draining");
        check(methods.contains("handshakeComplete"), "method:handshakeComplete");
        check(methods.contains("helloSent"), "method:helloSent");
        check(methods.contains("isMarked"), "method:isMarked");
        check(methods.contains("opened"), "method:opened");
        check(methods.contains("state"), "method:state");
        check(methods.contains("toString"), "method:toString");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("CLOSED"), "field:CLOSED");
        check(fields.contains("CLOSING"), "field:CLOSING");
        check(fields.contains("DRAINING"), "field:DRAINING");
        check(fields.contains("HISENT"), "field:HISENT");
        check(fields.contains("HSCOMPLETE"), "field:HSCOMPLETE");
        check(fields.contains("NEW"), "field:NEW");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
