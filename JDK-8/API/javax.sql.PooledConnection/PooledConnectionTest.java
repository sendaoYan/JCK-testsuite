/*
 * @test
 * @summary javax.sql.PooledConnection conformance: interface hierarchy and members.
 * @run main PooledConnectionTest
 */


public class PooledConnectionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.sql.PooledConnection");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("addConnectionEventListener"), "has method addConnectionEventListener");
        check(names.contains("addStatementEventListener"), "has method addStatementEventListener");
        check(names.contains("close"), "has method close");        System.out.println("PASS (" + passed + " assertions)");
    }
}
