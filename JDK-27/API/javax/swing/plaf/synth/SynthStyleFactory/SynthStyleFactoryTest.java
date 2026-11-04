/*
 * @test
 * @summary javax.swing.plaf.synth.SynthStyleFactory conformance: white-box structure + black-box behavior.
 * @run main SynthStyleFactoryTest
 */

import java.awt.Color;
import java.awt.Font;
import java.lang.reflect.Modifier;
import javax.swing.JComponent;
import javax.swing.plaf.synth.ColorType;
import javax.swing.plaf.synth.Region;
import javax.swing.plaf.synth.SynthContext;
import javax.swing.plaf.synth.SynthStyle;
import javax.swing.plaf.synth.SynthStyleFactory;

public class SynthStyleFactoryTest {
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
        Class<?> c = Class.forName("javax.swing.plaf.synth.SynthStyleFactory");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == true, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
        check(Modifier.isAbstract(c.getDeclaredMethod("getStyle", JComponent.class, Region.class).getModifiers()), "getStyle abstract");
    }

    static void testBlackBox() throws Exception {
        final SynthStyle produced = new TestStyle();
        SynthStyleFactory factory = new SynthStyleFactory() {
            public SynthStyle getStyle(JComponent c, Region id) { return produced; }
        };
        check(factory.getStyle(null, Region.BUTTON) == produced, "getStyle returns produced");
        check(factory.getStyle(null, Region.SLIDER) == produced, "getStyle consistent");
    }

    static class TestStyle extends SynthStyle {
        @Override
        protected Color getColorForState(SynthContext context, ColorType type) {
            return Color.RED;
        }
        @Override
        protected Font getFontForState(SynthContext context) {
            return new Font(Font.DIALOG, Font.PLAIN, 12);
        }
    }
}
