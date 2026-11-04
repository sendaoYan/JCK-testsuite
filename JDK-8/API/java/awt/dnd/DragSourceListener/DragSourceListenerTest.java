/*
 * @test
 * @summary java.awt.dnd.DragSourceListener conformance: interface reflection.
 * @run main DragSourceListenerTest
 */

public class DragSourceListenerTest {
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
        Class<?> c = Class.forName("java.awt.dnd.DragSourceListener");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.util.EventListener"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "dragEnter"), "method dragEnter");
        check(hasMethod(c, "dragOver"), "method dragOver");
        check(hasMethod(c, "dropActionChanged"), "method dropActionChanged");
        check(hasMethod(c, "dragExit"), "method dragExit");
        check(hasMethod(c, "dragDropEnd"), "method dragDropEnd");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
