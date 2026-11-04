/*
 * @test
 * @summary jdk.internal.net.http.quic.BuffersReader.SingleBufferReader conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.quic:+open
 * @run main SingleBufferReaderTest
 */
import java.lang.reflect.*;
import java.util.*;
public class SingleBufferReaderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.quic.BuffersReader$SingleBufferReader", false, SingleBufferReaderTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.quic.BuffersReader$SingleBufferReader"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass().getName().equals("jdk.internal.net.http.quic.BuffersReader"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("clear"), "method:clear");
        check(methods.contains("get"), "method:get");
        check(methods.contains("getAndRelease"), "method:getAndRelease");
        check(methods.contains("getAndReleaseBuffer"), "method:getAndReleaseBuffer");
        check(methods.contains("hasRemaining"), "method:hasRemaining");
        check(methods.contains("isEmpty"), "method:isEmpty");
        check(methods.contains("limit"), "method:limit");
        check(methods.contains("offset"), "method:offset");
        check(methods.contains("position"), "method:position");
        check(methods.contains("read"), "method:read");
        check(methods.contains("release"), "method:release");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
