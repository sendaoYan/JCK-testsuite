/*
 * @test
 * @summary jdk.internal.net.http.qpack.QPackException conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.qpack:+open
 * @run main QPackExceptionTest
 */
import java.lang.reflect.*;
import java.util.*;
public class QPackExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.qpack.QPackException", false, QPackExceptionTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.qpack.QPackException"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.RuntimeException"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("decoderStreamError"), "method:decoderStreamError");
        check(methods.contains("decompressionFailed"), "method:decompressionFailed");
        check(methods.contains("encoderStreamError"), "method:encoderStreamError");
        check(methods.contains("http3Error"), "method:http3Error");
        check(methods.contains("isConnectionError"), "method:isConnectionError");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
