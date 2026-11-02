/*
 * @test
 * @summary javax.management.relation.RelationTypeSupport conformance: concrete class reflection.
 * @run main RelationTypeSupportTest
 */

public class RelationTypeSupportTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.relation.RelationTypeSupport");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check(Class.forName("javax.management.relation.RelationType").isAssignableFrom(c), "implements/extends javax.management.relation.RelationType");
        check(hasMethod(c, "getRelationTypeName"), "declares getRelationTypeName");
        check(hasMethod(c, "getRoleInfo"), "declares getRoleInfo");
        check(hasMethod(c, "getRoleInfos"), "declares getRoleInfos");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
