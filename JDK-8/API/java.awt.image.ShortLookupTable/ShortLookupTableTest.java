/*
 * @test
 * @summary java.awt.image.ShortLookupTable conformance: class reflection.
 * @run main ShortLookupTableTest
 */

public class ShortLookupTableTest {
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
        Class<?> c = Class.forName("java.awt.image.ShortLookupTable");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.image.LookupTable"), "superclass java.awt.image.LookupTable");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getTable"), "method getTable");
        check(hasMethod(c, "lookupPixel"), "method lookupPixel");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
