/*
 * @test
 * @summary jdk.internal.net.http.quic.QuicStreamLimitException conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.quic:+open
 * @run main QuicStreamLimitExceptionTest
 */
import java.lang.reflect.*;
import java.util.*;
public class QuicStreamLimitExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.quic.QuicStreamLimitException", false, QuicStreamLimitExceptionTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.quic.QuicStreamLimitException"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Exception"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
