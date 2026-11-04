/*
 * @test
 * @summary com.sun.corba.se.spi.protocol.LocalClientRequestDispatcher conformance: type shape via reflection (headless-safe).
 * @run main LocalClientRequestDispatcherTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class LocalClientRequestDispatcherTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.spi.protocol.LocalClientRequestDispatcher", false, LocalClientRequestDispatcherTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface superclass null");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 4, "public declared method count");
        check(mn.contains("is_local"), "method is_local");
        check(mn.contains("servant_postinvoke"), "method servant_postinvoke");
        check(mn.contains("servant_preinvoke"), "method servant_preinvoke");
        check(mn.contains("useLocalInvocation"), "method useLocalInvocation");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
