/*
 * @test
 * @summary javax.accessibility.AccessibleText conformance: interface reflection.
 * @run main AccessibleTextTest
 */

public class AccessibleTextTest {
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
        Class<?> c = Class.forName("javax.accessibility.AccessibleText");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getIndexAtPoint"), "method getIndexAtPoint");
        check(hasMethod(c, "getCharacterBounds"), "method getCharacterBounds");
        check(hasMethod(c, "getCharCount"), "method getCharCount");
        check(hasMethod(c, "getCaretPosition"), "method getCaretPosition");
        check(hasMethod(c, "getAtIndex"), "method getAtIndex");
        check(hasMethod(c, "getAfterIndex"), "method getAfterIndex");
        check(hasMethod(c, "getBeforeIndex"), "method getBeforeIndex");
        check(hasMethod(c, "getCharacterAttribute"), "method getCharacterAttribute");
        check(hasMethod(c, "getSelectionStart"), "method getSelectionStart");
        check(hasMethod(c, "getSelectionEnd"), "method getSelectionEnd");
        check(hasMethod(c, "getSelectedText"), "method getSelectedText");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
