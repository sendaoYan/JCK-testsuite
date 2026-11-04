/*
 * @test
 * @summary java.awt.JobAttributes.DefaultSelectionType conformance: class reflection.
 * @run main DefaultSelectionTypeTest
 */

public class DefaultSelectionTypeTest {
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
        Class<?> c = Class.forName("java.awt.JobAttributes$DefaultSelectionType");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.AttributeValue"), "superclass java.awt.AttributeValue");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "toString"), "method toString");
        check(hasMethod(c, "hashCode"), "method hashCode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
