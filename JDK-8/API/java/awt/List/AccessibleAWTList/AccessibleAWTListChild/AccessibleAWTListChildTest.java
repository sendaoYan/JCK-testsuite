/*
 * @test
 * @summary java.awt.List.AccessibleAWTList.AccessibleAWTListChild conformance: class reflection.
 * @run main AccessibleAWTListChildTest
 */

public class AccessibleAWTListChildTest {
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
        Class<?> c = Class.forName("java.awt.List$AccessibleAWTList$AccessibleAWTListChild");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.Component$AccessibleAWTComponent"), "superclass java.awt.Component$AccessibleAWTComponent");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"javax.accessibility.Accessible"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "getAccessibleContext"), "method getAccessibleContext");
        check(hasMethod(c, "getAccessibleRole"), "method getAccessibleRole");
        check(hasMethod(c, "getAccessibleStateSet"), "method getAccessibleStateSet");
        check(hasMethod(c, "getLocale"), "method getLocale");
        check(hasMethod(c, "getAccessibleIndexInParent"), "method getAccessibleIndexInParent");
        check(hasMethod(c, "getAccessibleChildrenCount"), "method getAccessibleChildrenCount");
        check(hasMethod(c, "getAccessibleChild"), "method getAccessibleChild");
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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
