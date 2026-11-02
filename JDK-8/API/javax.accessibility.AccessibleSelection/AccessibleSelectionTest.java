/*
 * @test
 * @summary javax.accessibility.AccessibleSelection conformance: interface reflection.
 * @run main AccessibleSelectionTest
 */

public class AccessibleSelectionTest {
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
        Class<?> c = Class.forName("javax.accessibility.AccessibleSelection");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getAccessibleSelectionCount"), "method getAccessibleSelectionCount");
        check(hasMethod(c, "getAccessibleSelection"), "method getAccessibleSelection");
        check(hasMethod(c, "isAccessibleChildSelected"), "method isAccessibleChildSelected");
        check(hasMethod(c, "addAccessibleSelection"), "method addAccessibleSelection");
        check(hasMethod(c, "removeAccessibleSelection"), "method removeAccessibleSelection");
        check(hasMethod(c, "clearAccessibleSelection"), "method clearAccessibleSelection");
        check(hasMethod(c, "selectAllAccessibleSelection"), "method selectAllAccessibleSelection");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
