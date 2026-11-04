/*
 * @test
 * @summary jdk.internal.net.http.frame.Http2Frame conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.frame:+open
 * @run main Http2FrameTest
 */
import java.lang.reflect.*;
import java.util.*;
public class Http2FrameTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.frame.Http2Frame", false, Http2FrameTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.frame.Http2Frame"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("asString"), "method:asString");
        check(methods.contains("flagAsString"), "method:flagAsString");
        check(methods.contains("getFlag"), "method:getFlag");
        check(methods.contains("getFlags"), "method:getFlags");
        check(methods.contains("setFlag"), "method:setFlag");
        check(methods.contains("streamid"), "method:streamid");
        check(methods.contains("toString"), "method:toString");
        check(methods.contains("type"), "method:type");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("FRAME_HEADER_SIZE"), "field:FRAME_HEADER_SIZE");
        check(fields.contains("flags"), "field:flags");
        check(fields.contains("streamid"), "field:streamid");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
