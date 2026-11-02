/*
 * @test
 * @summary java.sql.SQLInput conformance: interface hierarchy and members.
 * @run main SQLInputTest
 */


public class SQLInputTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.sql.SQLInput");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("readArray"), "has method readArray");
        check(names.contains("readAsciiStream"), "has method readAsciiStream");
        check(names.contains("readBigDecimal"), "has method readBigDecimal");        System.out.println("PASS (" + passed + " assertions)");
    }
}
