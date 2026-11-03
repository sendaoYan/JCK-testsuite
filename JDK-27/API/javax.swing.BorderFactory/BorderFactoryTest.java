/*
 * @test
 * @summary javax.swing.BorderFactory conformance: white-box structure + black-box behavior.
 * @run main BorderFactoryTest
 */

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Insets;
import java.lang.reflect.Modifier;

import javax.swing.BorderFactory;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;

public class BorderFactoryTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean insetsEq(Border b, int top, int left, int bottom, int right) {
        Insets in = b.getBorderInsets(null);
        return in.top == top && in.left == left && in.bottom == bottom && in.right == right;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.BorderFactory");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(!Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // line border: uniform insets, opaque
        LineBorder line = (LineBorder) BorderFactory.createLineBorder(Color.RED, 2);
        check(insetsEq(line, 2, 2, 2, 2), "line border insets");
        check(line.isBorderOpaque(), "line border opaque");
        check(Color.RED.equals(line.getLineColor()), "line border color");

        // empty border: per-side insets
        EmptyBorder empty = (EmptyBorder) BorderFactory.createEmptyBorder(1, 2, 3, 4);
        check(insetsEq(empty, 1, 2, 3, 4), "empty border insets");
        check(!empty.isBorderOpaque(), "empty border not opaque");

        // compound border wraps inner+outer
        CompoundBorder compound = (CompoundBorder) BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK, 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5));
        check(insetsEq(compound, 6, 6, 6, 6), "compound border sums insets");

        // matte border: per-side insets
        MatteBorder matte = (MatteBorder) BorderFactory.createMatteBorder(1, 2, 3, 4, Color.BLUE);
        check(insetsEq(matte, 1, 2, 3, 4), "matte border insets");

        // titled border
        TitledBorder titled = BorderFactory.createTitledBorder("Title");
        check("Title".equals(titled.getTitle()), "titled border title");

        // etched + bevel borders are the right types
        EtchedBorder etched = (EtchedBorder) BorderFactory.createEtchedBorder();
        check(etched != null, "etched border");
        Border raised = BorderFactory.createRaisedBevelBorder();
        Border lowered = BorderFactory.createLoweredBevelBorder();
        check(raised != null && lowered != null, "bevel borders");
        Border softRaised = BorderFactory.createRaisedSoftBevelBorder();
        Border softLowered = BorderFactory.createLoweredSoftBevelBorder();
        check(softRaised != null && softLowered != null, "soft bevel borders");

        // stroke + dashed borders
        Border stroke = BorderFactory.createStrokeBorder(new BasicStroke(1.5f));
        check(stroke != null, "stroke border");
        Border dashed = BorderFactory.createDashedBorder(Color.GRAY, 2.0f, 1.0f);
        check(dashed != null, "dashed border");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
