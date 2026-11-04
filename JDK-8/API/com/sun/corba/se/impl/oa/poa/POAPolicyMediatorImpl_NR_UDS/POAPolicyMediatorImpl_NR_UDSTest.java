/*
 * @test
 * @summary com.sun.corba.se.impl.oa.poa.POAPolicyMediatorImpl_NR_UDS conformance: type shape via reflection (headless-safe).
 * @run main POAPolicyMediatorImpl_NR_UDSTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class POAPolicyMediatorImpl_NR_UDSTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.oa.poa.POAPolicyMediatorImpl_NR_UDS", false, POAPolicyMediatorImpl_NR_UDSTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.corba.se.impl.oa.poa.POAPolicyMediatorBase"), "superclass com.sun.corba.se.impl.oa.poa.POAPolicyMediatorBase");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 11, "public declared method count");
        check(mn.contains("activateObject"), "method activateObject");
        check(mn.contains("clearAOM"), "method clearAOM");
        check(mn.contains("deactivateObject"), "method deactivateObject");
        check(mn.contains("etherealizeAll"), "method etherealizeAll");
        check(mn.contains("getDefaultServant"), "method getDefaultServant");
        check(mn.contains("getServantManager"), "method getServantManager");
        check(mn.contains("idToServant"), "method idToServant");
        check(mn.contains("returnServant"), "method returnServant");
        check(mn.contains("servantToId"), "method servantToId");
        check(mn.contains("setDefaultServant"), "method setDefaultServant");
        check(mn.contains("setServantManager"), "method setServantManager");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
