/*
 * @test
 * @summary java.sql.Clob conformance: interface hierarchy and members.
 * @run main ClobTest
 */


public class ClobTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.sql.Clob");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("free"), "has method free");
        check(names.contains("getAsciiStream"), "has method getAsciiStream");
        check(names.contains("getCharacterStream"), "has method getCharacterStream");        System.out.println("PASS (" + passed + " assertions)");
    }
}
