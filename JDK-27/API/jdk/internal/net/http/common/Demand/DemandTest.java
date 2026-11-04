/*
 * @test
 * @summary jdk.internal.net.http.common.Demand conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.common:+open
 * @run main DemandTest
 */
import java.lang.reflect.*;
import java.util.*;
public class DemandTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.common.Demand", false, DemandTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.common.Demand"), "name");
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
        check(methods.contains("decreaseAndGet"), "method:decreaseAndGet");
        check(methods.contains("get"), "method:get");
        check(methods.contains("increase"), "method:increase");
        check(methods.contains("increaseIfFulfilled"), "method:increaseIfFulfilled");
        check(methods.contains("isFulfilled"), "method:isFulfilled");
        check(methods.contains("reset"), "method:reset");
        check(methods.contains("toString"), "method:toString");
        check(methods.contains("tryDecrement"), "method:tryDecrement");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
