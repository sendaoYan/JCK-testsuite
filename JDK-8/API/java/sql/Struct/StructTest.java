/*
 * @test
 * @summary java.sql.Struct conformance: interface hierarchy and members.
 * @run main StructTest
 */


public class StructTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.sql.Struct");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getAttributes"), "has method getAttributes");
        check(names.contains("getSQLTypeName"), "has method getSQLTypeName");        System.out.println("PASS (" + passed + " assertions)");
    }
}
