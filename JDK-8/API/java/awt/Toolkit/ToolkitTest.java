/*
 * @test
 * @summary java.awt.Toolkit conformance: abstract class reflection.
 * @run main ToolkitTest
 */

public class ToolkitTest {
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
        Class<?> c = Class.forName("java.awt.Toolkit");
        check(!c.isInterface() && !c.isEnum(), "not interface/enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "setDynamicLayout"), "method setDynamicLayout");
        check(hasMethod(c, "isDynamicLayoutActive"), "method isDynamicLayoutActive");
        check(hasMethod(c, "getScreenSize"), "method getScreenSize");
        check(hasMethod(c, "getScreenResolution"), "method getScreenResolution");
        check(hasMethod(c, "getScreenInsets"), "method getScreenInsets");
        check(hasMethod(c, "getColorModel"), "method getColorModel");
        check(hasMethod(c, "getFontList"), "method getFontList");
        check(hasMethod(c, "getFontMetrics"), "method getFontMetrics");
        check(hasMethod(c, "sync"), "method sync");
        check(hasMethod(c, "getDefaultToolkit"), "method getDefaultToolkit");
        check(hasMethod(c, "getImage"), "method getImage");
        check(hasMethod(c, "createImage"), "method createImage");
        check(hasMethod(c, "prepareImage"), "method prepareImage");
        check(hasMethod(c, "checkImage"), "method checkImage");
        check(hasMethod(c, "getPrintJob"), "method getPrintJob");
        check(hasMethod(c, "beep"), "method beep");
        check(hasMethod(c, "getSystemClipboard"), "method getSystemClipboard");
        check(hasMethod(c, "getSystemSelection"), "method getSystemSelection");
        check(hasMethod(c, "getMenuShortcutKeyMask"), "method getMenuShortcutKeyMask");
        check(hasMethod(c, "getLockingKeyState"), "method getLockingKeyState");
        check(hasMethod(c, "setLockingKeyState"), "method setLockingKeyState");
        check(hasMethod(c, "createCustomCursor"), "method createCustomCursor");
        check(hasMethod(c, "getBestCursorSize"), "method getBestCursorSize");
        check(hasMethod(c, "getMaximumCursorColors"), "method getMaximumCursorColors");
        check(hasMethod(c, "isFrameStateSupported"), "method isFrameStateSupported");
        check(hasMethod(c, "getProperty"), "method getProperty");
        check(hasMethod(c, "getSystemEventQueue"), "method getSystemEventQueue");
        check(hasMethod(c, "createDragSourceContextPeer"), "method createDragSourceContextPeer");
        check(hasMethod(c, "createDragGestureRecognizer"), "method createDragGestureRecognizer");
        check(hasMethod(c, "getDesktopProperty"), "method getDesktopProperty");
        check(hasMethod(c, "addPropertyChangeListener"), "method addPropertyChangeListener");
        check(hasMethod(c, "removePropertyChangeListener"), "method removePropertyChangeListener");
        check(hasMethod(c, "getPropertyChangeListeners"), "method getPropertyChangeListeners");
        check(hasMethod(c, "isAlwaysOnTopSupported"), "method isAlwaysOnTopSupported");
        check(hasMethod(c, "isModalityTypeSupported"), "method isModalityTypeSupported");
        check(hasMethod(c, "isModalExclusionTypeSupported"), "method isModalExclusionTypeSupported");
        check(hasMethod(c, "addAWTEventListener"), "method addAWTEventListener");
        check(hasMethod(c, "removeAWTEventListener"), "method removeAWTEventListener");
        check(hasMethod(c, "getAWTEventListeners"), "method getAWTEventListeners");
        check(hasMethod(c, "mapInputMethodHighlight"), "method mapInputMethodHighlight");
        check(hasMethod(c, "areExtraMouseButtonsEnabled"), "method areExtraMouseButtonsEnabled");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
