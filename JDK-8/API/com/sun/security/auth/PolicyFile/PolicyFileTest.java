/*
 * @test
 * @summary com.sun.security.auth.PolicyFile conformance: hierarchy and declared members via reflection.
 * @run main PolicyFileTest
 */

public class PolicyFileTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.security.auth.PolicyFile", false, null);
        check(c.getName().equals("com.sun.security.auth.PolicyFile"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.security.auth.Policy"), "superclass javax.security.auth.Policy");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("getPermissions"), "declares getPermissions");
        check(ms.contains("refresh"), "declares refresh");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
