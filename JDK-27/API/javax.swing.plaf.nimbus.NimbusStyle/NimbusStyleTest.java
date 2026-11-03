/*
 * @test
 * @summary javax.swing.plaf.nimbus.NimbusStyle conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main NimbusStyleTest
 */

import java.awt.EventQueue;
import java.awt.Insets;
import java.lang.reflect.Modifier;
import javax.swing.JButton;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import javax.swing.plaf.nimbus.NimbusStyle;
import javax.swing.plaf.synth.ColorType;
import javax.swing.plaf.synth.Region;
import javax.swing.plaf.synth.SynthConstants;
import javax.swing.plaf.synth.SynthContext;

public class NimbusStyleTest {
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

    public static void main(String[] args) throws Exception {
        runOnEDT(NimbusStyleTest::testWhiteBox);
        runOnEDT(NimbusStyleTest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.nimbus.NimbusStyle");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.plaf.synth.SynthStyle"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static void testBlackBox() throws Exception {
        LookAndFeel old = UIManager.getLookAndFeel();
        UIManager.setLookAndFeel(new NimbusLookAndFeel());
        try {
            JButton b = new JButton("x");
            NimbusStyle style = NimbusLookAndFeel.getStyle(b, Region.BUTTON);
            check(style != null, "style non-null");
            check(style instanceof NimbusStyle, "instanceof NimbusStyle");

            SynthContext ctx = new SynthContext(b, Region.BUTTON, style, SynthConstants.ENABLED);
            check(style.getColor(ctx, ColorType.BACKGROUND) != null, "getColor BACKGROUND");
            check(style.getColor(ctx, ColorType.FOREGROUND) != null, "getColor FOREGROUND");
            check(style.getColor(ctx, ColorType.FOCUS) != null, "getColor FOCUS");
            check(style.getFont(ctx) != null, "getFont");
            Insets insets = style.getInsets(ctx, new Insets(0, 0, 0, 0));
            check(insets != null, "getInsets");
            check(style.getGraphicsUtils(ctx) != null, "getGraphicsUtils");
            check(style.getPainter(ctx) != null, "getPainter");
            check(style.getBackgroundPainter(ctx) != null, "getBackgroundPainter");
            check(style.isOpaque(ctx) == false, "isOpaque false for button");
        } finally {
            UIManager.setLookAndFeel(old);
        }
    }
}
