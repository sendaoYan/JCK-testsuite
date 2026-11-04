/*
 * @test
 * @summary java.awt.event.MouseListener conformance: interface reflection.
 * @run main MouseListenerTest
 */

public class MouseListenerTest {
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
        Class<?> c = Class.forName("java.awt.event.MouseListener");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.util.EventListener"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "mouseClicked"), "method mouseClicked");
        check(hasMethod(c, "mousePressed"), "method mousePressed");
        check(hasMethod(c, "mouseReleased"), "method mouseReleased");
        check(hasMethod(c, "mouseEntered"), "method mouseEntered");
        check(hasMethod(c, "mouseExited"), "method mouseExited");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
