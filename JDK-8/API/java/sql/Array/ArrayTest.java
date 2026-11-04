/*
 * @test
 * @summary java.sql.Array conformance: interface hierarchy and members.
 * @run main ArrayTest
 */


public class ArrayTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.sql.Array");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("free"), "has method free");
        check(names.contains("getArray"), "has method getArray");
        check(names.contains("getBaseType"), "has method getBaseType");        System.out.println("PASS (" + passed + " assertions)");
    }
}
