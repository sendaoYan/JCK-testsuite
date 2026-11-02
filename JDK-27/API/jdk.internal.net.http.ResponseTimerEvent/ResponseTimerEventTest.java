/*
 * @test
 * @summary jdk.internal.net.http.ResponseTimerEvent conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http:+open
 * @run main ResponseTimerEventTest
 */
import java.lang.reflect.*;
import java.util.*;
public class ResponseTimerEventTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.ResponseTimerEvent", false, ResponseTimerEventTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.ResponseTimerEvent"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("jdk.internal.net.http.TimeoutEvent"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("compareTo"), "method:compareTo");
        check(methods.contains("deadline"), "method:deadline");
        check(methods.contains("handle"), "method:handle");
        check(methods.contains("toString"), "method:toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
