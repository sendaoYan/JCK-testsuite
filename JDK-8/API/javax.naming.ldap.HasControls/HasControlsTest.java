/*
 * @test
 * @summary javax.naming.ldap.HasControls conformance: interface hierarchy and members.
 * @run main HasControlsTest
 */


public class HasControlsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.naming.ldap.HasControls");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getControls"), "has method getControls");        System.out.println("PASS (" + passed + " assertions)");
    }
}
