/*
 * @test
 * @summary jdk.internal.net.http.frame.PriorityFrame conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.frame:+open
 * @run main PriorityFrameTest
 */
import java.lang.reflect.*;
import java.util.*;
public class PriorityFrameTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.frame.PriorityFrame", false, PriorityFrameTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.frame.PriorityFrame"), "name");
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
        check(methods.contains("exclusive"), "method:exclusive");
        check(methods.contains("streamDependency"), "method:streamDependency");
        check(methods.contains("type"), "method:type");
        check(methods.contains("weight"), "method:weight");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("TYPE"), "field:TYPE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
