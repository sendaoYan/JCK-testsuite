/*
 * @test
 * @summary javax.swing.plaf.synth.SynthUI conformance: white-box structure + black-box behavior.
 * @run main SynthUITest
 */

import java.awt.Graphics;
import java.lang.reflect.Modifier;
import javax.swing.JComponent;
import javax.swing.plaf.synth.SynthContext;
import javax.swing.plaf.synth.SynthUI;

public class SynthUITest {
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
        Class<?> c = Class.forName("javax.swing.plaf.synth.SynthUI");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == true, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.plaf.synth.SynthConstants"), "interface 0");
    }

    static void testBlackBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.synth.SynthUI");
        // SynthUI is an interface extending SynthConstants: it cannot be instantiated.
        check(c.isInterface() == true, "SynthUI is an interface (not instantiable)");
        check(Modifier.isAbstract(c.getDeclaredMethod("getContext", JComponent.class).getModifiers()), "getContext abstract");
        check(Modifier.isAbstract(c.getDeclaredMethod("paintBorder", SynthContext.class, Graphics.class, int.class, int.class, int.class, int.class).getModifiers()), "paintBorder abstract");
        check(c.getMethod("getContext", JComponent.class).getReturnType() == SynthContext.class, "getContext returns SynthContext");

        // Concrete Synth*UI delegates implement this contract.
        check(SynthUI.class.isAssignableFrom(Class.forName("javax.swing.plaf.synth.SynthButtonUI")), "SynthButtonUI implements SynthUI");
        check(SynthUI.class.isAssignableFrom(Class.forName("javax.swing.plaf.synth.SynthTreeUI")), "SynthTreeUI implements SynthUI");
        check(SynthUI.class.isAssignableFrom(Class.forName("javax.swing.plaf.synth.SynthViewportUI")), "SynthViewportUI implements SynthUI");
    }
}
