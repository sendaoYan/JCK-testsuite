/*
 * @test
 * @summary java.sql.Savepoint conformance: interface hierarchy and members.
 * @run main SavepointTest
 */


public class SavepointTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.sql.Savepoint");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getSavepointId"), "has method getSavepointId");
        check(names.contains("getSavepointName"), "has method getSavepointName");        System.out.println("PASS (" + passed + " assertions)");
    }
}
