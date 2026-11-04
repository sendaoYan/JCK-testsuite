/*
 * @test
 * @summary com.sun.corba.se.impl.oa.poa.POAPolicyMediator conformance: type shape via reflection (headless-safe).
 * @run main POAPolicyMediatorTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class POAPolicyMediatorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.oa.poa.POAPolicyMediator", false, POAPolicyMediatorTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface superclass null");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 16, "public declared method count");
        check(mn.contains("activateObject"), "method activateObject");
        check(mn.contains("clearAOM"), "method clearAOM");
        check(mn.contains("deactivateObject"), "method deactivateObject");
        check(mn.contains("etherealizeAll"), "method etherealizeAll");
        check(mn.contains("getDefaultServant"), "method getDefaultServant");
        check(mn.contains("getInvocationServant"), "method getInvocationServant");
        check(mn.contains("getPolicies"), "method getPolicies");
        check(mn.contains("getScid"), "method getScid");
        check(mn.contains("getServantManager"), "method getServantManager");
        check(mn.contains("getServerId"), "method getServerId");
        check(mn.contains("idToServant"), "method idToServant");
        check(mn.contains("newSystemId"), "method newSystemId");
        check(mn.contains("returnServant"), "method returnServant");
        check(mn.contains("servantToId"), "method servantToId");
        check(mn.contains("setDefaultServant"), "method setDefaultServant");
        check(mn.contains("setServantManager"), "method setServantManager");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
