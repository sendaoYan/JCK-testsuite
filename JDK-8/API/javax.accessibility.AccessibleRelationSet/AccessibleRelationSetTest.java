/*
 * @test
 * @summary javax.accessibility.AccessibleRelationSet conformance: class reflection.
 * @run main AccessibleRelationSetTest
 */

public class AccessibleRelationSetTest {
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
        Class<?> c = Class.forName("javax.accessibility.AccessibleRelationSet");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "add"), "method add");
        check(hasMethod(c, "addAll"), "method addAll");
        check(hasMethod(c, "remove"), "method remove");
        check(hasMethod(c, "clear"), "method clear");
        check(hasMethod(c, "size"), "method size");
        check(hasMethod(c, "contains"), "method contains");
        check(hasMethod(c, "get"), "method get");
        check(hasMethod(c, "toArray"), "method toArray");
        check(hasMethod(c, "toString"), "method toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
