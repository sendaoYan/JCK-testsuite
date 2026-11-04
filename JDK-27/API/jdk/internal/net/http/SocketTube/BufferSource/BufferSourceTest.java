/*
 * @test
 * @summary jdk.internal.net.http.SocketTube.BufferSource conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http:+open
 * @run main BufferSourceTest
 */
import java.lang.reflect.*;
import java.util.*;
public class BufferSourceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.SocketTube$BufferSource", false, BufferSourceTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.SocketTube$BufferSource"), "name");
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
        check(methods.contains("append"), "method:append");
        check(methods.contains("getBuffer"), "method:getBuffer");
        check(methods.contains("returnUnused"), "method:returnUnused");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
