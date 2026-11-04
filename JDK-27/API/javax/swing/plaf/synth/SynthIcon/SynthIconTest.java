/*
 * @test
 * @summary javax.swing.plaf.synth.SynthIcon conformance: white-box structure + black-box behavior.
 * @run main SynthIconTest
 */

import java.awt.Component;
import java.awt.Graphics;
import java.lang.reflect.Modifier;
import javax.swing.Icon;
import javax.swing.plaf.synth.SynthContext;
import javax.swing.plaf.synth.SynthIcon;

public class SynthIconTest {
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
        Class<?> c = Class.forName("javax.swing.plaf.synth.SynthIcon");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == true, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.Icon"), "interface 0");
    }

    static boolean painted;

    static void testBlackBox() throws Exception {
        SynthIcon icon = new SynthIcon() {
            public void paintIcon(SynthContext context, Graphics g, int x, int y, int width, int height) {
                painted = true;
            }
            public int getIconWidth(SynthContext context) {
                return context == null ? 16 : 20;
            }
            public int getIconHeight(SynthContext context) {
                return context == null ? 32 : 40;
            }
        };

        check(icon instanceof Icon, "instanceof Icon");
        check(icon.getIconWidth() == 16, "default getIconWidth delegates to null context");
        check(icon.getIconHeight() == 32, "default getIconHeight delegates to null context");
        check(icon.getIconWidth(null) == 16, "getIconWidth(null)");
        check(icon.getIconHeight(null) == 32, "getIconHeight(null)");

        painted = false;
        icon.paintIcon((Component) null, (Graphics) null, 3, 4);
        check(painted, "default paintIcon delegates to context variant");
    }
}
