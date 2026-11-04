/*
 * @test
 * @summary jdk.internal.net.http.frame.PushPromiseFrame conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.frame:+open
 * @run main PushPromiseFrameTest
 */
import java.lang.reflect.*;
import java.util.*;
public class PushPromiseFrameTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.frame.PushPromiseFrame", false, PushPromiseFrameTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.frame.PushPromiseFrame"), "name");
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
        check(methods.contains("endHeaders"), "method:endHeaders");
        check(methods.contains("flagAsString"), "method:flagAsString");
        check(methods.contains("getPadLength"), "method:getPadLength");
        check(methods.contains("getPromisedStream"), "method:getPromisedStream");
        check(methods.contains("setPadLength"), "method:setPadLength");
        check(methods.contains("toString"), "method:toString");
        check(methods.contains("type"), "method:type");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("END_HEADERS"), "field:END_HEADERS");
        check(fields.contains("PADDED"), "field:PADDED");
        check(fields.contains("TYPE"), "field:TYPE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
