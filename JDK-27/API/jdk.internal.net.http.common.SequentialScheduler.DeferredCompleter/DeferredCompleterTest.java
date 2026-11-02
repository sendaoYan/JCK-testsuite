/*
 * @test
 * @summary jdk.internal.net.http.common.SequentialScheduler.DeferredCompleter conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.common:+open
 * @run main DeferredCompleterTest
 */
import java.lang.reflect.*;
import java.util.*;
public class DeferredCompleterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.common.SequentialScheduler$DeferredCompleter", false, DeferredCompleterTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.common.SequentialScheduler$DeferredCompleter"), "name");
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
        check(methods.contains("complete"), "method:complete");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
