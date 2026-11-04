/*
 * @test
 * @summary java.awt.font.MultipleMaster conformance: interface reflection.
 * @run main MultipleMasterTest
 */

public class MultipleMasterTest {
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
        Class<?> c = Class.forName("java.awt.font.MultipleMaster");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getNumDesignAxes"), "method getNumDesignAxes");
        check(hasMethod(c, "getDesignAxisRanges"), "method getDesignAxisRanges");
        check(hasMethod(c, "getDesignAxisDefaults"), "method getDesignAxisDefaults");
        check(hasMethod(c, "getDesignAxisNames"), "method getDesignAxisNames");
        check(hasMethod(c, "deriveMMFont"), "method deriveMMFont");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
