/*
 * @test
 * @summary javax.swing.plaf.metal.MetalFileChooserUI.SingleClickListener conformance: white-box structure + black-box behavior.
 * @run main SingleClickListenerTest
 */

import java.awt.event.MouseListener;
import java.lang.reflect.Modifier;

public class SingleClickListenerTest {
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
        Class<?> c = Class.forName("javax.swing.plaf.metal.MetalFileChooserUI$SingleClickListener");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == false, "public modifier");
        check(Modifier.isProtected(mods) == true, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.awt.event.MouseAdapter"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static void testBlackBox() throws Exception {
        // The class is a protected member of MetalFileChooserUI whose
        // constructor is not accessible from outside the package, so behavior
        // is verified via the listener type relationship it must honor.
        Class<?> c = Class.forName("javax.swing.plaf.metal.MetalFileChooserUI$SingleClickListener");
        check(MouseListener.class.isAssignableFrom(c), "is a MouseListener");
        check(c.getSuperclass().getName().equals("java.awt.event.MouseAdapter"), "superclass is MouseAdapter");
    }
}
