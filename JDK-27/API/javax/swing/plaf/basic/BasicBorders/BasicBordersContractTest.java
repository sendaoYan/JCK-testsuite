/*
 * @test
 * @summary javax.swing.plaf.basic.BasicBorders conformance: factory types, exact insets, nested border constructors, deterministic paint (headless, Metal LaF).
 * @run main BasicBordersContractTest
 */

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.image.BufferedImage;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuBar;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicBorders;

/**
 * BasicBorders is the border kit shared by the Basic/Metal look-and-feels.
 * Locked: the static factory return types and UIResource marking, the exact
 * border insets both on a fresh (UI-populated) component and on directly
 * constructed nested borders, and fixed-geometry paint behaviour.
 */
public class BasicBordersContractTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean insetsEq(Insets in, int t, int l, int b, int r) {
        return in != null && in.top == t && in.left == l && in.bottom == b && in.right == r;
    }

    static int drawnPixels(BufferedImage img) {
        int n = 0;
        for (int y = 0; y < img.getHeight(); y++)
            for (int x = 0; x < img.getWidth(); x++)
                if ((img.getRGB(x, y) >>> 24) != 0) n++;
        return n;
    }

    static void testFactoryTypes() {
        check(BasicBorders.getButtonBorder() instanceof BorderUIResource.CompoundBorderUIResource,
                "getButtonBorder -> CompoundBorderUIResource");
        check(BasicBorders.getButtonBorder() instanceof UIResource, "button border is UIResource");
        check(BasicBorders.getRadioButtonBorder() instanceof BorderUIResource.CompoundBorderUIResource,
                "getRadioButtonBorder -> CompoundBorderUIResource");
        check(BasicBorders.getToggleButtonBorder() instanceof BorderUIResource.CompoundBorderUIResource,
                "getToggleButtonBorder -> CompoundBorderUIResource");
        check(BasicBorders.getMenuBarBorder() instanceof BasicBorders.MenuBarBorder,
                "getMenuBarBorder -> MenuBarBorder");
        check(BasicBorders.getMenuBarBorder() instanceof UIResource, "menu bar border is UIResource");
        check(BasicBorders.getSplitPaneBorder() instanceof BasicBorders.SplitPaneBorder,
                "getSplitPaneBorder -> SplitPaneBorder");
        check(BasicBorders.getSplitPaneDividerBorder().getClass().getName()
                        .endsWith("BasicBorders$SplitPaneDividerBorder"),
                "getSplitPaneDividerBorder -> SplitPaneDividerBorder (package-private class)");
        check(BasicBorders.getTextFieldBorder() instanceof BasicBorders.FieldBorder,
                "getTextFieldBorder -> FieldBorder");
        check(BasicBorders.getProgressBarBorder() instanceof BorderUIResource.LineBorderUIResource,
                "getProgressBarBorder -> LineBorderUIResource");
        check(BasicBorders.getInternalFrameBorder() instanceof BorderUIResource.CompoundBorderUIResource,
                "getInternalFrameBorder -> CompoundBorderUIResource");
    }

    /** Compound factory borders compose an ETCHED-like border with the component margin. */
    static void testFactoryInsetsOnUIMountedComponents() {
        JButton b = new JButton();      // fresh button: UI installs margin (2,14,2,14)
        b.updateUI();
        Insets bi = BasicBorders.getButtonBorder().getBorderInsets(b);
        check(insetsEq(bi, 4, 17, 5, 17),
                "button border insets = border(2,3,3,3) + margin(2,14,2,14): " + bi);

        JToggleButton tb = new JToggleButton();
        tb.updateUI();
        Insets ti = BasicBorders.getToggleButtonBorder().getBorderInsets(tb);
        check(insetsEq(ti, 4, 16, 4, 16), "toggle border insets on toggle button: " + ti);

        // radio buttons carry their own icon margin: compound collapses differently
        JRadioButton rb = new JRadioButton();
        rb.updateUI();
        Insets ri = BasicBorders.getRadioButtonBorder().getBorderInsets(rb);
        check(insetsEq(ri, 4, 4, 4, 4), "radio border insets on radio button (own margin): " + ri);
        Insets ri2 = BasicBorders.getRadioButtonBorder().getBorderInsets(b);
        check(insetsEq(ri2, 4, 16, 4, 16), "radio border insets on plain button: " + ri2);

        // margin-free components: compound collapses to the plain border part
        Insets mi = BasicBorders.getMenuBarBorder().getBorderInsets(new JMenuBar());
        check(insetsEq(mi, 0, 0, 2, 0), "menu bar border insets (0,0,2,0): " + mi);

        Insets si = BasicBorders.getSplitPaneBorder().getBorderInsets(b);
        check(insetsEq(si, 1, 1, 1, 1), "split pane border insets (1,1,1,1)");

        Insets di = BasicBorders.getSplitPaneDividerBorder().getBorderInsets(b);
        check(insetsEq(di, 1, 1, 1, 1), "split pane divider border insets (1,1,1,1)");
        check(BasicBorders.getSplitPaneDividerBorder().isBorderOpaque(),
                "split pane divider border is opaque");

        Insets pi = BasicBorders.getProgressBarBorder().getBorderInsets(b);
        check(insetsEq(pi, 2, 2, 2, 2), "progress bar border insets (2,2,2,2)");

        JTextField tf = new JTextField();
        tf.updateUI();
        Insets fi = BasicBorders.getTextFieldBorder().getBorderInsets(tf);
        check(insetsEq(fi, 2, 2, 2, 2), "field border insets (2,2,2,2) on text field: " + fi);
    }

    static void testNestedBorderConstructors() {
        Color shadow = Color.BLACK, dark = Color.DARK_GRAY, hi = Color.WHITE, hiShadow = Color.LIGHT_GRAY;
        BasicBorders.ButtonBorder bb =
                new BasicBorders.ButtonBorder(shadow, dark, hi, hiShadow) { };
        check(bb instanceof UIResource, "ButtonBorder is UIResource");
        JButton b = new JButton();
        b.updateUI();
        check(insetsEq(bb.getBorderInsets(b), 2, 3, 3, 3),
                "ButtonBorder insets asymmetric (2,3,3,3): " + bb.getBorderInsets(b));
        check(!bb.isBorderOpaque(), "ButtonBorder not opaque");

        BasicBorders.MenuBarBorder mb = new BasicBorders.MenuBarBorder(shadow, hi) { };
        check(insetsEq(mb.getBorderInsets(b), 0, 0, 2, 0), "MenuBarBorder ctor insets (0,0,2,0)");
        check(!mb.isBorderOpaque(), "MenuBarBorder not opaque");

        BasicBorders.FieldBorder fb = new BasicBorders.FieldBorder(shadow, dark, hi, hiShadow) { };
        check(insetsEq(fb.getBorderInsets(tf()), 2, 2, 2, 2), "FieldBorder ctor insets (2,2,2,2)");

        BasicBorders.ToggleButtonBorder tb =
                new BasicBorders.ToggleButtonBorder(shadow, dark, hi, hiShadow) { };
        check(insetsEq(tb.getBorderInsets(tbtn()), 2, 2, 2, 2), "ToggleButtonBorder ctor insets (2,2,2,2)");

        BasicBorders.RadioButtonBorder rb =
                new BasicBorders.RadioButtonBorder(shadow, dark, hi, hiShadow) { };
        check(insetsEq(rb.getBorderInsets(rbtn()), 2, 2, 2, 2), "RadioButtonBorder ctor insets (2,2,2,2)");

        BasicBorders.SplitPaneBorder sp = new BasicBorders.SplitPaneBorder(hi, shadow);
        check(insetsEq(sp.getBorderInsets(b), 1, 1, 1, 1), "SplitPaneBorder ctor insets (1,1,1,1)");
        check(sp.isBorderOpaque(), "SplitPaneBorder is opaque");
    }

    static JComponent tf() { JTextField t = new JTextField(); t.updateUI(); return t; }
    static JComponent tbtn() { JToggleButton t = new JToggleButton(); t.updateUI(); return t; }
    static JComponent rbtn() { JRadioButton r = new JRadioButton(); r.updateUI(); return r; }

    /** Fixed-geometry paint: the menu bar border paints exactly its 2-pixel bottom rule. */
    static void testPaintGeometry() {
        BasicBorders.MenuBarBorder mb =
                new BasicBorders.MenuBarBorder(Color.BLACK, Color.WHITE) { };
        BufferedImage img = new BufferedImage(40, 20, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        mb.paintBorder(new JMenuBar(), g, 0, 0, 40, 20);
        g.dispose();
        check(drawnPixels(img) == 80, "MenuBarBorder paints exactly 2 rows of 40 (80 px)");
        check((img.getRGB(5, 0) >>> 24) == 0, "nothing painted above the rule");
        check((img.getRGB(5, 19) >>> 24) != 0, "bottom rule row painted");

        // progress bar border is a plain line border: ring only, interior untouched
        Border pb = BasicBorders.getProgressBarBorder();
        BufferedImage img2 = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img2.createGraphics();
        pb.paintBorder(new JButton(), g2, 0, 0, 10, 10);
        g2.dispose();
        check((img2.getRGB(5, 5) >>> 24) == 0, "line border leaves interior untouched");
        check((img2.getRGB(0, 5) >>> 24) != 0, "line border paints the ring");
        // cross-implementation contract: equals the equivalent plain LineBorder geometry
        LineBorder plain = new LineBorder(Color.BLACK, 2);
        check(insetsEq(plain.getBorderInsets(null), 2, 2, 2, 2), "line geometry insets agree");
        CompoundBorder probe = new CompoundBorder(plain, null);
        check(probe.getBorderInsets(null) != null, "compound with null inside border keeps insets");
    }

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(new javax.swing.plaf.metal.MetalLookAndFeel());
        testFactoryTypes();
        testFactoryInsetsOnUIMountedComponents();
        testNestedBorderConstructors();
        testPaintGeometry();
        javax.swing.plaf.metal.MetalLookAndFeel.setCurrentTheme(
                new javax.swing.plaf.metal.OceanTheme());
        System.out.println("PASS (" + passed + " assertions)");
    }
}
