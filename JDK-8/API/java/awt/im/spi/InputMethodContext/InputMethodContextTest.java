/*
 * @test
 * @summary java.awt.im.spi.InputMethodContext conformance: interface reflection.
 * @run main InputMethodContextTest
 */

public class InputMethodContextTest {
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
        Class<?> c = Class.forName("java.awt.im.spi.InputMethodContext");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.awt.im.InputMethodRequests"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "dispatchInputMethodEvent"), "method dispatchInputMethodEvent");
        check(hasMethod(c, "createInputMethodWindow"), "method createInputMethodWindow");
        check(hasMethod(c, "createInputMethodJFrame"), "method createInputMethodJFrame");
        check(hasMethod(c, "enableClientWindowNotification"), "method enableClientWindowNotification");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
