/*
 * @test
 * @summary java.sql.Statement conformance: interface hierarchy and members.
 * @run main StatementTest
 */


public class StatementTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.sql.Statement");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> expIfs = new java.util.HashSet<String>();
        expIfs.add("java.lang.AutoCloseable");
        expIfs.add("java.sql.Wrapper");
        java.util.Set<String> actIfs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) actIfs.add(i.getName());
        check(actIfs.equals(expIfs), "direct interfaces");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("addBatch"), "has method addBatch");
        check(names.contains("cancel"), "has method cancel");
        check(names.contains("clearBatch"), "has method clearBatch");        System.out.println("PASS (" + passed + " assertions)");
    }
}
