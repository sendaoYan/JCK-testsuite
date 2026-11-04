/*
 * @test
 * @summary com.sun.corba.se.impl.naming.namingutil.INSURLBase conformance: type shape via reflection (headless-safe).
 * @run main INSURLBaseTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class INSURLBaseTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.naming.namingutil.INSURLBase", false, INSURLBaseTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("com.sun.corba.se.impl.naming.namingutil.INSURL"), "interface com.sun.corba.se.impl.naming.namingutil.INSURL");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 6, "public declared method count");
        check(mn.contains("dPrint"), "method dPrint");
        check(mn.contains("getEndpointInfo"), "method getEndpointInfo");
        check(mn.contains("getKeyString"), "method getKeyString");
        check(mn.contains("getRIRFlag"), "method getRIRFlag");
        check(mn.contains("getStringifiedName"), "method getStringifiedName");
        check(mn.contains("isCorbanameURL"), "method isCorbanameURL");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
