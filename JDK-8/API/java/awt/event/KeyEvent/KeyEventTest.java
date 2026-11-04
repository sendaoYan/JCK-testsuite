/*
 * @test
 * @summary java.awt.event.KeyEvent conformance: class reflection.
 * @run main KeyEventTest
 */

public class KeyEventTest {
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
        Class<?> c = Class.forName("java.awt.event.KeyEvent");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.event.InputEvent"), "superclass java.awt.event.InputEvent");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getKeyCode"), "method getKeyCode");
        check(hasMethod(c, "setKeyCode"), "method setKeyCode");
        check(hasMethod(c, "getKeyChar"), "method getKeyChar");
        check(hasMethod(c, "setKeyChar"), "method setKeyChar");
        check(hasMethod(c, "setModifiers"), "method setModifiers");
        check(hasMethod(c, "getKeyLocation"), "method getKeyLocation");
        check(hasMethod(c, "getKeyText"), "method getKeyText");
        check(hasMethod(c, "getKeyModifiersText"), "method getKeyModifiersText");
        check(hasMethod(c, "isActionKey"), "method isActionKey");
        check(hasMethod(c, "paramString"), "method paramString");
        check(hasMethod(c, "getExtendedKeyCode"), "method getExtendedKeyCode");
        check(hasMethod(c, "getExtendedKeyCodeForChar"), "method getExtendedKeyCodeForChar");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
