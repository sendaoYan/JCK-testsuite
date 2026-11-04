/*
 * @test
 * @summary java.awt.MenuBar.AccessibleAWTMenuBar conformance: class reflection.
 * @run main AccessibleAWTMenuBarTest
 */

public class AccessibleAWTMenuBarTest {
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
        Class<?> c = Class.forName("java.awt.MenuBar$AccessibleAWTMenuBar");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.MenuComponent$AccessibleAWTMenuComponent"), "superclass java.awt.MenuComponent$AccessibleAWTMenuComponent");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getAccessibleRole"), "method getAccessibleRole");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
