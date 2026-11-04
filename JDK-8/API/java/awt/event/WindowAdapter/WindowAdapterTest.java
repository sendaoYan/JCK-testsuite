/*
 * @test
 * @summary java.awt.event.WindowAdapter conformance: abstract class reflection.
 * @run main WindowAdapterTest
 */

public class WindowAdapterTest {
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
        Class<?> c = Class.forName("java.awt.event.WindowAdapter");
        check(!c.isInterface() && !c.isEnum(), "not interface/enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.awt.event.WindowFocusListener", "java.awt.event.WindowListener", "java.awt.event.WindowStateListener"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "windowOpened"), "method windowOpened");
        check(hasMethod(c, "windowClosing"), "method windowClosing");
        check(hasMethod(c, "windowClosed"), "method windowClosed");
        check(hasMethod(c, "windowIconified"), "method windowIconified");
        check(hasMethod(c, "windowDeiconified"), "method windowDeiconified");
        check(hasMethod(c, "windowActivated"), "method windowActivated");
        check(hasMethod(c, "windowDeactivated"), "method windowDeactivated");
        check(hasMethod(c, "windowStateChanged"), "method windowStateChanged");
        check(hasMethod(c, "windowGainedFocus"), "method windowGainedFocus");
        check(hasMethod(c, "windowLostFocus"), "method windowLostFocus");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
