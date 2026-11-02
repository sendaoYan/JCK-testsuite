/*
 * @test
 * @summary java.awt.DefaultKeyboardFocusManager conformance: class reflection.
 * @run main DefaultKeyboardFocusManagerTest
 */

public class DefaultKeyboardFocusManagerTest {
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
        Class<?> c = Class.forName("java.awt.DefaultKeyboardFocusManager");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.KeyboardFocusManager"), "superclass java.awt.KeyboardFocusManager");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "dispatchEvent"), "method dispatchEvent");
        check(hasMethod(c, "dispatchKeyEvent"), "method dispatchKeyEvent");
        check(hasMethod(c, "postProcessKeyEvent"), "method postProcessKeyEvent");
        check(hasMethod(c, "processKeyEvent"), "method processKeyEvent");
        check(hasMethod(c, "focusPreviousComponent"), "method focusPreviousComponent");
        check(hasMethod(c, "focusNextComponent"), "method focusNextComponent");
        check(hasMethod(c, "upFocusCycle"), "method upFocusCycle");
        check(hasMethod(c, "downFocusCycle"), "method downFocusCycle");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
