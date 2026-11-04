/*
 * @test
 * @summary jdk.internal.net.http.frame.HeadersFrame conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.frame:+open
 * @run main HeadersFrameTest
 */
import java.lang.reflect.*;
import java.util.*;
public class HeadersFrameTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.frame.HeadersFrame", false, HeadersFrameTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.frame.HeadersFrame"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("jdk.internal.net.http.frame.HeaderFrame"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("flagAsString"), "method:flagAsString");
        check(methods.contains("getExclusive"), "method:getExclusive");
        check(methods.contains("getStreamDependency"), "method:getStreamDependency");
        check(methods.contains("getWeight"), "method:getWeight");
        check(methods.contains("setPadLength"), "method:setPadLength");
        check(methods.contains("setPriority"), "method:setPriority");
        check(methods.contains("type"), "method:type");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("END_STREAM"), "field:END_STREAM");
        check(fields.contains("PADDED"), "field:PADDED");
        check(fields.contains("PRIORITY"), "field:PRIORITY");
        check(fields.contains("TYPE"), "field:TYPE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
