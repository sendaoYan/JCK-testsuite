/*
 * @test
 * @summary javax.sql.rowset.RowSetFactory conformance: interface hierarchy and members.
 * @run main RowSetFactoryTest
 */


public class RowSetFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.sql.rowset.RowSetFactory");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("createCachedRowSet"), "has method createCachedRowSet");
        check(names.contains("createFilteredRowSet"), "has method createFilteredRowSet");
        check(names.contains("createJdbcRowSet"), "has method createJdbcRowSet");        System.out.println("PASS (" + passed + " assertions)");
    }
}
