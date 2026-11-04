/*
 * @test
 * @summary com.sun.corba.se.spi.resolver.ResolverDefault conformance: type shape via reflection (headless-safe).
 * @run main ResolverDefaultTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class ResolverDefaultTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.spi.resolver.ResolverDefault", false, ResolverDefaultTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 8, "public declared method count");
        check(mn.contains("makeBootstrapResolver"), "method makeBootstrapResolver");
        check(mn.contains("makeCompositeResolver"), "method makeCompositeResolver");
        check(mn.contains("makeFileResolver"), "method makeFileResolver");
        check(mn.contains("makeINSURLOperation"), "method makeINSURLOperation");
        check(mn.contains("makeLocalResolver"), "method makeLocalResolver");
        check(mn.contains("makeORBDefaultInitRefResolver"), "method makeORBDefaultInitRefResolver");
        check(mn.contains("makeORBInitRefResolver"), "method makeORBInitRefResolver");
        check(mn.contains("makeSplitLocalResolver"), "method makeSplitLocalResolver");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
