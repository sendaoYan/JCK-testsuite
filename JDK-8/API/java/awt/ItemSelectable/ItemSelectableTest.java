/*
 * @test
 * @summary java.awt.ItemSelectable conformance: interface reflection.
 * @run main ItemSelectableTest
 */

public class ItemSelectableTest {
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
        Class<?> c = Class.forName("java.awt.ItemSelectable");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getSelectedObjects"), "method getSelectedObjects");
        check(hasMethod(c, "addItemListener"), "method addItemListener");
        check(hasMethod(c, "removeItemListener"), "method removeItemListener");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
