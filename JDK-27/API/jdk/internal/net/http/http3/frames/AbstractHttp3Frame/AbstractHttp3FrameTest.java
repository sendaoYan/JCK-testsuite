/*
 * @test
 * @summary jdk.internal.net.http.http3.frames.AbstractHttp3Frame conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.http3.frames:+open
 * @run main AbstractHttp3FrameTest
 */
import java.lang.reflect.*;
import java.util.*;
public class AbstractHttp3FrameTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.http3.frames.AbstractHttp3Frame", false, AbstractHttp3FrameTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.http3.frames.AbstractHttp3Frame"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("jdk.internal.net.http.http3.frames.Http3Frame"), "iface:jdk.internal.net.http.http3.frames.Http3Frame");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("checkPayloadSize"), "method:checkPayloadSize");
        check(methods.contains("decodeRequiredType"), "method:decodeRequiredType");
        check(methods.contains("headersSize"), "method:headersSize");
        check(methods.contains("size"), "method:size");
        check(methods.contains("streamingLength"), "method:streamingLength");
        check(methods.contains("toString"), "method:toString");
        check(methods.contains("type"), "method:type");
        check(methods.contains("typeAsString"), "method:typeAsString");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("RANDOM"), "field:RANDOM");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
