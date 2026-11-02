/*
 * @test
 * @summary java.awt.MenuComponent.AccessibleAWTMenuComponent conformance: abstract class reflection.
 * @run main AccessibleAWTMenuComponentTest
 */

public class AccessibleAWTMenuComponentTest {
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
        Class<?> c = Class.forName("java.awt.MenuComponent$AccessibleAWTMenuComponent");
        check(!c.isInterface() && !c.isEnum(), "not interface/enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("javax.accessibility.AccessibleContext"), "superclass javax.accessibility.AccessibleContext");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.io.Serializable", "javax.accessibility.AccessibleComponent", "javax.accessibility.AccessibleSelection"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "getAccessibleSelection"), "method getAccessibleSelection");
        check(hasMethod(c, "getAccessibleName"), "method getAccessibleName");
        check(hasMethod(c, "getAccessibleDescription"), "method getAccessibleDescription");
        check(hasMethod(c, "getAccessibleRole"), "method getAccessibleRole");
        check(hasMethod(c, "getAccessibleStateSet"), "method getAccessibleStateSet");
        check(hasMethod(c, "getAccessibleParent"), "method getAccessibleParent");
        check(hasMethod(c, "getAccessibleIndexInParent"), "method getAccessibleIndexInParent");
        check(hasMethod(c, "getAccessibleChildrenCount"), "method getAccessibleChildrenCount");
        check(hasMethod(c, "getAccessibleChild"), "method getAccessibleChild");
        check(hasMethod(c, "getLocale"), "method getLocale");
        check(hasMethod(c, "getAccessibleComponent"), "method getAccessibleComponent");
        check(hasMethod(c, "getBackground"), "method getBackground");
        check(hasMethod(c, "setBackground"), "method setBackground");
        check(hasMethod(c, "getForeground"), "method getForeground");
        check(hasMethod(c, "setForeground"), "method setForeground");
        check(hasMethod(c, "getCursor"), "method getCursor");
        check(hasMethod(c, "setCursor"), "method setCursor");
        check(hasMethod(c, "getFont"), "method getFont");
        check(hasMethod(c, "setFont"), "method setFont");
        check(hasMethod(c, "getFontMetrics"), "method getFontMetrics");
        check(hasMethod(c, "isEnabled"), "method isEnabled");
        check(hasMethod(c, "setEnabled"), "method setEnabled");
        check(hasMethod(c, "isVisible"), "method isVisible");
        check(hasMethod(c, "setVisible"), "method setVisible");
        check(hasMethod(c, "isShowing"), "method isShowing");
        check(hasMethod(c, "contains"), "method contains");
        check(hasMethod(c, "getLocationOnScreen"), "method getLocationOnScreen");
        check(hasMethod(c, "getLocation"), "method getLocation");
        check(hasMethod(c, "setLocation"), "method setLocation");
        check(hasMethod(c, "getBounds"), "method getBounds");
        check(hasMethod(c, "setBounds"), "method setBounds");
        check(hasMethod(c, "getSize"), "method getSize");
        check(hasMethod(c, "setSize"), "method setSize");
        check(hasMethod(c, "getAccessibleAt"), "method getAccessibleAt");
        check(hasMethod(c, "isFocusTraversable"), "method isFocusTraversable");
        check(hasMethod(c, "requestFocus"), "method requestFocus");
        check(hasMethod(c, "addFocusListener"), "method addFocusListener");
        check(hasMethod(c, "removeFocusListener"), "method removeFocusListener");
        check(hasMethod(c, "getAccessibleSelectionCount"), "method getAccessibleSelectionCount");
        check(hasMethod(c, "isAccessibleChildSelected"), "method isAccessibleChildSelected");
        check(hasMethod(c, "addAccessibleSelection"), "method addAccessibleSelection");
        check(hasMethod(c, "removeAccessibleSelection"), "method removeAccessibleSelection");
        check(hasMethod(c, "clearAccessibleSelection"), "method clearAccessibleSelection");
        check(hasMethod(c, "selectAllAccessibleSelection"), "method selectAllAccessibleSelection");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
