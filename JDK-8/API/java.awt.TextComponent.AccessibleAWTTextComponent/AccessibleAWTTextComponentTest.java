/*
 * @test
 * @summary java.awt.TextComponent.AccessibleAWTTextComponent conformance: class reflection.
 * @run main AccessibleAWTTextComponentTest
 */

public class AccessibleAWTTextComponentTest {
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
        Class<?> c = Class.forName("java.awt.TextComponent$AccessibleAWTTextComponent");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.Component$AccessibleAWTComponent"), "superclass java.awt.Component$AccessibleAWTComponent");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.awt.event.TextListener", "javax.accessibility.AccessibleText"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "textValueChanged"), "method textValueChanged");
        check(hasMethod(c, "getAccessibleStateSet"), "method getAccessibleStateSet");
        check(hasMethod(c, "getAccessibleRole"), "method getAccessibleRole");
        check(hasMethod(c, "getAccessibleText"), "method getAccessibleText");
        check(hasMethod(c, "getIndexAtPoint"), "method getIndexAtPoint");
        check(hasMethod(c, "getCharacterBounds"), "method getCharacterBounds");
        check(hasMethod(c, "getCharCount"), "method getCharCount");
        check(hasMethod(c, "getCaretPosition"), "method getCaretPosition");
        check(hasMethod(c, "getCharacterAttribute"), "method getCharacterAttribute");
        check(hasMethod(c, "getSelectionStart"), "method getSelectionStart");
        check(hasMethod(c, "getSelectionEnd"), "method getSelectionEnd");
        check(hasMethod(c, "getSelectedText"), "method getSelectedText");
        check(hasMethod(c, "getAtIndex"), "method getAtIndex");
        check(hasMethod(c, "getAfterIndex"), "method getAfterIndex");
        check(hasMethod(c, "getBeforeIndex"), "method getBeforeIndex");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
