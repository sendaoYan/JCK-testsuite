/*
 * @test
 * @summary jdk.internal.net.http.quic.frames.ResetStreamFrame conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.quic.frames:+open
 * @run main ResetStreamFrameTest
 */
import java.lang.reflect.*;
import java.util.*;
public class ResetStreamFrameTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.quic.frames.ResetStreamFrame", false, ResetStreamFrameTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.quic.frames.ResetStreamFrame"), "name");
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
        check(methods.contains("encode"), "method:encode");
        check(methods.contains("errorCode"), "method:errorCode");
        check(methods.contains("finalSize"), "method:finalSize");
        check(methods.contains("size"), "method:size");
        check(methods.contains("streamId"), "method:streamId");
        check(methods.contains("toString"), "method:toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
