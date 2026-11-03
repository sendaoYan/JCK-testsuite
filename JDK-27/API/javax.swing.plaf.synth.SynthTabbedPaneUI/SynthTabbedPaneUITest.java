/*
 * @test
 * @summary javax.swing.plaf.synth.SynthTabbedPaneUI conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main SynthTabbedPaneUITest
 */

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import javax.swing.*;
import javax.swing.plaf.synth.ColorType;
import javax.swing.plaf.synth.Region;
import javax.swing.plaf.synth.SynthConstants;
import javax.swing.plaf.synth.SynthContext;
import javax.swing.plaf.synth.SynthStyle;
import javax.swing.plaf.synth.SynthUI;
import javax.swing.plaf.synth.SynthTabbedPaneUI;

public class SynthTabbedPaneUITest {
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
        } catch (InvocationTargetException e) {
            Throwable t = e.getCause();
            if (t instanceof RuntimeException) throw (RuntimeException) t;
            if (t instanceof Error) throw (Error) t;
            throw new RuntimeException(t);
        }
    }

    static void noThrow(ThrowingRunnable r, String msg) {
        try { r.run(); passed++; }
        catch (Exception e) { throw new AssertionError(msg + " should not throw", e); }
    }

    static void expectNPE(ThrowingRunnable r, String msg) {
        try { r.run(); }
        catch (NullPointerException expected) { passed++; return; }
        catch (Exception e) { throw new AssertionError(msg + " expected NPE but got " + e, e); }
        throw new AssertionError(msg + " expected NPE but no exception");
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(SynthTabbedPaneUITest::testWhiteBox);
        runOnEDT(SynthTabbedPaneUITest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.synth.SynthTabbedPaneUI");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.plaf.basic.BasicTabbedPaneUI"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 2, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.beans.PropertyChangeListener"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("javax.swing.plaf.synth.SynthUI"), "interface 1");
    }

    static void testBlackBox() throws Exception {
        JComponent comp = new JTabbedPane();
        SynthTabbedPaneUI ui = (SynthTabbedPaneUI) SynthTabbedPaneUI.createUI(comp);
        check(ui != null, "createUI non-null");
        check(ui instanceof SynthTabbedPaneUI, "createUI exact type");
        check(ui instanceof SynthUI, "implements SynthUI");

        SynthContext ctx = ui.getContext(comp);
        check(ctx != null, "getContext non-null");
        check(ctx.getComponent() == comp, "getContext component");
        check(ctx.getRegion() != null, "getContext region");

        SynthStyle style = new TestStyle();
        SynthContext borderCtx = new SynthContext(comp, Region.PANEL, style, SynthConstants.ENABLED);
        BufferedImage img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics g = img.getGraphics();
        noThrow(() -> ui.paintBorder(borderCtx, g, 0, 0, 64, 64), "paintBorder offscreen completes");
        g.dispose();

        check(ui.getPreferredSize(comp) == null, "getPreferredSize null (default)");
        check(ui.getMinimumSize(comp) == null, "getMinimumSize null (default)");
        check(ui.getMaximumSize(comp) == null, "getMaximumSize null (default)");

        expectNPE(() -> ui.installUI(comp), "installUI NPE without SynthLookAndFeel");
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
