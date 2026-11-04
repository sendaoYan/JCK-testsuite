/*
 * @test
 * @summary com.sun.corba.se.impl.oa.poa.RequestProcessingPolicyImpl conformance: type shape via reflection (headless-safe).
 * @run main RequestProcessingPolicyImplTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class RequestProcessingPolicyImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.oa.poa.RequestProcessingPolicyImpl", false, RequestProcessingPolicyImplTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("org.omg.CORBA.LocalObject"), "superclass org.omg.CORBA.LocalObject");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("org.omg.PortableServer.RequestProcessingPolicy"), "interface org.omg.PortableServer.RequestProcessingPolicy");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 5, "public declared method count");
        check(mn.contains("copy"), "method copy");
        check(mn.contains("destroy"), "method destroy");
        check(mn.contains("policy_type"), "method policy_type");
        check(mn.contains("toString"), "method toString");
        check(mn.contains("value"), "method value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
