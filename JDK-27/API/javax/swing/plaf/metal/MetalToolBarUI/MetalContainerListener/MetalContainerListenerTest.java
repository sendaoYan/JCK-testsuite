/*
 * @test
 * @summary javax.swing.plaf.metal.MetalToolBarUI.MetalContainerListener conformance: white-box structure + black-box behavior.
 * @run main MetalContainerListenerTest
 */

import java.awt.event.ContainerListener;
import java.lang.reflect.Modifier;

public class MetalContainerListenerTest {
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
        Class<?> c = Class.forName("javax.swing.plaf.metal.MetalToolBarUI$MetalContainerListener");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == false, "public modifier");
        check(Modifier.isProtected(mods) == true, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.plaf.basic.BasicToolBarUI$ToolBarContListener"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static void testBlackBox() throws Exception {
        // The sole constructor is protected, so instantiation is not possible
        // from outside the package; verify behavior via type relationships.
        Class<?> c = Class.forName("javax.swing.plaf.metal.MetalToolBarUI$MetalContainerListener");
        check(ContainerListener.class.isAssignableFrom(c), "is a ContainerListener");
        check(c.getSuperclass().getName().equals("javax.swing.plaf.basic.BasicToolBarUI$ToolBarContListener"), "superclass is ToolBarContListener");
    }
}
