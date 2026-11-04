/*
 * @test
 * @summary com.sun.corba.se.impl.oa.poa.POAManagerImpl conformance: type shape via reflection (headless-safe).
 * @run main POAManagerImplTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class POAManagerImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.oa.poa.POAManagerImpl", false, POAManagerImplTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("org.omg.CORBA.LocalObject"), "superclass org.omg.CORBA.LocalObject");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("org.omg.PortableServer.POAManager"), "interface org.omg.PortableServer.POAManager");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 9, "public declared method count");
        check(mn.contains("activate"), "method activate");
        check(mn.contains("deactivate"), "method deactivate");
        check(mn.contains("discard_requests"), "method discard_requests");
        check(mn.contains("getManagerId"), "method getManagerId");
        check(mn.contains("getORTState"), "method getORTState");
        check(mn.contains("get_state"), "method get_state");
        check(mn.contains("hold_requests"), "method hold_requests");
        check(mn.contains("implicitActivation"), "method implicitActivation");
        check(mn.contains("toString"), "method toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
