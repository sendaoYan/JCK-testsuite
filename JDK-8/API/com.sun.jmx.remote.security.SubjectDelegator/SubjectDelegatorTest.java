/*
 * @test
 * @summary com.sun.jmx.remote.security.SubjectDelegator conformance: hierarchy and declared members via reflection.
 * @run main SubjectDelegatorTest
 */

public class SubjectDelegatorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jmx.remote.security.SubjectDelegator", false, null);
        check(c.getName().equals("com.sun.jmx.remote.security.SubjectDelegator"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("checkRemoveCallerContext"), "declares checkRemoveCallerContext");
        check(ms.contains("delegatedContext"), "declares delegatedContext");
        check(ms.contains("getDelegatedAcc"), "declares getDelegatedAcc");
        check(ms.contains("getSubjectPrincipals"), "declares getSubjectPrincipals");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
