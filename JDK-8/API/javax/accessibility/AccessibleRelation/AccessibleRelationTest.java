/*
 * @test
 * @summary javax.accessibility.AccessibleRelation conformance: class reflection.
 * @run main AccessibleRelationTest
 */

public class AccessibleRelationTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.accessibility.AccessibleRelation");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("javax.accessibility.AccessibleBundle"), "superclass javax.accessibility.AccessibleBundle");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getKey"), "method getKey");
        check(hasMethod(c, "getTarget"), "method getTarget");
        check(hasMethod(c, "setTarget"), "method setTarget");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
