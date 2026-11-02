/*
 * @test
 * @summary java.awt.ScrollPane conformance: class reflection.
 * @run main ScrollPaneTest
 */

public class ScrollPaneTest {
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
        Class<?> c = Class.forName("java.awt.ScrollPane");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.Container"), "superclass java.awt.Container");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"javax.accessibility.Accessible"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "getScrollbarDisplayPolicy"), "method getScrollbarDisplayPolicy");
        check(hasMethod(c, "getViewportSize"), "method getViewportSize");
        check(hasMethod(c, "getHScrollbarHeight"), "method getHScrollbarHeight");
        check(hasMethod(c, "getVScrollbarWidth"), "method getVScrollbarWidth");
        check(hasMethod(c, "getVAdjustable"), "method getVAdjustable");
        check(hasMethod(c, "getHAdjustable"), "method getHAdjustable");
        check(hasMethod(c, "setScrollPosition"), "method setScrollPosition");
        check(hasMethod(c, "getScrollPosition"), "method getScrollPosition");
        check(hasMethod(c, "setLayout"), "method setLayout");
        check(hasMethod(c, "doLayout"), "method doLayout");
        check(hasMethod(c, "layout"), "method layout");
        check(hasMethod(c, "printComponents"), "method printComponents");
        check(hasMethod(c, "addNotify"), "method addNotify");
        check(hasMethod(c, "paramString"), "method paramString");
        check(hasMethod(c, "setWheelScrollingEnabled"), "method setWheelScrollingEnabled");
        check(hasMethod(c, "isWheelScrollingEnabled"), "method isWheelScrollingEnabled");
        check(hasMethod(c, "getAccessibleContext"), "method getAccessibleContext");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
