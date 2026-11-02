/*
 * @test
 * @summary java.awt.dnd.DragGestureRecognizer conformance: abstract class reflection.
 * @run main DragGestureRecognizerTest
 */

public class DragGestureRecognizerTest {
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
        Class<?> c = Class.forName("java.awt.dnd.DragGestureRecognizer");
        check(!c.isInterface() && !c.isEnum(), "not interface/enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.io.Serializable"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "getDragSource"), "method getDragSource");
        check(hasMethod(c, "getComponent"), "method getComponent");
        check(hasMethod(c, "setComponent"), "method setComponent");
        check(hasMethod(c, "getSourceActions"), "method getSourceActions");
        check(hasMethod(c, "setSourceActions"), "method setSourceActions");
        check(hasMethod(c, "getTriggerEvent"), "method getTriggerEvent");
        check(hasMethod(c, "resetRecognizer"), "method resetRecognizer");
        check(hasMethod(c, "addDragGestureListener"), "method addDragGestureListener");
        check(hasMethod(c, "removeDragGestureListener"), "method removeDragGestureListener");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
