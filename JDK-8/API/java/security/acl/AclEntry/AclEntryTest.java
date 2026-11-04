/*
 * @test
 * @summary java.security.acl.AclEntry conformance: interface hierarchy and key methods.
 * @run main AclEntryTest
 */

public class AclEntryTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.acl.AclEntry");
        check(c.isInterface(), "is interface");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("addPermission"), "declares addPermission");
        check(ms.contains("checkPermission"), "declares checkPermission");
        check(ms.contains("clone"), "declares clone");
        check(ms.contains("getPrincipal"), "declares getPrincipal");
        check(ms.contains("isNegative"), "declares isNegative");
        check(ms.contains("permissions"), "declares permissions");
        check(ms.contains("removePermission"), "declares removePermission");
        check(ms.contains("setNegativePermissions"), "declares setNegativePermissions");
        check(ms.contains("setPrincipal"), "declares setPrincipal");
        check(ms.contains("toString"), "declares toString");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
