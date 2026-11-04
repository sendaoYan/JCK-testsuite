/*
 * @test
 * @summary com.sun.corba.se.spi.oa.OAInvocationInfo conformance: type shape via reflection (headless-safe).
 * @run main OAInvocationInfoTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class OAInvocationInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.spi.oa.OAInvocationInfo", false, OAInvocationInfoTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("org.omg.CORBA.portable.ServantObject"), "superclass org.omg.CORBA.portable.ServantObject");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 9, "public declared method count");
        check(mn.contains("getCookieHolder"), "method getCookieHolder");
        check(mn.contains("getCopierFactory"), "method getCopierFactory");
        check(mn.contains("getOperation"), "method getOperation");
        check(mn.contains("getServantContainer"), "method getServantContainer");
        check(mn.contains("id"), "method id");
        check(mn.contains("oa"), "method oa");
        check(mn.contains("setCopierFactory"), "method setCopierFactory");
        check(mn.contains("setOperation"), "method setOperation");
        check(mn.contains("setServant"), "method setServant");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
