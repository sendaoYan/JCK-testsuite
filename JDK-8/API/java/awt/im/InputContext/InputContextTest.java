/*
 * @test
 * @summary java.awt.im.InputContext conformance: class reflection.
 * @run main InputContextTest
 */

public class InputContextTest {
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
        Class<?> c = Class.forName("java.awt.im.InputContext");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getInstance"), "method getInstance");
        check(hasMethod(c, "selectInputMethod"), "method selectInputMethod");
        check(hasMethod(c, "getLocale"), "method getLocale");
        check(hasMethod(c, "setCharacterSubsets"), "method setCharacterSubsets");
        check(hasMethod(c, "setCompositionEnabled"), "method setCompositionEnabled");
        check(hasMethod(c, "isCompositionEnabled"), "method isCompositionEnabled");
        check(hasMethod(c, "reconvert"), "method reconvert");
        check(hasMethod(c, "dispatchEvent"), "method dispatchEvent");
        check(hasMethod(c, "removeNotify"), "method removeNotify");
        check(hasMethod(c, "endComposition"), "method endComposition");
        check(hasMethod(c, "dispose"), "method dispose");
        check(hasMethod(c, "getInputMethodControlObject"), "method getInputMethodControlObject");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
