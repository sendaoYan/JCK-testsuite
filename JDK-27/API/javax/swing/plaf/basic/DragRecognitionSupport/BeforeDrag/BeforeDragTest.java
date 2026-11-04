/*
 * @test
 * @summary javax.swing.plaf.basic.DragRecognitionSupport.BeforeDrag conformance: white-box structure + black-box behavior.
 * @run main BeforeDragTest
 */
import java.lang.reflect.Modifier;

public class BeforeDragTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean hasDeclaredMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (m.getName().equals(name)) return true;
        }
        return false;
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.basic.DragRecognitionSupport$BeforeDrag");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == true, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(Modifier.isAbstract(mods) == true, "abstract modifier");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");

        check(hasDeclaredMethod(c, "dragStarting"), "declares method dragStarting");
    }

    static void testBlackBox() throws Exception {
        final boolean[] called = { false };
        Class<?> c = Class.forName("javax.swing.plaf.basic.DragRecognitionSupport$BeforeDrag");
        java.lang.reflect.Method m = c.getMethod("dragStarting", java.awt.event.MouseEvent.class);
        Object proxy = java.lang.reflect.Proxy.newProxyInstance(
            BeforeDragTest.class.getClassLoader(), new Class<?>[]{ c },
            (p, method, args) -> { if (method.getName().equals("dragStarting")) called[0] = true; return null; });
        java.awt.event.MouseEvent ev = new java.awt.event.MouseEvent(new java.awt.Component(){}, java.awt.event.MouseEvent.MOUSE_PRESSED, 0L, 0, 5, 5, 1, false);
        m.invoke(proxy, ev);
        check(called[0], "dragStarting invoked via proxy");
    }

}
