/*
 * @test
 * @summary java.awt.KeyboardFocusManager conformance: abstract class reflection.
 * @run main KeyboardFocusManagerTest
 */

public class KeyboardFocusManagerTest {
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
        Class<?> c = Class.forName("java.awt.KeyboardFocusManager");
        check(!c.isInterface() && !c.isEnum(), "not interface/enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.awt.KeyEventDispatcher", "java.awt.KeyEventPostProcessor"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "getCurrentKeyboardFocusManager"), "method getCurrentKeyboardFocusManager");
        check(hasMethod(c, "setCurrentKeyboardFocusManager"), "method setCurrentKeyboardFocusManager");
        check(hasMethod(c, "getFocusOwner"), "method getFocusOwner");
        check(hasMethod(c, "clearFocusOwner"), "method clearFocusOwner");
        check(hasMethod(c, "clearGlobalFocusOwner"), "method clearGlobalFocusOwner");
        check(hasMethod(c, "getPermanentFocusOwner"), "method getPermanentFocusOwner");
        check(hasMethod(c, "getFocusedWindow"), "method getFocusedWindow");
        check(hasMethod(c, "getActiveWindow"), "method getActiveWindow");
        check(hasMethod(c, "getDefaultFocusTraversalPolicy"), "method getDefaultFocusTraversalPolicy");
        check(hasMethod(c, "setDefaultFocusTraversalPolicy"), "method setDefaultFocusTraversalPolicy");
        check(hasMethod(c, "setDefaultFocusTraversalKeys"), "method setDefaultFocusTraversalKeys");
        check(hasMethod(c, "getDefaultFocusTraversalKeys"), "method getDefaultFocusTraversalKeys");
        check(hasMethod(c, "getCurrentFocusCycleRoot"), "method getCurrentFocusCycleRoot");
        check(hasMethod(c, "setGlobalCurrentFocusCycleRoot"), "method setGlobalCurrentFocusCycleRoot");
        check(hasMethod(c, "addPropertyChangeListener"), "method addPropertyChangeListener");
        check(hasMethod(c, "removePropertyChangeListener"), "method removePropertyChangeListener");
        check(hasMethod(c, "getPropertyChangeListeners"), "method getPropertyChangeListeners");
        check(hasMethod(c, "addVetoableChangeListener"), "method addVetoableChangeListener");
        check(hasMethod(c, "removeVetoableChangeListener"), "method removeVetoableChangeListener");
        check(hasMethod(c, "getVetoableChangeListeners"), "method getVetoableChangeListeners");
        check(hasMethod(c, "addKeyEventDispatcher"), "method addKeyEventDispatcher");
        check(hasMethod(c, "removeKeyEventDispatcher"), "method removeKeyEventDispatcher");
        check(hasMethod(c, "addKeyEventPostProcessor"), "method addKeyEventPostProcessor");
        check(hasMethod(c, "removeKeyEventPostProcessor"), "method removeKeyEventPostProcessor");
        check(hasMethod(c, "dispatchEvent"), "method dispatchEvent");
        check(hasMethod(c, "redispatchEvent"), "method redispatchEvent");
        check(hasMethod(c, "dispatchKeyEvent"), "method dispatchKeyEvent");
        check(hasMethod(c, "postProcessKeyEvent"), "method postProcessKeyEvent");
        check(hasMethod(c, "processKeyEvent"), "method processKeyEvent");
        check(hasMethod(c, "focusNextComponent"), "method focusNextComponent");
        check(hasMethod(c, "focusPreviousComponent"), "method focusPreviousComponent");
        check(hasMethod(c, "upFocusCycle"), "method upFocusCycle");
        check(hasMethod(c, "downFocusCycle"), "method downFocusCycle");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
