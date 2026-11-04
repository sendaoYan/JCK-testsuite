/*
 * @test
 * @summary java.awt.font.OpenType conformance: interface reflection.
 * @run main OpenTypeTest
 */

public class OpenTypeTest {
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
        Class<?> c = Class.forName("java.awt.font.OpenType");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getVersion"), "method getVersion");
        check(hasMethod(c, "getFontTable"), "method getFontTable");
        check(hasMethod(c, "getFontTableSize"), "method getFontTableSize");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
