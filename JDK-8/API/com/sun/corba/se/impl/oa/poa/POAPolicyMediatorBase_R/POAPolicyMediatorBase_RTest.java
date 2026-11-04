/*
 * @test
 * @summary com.sun.corba.se.impl.oa.poa.POAPolicyMediatorBase_R conformance: type shape via reflection (headless-safe).
 * @run main POAPolicyMediatorBase_RTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class POAPolicyMediatorBase_RTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.oa.poa.POAPolicyMediatorBase_R", false, POAPolicyMediatorBase_RTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.corba.se.impl.oa.poa.POAPolicyMediatorBase"), "superclass com.sun.corba.se.impl.oa.poa.POAPolicyMediatorBase");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 5, "public declared method count");
        check(mn.contains("activateObject"), "method activateObject");
        check(mn.contains("clearAOM"), "method clearAOM");
        check(mn.contains("deactivateObject"), "method deactivateObject");
        check(mn.contains("returnServant"), "method returnServant");
        check(mn.contains("servantToId"), "method servantToId");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
