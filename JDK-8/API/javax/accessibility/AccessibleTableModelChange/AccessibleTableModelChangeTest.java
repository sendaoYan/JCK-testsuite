/*
 * @test
 * @summary javax.accessibility.AccessibleTableModelChange conformance: interface reflection.
 * @run main AccessibleTableModelChangeTest
 */

public class AccessibleTableModelChangeTest {
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
        Class<?> c = Class.forName("javax.accessibility.AccessibleTableModelChange");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getType"), "method getType");
        check(hasMethod(c, "getFirstRow"), "method getFirstRow");
        check(hasMethod(c, "getLastRow"), "method getLastRow");
        check(hasMethod(c, "getFirstColumn"), "method getFirstColumn");
        check(hasMethod(c, "getLastColumn"), "method getLastColumn");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
