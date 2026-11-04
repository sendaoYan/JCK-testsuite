/*
 * @test
 * @summary jdk.internal.net.http.hpack.DecodingCallback conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.hpack:+open
 * @run main DecodingCallbackTest
 */
import java.lang.reflect.*;
import java.util.*;
public class DecodingCallbackTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.hpack.DecodingCallback", false, DecodingCallbackTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.hpack.DecodingCallback"), "name");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() == null, "superclass-null");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("onDecoded"), "method:onDecoded");
        check(methods.contains("onIndexed"), "method:onIndexed");
        check(methods.contains("onLiteral"), "method:onLiteral");
        check(methods.contains("onLiteralNeverIndexed"), "method:onLiteralNeverIndexed");
        check(methods.contains("onLiteralWithIndexing"), "method:onLiteralWithIndexing");
        check(methods.contains("onMaxHeaderListSizeReached"), "method:onMaxHeaderListSizeReached");
        check(methods.contains("onMaxLiteralWithIndexingReached"), "method:onMaxLiteralWithIndexingReached");
        check(methods.contains("onSizeUpdate"), "method:onSizeUpdate");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
