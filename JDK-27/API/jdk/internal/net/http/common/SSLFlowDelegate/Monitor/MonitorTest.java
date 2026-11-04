/*
 * @test
 * @summary jdk.internal.net.http.common.SSLFlowDelegate.Monitor conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.common:+open
 * @run main MonitorTest
 */
import java.lang.reflect.*;
import java.util.*;
public class MonitorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.common.SSLFlowDelegate$Monitor", false, MonitorTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.common.SSLFlowDelegate$Monitor"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Thread"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("add"), "method:add");
        check(methods.contains("remove"), "method:remove");
        check(methods.contains("run"), "method:run");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
