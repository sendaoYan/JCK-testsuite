/*
 * @test
 * @summary java.awt.Frame conformance: class reflection.
 * @run main FrameTest
 */

public class FrameTest {
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
        Class<?> c = Class.forName("java.awt.Frame");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.Window"), "superclass java.awt.Window");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.awt.MenuContainer"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "addNotify"), "method addNotify");
        check(hasMethod(c, "getTitle"), "method getTitle");
        check(hasMethod(c, "setTitle"), "method setTitle");
        check(hasMethod(c, "getIconImage"), "method getIconImage");
        check(hasMethod(c, "setIconImage"), "method setIconImage");
        check(hasMethod(c, "getMenuBar"), "method getMenuBar");
        check(hasMethod(c, "setMenuBar"), "method setMenuBar");
        check(hasMethod(c, "isResizable"), "method isResizable");
        check(hasMethod(c, "setResizable"), "method setResizable");
        check(hasMethod(c, "setState"), "method setState");
        check(hasMethod(c, "setExtendedState"), "method setExtendedState");
        check(hasMethod(c, "getState"), "method getState");
        check(hasMethod(c, "getExtendedState"), "method getExtendedState");
        check(hasMethod(c, "setMaximizedBounds"), "method setMaximizedBounds");
        check(hasMethod(c, "getMaximizedBounds"), "method getMaximizedBounds");
        check(hasMethod(c, "setUndecorated"), "method setUndecorated");
        check(hasMethod(c, "isUndecorated"), "method isUndecorated");
        check(hasMethod(c, "setOpacity"), "method setOpacity");
        check(hasMethod(c, "setShape"), "method setShape");
        check(hasMethod(c, "setBackground"), "method setBackground");
        check(hasMethod(c, "remove"), "method remove");
        check(hasMethod(c, "removeNotify"), "method removeNotify");
        check(hasMethod(c, "setCursor"), "method setCursor");
        check(hasMethod(c, "getCursorType"), "method getCursorType");
        check(hasMethod(c, "getFrames"), "method getFrames");
        check(hasMethod(c, "getAccessibleContext"), "method getAccessibleContext");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
