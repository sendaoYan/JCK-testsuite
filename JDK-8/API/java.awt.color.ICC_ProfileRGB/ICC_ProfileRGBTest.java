/*
 * @test
 * @summary java.awt.color.ICC_ProfileRGB conformance: class reflection.
 * @run main ICC_ProfileRGBTest
 */

public class ICC_ProfileRGBTest {
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
        Class<?> c = Class.forName("java.awt.color.ICC_ProfileRGB");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.color.ICC_Profile"), "superclass java.awt.color.ICC_Profile");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getMediaWhitePoint"), "method getMediaWhitePoint");
        check(hasMethod(c, "getMatrix"), "method getMatrix");
        check(hasMethod(c, "getGamma"), "method getGamma");
        check(hasMethod(c, "getTRC"), "method getTRC");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
