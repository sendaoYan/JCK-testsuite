/*
 * @test
 * @summary com.sun.corba.se.impl.oa.poa.Policies conformance: type shape via reflection (headless-safe).
 * @run main PoliciesTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class PoliciesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.oa.poa.Policies", false, PoliciesTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 18, "public declared method count");
        check(mn.contains("forceZeroPort"), "method forceZeroPort");
        check(mn.contains("getCopierId"), "method getCopierId");
        check(mn.contains("get_effective_policy"), "method get_effective_policy");
        check(mn.contains("isImplicitlyActivated"), "method isImplicitlyActivated");
        check(mn.contains("isMultipleIds"), "method isMultipleIds");
        check(mn.contains("isOrbControlledThreads"), "method isOrbControlledThreads");
        check(mn.contains("isPersistent"), "method isPersistent");
        check(mn.contains("isSingleThreaded"), "method isSingleThreaded");
        check(mn.contains("isSystemAssignedIds"), "method isSystemAssignedIds");
        check(mn.contains("isTransient"), "method isTransient");
        check(mn.contains("isUniqueIds"), "method isUniqueIds");
        check(mn.contains("isUserAssignedIds"), "method isUserAssignedIds");
        check(mn.contains("retainServants"), "method retainServants");
        check(mn.contains("servantCachingLevel"), "method servantCachingLevel");
        check(mn.contains("toString"), "method toString");
        check(mn.contains("useActiveMapOnly"), "method useActiveMapOnly");
        check(mn.contains("useDefaultServant"), "method useDefaultServant");
        check(mn.contains("useServantManager"), "method useServantManager");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
