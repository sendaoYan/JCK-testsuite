/*
 * @test
 * @summary javax.sql.rowset.Joinable conformance: interface hierarchy and members.
 * @run main JoinableTest
 */


public class JoinableTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.sql.rowset.Joinable");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getMatchColumnIndexes"), "has method getMatchColumnIndexes");
        check(names.contains("getMatchColumnNames"), "has method getMatchColumnNames");
        check(names.contains("setMatchColumn"), "has method setMatchColumn");        System.out.println("PASS (" + passed + " assertions)");
    }
}
