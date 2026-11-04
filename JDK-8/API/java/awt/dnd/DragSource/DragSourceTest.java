/*
 * @test
 * @summary java.awt.dnd.DragSource conformance: class reflection.
 * @run main DragSourceTest
 */

public class DragSourceTest {
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
        Class<?> c = Class.forName("java.awt.dnd.DragSource");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.io.Serializable"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "getDefaultDragSource"), "method getDefaultDragSource");
        check(hasMethod(c, "isDragImageSupported"), "method isDragImageSupported");
        check(hasMethod(c, "startDrag"), "method startDrag");
        check(hasMethod(c, "getFlavorMap"), "method getFlavorMap");
        check(hasMethod(c, "createDragGestureRecognizer"), "method createDragGestureRecognizer");
        check(hasMethod(c, "createDefaultDragGestureRecognizer"), "method createDefaultDragGestureRecognizer");
        check(hasMethod(c, "addDragSourceListener"), "method addDragSourceListener");
        check(hasMethod(c, "removeDragSourceListener"), "method removeDragSourceListener");
        check(hasMethod(c, "getDragSourceListeners"), "method getDragSourceListeners");
        check(hasMethod(c, "addDragSourceMotionListener"), "method addDragSourceMotionListener");
        check(hasMethod(c, "removeDragSourceMotionListener"), "method removeDragSourceMotionListener");
        check(hasMethod(c, "getDragSourceMotionListeners"), "method getDragSourceMotionListeners");
        check(hasMethod(c, "getListeners"), "method getListeners");
        check(hasMethod(c, "getDragThreshold"), "method getDragThreshold");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
