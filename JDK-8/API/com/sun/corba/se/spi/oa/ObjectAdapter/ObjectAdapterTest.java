/*
 * @test
 * @summary com.sun.corba.se.spi.oa.ObjectAdapter conformance: type shape via reflection (headless-safe).
 * @run main ObjectAdapterTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class ObjectAdapterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.spi.oa.ObjectAdapter", false, ObjectAdapterTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface superclass null");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 15, "public declared method count");
        check(mn.contains("enter"), "method enter");
        check(mn.contains("exit"), "method exit");
        check(mn.contains("getAdapterTemplate"), "method getAdapterTemplate");
        check(mn.contains("getCurrentFactory"), "method getCurrentFactory");
        check(mn.contains("getEffectivePolicy"), "method getEffectivePolicy");
        check(mn.contains("getIORTemplate"), "method getIORTemplate");
        check(mn.contains("getInterfaces"), "method getInterfaces");
        check(mn.contains("getInvocationServant"), "method getInvocationServant");
        check(mn.contains("getLocalServant"), "method getLocalServant");
        check(mn.contains("getManagerId"), "method getManagerId");
        check(mn.contains("getORB"), "method getORB");
        check(mn.contains("getState"), "method getState");
        check(mn.contains("makeInvocationInfo"), "method makeInvocationInfo");
        check(mn.contains("returnServant"), "method returnServant");
        check(mn.contains("setCurrentFactory"), "method setCurrentFactory");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
