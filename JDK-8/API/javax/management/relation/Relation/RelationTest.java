/*
 * @test
 * @summary javax.management.relation.Relation conformance: interface reflection.
 * @run main RelationTest
 */

public class RelationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.relation.Relation");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(hasMethod(c, "getAllRoles"), "declares getAllRoles");
        check(hasMethod(c, "getReferencedMBeans"), "declares getReferencedMBeans");
        check(hasMethod(c, "getRelationId"), "declares getRelationId");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
