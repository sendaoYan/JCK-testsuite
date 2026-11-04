/*
 * @test
 * @summary java.sql.SQLOutput conformance: interface hierarchy and members.
 * @run main SQLOutputTest
 */


public class SQLOutputTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.sql.SQLOutput");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("writeArray"), "has method writeArray");
        check(names.contains("writeAsciiStream"), "has method writeAsciiStream");
        check(names.contains("writeBigDecimal"), "has method writeBigDecimal");        System.out.println("PASS (" + passed + " assertions)");
    }
}
