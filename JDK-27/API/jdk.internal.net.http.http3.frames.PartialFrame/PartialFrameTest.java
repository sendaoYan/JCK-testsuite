/*
 * @test
 * @summary jdk.internal.net.http.http3.frames.PartialFrame conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.http3.frames:+open
 * @run main PartialFrameTest
 */
import java.lang.reflect.*;
import java.util.*;
public class PartialFrameTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.http3.frames.PartialFrame", false, PartialFrameTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.http3.frames.PartialFrame"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("jdk.internal.net.http.http3.frames.AbstractHttp3Frame"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("nextPayloadBytes"), "method:nextPayloadBytes");
        check(methods.contains("remaining"), "method:remaining");
        check(methods.contains("streamingLength"), "method:streamingLength");
        check(methods.contains("toString"), "method:toString");
        check(methods.contains("writeHeaders"), "method:writeHeaders");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
