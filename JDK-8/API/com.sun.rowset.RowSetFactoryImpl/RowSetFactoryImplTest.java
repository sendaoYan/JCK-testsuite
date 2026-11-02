/*
 * @test
 * @summary com.sun.rowset.RowSetFactoryImpl conformance: hierarchy and declared members via reflection.
 * @run main RowSetFactoryImplTest
 */

public class RowSetFactoryImplTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.rowset.RowSetFactoryImpl", false, null);
        check(c.getName().equals("com.sun.rowset.RowSetFactoryImpl"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("javax.sql.rowset.RowSetFactory"), "implements javax.sql.rowset.RowSetFactory");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("createCachedRowSet"), "declares createCachedRowSet");
        check(ms.contains("createFilteredRowSet"), "declares createFilteredRowSet");
        check(ms.contains("createJdbcRowSet"), "declares createJdbcRowSet");
        check(ms.contains("createJoinRowSet"), "declares createJoinRowSet");
        check(ms.contains("createWebRowSet"), "declares createWebRowSet");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
