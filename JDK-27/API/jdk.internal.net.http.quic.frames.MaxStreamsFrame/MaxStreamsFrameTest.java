/*
 * @test
 * @summary jdk.internal.net.http.quic.frames.MaxStreamsFrame conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.quic.frames:+open
 * @run main MaxStreamsFrameTest
 */
import java.lang.reflect.*;
import java.util.*;
public class MaxStreamsFrameTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.quic.frames.MaxStreamsFrame", false, MaxStreamsFrameTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.quic.frames.MaxStreamsFrame"), "name");
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
        check(methods.contains("getTypeField"), "method:getTypeField");
        check(methods.contains("isBidi"), "method:isBidi");
        check(methods.contains("maxStreams"), "method:maxStreams");
        check(methods.contains("size"), "method:size");
        check(methods.contains("toString"), "method:toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
