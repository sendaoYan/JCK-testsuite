/*
 * @test
 * @summary jdk.internal.net.http.qpack.Decoder conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.qpack:+open
 * @run main DecoderTest
 */
import java.lang.reflect.*;
import java.util.*;
public class DecoderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.qpack.Decoder", false, DecoderTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.qpack.Decoder"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("ackSection"), "method:ackSection");
        check(methods.contains("ackTableInsertions"), "method:ackTableInsertions");
        check(methods.contains("cancelStream"), "method:cancelStream");
        check(methods.contains("configure"), "method:configure");
        check(methods.contains("decodeHeader"), "method:decodeHeader");
        check(methods.contains("decoderStreams"), "method:decoderStreams");
        check(methods.contains("newHeaderFrameReader"), "method:newHeaderFrameReader");
        check(methods.contains("resetInsertionsCounter"), "method:resetInsertionsCounter");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
