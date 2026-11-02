/*
 * @test
 * @summary java.sql.Ref conformance: interface hierarchy and members.
 * @run main RefTest
 */


public class RefTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.sql.Ref");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getBaseTypeName"), "has method getBaseTypeName");
        check(names.contains("getObject"), "has method getObject");
        check(names.contains("setObject"), "has method setObject");        System.out.println("PASS (" + passed + " assertions)");
    }
}
