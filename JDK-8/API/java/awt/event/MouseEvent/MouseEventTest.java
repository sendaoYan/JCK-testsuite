/*
 * @test
 * @summary java.awt.event.MouseEvent conformance: class reflection.
 * @run main MouseEventTest
 */

public class MouseEventTest {
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
        Class<?> c = Class.forName("java.awt.event.MouseEvent");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.event.InputEvent"), "superclass java.awt.event.InputEvent");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getLocationOnScreen"), "method getLocationOnScreen");
        check(hasMethod(c, "getXOnScreen"), "method getXOnScreen");
        check(hasMethod(c, "getYOnScreen"), "method getYOnScreen");
        check(hasMethod(c, "getModifiersEx"), "method getModifiersEx");
        check(hasMethod(c, "getX"), "method getX");
        check(hasMethod(c, "getY"), "method getY");
        check(hasMethod(c, "getPoint"), "method getPoint");
        check(hasMethod(c, "translatePoint"), "method translatePoint");
        check(hasMethod(c, "getClickCount"), "method getClickCount");
        check(hasMethod(c, "getButton"), "method getButton");
        check(hasMethod(c, "isPopupTrigger"), "method isPopupTrigger");
        check(hasMethod(c, "getMouseModifiersText"), "method getMouseModifiersText");
        check(hasMethod(c, "paramString"), "method paramString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
