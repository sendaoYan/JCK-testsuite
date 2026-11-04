/*
 * @test
 * @summary jdk.internal.net.http.websocket.Frame.Masker conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.websocket:+open
 * @run main MaskerTest
 */
import java.lang.reflect.*;
import java.util.*;
public class MaskerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.websocket.Frame$Masker", false, MaskerTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.websocket.Frame$Masker"), "name");
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
        check(methods.contains("applyMask"), "method:applyMask");
        check(methods.contains("setMask"), "method:setMask");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
