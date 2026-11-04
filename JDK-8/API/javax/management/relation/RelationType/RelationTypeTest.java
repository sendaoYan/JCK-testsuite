/*
 * @test
 * @summary javax.management.relation.RelationType conformance: interface reflection.
 * @run main RelationTypeTest
 */

public class RelationTypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.relation.RelationType");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(Class.forName("java.io.Serializable").isAssignableFrom(c), "implements/extends java.io.Serializable");
        check(hasMethod(c, "getRelationTypeName"), "declares getRelationTypeName");
        check(hasMethod(c, "getRoleInfo"), "declares getRoleInfo");
        check(hasMethod(c, "getRoleInfos"), "declares getRoleInfos");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
