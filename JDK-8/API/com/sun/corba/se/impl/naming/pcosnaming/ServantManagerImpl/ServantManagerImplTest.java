/*
 * @test
 * @summary com.sun.corba.se.impl.naming.pcosnaming.ServantManagerImpl conformance: type shape via reflection (headless-safe).
 * @run main ServantManagerImplTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class ServantManagerImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.naming.pcosnaming.ServantManagerImpl", false, ServantManagerImplTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("org.omg.CORBA.LocalObject"), "superclass org.omg.CORBA.LocalObject");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("org.omg.PortableServer.ServantLocator"), "interface org.omg.PortableServer.ServantLocator");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 7, "public declared method count");
        check(mn.contains("addContext"), "method addContext");
        check(mn.contains("getNewObjectKey"), "method getNewObjectKey");
        check(mn.contains("getRootObjectKey"), "method getRootObjectKey");
        check(mn.contains("postinvoke"), "method postinvoke");
        check(mn.contains("preinvoke"), "method preinvoke");
        check(mn.contains("readInContext"), "method readInContext");
        check(mn.contains("updateContext"), "method updateContext");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
