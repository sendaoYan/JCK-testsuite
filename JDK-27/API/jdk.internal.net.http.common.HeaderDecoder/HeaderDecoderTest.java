/*
 * @test
 * @summary jdk.internal.net.http.common.HeaderDecoder conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.common:+open
 * @run main HeaderDecoderTest
 */
import java.lang.reflect.*;
import java.util.*;
public class HeaderDecoderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.common.HeaderDecoder", false, HeaderDecoderTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.common.HeaderDecoder"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("jdk.internal.net.http.common.ValidatingHeadersConsumer"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("addHeader"), "method:addHeader");
        check(methods.contains("headers"), "method:headers");
        check(methods.contains("onDecoded"), "method:onDecoded");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
