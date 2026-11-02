/*
 * @test
 * @summary javax.sql.rowset.FilteredRowSet conformance: interface hierarchy and members.
 * @run main FilteredRowSetTest
 */


public class FilteredRowSetTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.sql.rowset.FilteredRowSet");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> expIfs = new java.util.HashSet<String>();
        expIfs.add("javax.sql.rowset.WebRowSet");
        java.util.Set<String> actIfs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) actIfs.add(i.getName());
        check(actIfs.equals(expIfs), "direct interfaces");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("absolute"), "has method absolute");
        check(names.contains("acceptChanges"), "has method acceptChanges");
        check(names.contains("addRowSetListener"), "has method addRowSetListener");        System.out.println("PASS (" + passed + " assertions)");
    }
}
