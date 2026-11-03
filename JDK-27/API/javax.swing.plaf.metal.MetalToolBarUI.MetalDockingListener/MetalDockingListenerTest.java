/*
 * @test
 * @summary javax.swing.plaf.metal.MetalToolBarUI.MetalDockingListener conformance: white-box structure + black-box behavior.
 * @run main MetalDockingListenerTest
 */

import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.lang.reflect.Modifier;

public class MetalDockingListenerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.metal.MetalToolBarUI$MetalDockingListener");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == false, "public modifier");
        check(Modifier.isProtected(mods) == true, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.plaf.basic.BasicToolBarUI$DockingListener"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static void testBlackBox() throws Exception {
        // The class is a protected member of MetalToolBarUI whose constructor
        // is not accessible from outside the package, so behavior is verified
        // via the listener type relationships it must honor.
        Class<?> c = Class.forName("javax.swing.plaf.metal.MetalToolBarUI$MetalDockingListener");
        check(MouseListener.class.isAssignableFrom(c), "is a MouseListener");
        check(MouseMotionListener.class.isAssignableFrom(c), "is a MouseMotionListener");
    }
}
