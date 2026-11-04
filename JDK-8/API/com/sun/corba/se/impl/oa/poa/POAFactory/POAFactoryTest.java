/*
 * @test
 * @summary com.sun.corba.se.impl.oa.poa.POAFactory conformance: type shape via reflection (headless-safe).
 * @run main POAFactoryTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class POAFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.oa.poa.POAFactory", false, POAFactoryTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("com.sun.corba.se.spi.oa.ObjectAdapterFactory"), "interface com.sun.corba.se.spi.oa.ObjectAdapterFactory");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 15, "public declared method count");
        check(mn.contains("addPoaManager"), "method addPoaManager");
        check(mn.contains("find"), "method find");
        check(mn.contains("getDelegateImpl"), "method getDelegateImpl");
        check(mn.contains("getORB"), "method getORB");
        check(mn.contains("getRootPOA"), "method getRootPOA");
        check(mn.contains("getWrapper"), "method getWrapper");
        check(mn.contains("init"), "method init");
        check(mn.contains("lookupPOA"), "method lookupPOA");
        check(mn.contains("newPOAId"), "method newPOAId");
        check(mn.contains("newPOAManagerId"), "method newPOAManagerId");
        check(mn.contains("registerPOAForServant"), "method registerPOAForServant");
        check(mn.contains("registerRootPOA"), "method registerRootPOA");
        check(mn.contains("removePoaManager"), "method removePoaManager");
        check(mn.contains("shutdown"), "method shutdown");
        check(mn.contains("unregisterPOAForServant"), "method unregisterPOAForServant");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
