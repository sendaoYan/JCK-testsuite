/*
 * @test
 * @summary com.sun.corba.se.impl.oa.toa.TransientObjectManager conformance: type shape via reflection (headless-safe).
 * @run main TransientObjectManagerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class TransientObjectManagerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.oa.toa.TransientObjectManager", false, TransientObjectManagerTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 5, "public declared method count");
        check(mn.contains("deleteServant"), "method deleteServant");
        check(mn.contains("getKey"), "method getKey");
        check(mn.contains("lookupServant"), "method lookupServant");
        check(mn.contains("lookupServantData"), "method lookupServantData");
        check(mn.contains("storeServant"), "method storeServant");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
