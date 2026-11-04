/*
 * @test
 * @summary javax.sql.RowSetInternal conformance: interface hierarchy and members.
 * @run main RowSetInternalTest
 */


public class RowSetInternalTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.sql.RowSetInternal");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getConnection"), "has method getConnection");
        check(names.contains("getOriginal"), "has method getOriginal");
        check(names.contains("getOriginalRow"), "has method getOriginalRow");        System.out.println("PASS (" + passed + " assertions)");
    }
}
