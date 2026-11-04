/*
 * @test
 * @summary java.awt.TextArea conformance: class reflection.
 * @run main TextAreaTest
 */

public class TextAreaTest {
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
        Class<?> c = Class.forName("java.awt.TextArea");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.TextComponent"), "superclass java.awt.TextComponent");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "addNotify"), "method addNotify");
        check(hasMethod(c, "insert"), "method insert");
        check(hasMethod(c, "insertText"), "method insertText");
        check(hasMethod(c, "append"), "method append");
        check(hasMethod(c, "appendText"), "method appendText");
        check(hasMethod(c, "replaceRange"), "method replaceRange");
        check(hasMethod(c, "replaceText"), "method replaceText");
        check(hasMethod(c, "getRows"), "method getRows");
        check(hasMethod(c, "setRows"), "method setRows");
        check(hasMethod(c, "getColumns"), "method getColumns");
        check(hasMethod(c, "setColumns"), "method setColumns");
        check(hasMethod(c, "getScrollbarVisibility"), "method getScrollbarVisibility");
        check(hasMethod(c, "getPreferredSize"), "method getPreferredSize");
        check(hasMethod(c, "preferredSize"), "method preferredSize");
        check(hasMethod(c, "getMinimumSize"), "method getMinimumSize");
        check(hasMethod(c, "minimumSize"), "method minimumSize");
        check(hasMethod(c, "getAccessibleContext"), "method getAccessibleContext");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
