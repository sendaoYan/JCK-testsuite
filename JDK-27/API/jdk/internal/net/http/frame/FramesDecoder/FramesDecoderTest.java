/*
 * @test
 * @summary jdk.internal.net.http.frame.FramesDecoder conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.frame:+open
 * @run main FramesDecoderTest
 */
import java.lang.reflect.*;
import java.util.*;
public class FramesDecoderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.frame.FramesDecoder", false, FramesDecoderTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.frame.FramesDecoder"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("close"), "method:close");
        check(methods.contains("decode"), "method:decode");
        check(methods.contains("getByte"), "method:getByte");
        check(methods.contains("getBytes"), "method:getBytes");
        check(methods.contains("getInt"), "method:getInt");
        check(methods.contains("getShort"), "method:getShort");
        check(methods.contains("skipBytes"), "method:skipBytes");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
