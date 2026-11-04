/*
 * @test
 * @summary javax.swing.plaf.synth conformance: SynthLookAndFeel metadata + load(InputStream), Region constants, ColorType/SynthConstants values, SynthContext, SynthStyle (headless).
 * @run main SynthLookAndFeelContractTest
 */

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.ParseException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicLookAndFeel;
import javax.swing.plaf.synth.ColorType;
import javax.swing.plaf.synth.Region;
import javax.swing.plaf.synth.SynthConstants;
import javax.swing.plaf.synth.SynthContext;
import javax.swing.plaf.synth.SynthGraphicsUtils;
import javax.swing.plaf.synth.SynthLookAndFeel;
import javax.swing.plaf.synth.SynthStyle;
import javax.swing.plaf.synth.SynthUI;

/**
 * Synth is the XML-skinned look-and-feel engine. Locked here: identity
 * metadata, the load(InputStream, Class) contract (null validation, malformed
 * XML rejection, well-formed minimal document accepted, installed-state
 * delegation to Synth delegates), the Region/ColorType/SynthConstants value
 * tables and the SynthContext/SynthStyle object contracts.
 */
public class SynthLookAndFeelContractTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static InputStream in(String xml) throws Exception {
        return new ByteArrayInputStream(xml.getBytes("UTF-8"));
    }

    static void testMetadata() {
        SynthLookAndFeel lf = new SynthLookAndFeel();
        check("Synth look and feel".equals(lf.getName()), "getName");
        check("Synth".equals(lf.getID()), "getID");
        check("Synth look and feel".equals(lf.getDescription()), "getDescription");
        check(lf.isSupportedLookAndFeel(), "supported");
        check(!lf.isNativeLookAndFeel(), "not native");
        check(!lf.getSupportsWindowDecorations(), "no window decorations");
        check(lf instanceof BasicLookAndFeel, "Synth is built on BasicLookAndFeel");
        check(lf.toString().contains("Synth"), "toString based on description");
    }

    static void testLoadValidation() throws Exception {
        SynthLookAndFeel lf = new SynthLookAndFeel();
        // null input stream rejected
        try {
            lf.load((InputStream) null, SynthLookAndFeelContractTest.class);
            check(false, "load(null, Class) must throw IAE");
        } catch (IllegalArgumentException expected) {
            check(true, "load(null, Class) throws IAE");
        }
        // null resource base rejected
        try {
            lf.load(in("<synth/>"), null);
            check(false, "load(stream, null) must throw IAE");
        } catch (IllegalArgumentException expected) {
            check(true, "load(stream, null) throws IAE");
        }
        // malformed XML rejected with ParseException
        try {
            lf.load(in("this is not xml <<<"), SynthLookAndFeelContractTest.class);
            check(false, "load(malformed XML) must throw ParseException");
        } catch (ParseException expected) {
            check(true, "load(malformed XML) throws ParseException");
        }
        // well-formed minimal documents accepted; repeated loads are additive-safe
        lf.load(in("<synth></synth>"), SynthLookAndFeelContractTest.class);
        check(true, "load(minimal synth document) accepted");
        lf.load(in("<synth></synth>"), SynthLookAndFeelContractTest.class);
        check(true, "second load accepted");
    }

    static void testInstalledSynthDelegates() throws Exception {
        javax.swing.plaf.metal.MetalLookAndFeel.setCurrentTheme(
                new javax.swing.plaf.metal.OceanTheme());
        UIManager.setLookAndFeel(new javax.swing.plaf.metal.MetalLookAndFeel());
        SynthLookAndFeel slf = new SynthLookAndFeel();
        slf.load(in("<synth></synth>"), SynthLookAndFeelContractTest.class);
        UIManager.setLookAndFeel(slf);
        JButton b = new JButton();
        b.updateUI();
        check(b.getUI() instanceof SynthUI, "button delegate is a SynthUI under Synth");
        check(b.getUI().getClass().getName().equals("javax.swing.plaf.synth.SynthButtonUI"),
                "button delegate is SynthButtonUI");
        JLabel l = new JLabel("x");
        l.updateUI();
        check(l.getUI() instanceof SynthUI, "label delegate is a SynthUI under Synth");
        Object viaCreate = Class.forName("javax.swing.plaf.synth.SynthButtonUI")
                .getMethod("createUI", javax.swing.JComponent.class).invoke(null, b);
        check(viaCreate instanceof ComponentUI, "SynthButtonUI.createUI returns a ComponentUI");
        // back to Metal for the remaining sections
        UIManager.setLookAndFeel(new javax.swing.plaf.metal.MetalLookAndFeel());
    }

    static void testColorTypeConstants() {
        check(ColorType.FOREGROUND.getID() == 0, "FOREGROUND id 0");
        check(ColorType.BACKGROUND.getID() == 1, "BACKGROUND id 1");
        check(ColorType.TEXT_FOREGROUND.getID() == 2, "TEXT_FOREGROUND id 2");
        check(ColorType.TEXT_BACKGROUND.getID() == 3, "TEXT_BACKGROUND id 3");
        check(ColorType.FOCUS.getID() == 4, "FOCUS id 4");
        check("Foreground".equals(ColorType.FOREGROUND.toString()), "FOREGROUND toString");
        check("Background".equals(ColorType.BACKGROUND.toString()), "BACKGROUND toString");
        check("Focus".equals(ColorType.FOCUS.toString()), "FOCUS toString");
        check(ColorType.FOREGROUND != ColorType.BACKGROUND, "constants are distinct objects");
    }

    static void testSynthConstants() {
        check(SynthConstants.ENABLED == 1 << 0, "ENABLED 1");
        check(SynthConstants.MOUSE_OVER == 1 << 1, "MOUSE_OVER 2");
        check(SynthConstants.PRESSED == 1 << 2, "PRESSED 4");
        check(SynthConstants.DISABLED == 1 << 3, "DISABLED 8");
        check(SynthConstants.FOCUSED == 1 << 8, "FOCUSED 256");
        check(SynthConstants.SELECTED == 1 << 9, "SELECTED 512");
        check(SynthConstants.DEFAULT == 1 << 10, "DEFAULT 1024");
        // states compose with OR
        check((SynthConstants.ENABLED | SynthConstants.MOUSE_OVER)
                == (SynthConstants.ENABLED | 1 << 1), "states are bit flags");
    }

    static void testSynthContext() {
        SynthStyle style = new ProbeStyle();
        JLabel c = new JLabel("x");
        SynthContext ctx = new SynthContext(c, Region.LABEL, style, SynthConstants.ENABLED);
        check(ctx.getComponent() == c, "getComponent round-trip");
        check(ctx.getRegion() == Region.LABEL, "getRegion round-trip (same singleton)");
        check(ctx.getStyle() == style, "getStyle round-trip");
        check(ctx.getComponentState() == SynthConstants.ENABLED, "getComponentState round-trip");
        try {
            new SynthContext(null, Region.LABEL, style, 0);
            check(false, "null component must throw NPE");
        } catch (NullPointerException expected) {
            check(true, "null component throws NPE");
        }
        try {
            new SynthContext(c, null, style, 0);
            check(false, "null region must throw NPE");
        } catch (NullPointerException expected) {
            check(true, "null region throws NPE");
        }
        try {
            new SynthContext(c, Region.LABEL, null, 0);
            check(false, "null style must throw NPE");
        } catch (NullPointerException expected) {
            check(true, "null style throws NPE");
        }
    }

    /** Minimal concrete SynthStyle with fixed state answers. */
    static class ProbeStyle extends SynthStyle {
        protected Font getFontForState(SynthContext c) { return null; }
        protected Color getColorForState(SynthContext c, ColorType t) { return Color.RED; }
        public javax.swing.plaf.InsetsUIResource getInsets(SynthContext c) {
            return null;
        }
        public javax.swing.plaf.synth.SynthPainter getPainter(SynthContext c) { return null; }
    }

    static void testSynthStyleDefaults() {
        ProbeStyle style = new ProbeStyle();
        JLabel c = new JLabel("x");
        SynthContext ctx = new SynthContext(c, Region.LABEL, style, SynthConstants.ENABLED);
        // color resolution routes to getColorForState
        check(Color.RED.equals(style.getColor(ctx, ColorType.FOREGROUND)),
                "getColor routes to getColorForState");
        // unknown keys resolve to null/false/default without throwing
        check(style.get(ctx, "no.such.key") == null, "get(unknown key) null");
        check(style.getInt(ctx, "no.such.key", 7) == 7, "getInt falls back to default");
        check(style.getBoolean(ctx, "no.such.key", true), "getBoolean falls back to default");
        check(style.getString(ctx, "no.such.key", "d").equals("d"), "getString falls back");
        check(style.getInsets(ctx, null) != null, "getInsets(ctx, null) allocates");
        Insets reused = new Insets(9, 9, 9, 9);
        check(style.getInsets(ctx, reused) == reused, "getInsets fills the passed insets");
        check(style.getPainter(ctx) == null, "probe painter is null");
        check(style.getGraphicsUtils(ctx) != null, "default graphics utils non-null");
        // SynthGraphicsUtils is a stateless helper class
        SynthGraphicsUtils utils = new SynthGraphicsUtils();
        check(utils != null, "SynthGraphicsUtils constructible");
    }

    /** javax.swing.plaf.synth.SynthIcon exists only since JDK 9. */
    static void testSynthIconInterface() throws Exception {
        Class<?> iface = Class.forName("javax.swing.plaf.synth.SynthIcon");
        check(iface.isInterface(), "SynthIcon is an interface");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : iface.getMethods()) names.add(m.getName());
        check(names.contains("paintIcon"), "SynthIcon.paintIcon(SynthContext,Graphics,x,y,w,h)");
        check(names.contains("getIconWidth"), "SynthIcon.getIconWidth(SynthContext)");
        check(names.contains("getIconHeight"), "SynthIcon.getIconHeight(SynthContext)");
        // SynthIcon is referenced by the Synth engine for context-aware icons
        check(javax.swing.Icon.class.isAssignableFrom(iface),
                "SynthIcon is assignable to Icon");
    }

    public static void main(String[] args) throws Exception {
        testMetadata();
        testLoadValidation();
        testInstalledSynthDelegates();
        testColorTypeConstants();
        testSynthConstants();
        testSynthContext();
        testSynthStyleDefaults();
        testSynthIconInterface();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
