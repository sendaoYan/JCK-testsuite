/*
 * @test
 * @summary java.awt.TextField conformance: class reflection.
 * @run main TextFieldTest
 */

public class TextFieldTest {
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
        Class<?> c = Class.forName("java.awt.TextField");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.TextComponent"), "superclass java.awt.TextComponent");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "addNotify"), "method addNotify");
        check(hasMethod(c, "getEchoChar"), "method getEchoChar");
        check(hasMethod(c, "setEchoChar"), "method setEchoChar");
        check(hasMethod(c, "setEchoCharacter"), "method setEchoCharacter");
        check(hasMethod(c, "setText"), "method setText");
        check(hasMethod(c, "echoCharIsSet"), "method echoCharIsSet");
        check(hasMethod(c, "getColumns"), "method getColumns");
        check(hasMethod(c, "setColumns"), "method setColumns");
        check(hasMethod(c, "getPreferredSize"), "method getPreferredSize");
        check(hasMethod(c, "preferredSize"), "method preferredSize");
        check(hasMethod(c, "getMinimumSize"), "method getMinimumSize");
        check(hasMethod(c, "minimumSize"), "method minimumSize");
        check(hasMethod(c, "addActionListener"), "method addActionListener");
        check(hasMethod(c, "removeActionListener"), "method removeActionListener");
        check(hasMethod(c, "getActionListeners"), "method getActionListeners");
        check(hasMethod(c, "getListeners"), "method getListeners");
        check(hasMethod(c, "getAccessibleContext"), "method getAccessibleContext");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
