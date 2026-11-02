/*
 * @test
 * @summary javax.sql.RowSetWriter conformance: interface hierarchy and members.
 * @run main RowSetWriterTest
 */


public class RowSetWriterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.sql.RowSetWriter");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("writeData"), "has method writeData");        System.out.println("PASS (" + passed + " assertions)");
    }
}
