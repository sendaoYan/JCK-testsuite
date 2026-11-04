/*
 * @test
 * @summary java.awt.Dialog conformance: class reflection.
 * @run main DialogTest
 */

public class DialogTest {
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
        Class<?> c = Class.forName("java.awt.Dialog");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.Window"), "superclass java.awt.Window");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "addNotify"), "method addNotify");
        check(hasMethod(c, "isModal"), "method isModal");
        check(hasMethod(c, "setModal"), "method setModal");
        check(hasMethod(c, "getModalityType"), "method getModalityType");
        check(hasMethod(c, "setModalityType"), "method setModalityType");
        check(hasMethod(c, "getTitle"), "method getTitle");
        check(hasMethod(c, "setTitle"), "method setTitle");
        check(hasMethod(c, "setVisible"), "method setVisible");
        check(hasMethod(c, "show"), "method show");
        check(hasMethod(c, "hide"), "method hide");
        check(hasMethod(c, "toBack"), "method toBack");
        check(hasMethod(c, "isResizable"), "method isResizable");
        check(hasMethod(c, "setResizable"), "method setResizable");
        check(hasMethod(c, "setUndecorated"), "method setUndecorated");
        check(hasMethod(c, "isUndecorated"), "method isUndecorated");
        check(hasMethod(c, "setOpacity"), "method setOpacity");
        check(hasMethod(c, "setShape"), "method setShape");
        check(hasMethod(c, "setBackground"), "method setBackground");
        check(hasMethod(c, "getAccessibleContext"), "method getAccessibleContext");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
