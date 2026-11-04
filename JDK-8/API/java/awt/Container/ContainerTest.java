/*
 * @test
 * @summary java.awt.Container conformance: class reflection.
 * @run main ContainerTest
 */

public class ContainerTest {
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
        Class<?> c = Class.forName("java.awt.Container");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.Component"), "superclass java.awt.Component");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getComponentCount"), "method getComponentCount");
        check(hasMethod(c, "countComponents"), "method countComponents");
        check(hasMethod(c, "getComponent"), "method getComponent");
        check(hasMethod(c, "getComponents"), "method getComponents");
        check(hasMethod(c, "getInsets"), "method getInsets");
        check(hasMethod(c, "insets"), "method insets");
        check(hasMethod(c, "add"), "method add");
        check(hasMethod(c, "setComponentZOrder"), "method setComponentZOrder");
        check(hasMethod(c, "getComponentZOrder"), "method getComponentZOrder");
        check(hasMethod(c, "remove"), "method remove");
        check(hasMethod(c, "removeAll"), "method removeAll");
        check(hasMethod(c, "getLayout"), "method getLayout");
        check(hasMethod(c, "setLayout"), "method setLayout");
        check(hasMethod(c, "doLayout"), "method doLayout");
        check(hasMethod(c, "layout"), "method layout");
        check(hasMethod(c, "isValidateRoot"), "method isValidateRoot");
        check(hasMethod(c, "invalidate"), "method invalidate");
        check(hasMethod(c, "validate"), "method validate");
        check(hasMethod(c, "setFont"), "method setFont");
        check(hasMethod(c, "getPreferredSize"), "method getPreferredSize");
        check(hasMethod(c, "preferredSize"), "method preferredSize");
        check(hasMethod(c, "getMinimumSize"), "method getMinimumSize");
        check(hasMethod(c, "minimumSize"), "method minimumSize");
        check(hasMethod(c, "getMaximumSize"), "method getMaximumSize");
        check(hasMethod(c, "getAlignmentX"), "method getAlignmentX");
        check(hasMethod(c, "getAlignmentY"), "method getAlignmentY");
        check(hasMethod(c, "paint"), "method paint");
        check(hasMethod(c, "update"), "method update");
        check(hasMethod(c, "print"), "method print");
        check(hasMethod(c, "paintComponents"), "method paintComponents");
        check(hasMethod(c, "printComponents"), "method printComponents");
        check(hasMethod(c, "addContainerListener"), "method addContainerListener");
        check(hasMethod(c, "removeContainerListener"), "method removeContainerListener");
        check(hasMethod(c, "getContainerListeners"), "method getContainerListeners");
        check(hasMethod(c, "getListeners"), "method getListeners");
        check(hasMethod(c, "deliverEvent"), "method deliverEvent");
        check(hasMethod(c, "getComponentAt"), "method getComponentAt");
        check(hasMethod(c, "locate"), "method locate");
        check(hasMethod(c, "getMousePosition"), "method getMousePosition");
        check(hasMethod(c, "findComponentAt"), "method findComponentAt");
        check(hasMethod(c, "addNotify"), "method addNotify");
        check(hasMethod(c, "removeNotify"), "method removeNotify");
        check(hasMethod(c, "isAncestorOf"), "method isAncestorOf");
        check(hasMethod(c, "list"), "method list");
        check(hasMethod(c, "setFocusTraversalKeys"), "method setFocusTraversalKeys");
        check(hasMethod(c, "getFocusTraversalKeys"), "method getFocusTraversalKeys");
        check(hasMethod(c, "areFocusTraversalKeysSet"), "method areFocusTraversalKeysSet");
        check(hasMethod(c, "isFocusCycleRoot"), "method isFocusCycleRoot");
        check(hasMethod(c, "setFocusTraversalPolicy"), "method setFocusTraversalPolicy");
        check(hasMethod(c, "getFocusTraversalPolicy"), "method getFocusTraversalPolicy");
        check(hasMethod(c, "isFocusTraversalPolicySet"), "method isFocusTraversalPolicySet");
        check(hasMethod(c, "setFocusCycleRoot"), "method setFocusCycleRoot");
        check(hasMethod(c, "setFocusTraversalPolicyProvider"), "method setFocusTraversalPolicyProvider");
        check(hasMethod(c, "isFocusTraversalPolicyProvider"), "method isFocusTraversalPolicyProvider");
        check(hasMethod(c, "transferFocusDownCycle"), "method transferFocusDownCycle");
        check(hasMethod(c, "applyComponentOrientation"), "method applyComponentOrientation");
        check(hasMethod(c, "addPropertyChangeListener"), "method addPropertyChangeListener");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
