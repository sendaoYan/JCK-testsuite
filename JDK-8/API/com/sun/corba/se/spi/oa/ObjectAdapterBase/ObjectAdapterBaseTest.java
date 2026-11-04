/*
 * @test
 * @summary com.sun.corba.se.spi.oa.ObjectAdapterBase conformance: type shape via reflection (headless-safe).
 * @run main ObjectAdapterBaseTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class ObjectAdapterBaseTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.spi.oa.ObjectAdapterBase", false, ObjectAdapterBaseTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("org.omg.CORBA.LocalObject"), "superclass org.omg.CORBA.LocalObject");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("com.sun.corba.se.spi.oa.ObjectAdapter"), "interface com.sun.corba.se.spi.oa.ObjectAdapter");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 23, "public declared method count");
        check(mn.contains("enter"), "method enter");
        check(mn.contains("exit"), "method exit");
        check(mn.contains("getAdapterId"), "method getAdapterId");
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
        check(mn.contains("initializeTemplate"), "method initializeTemplate");
        check(mn.contains("invocationWrapper"), "method invocationWrapper");
        check(mn.contains("iorWrapper"), "method iorWrapper");
        check(mn.contains("lifecycleWrapper"), "method lifecycleWrapper");
        check(mn.contains("makeInvocationInfo"), "method makeInvocationInfo");
        check(mn.contains("makeObject"), "method makeObject");
        check(mn.contains("omgInvocationWrapper"), "method omgInvocationWrapper");
        check(mn.contains("omgLifecycleWrapper"), "method omgLifecycleWrapper");
        check(mn.contains("returnServant"), "method returnServant");
        check(mn.contains("setCurrentFactory"), "method setCurrentFactory");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
