/*
 * @test
 * @summary java.awt.dnd.MouseDragGestureRecognizer conformance: abstract class reflection.
 * @run main MouseDragGestureRecognizerTest
 */

public class MouseDragGestureRecognizerTest {
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
        Class<?> c = Class.forName("java.awt.dnd.MouseDragGestureRecognizer");
        check(!c.isInterface() && !c.isEnum(), "not interface/enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.awt.dnd.DragGestureRecognizer"), "superclass java.awt.dnd.DragGestureRecognizer");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.awt.event.MouseListener", "java.awt.event.MouseMotionListener"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "mouseClicked"), "method mouseClicked");
        check(hasMethod(c, "mousePressed"), "method mousePressed");
        check(hasMethod(c, "mouseReleased"), "method mouseReleased");
        check(hasMethod(c, "mouseEntered"), "method mouseEntered");
        check(hasMethod(c, "mouseExited"), "method mouseExited");
        check(hasMethod(c, "mouseDragged"), "method mouseDragged");
        check(hasMethod(c, "mouseMoved"), "method mouseMoved");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
