/*
 * @test
 * @summary java.awt.event.InputEvent conformance: abstract class reflection.
 * @run main InputEventTest
 */

public class InputEventTest {
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
        Class<?> c = Class.forName("java.awt.event.InputEvent");
        check(!c.isInterface() && !c.isEnum(), "not interface/enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.awt.event.ComponentEvent"), "superclass java.awt.event.ComponentEvent");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getMaskForButton"), "method getMaskForButton");
        check(hasMethod(c, "isShiftDown"), "method isShiftDown");
        check(hasMethod(c, "isControlDown"), "method isControlDown");
        check(hasMethod(c, "isMetaDown"), "method isMetaDown");
        check(hasMethod(c, "isAltDown"), "method isAltDown");
        check(hasMethod(c, "isAltGraphDown"), "method isAltGraphDown");
        check(hasMethod(c, "getWhen"), "method getWhen");
        check(hasMethod(c, "getModifiers"), "method getModifiers");
        check(hasMethod(c, "getModifiersEx"), "method getModifiersEx");
        check(hasMethod(c, "consume"), "method consume");
        check(hasMethod(c, "isConsumed"), "method isConsumed");
        check(hasMethod(c, "getModifiersExText"), "method getModifiersExText");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
