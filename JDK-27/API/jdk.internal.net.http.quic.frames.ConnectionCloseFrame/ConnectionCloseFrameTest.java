/*
 * @test
 * @summary jdk.internal.net.http.quic.frames.ConnectionCloseFrame conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.quic.frames:+open
 * @run main ConnectionCloseFrameTest
 */
import java.lang.reflect.*;
import java.util.*;
public class ConnectionCloseFrameTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.quic.frames.ConnectionCloseFrame", false, ConnectionCloseFrameTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.quic.frames.ConnectionCloseFrame"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass().getName().equals("jdk.internal.net.http.quic.frames.QuicFrame"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("clearApplicationState"), "method:clearApplicationState");
        check(methods.contains("encode"), "method:encode");
        check(methods.contains("errorCode"), "method:errorCode");
        check(methods.contains("errorFrameType"), "method:errorFrameType");
        check(methods.contains("getTypeField"), "method:getTypeField");
        check(methods.contains("isAckEliciting"), "method:isAckEliciting");
        check(methods.contains("isApplicationCode"), "method:isApplicationCode");
        check(methods.contains("isQuicTransportCode"), "method:isQuicTransportCode");
        check(methods.contains("reason"), "method:reason");
        check(methods.contains("reasonString"), "method:reasonString");
        check(methods.contains("size"), "method:size");
        check(methods.contains("toString"), "method:toString");
        check(methods.contains("variant"), "method:variant");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("APPLICATION_ERROR"), "field:APPLICATION_ERROR");
        check(fields.contains("CONNECTION_CLOSE_VARIANT"), "field:CONNECTION_CLOSE_VARIANT");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
