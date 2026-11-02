/*
 * @test
 * @summary com.sun.corba.se.spi.protocol.RequestDispatcherDefault conformance: type shape via reflection (headless-safe).
 * @run main RequestDispatcherDefaultTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class RequestDispatcherDefaultTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.spi.protocol.RequestDispatcherDefault", false, RequestDispatcherDefaultTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 9, "public declared method count");
        check(mn.contains("makeBootstrapServerRequestDispatcher"), "method makeBootstrapServerRequestDispatcher");
        check(mn.contains("makeClientRequestDispatcher"), "method makeClientRequestDispatcher");
        check(mn.contains("makeFullServantCacheLocalClientRequestDispatcherFactory"), "method makeFullServantCacheLocalClientRequestDispatcherFactory");
        check(mn.contains("makeINSServerRequestDispatcher"), "method makeINSServerRequestDispatcher");
        check(mn.contains("makeInfoOnlyServantCacheLocalClientRequestDispatcherFactory"), "method makeInfoOnlyServantCacheLocalClientRequestDispatcherFactory");
        check(mn.contains("makeJIDLLocalClientRequestDispatcherFactory"), "method makeJIDLLocalClientRequestDispatcherFactory");
        check(mn.contains("makeMinimalServantCacheLocalClientRequestDispatcherFactory"), "method makeMinimalServantCacheLocalClientRequestDispatcherFactory");
        check(mn.contains("makePOALocalClientRequestDispatcherFactory"), "method makePOALocalClientRequestDispatcherFactory");
        check(mn.contains("makeServerRequestDispatcher"), "method makeServerRequestDispatcher");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
