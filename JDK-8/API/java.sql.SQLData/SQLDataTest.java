/*
 * @test
 * @summary java.sql.SQLData conformance: interface hierarchy and members.
 * @run main SQLDataTest
 */


public class SQLDataTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.sql.SQLData");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getSQLTypeName"), "has method getSQLTypeName");
        check(names.contains("readSQL"), "has method readSQL");
        check(names.contains("writeSQL"), "has method writeSQL");        System.out.println("PASS (" + passed + " assertions)");
    }
}
