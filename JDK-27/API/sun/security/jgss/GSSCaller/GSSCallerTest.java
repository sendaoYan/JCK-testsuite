/*
 * @test
 * @summary sun.security.jgss.GSSCaller conformance: internal package reflection.
 * @modules java.security.jgss/sun.security.jgss:+open
 * @run main GSSCallerTest
 */
import java.lang.reflect.*;
import java.util.*;
public class GSSCallerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.jgss.GSSCaller", false, GSSCallerTest.class.getClassLoader());
        check(c.getName().equals("sun.security.jgss.GSSCaller"), "name");
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
        check(methods.contains("toString"), "method:toString");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("CALLER_ACCEPT"), "field:CALLER_ACCEPT");
        check(fields.contains("CALLER_INITIATE"), "field:CALLER_INITIATE");
        check(fields.contains("CALLER_UNKNOWN"), "field:CALLER_UNKNOWN");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
