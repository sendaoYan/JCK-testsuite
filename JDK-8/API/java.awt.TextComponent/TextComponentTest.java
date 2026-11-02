/*
 * @test
 * @summary java.awt.TextComponent conformance: class reflection.
 * @run main TextComponentTest
 */

public class TextComponentTest {
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
        Class<?> c = Class.forName("java.awt.TextComponent");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.Component"), "superclass java.awt.Component");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"javax.accessibility.Accessible"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "enableInputMethods"), "method enableInputMethods");
        check(hasMethod(c, "getInputMethodRequests"), "method getInputMethodRequests");
        check(hasMethod(c, "addNotify"), "method addNotify");
        check(hasMethod(c, "removeNotify"), "method removeNotify");
        check(hasMethod(c, "setText"), "method setText");
        check(hasMethod(c, "getText"), "method getText");
        check(hasMethod(c, "getSelectedText"), "method getSelectedText");
        check(hasMethod(c, "isEditable"), "method isEditable");
        check(hasMethod(c, "setEditable"), "method setEditable");
        check(hasMethod(c, "getBackground"), "method getBackground");
        check(hasMethod(c, "setBackground"), "method setBackground");
        check(hasMethod(c, "getSelectionStart"), "method getSelectionStart");
        check(hasMethod(c, "setSelectionStart"), "method setSelectionStart");
        check(hasMethod(c, "getSelectionEnd"), "method getSelectionEnd");
        check(hasMethod(c, "setSelectionEnd"), "method setSelectionEnd");
        check(hasMethod(c, "select"), "method select");
        check(hasMethod(c, "selectAll"), "method selectAll");
        check(hasMethod(c, "setCaretPosition"), "method setCaretPosition");
        check(hasMethod(c, "getCaretPosition"), "method getCaretPosition");
        check(hasMethod(c, "addTextListener"), "method addTextListener");
        check(hasMethod(c, "removeTextListener"), "method removeTextListener");
        check(hasMethod(c, "getTextListeners"), "method getTextListeners");
        check(hasMethod(c, "getListeners"), "method getListeners");
        check(hasMethod(c, "getAccessibleContext"), "method getAccessibleContext");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
