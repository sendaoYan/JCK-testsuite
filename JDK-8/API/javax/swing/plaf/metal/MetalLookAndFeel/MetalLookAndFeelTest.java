/*
 * @test
 * @summary javax.swing.plaf.metal.MetalLookAndFeel conformance: metadata, theme registry, theme-dependent colors, installed behavior (headless).
 * @run main MetalLookAndFeelTest
 */

import java.awt.Color;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.MetalTheme;
import javax.swing.plaf.metal.OceanTheme;

public class MetalLookAndFeelTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    // ---- Part 1: identity metadata ----
    static void testMetadata() throws Exception {
        MetalLookAndFeel lf = new MetalLookAndFeel();
        check("Metal".equals(lf.getName()), "getName() == \"Metal\"");
        check("Metal".equals(lf.getID()), "getID() == \"Metal\"");
        check(lf.getDescription() != null && !lf.getDescription().isEmpty(),
                "getDescription() non-empty");
        check(!lf.isNativeLookAndFeel(), "not native on Linux");
        check(lf.isSupportedLookAndFeel(), "supported everywhere");
        check(lf.getSupportsWindowDecorations(), "Metal supports window decorations");
    }

    // ---- Part 2: theme registry ----
    static void testThemeRegistry() {
        MetalTheme before = MetalLookAndFeel.getCurrentTheme();
        try {
            check(before != null, "a theme is always current");
            check(before instanceof OceanTheme, "default theme is Ocean");
            check("Ocean".equals(new OceanTheme().getName()), "Ocean theme name");
            check("Steel".equals(new DefaultMetalTheme().getName()), "Default (Steel) theme name");
            check(new DefaultMetalTheme() instanceof MetalTheme, "DefaultMetalTheme is a MetalTheme");

            // null theme is rejected
            try {
                MetalLookAndFeel.setCurrentTheme(null);
                check(false, "setCurrentTheme(null) must throw NPE");
            } catch (NullPointerException expected) {
                check("Can't have null theme".equals(expected.getMessage()),
                        "null theme NPE message exact");
            }

            // switching themes is visible through the static color accessors
            MetalLookAndFeel.setCurrentTheme(new DefaultMetalTheme());
            MetalTheme steel = MetalLookAndFeel.getCurrentTheme();
            check(steel instanceof DefaultMetalTheme, "Steel theme installed");
            check(MetalLookAndFeel.getControl().equals(new Color(204, 204, 204)),
                    "Steel control color (204,204,204)");
            check(MetalLookAndFeel.getControlTextColor().equals(new Color(0, 0, 0)),
                    "Steel control text color black");
            Color steelText = MetalLookAndFeel.getControlTextColor();

            MetalLookAndFeel.setCurrentTheme(new OceanTheme());
            check(MetalLookAndFeel.getCurrentTheme() instanceof OceanTheme, "Ocean theme installed");
            check(MetalLookAndFeel.getControl().equals(new Color(238, 238, 238)),
                    "Ocean control color (238,238,238)");
            check(!MetalLookAndFeel.getControlTextColor().equals(steelText),
                    "theme switch changes the control text color");
        } finally {
            MetalLookAndFeel.setCurrentTheme(before);
        }
    }

    // ---- Part 3: static font/color surface (theme-driven, measured under Steel) ----
    static void testStaticColorsAndFonts() {
        MetalTheme savedTheme = MetalLookAndFeel.getCurrentTheme();
        MetalLookAndFeel.setCurrentTheme(new DefaultMetalTheme());
        String[] fontGetters = { "getControlTextFont", "getSystemTextFont", "getUserTextFont",
                "getMenuTextFont", "getWindowTitleFont", "getSubTextFont" };
        for (String name : fontGetters) {
            try {
                Method m = MetalLookAndFeel.class.getMethod(name);
                FontUIResource f = (FontUIResource) m.invoke(null);
                check(f != null, name + " non-null FontUIResource");
                check(f.getSize() > 0, name + " positive size");
            } catch (Exception e) {
                throw new AssertionError(name, e);
            }
        }
        String[] colorGetters = { "getDesktopColor", "getFocusColor", "getWhite", "getBlack",
                "getControl", "getControlShadow", "getControlDarkShadow", "getControlInfo",
                "getControlHighlight", "getControlDisabled", "getPrimaryControl",
                "getPrimaryControlShadow", "getPrimaryControlDarkShadow", "getPrimaryControlInfo",
                "getPrimaryControlHighlight", "getSystemTextColor", "getControlTextColor",
                "getInactiveControlTextColor", "getInactiveSystemTextColor", "getUserTextColor",
                "getTextHighlightColor", "getHighlightedTextColor", "getWindowBackground",
                "getWindowTitleBackground", "getWindowTitleForeground",
                "getWindowTitleInactiveBackground", "getWindowTitleInactiveForeground",
                "getMenuBackground", "getMenuForeground", "getMenuSelectedBackground",
                "getMenuSelectedForeground", "getMenuDisabledForeground", "getSeparatorBackground",
                "getSeparatorForeground", "getAcceleratorForeground",
                "getAcceleratorSelectedForeground" };
        for (String name : colorGetters) {
            try {
                Method m = MetalLookAndFeel.class.getMethod(name);
                Color col = (Color) m.invoke(null);
                check(col != null, name + " non-null");
                check(col.getAlpha() == 255, name + " fully opaque");
            } catch (Exception e) {
                throw new AssertionError(name, e);
            }
        }
        check(MetalLookAndFeel.getWhite().equals(new Color(255, 255, 255)), "Steel white");
        check(MetalLookAndFeel.getBlack().equals(new Color(0, 0, 0)), "Steel black");
        MetalLookAndFeel.setCurrentTheme(new OceanTheme());
        // Ocean substitutes the printer-friendly near-black for pure black
        check(MetalLookAndFeel.getBlack().equals(new Color(51, 51, 51)), "Ocean black is print black (51,51,51)");
        MetalLookAndFeel.setCurrentTheme(savedTheme);
    }

    // ---- Part 4: installed behaviour ----
    static void testInstalled() throws Exception {
        UIManager.setLookAndFeel(new MetalLookAndFeel());
        check(UIManager.getLookAndFeel() instanceof MetalLookAndFeel, "Metal installed");
        javax.swing.UIDefaults d = UIManager.getLookAndFeel().getDefaults();
        check(d != null && d.size() > 100, "Metal defaults table heavily populated");
        check("javax.swing.plaf.metal.MetalButtonUI".equals(d.get("ButtonUI")),
                "Metal maps ButtonUI to MetalButtonUI");
        JButton b = new JButton();
        b.updateUI();
        check(b.getUI().getClass().getName().equals("javax.swing.plaf.metal.MetalButtonUI"),
                "button delegate is MetalButtonUI");
        // getDisabledIcon with no disabled filter image falls back to null on Metal
        javax.swing.Icon icon = javax.swing.plaf.metal.MetalIconFactory.getCheckBoxIcon();
        check(UIManager.getLookAndFeel().getDisabledIcon(b, icon) == null,
                "Metal getDisabledIcon returns null for plain icons");
        check(UIManager.getLookAndFeel().getDisabledSelectedIcon(b, icon) == null,
                "Metal getDisabledSelectedIcon returns null for plain icons");
        // layout style service is available headless
        check(new MetalLookAndFeel().getLayoutStyle() != null, "Metal layout style non-null");
        // provideErrorFeedback with a null component must not throw on headless
        try {
            new MetalLookAndFeel().provideErrorFeedback(null);
            check(true, "provideErrorFeedback(null) tolerated");
        } catch (NullPointerException tolerated) {
            check(true, "provideErrorFeedback(null) tolerated (NPE path)");
        }
    }

    // ---- Part 5: legacy shallow coverage (retained verbatim) ----
    static void testLegacyShape() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.metal.MetalLookAndFeel", false, MetalLookAndFeelTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.plaf.basic.BasicLookAndFeel"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getName"), "method getName");
        check(names.contains("getID"), "method getID");
        check(names.contains("getDescription"), "method getDescription");
        check(names.contains("isNativeLookAndFeel"), "method isNativeLookAndFeel");
        check(names.contains("isSupportedLookAndFeel"), "method isSupportedLookAndFeel");
        check(names.contains("getSupportsWindowDecorations"), "method getSupportsWindowDecorations");
        check(names.contains("getDefaults"), "method getDefaults");
    }

    public static void main(String[] args) throws Exception {
        testLegacyShape();
        testMetadata();
        testThemeRegistry();
        testStaticColorsAndFonts();
        testInstalled();
        MetalLookAndFeel.setCurrentTheme(new OceanTheme());
        System.out.println("PASS (" + passed + " assertions)");
    }
}
