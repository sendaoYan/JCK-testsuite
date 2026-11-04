/*
 * @test
 * @summary java.sql.SQLType conformance: interface hierarchy and members.
 * @run main SQLTypeTest
 */


public class SQLTypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.sql.SQLType");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getName"), "has method getName");
        check(names.contains("getVendor"), "has method getVendor");
        check(names.contains("getVendorTypeNumber"), "has method getVendorTypeNumber");        System.out.println("PASS (" + passed + " assertions)");
    }
}
