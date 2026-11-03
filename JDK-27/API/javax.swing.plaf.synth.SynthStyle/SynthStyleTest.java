/*
 * @test
 * @summary javax.swing.plaf.synth.SynthStyle conformance: white-box structure + black-box behavior.
 * @run main SynthStyleTest
 */

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.lang.reflect.Modifier;
import javax.swing.JPanel;
import javax.swing.plaf.synth.ColorType;
import javax.swing.plaf.synth.Region;
import javax.swing.plaf.synth.SynthConstants;
import javax.swing.plaf.synth.SynthContext;
import javax.swing.plaf.synth.SynthGraphicsUtils;
import javax.swing.plaf.synth.SynthStyle;

public class SynthStyleTest {
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
        Class<?> c = Class.forName("javax.swing.plaf.synth.SynthStyle");
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
        check(Modifier.isAbstract(c.getDeclaredMethod("getColorForState", SynthContext.class, ColorType.class).getModifiers()), "getColorForState abstract");
        check(Modifier.isAbstract(c.getDeclaredMethod("getFontForState", SynthContext.class).getModifiers()), "getFontForState abstract");
    }

    static void testBlackBox() throws Exception {
        SynthStyle style = new TestStyle();
        JPanel panel = new JPanel();
        SynthContext ctx = new SynthContext(panel, Region.PANEL, style, SynthConstants.ENABLED);

        SynthGraphicsUtils gu = style.getGraphicsUtils(ctx);
        check(gu != null, "getGraphicsUtils non-null");

        check(Color.RED.equals(style.getColor(ctx, ColorType.BACKGROUND)), "getColor delegates to getColorForState");
        check(style.getFont(ctx) != null, "getFont non-null");

        Insets insets = style.getInsets(ctx, new Insets(5, 6, 7, 8));
        check(insets != null, "getInsets non-null");
        check(insets.top == 0 && insets.bottom == 0 && insets.left == 0 && insets.right == 0, "getInsets zeroes");

        check(style.getPainter(ctx) == null, "getPainter null by default");
        check(style.isOpaque(ctx) == true, "isOpaque default true");

        check(style.get(ctx, "NoSuchKey") == null, "get unknown key null");
        check(style.getInt(ctx, "NoSuchKey", 42) == 42, "getInt default");
        check(style.getBoolean(ctx, "NoSuchKey", true) == true, "getBoolean default");
        check(style.getIcon(ctx, "NoSuchKey") == null, "getIcon unknown null");
        check("fallback".equals(style.getString(ctx, "NoSuchKey", "fallback")), "getString default");

        check(Color.BLACK.equals(style.get(ctx, "Table.dropLineColor")), "built-in dropLineColor");
        check(Color.RED.equals(style.get(ctx, "Table.dropLineShortColor")), "built-in dropLineShortColor");
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
