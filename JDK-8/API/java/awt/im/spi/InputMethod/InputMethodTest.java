/*
 * @test
 * @summary java.awt.im.spi.InputMethod conformance: interface reflection.
 * @run main InputMethodTest
 */

public class InputMethodTest {
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
        Class<?> c = Class.forName("java.awt.im.spi.InputMethod");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "setInputMethodContext"), "method setInputMethodContext");
        check(hasMethod(c, "setLocale"), "method setLocale");
        check(hasMethod(c, "getLocale"), "method getLocale");
        check(hasMethod(c, "setCharacterSubsets"), "method setCharacterSubsets");
        check(hasMethod(c, "setCompositionEnabled"), "method setCompositionEnabled");
        check(hasMethod(c, "isCompositionEnabled"), "method isCompositionEnabled");
        check(hasMethod(c, "reconvert"), "method reconvert");
        check(hasMethod(c, "dispatchEvent"), "method dispatchEvent");
        check(hasMethod(c, "notifyClientWindowChange"), "method notifyClientWindowChange");
        check(hasMethod(c, "activate"), "method activate");
        check(hasMethod(c, "deactivate"), "method deactivate");
        check(hasMethod(c, "hideWindows"), "method hideWindows");
        check(hasMethod(c, "removeNotify"), "method removeNotify");
        check(hasMethod(c, "endComposition"), "method endComposition");
        check(hasMethod(c, "dispose"), "method dispose");
        check(hasMethod(c, "getControlObject"), "method getControlObject");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
