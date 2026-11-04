/*
 * @test
 * @summary javax.swing.plaf.metal.MetalLookAndFeel conformance: white-box structure + black-box behavior.
 * @run main MetalLookAndFeelTest
 */

import java.lang.reflect.Modifier;
import javax.swing.UIDefaults;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.metal.MetalLookAndFeel;

public class MetalLookAndFeelTest {
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
        Class<?> c = Class.forName("javax.swing.plaf.metal.MetalLookAndFeel");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.plaf.basic.BasicLookAndFeel"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static void testBlackBox() throws Exception {
        MetalLookAndFeel laf = new MetalLookAndFeel();
        check("Metal".equals(laf.getName()), "name");
        check("Metal".equals(laf.getID()), "id");
        check("The Java(tm) Look and Feel".equals(laf.getDescription()), "description");
        check(laf.isNativeLookAndFeel() == false, "not native LAF");
        check(laf.isSupportedLookAndFeel() == true, "supported LAF");
        check(laf.getSupportsWindowDecorations() == true, "window decorations supported");

        UIDefaults defaults = laf.getDefaults();
        check(defaults != null, "defaults non-null");
        check(defaults.size() > 0, "defaults non-empty");
        check(MetalLookAndFeel.getCurrentTheme() != null, "current theme non-null");
        check(laf.getLayoutStyle() != null, "layout style non-null");

        FontUIResource[] fonts = new FontUIResource[] {
            MetalLookAndFeel.getControlTextFont(),
            MetalLookAndFeel.getSystemTextFont(),
            MetalLookAndFeel.getUserTextFont(),
            MetalLookAndFeel.getMenuTextFont(),
            MetalLookAndFeel.getWindowTitleFont(),
            MetalLookAndFeel.getSubTextFont(),
        };
        for (FontUIResource f : fonts) {
            check(f != null, "font non-null");
        }

        ColorUIResource[] colors = new ColorUIResource[] {
            MetalLookAndFeel.getControl(),
            MetalLookAndFeel.getControlTextColor(),
            MetalLookAndFeel.getPrimaryControl(),
            MetalLookAndFeel.getSystemTextColor(),
            MetalLookAndFeel.getDesktopColor(),
            MetalLookAndFeel.getFocusColor(),
        };
        for (ColorUIResource col : colors) {
            check(col != null, "color non-null");
        }
    }
}
