/*
 * @test
 * @summary java.awt.dnd.DragSourceContext conformance: class reflection.
 * @run main DragSourceContextTest
 */

public class DragSourceContextTest {
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
        Class<?> c = Class.forName("java.awt.dnd.DragSourceContext");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.awt.dnd.DragSourceListener", "java.awt.dnd.DragSourceMotionListener", "java.io.Serializable"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "getDragSource"), "method getDragSource");
        check(hasMethod(c, "getComponent"), "method getComponent");
        check(hasMethod(c, "getTrigger"), "method getTrigger");
        check(hasMethod(c, "getSourceActions"), "method getSourceActions");
        check(hasMethod(c, "setCursor"), "method setCursor");
        check(hasMethod(c, "getCursor"), "method getCursor");
        check(hasMethod(c, "addDragSourceListener"), "method addDragSourceListener");
        check(hasMethod(c, "removeDragSourceListener"), "method removeDragSourceListener");
        check(hasMethod(c, "transferablesFlavorsChanged"), "method transferablesFlavorsChanged");
        check(hasMethod(c, "dragEnter"), "method dragEnter");
        check(hasMethod(c, "dragOver"), "method dragOver");
        check(hasMethod(c, "dragExit"), "method dragExit");
        check(hasMethod(c, "dropActionChanged"), "method dropActionChanged");
        check(hasMethod(c, "dragDropEnd"), "method dragDropEnd");
        check(hasMethod(c, "dragMouseMoved"), "method dragMouseMoved");
        check(hasMethod(c, "getTransferable"), "method getTransferable");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
