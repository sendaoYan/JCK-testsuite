/*
 * @test
 * @summary com.sun.corba.se.impl.oa.toa.TOAImpl conformance: type shape via reflection (headless-safe).
 * @run main TOAImplTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class TOAImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.oa.toa.TOAImpl", false, TOAImplTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.corba.se.spi.oa.ObjectAdapterBase"), "superclass com.sun.corba.se.spi.oa.ObjectAdapterBase");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("com.sun.corba.se.impl.oa.toa.TOA"), "interface com.sun.corba.se.impl.oa.toa.TOA");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 12, "public declared method count");
        check(mn.contains("connect"), "method connect");
        check(mn.contains("disconnect"), "method disconnect");
        check(mn.contains("enter"), "method enter");
        check(mn.contains("exit"), "method exit");
        check(mn.contains("getEffectivePolicy"), "method getEffectivePolicy");
        check(mn.contains("getInterfaces"), "method getInterfaces");
        check(mn.contains("getInvocationServant"), "method getInvocationServant");
        check(mn.contains("getLocalServant"), "method getLocalServant");
        check(mn.contains("getManagerId"), "method getManagerId");
        check(mn.contains("getObjectCopierFactory"), "method getObjectCopierFactory");
        check(mn.contains("getState"), "method getState");
        check(mn.contains("returnServant"), "method returnServant");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
