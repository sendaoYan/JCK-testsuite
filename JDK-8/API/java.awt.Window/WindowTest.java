/*
 * @test
 * @summary java.awt.Window conformance: class reflection.
 * @run main WindowTest
 */

public class WindowTest {
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
        Class<?> c = Class.forName("java.awt.Window");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.Container"), "superclass java.awt.Container");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"javax.accessibility.Accessible"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "getIconImages"), "method getIconImages");
        check(hasMethod(c, "setIconImages"), "method setIconImages");
        check(hasMethod(c, "setIconImage"), "method setIconImage");
        check(hasMethod(c, "addNotify"), "method addNotify");
        check(hasMethod(c, "removeNotify"), "method removeNotify");
        check(hasMethod(c, "pack"), "method pack");
        check(hasMethod(c, "setMinimumSize"), "method setMinimumSize");
        check(hasMethod(c, "setSize"), "method setSize");
        check(hasMethod(c, "setLocation"), "method setLocation");
        check(hasMethod(c, "reshape"), "method reshape");
        check(hasMethod(c, "setVisible"), "method setVisible");
        check(hasMethod(c, "show"), "method show");
        check(hasMethod(c, "hide"), "method hide");
        check(hasMethod(c, "dispose"), "method dispose");
        check(hasMethod(c, "toFront"), "method toFront");
        check(hasMethod(c, "toBack"), "method toBack");
        check(hasMethod(c, "getToolkit"), "method getToolkit");
        check(hasMethod(c, "getWarningString"), "method getWarningString");
        check(hasMethod(c, "getLocale"), "method getLocale");
        check(hasMethod(c, "getInputContext"), "method getInputContext");
        check(hasMethod(c, "setCursor"), "method setCursor");
        check(hasMethod(c, "getOwner"), "method getOwner");
        check(hasMethod(c, "getOwnedWindows"), "method getOwnedWindows");
        check(hasMethod(c, "getWindows"), "method getWindows");
        check(hasMethod(c, "getOwnerlessWindows"), "method getOwnerlessWindows");
        check(hasMethod(c, "setModalExclusionType"), "method setModalExclusionType");
        check(hasMethod(c, "getModalExclusionType"), "method getModalExclusionType");
        check(hasMethod(c, "addWindowListener"), "method addWindowListener");
        check(hasMethod(c, "addWindowStateListener"), "method addWindowStateListener");
        check(hasMethod(c, "addWindowFocusListener"), "method addWindowFocusListener");
        check(hasMethod(c, "removeWindowListener"), "method removeWindowListener");
        check(hasMethod(c, "removeWindowStateListener"), "method removeWindowStateListener");
        check(hasMethod(c, "removeWindowFocusListener"), "method removeWindowFocusListener");
        check(hasMethod(c, "getWindowListeners"), "method getWindowListeners");
        check(hasMethod(c, "getWindowFocusListeners"), "method getWindowFocusListeners");
        check(hasMethod(c, "getWindowStateListeners"), "method getWindowStateListeners");
        check(hasMethod(c, "getListeners"), "method getListeners");
        check(hasMethod(c, "setAlwaysOnTop"), "method setAlwaysOnTop");
        check(hasMethod(c, "isAlwaysOnTopSupported"), "method isAlwaysOnTopSupported");
        check(hasMethod(c, "isAlwaysOnTop"), "method isAlwaysOnTop");
        check(hasMethod(c, "getFocusOwner"), "method getFocusOwner");
        check(hasMethod(c, "getMostRecentFocusOwner"), "method getMostRecentFocusOwner");
        check(hasMethod(c, "isActive"), "method isActive");
        check(hasMethod(c, "isFocused"), "method isFocused");
        check(hasMethod(c, "getFocusTraversalKeys"), "method getFocusTraversalKeys");
        check(hasMethod(c, "setFocusCycleRoot"), "method setFocusCycleRoot");
        check(hasMethod(c, "isFocusCycleRoot"), "method isFocusCycleRoot");
        check(hasMethod(c, "getFocusCycleRootAncestor"), "method getFocusCycleRootAncestor");
        check(hasMethod(c, "isFocusableWindow"), "method isFocusableWindow");
        check(hasMethod(c, "getFocusableWindowState"), "method getFocusableWindowState");
        check(hasMethod(c, "setFocusableWindowState"), "method setFocusableWindowState");
        check(hasMethod(c, "setAutoRequestFocus"), "method setAutoRequestFocus");
        check(hasMethod(c, "isAutoRequestFocus"), "method isAutoRequestFocus");
        check(hasMethod(c, "addPropertyChangeListener"), "method addPropertyChangeListener");
        check(hasMethod(c, "isValidateRoot"), "method isValidateRoot");
        check(hasMethod(c, "postEvent"), "method postEvent");
        check(hasMethod(c, "isShowing"), "method isShowing");
        check(hasMethod(c, "applyResourceBundle"), "method applyResourceBundle");
        check(hasMethod(c, "setType"), "method setType");
        check(hasMethod(c, "getType"), "method getType");
        check(hasMethod(c, "getAccessibleContext"), "method getAccessibleContext");
        check(hasMethod(c, "setLocationRelativeTo"), "method setLocationRelativeTo");
        check(hasMethod(c, "createBufferStrategy"), "method createBufferStrategy");
        check(hasMethod(c, "getBufferStrategy"), "method getBufferStrategy");
        check(hasMethod(c, "setLocationByPlatform"), "method setLocationByPlatform");
        check(hasMethod(c, "isLocationByPlatform"), "method isLocationByPlatform");
        check(hasMethod(c, "setBounds"), "method setBounds");
        check(hasMethod(c, "getOpacity"), "method getOpacity");
        check(hasMethod(c, "setOpacity"), "method setOpacity");
        check(hasMethod(c, "getShape"), "method getShape");
        check(hasMethod(c, "setShape"), "method setShape");
        check(hasMethod(c, "getBackground"), "method getBackground");
        check(hasMethod(c, "setBackground"), "method setBackground");
        check(hasMethod(c, "isOpaque"), "method isOpaque");
        check(hasMethod(c, "paint"), "method paint");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
