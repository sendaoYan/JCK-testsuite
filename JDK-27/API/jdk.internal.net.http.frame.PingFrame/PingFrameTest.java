/*
 * @test
 * @summary jdk.internal.net.http.frame.PingFrame conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.frame:+open
 * @run main PingFrameTest
 */
import java.lang.reflect.*;
import java.util.*;
public class PingFrameTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.frame.PingFrame", false, PingFrameTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.frame.PingFrame"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("jdk.internal.net.http.frame.Http2Frame"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("flagAsString"), "method:flagAsString");
        check(methods.contains("getData"), "method:getData");
        check(methods.contains("type"), "method:type");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("ACK"), "field:ACK");
        check(fields.contains("TYPE"), "field:TYPE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
