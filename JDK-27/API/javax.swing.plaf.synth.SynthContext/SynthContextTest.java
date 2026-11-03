/*
 * @test
 * @summary javax.swing.plaf.synth.SynthContext conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main SynthContextTest
 */

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.lang.reflect.Modifier;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.plaf.synth.ColorType;
import javax.swing.plaf.synth.Region;
import javax.swing.plaf.synth.SynthConstants;
import javax.swing.plaf.synth.SynthContext;
import javax.swing.plaf.synth.SynthStyle;

public class SynthContextTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Exception; }
    static void runOnEDT(ThrowingRunnable r) throws Exception {
        try {
            EventQueue.invokeAndWait(() -> {
                try { r.run(); }
                catch (Exception e) { throw new RuntimeException(e); }
            });
        } catch (java.lang.reflect.InvocationTargetException e) {
            Throwable t = e.getCause();
            if (t instanceof RuntimeException) throw (RuntimeException) t;
            if (t instanceof Error) throw (Error) t;
            throw new RuntimeException(t);
        }
    }

    static SynthStyle minimalStyle() {
        return new SynthStyle() {
            protected Color getColorForState(SynthContext c, ColorType t) { return Color.BLACK; }
            protected Font getFontForState(SynthContext c) { return new Font(Font.DIALOG, Font.PLAIN, 12); }
        };
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(SynthContextTest::testWhiteBox);
        runOnEDT(SynthContextTest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.synth.SynthContext");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static void testBlackBox() throws Exception {
        JComponent comp = new JLabel("x");
        SynthStyle style = minimalStyle();
        Region region = Region.LABEL;
        SynthContext ctx = new SynthContext(comp, region, style, SynthConstants.ENABLED);
        check(ctx.getComponent() == comp, "getComponent");
        check(ctx.getRegion() == region, "getRegion");
        check(ctx.getStyle() == style, "getStyle");
        check(ctx.getComponentState() == SynthConstants.ENABLED, "getComponentState");

        boolean npe1 = false;
        try { new SynthContext(null, region, style, 0); } catch (NullPointerException e) { npe1 = true; }
        check(npe1, "null component NPE");

        boolean npe2 = false;
        try { new SynthContext(comp, null, style, 0); } catch (NullPointerException e) { npe2 = true; }
        check(npe2, "null region NPE");

        boolean npe3 = false;
        try { new SynthContext(comp, region, null, 0); } catch (NullPointerException e) { npe3 = true; }
        check(npe3, "null style NPE");
    }
}
