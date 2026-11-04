/*
 * @test
 * @summary jdk.internal.net.http.quic.ConnectionTerminator conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.quic:+open
 * @run main ConnectionTerminatorTest
 */
import java.lang.reflect.*;
import java.util.*;
public class ConnectionTerminatorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.quic.ConnectionTerminator", false, ConnectionTerminatorTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.quic.ConnectionTerminator"), "name");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() == null, "superclass-null");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("appLayerMaxIdle"), "method:appLayerMaxIdle");
        check(methods.contains("markActive"), "method:markActive");
        check(methods.contains("terminate"), "method:terminate");
        check(methods.contains("tryReserveForUse"), "method:tryReserveForUse");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
