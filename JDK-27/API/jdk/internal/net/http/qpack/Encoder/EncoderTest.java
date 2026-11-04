/*
 * @test
 * @summary jdk.internal.net.http.qpack.Encoder conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.qpack:+open
 * @run main EncoderTest
 */
import java.lang.reflect.*;
import java.util.*;
public class EncoderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.qpack.Encoder", false, EncoderTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.qpack.Encoder"), "name");
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
        check(methods.contains("configure"), "method:configure");
        check(methods.contains("encodeHeaders"), "method:encodeHeaders");
        check(methods.contains("encoderStreams"), "method:encoderStreams");
        check(methods.contains("generateFieldLineSectionPrefix"), "method:generateFieldLineSectionPrefix");
        check(methods.contains("header"), "method:header");
        check(methods.contains("knownReceivedCount"), "method:knownReceivedCount");
        check(methods.contains("newEncodingContext"), "method:newEncodingContext");
        check(methods.contains("newHeaderFrameWriter"), "method:newHeaderFrameWriter");
        check(methods.contains("registerUnackedFieldLineSection"), "method:registerUnackedFieldLineSection");
        check(methods.contains("setTableCapacity"), "method:setTableCapacity");
        check(methods.contains("unackedFieldLineSectionsRange"), "method:unackedFieldLineSectionsRange");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
