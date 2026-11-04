/*
 * @test
 * @summary java.awt.dnd.DragSourceAdapter conformance: abstract class reflection.
 * @run main DragSourceAdapterTest
 */

public class DragSourceAdapterTest {
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
        Class<?> c = Class.forName("java.awt.dnd.DragSourceAdapter");
        check(!c.isInterface() && !c.isEnum(), "not interface/enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.awt.dnd.DragSourceListener", "java.awt.dnd.DragSourceMotionListener"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "dragEnter"), "method dragEnter");
        check(hasMethod(c, "dragOver"), "method dragOver");
        check(hasMethod(c, "dragMouseMoved"), "method dragMouseMoved");
        check(hasMethod(c, "dropActionChanged"), "method dropActionChanged");
        check(hasMethod(c, "dragExit"), "method dragExit");
        check(hasMethod(c, "dragDropEnd"), "method dragDropEnd");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
