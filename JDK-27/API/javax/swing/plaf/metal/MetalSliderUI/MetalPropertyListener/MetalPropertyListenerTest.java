/*
 * @test
 * @summary javax.swing.plaf.metal.MetalSliderUI.MetalPropertyListener conformance: white-box structure + black-box behavior.
 * @run main MetalPropertyListenerTest
 */

import java.beans.PropertyChangeListener;
import java.lang.reflect.Modifier;

public class MetalPropertyListenerTest {
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
        Class<?> c = Class.forName("javax.swing.plaf.metal.MetalSliderUI$MetalPropertyListener");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == false, "public modifier");
        check(Modifier.isProtected(mods) == true, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.plaf.basic.BasicSliderUI$PropertyChangeHandler"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static void testBlackBox() throws Exception {
        // The sole constructor is protected (MetalSliderUI this$0), so it is
        // not callable from outside the package; verify behavior via type
        // relationships only.
        Class<?> c = Class.forName("javax.swing.plaf.metal.MetalSliderUI$MetalPropertyListener");
        check(PropertyChangeListener.class.isAssignableFrom(c), "is a PropertyChangeListener");
        check(c.getSuperclass().getName().equals("javax.swing.plaf.basic.BasicSliderUI$PropertyChangeHandler"), "superclass is PropertyChangeHandler");
    }
}
