/*
 * @test
 * @summary jdk.internal.net.http.common.MinimalFuture conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.common:+open
 * @run main MinimalFutureTest
 */
import java.lang.reflect.*;
import java.util.*;
public class MinimalFutureTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.common.MinimalFuture", false, MinimalFutureTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.common.MinimalFuture"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass().getName().equals("java.util.concurrent.CompletableFuture"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("cancel"), "method:cancel");
        check(methods.contains("completedFuture"), "method:completedFuture");
        check(methods.contains("failedFuture"), "method:failedFuture");
        check(methods.contains("newIncompleteFuture"), "method:newIncompleteFuture");
        check(methods.contains("obtrudeException"), "method:obtrudeException");
        check(methods.contains("obtrudeValue"), "method:obtrudeValue");
        check(methods.contains("of"), "method:of");
        check(methods.contains("supply"), "method:supply");
        check(methods.contains("toString"), "method:toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
