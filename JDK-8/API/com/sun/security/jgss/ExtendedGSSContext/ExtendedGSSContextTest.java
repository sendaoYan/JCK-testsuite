/*
 * @test
 * @summary com.sun.security.jgss.ExtendedGSSContext conformance: hierarchy and declared members via reflection.
 * @run main ExtendedGSSContextTest
 */

public class ExtendedGSSContextTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.security.jgss.ExtendedGSSContext", false, null);
        check(c.getName().equals("com.sun.security.jgss.ExtendedGSSContext"), "binary name");
        check(c.isInterface() == true, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "is abstract");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("org.ietf.jgss.GSSContext"), "implements org.ietf.jgss.GSSContext");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("getDelegPolicyState"), "declares getDelegPolicyState");
        check(ms.contains("inquireSecContext"), "declares inquireSecContext");
        check(ms.contains("requestDelegPolicy"), "declares requestDelegPolicy");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
